package com.smartcart.config;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.zaxxer.hikari.HikariDataSource;

@Configuration
public class DatabaseConfig {

	private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

	@Value("${spring.datasource.url}")
	private String rawUrl;

	@Value("${spring.datasource.username:root}")
	private String configuredUsername;

	@Value("${spring.datasource.password:root}")
	private String configuredPassword;

	@Value("${spring.datasource.driver-class-name:com.mysql.cj.jdbc.Driver}")
	private String driverClassName;

	public static class ConnectionInfo {
		public final String jdbcUrl;
		public final String username;
		public final String password;
		public final String usernameSource;
		public final String passwordSource;

		public ConnectionInfo(String jdbcUrl, String username, String password, String usernameSource, String passwordSource) {
			this.jdbcUrl = jdbcUrl;
			this.username = username;
			this.password = password;
			this.usernameSource = usernameSource;
			this.passwordSource = passwordSource;
		}
	}

	@Bean
	@Primary
	public DataSource dataSource() {
		ConnectionInfo info = parseConnectionInfo(rawUrl, configuredUsername, configuredPassword);

		// Safe Diagnostics (Never prints passwords or secrets)
		logSafeDiagnostics(info);

		HikariDataSource dataSource = new HikariDataSource();
		dataSource.setDriverClassName(driverClassName);
		dataSource.setJdbcUrl(info.jdbcUrl);
		dataSource.setUsername(info.username);
		dataSource.setPassword(info.password);

		// Optimized pool settings for free-tier cloud deployment
		dataSource.setMaximumPoolSize(5);
		dataSource.setMinimumIdle(1);
		dataSource.setConnectionTimeout(30000);
		dataSource.setIdleTimeout(600000);
		dataSource.setMaxLifetime(1800000);

		return dataSource;
	}

	private void logSafeDiagnostics(ConnectionInfo info) {
		boolean isCloud = info.jdbcUrl != null && info.jdbcUrl.contains("tidbcloud.com");
		String msg = "\n============================================================\n" +
				"SMARTCART DATABASE CONNECTION DIAGNOSTICS (SAFE MODE)\n" +
				"Target JDBC URL      : " + maskUrlForLogs(info.jdbcUrl) + "\n" +
				"Target DB Username   : " + info.username + " (source: " + info.usernameSource + ")\n" +
				"Password Configured  : " + (info.password != null && !info.password.isBlank()) +
				" (length: " + (info.password != null ? info.password.length() : 0) + " chars, source: " + info.passwordSource + ")\n" +
				"============================================================";

		System.out.println(msg);
		log.info(msg);

		if (isCloud) {
			if ("root".equalsIgnoreCase(info.username)) {
				String alert = "DIAGNOSTIC ALERT: Username is 'root'! TiDB Cloud requires a cluster user like '<prefix>.root'.";
				System.err.println(alert);
				log.error(alert);
			}
			if ("root".equals(info.password)) {
				String alert = "DIAGNOSTIC ALERT: Password is still the local default 'root'! Cloud password was not provided.";
				System.err.println(alert);
				log.error(alert);
			}
			if (info.password != null && info.password.contains("<PASSWORD>")) {
				String alert = "DIAGNOSTIC ALERT: Password contains the literal placeholder '<PASSWORD>'.";
				System.err.println(alert);
				log.error(alert);
			}
		}
	}

	private static String maskUrlForLogs(String url) {
		if (url == null) return "null";
		return url.replaceAll("://[^@]+@", "://***:***@");
	}

	public static ConnectionInfo parseConnectionInfo(String rawUrl, String defaultUser, String defaultPass) {
		if (rawUrl == null || rawUrl.isBlank()) {
			return new ConnectionInfo(rawUrl, cleanValue(defaultUser), cleanValue(defaultPass), "DEFAULT", "DEFAULT");
		}

		String trimmed = rawUrl.trim();
		String username = cleanValue(defaultUser);
		String password = cleanValue(defaultPass);
		String userSource = ("root".equals(username)) ? "DEFAULT_LOCAL" : "APPLICATION_PROPERTIES";
		String passSource = ("root".equals(password)) ? "DEFAULT_LOCAL" : "APPLICATION_PROPERTIES";

		// 1. Check explicit environment variables (Highest priority)
		String envUser = cleanValue(getFirstNonBlankEnv("SPRING_DATASOURCE_USERNAME", "DB_USERNAME", "DATABASE_USERNAME", "MYSQLUSER"));
		String envPass = cleanValue(getFirstNonBlankEnv("SPRING_DATASOURCE_PASSWORD", "DB_PASSWORD", "DATABASE_PASSWORD", "MYSQLPASSWORD"));

		if (envUser != null && !envUser.isBlank() && !"root".equals(envUser)) {
			username = envUser;
			userSource = "ENVIRONMENT_VARIABLE";
		}
		if (envPass != null && !envPass.isBlank() && !"root".equals(envPass)) {
			password = envPass;
			passSource = "ENVIRONMENT_VARIABLE";
		}

		// 2. Check if credentials are embedded in URL (user:password@)
		String withoutJdbc = trimmed.startsWith("jdbc:") ? trimmed.substring(5) : trimmed;

		if (withoutJdbc.contains("@")) {
			try {
				int schemeIdx = withoutJdbc.indexOf("://");
				String remainder = schemeIdx != -1 ? withoutJdbc.substring(schemeIdx + 3) : withoutJdbc;

				int atIdx = remainder.lastIndexOf("@");
				String userInfo = remainder.substring(0, atIdx);
				String hostPortDb = remainder.substring(atIdx + 1);

				String[] userPass = userInfo.split(":", 2);
				if (userPass.length > 0 && !userPass[0].isBlank()) {
					String extractedUser = cleanValue(decodeIfPercentEncoded(userPass[0]));
					if (username == null || "root".equals(username) || "DEFAULT_LOCAL".equals(userSource)) {
						username = extractedUser;
						userSource = "EMBEDDED_IN_URL";
					}
				}

				if (userPass.length > 1 && !userPass[1].isBlank()) {
					String extractedPass = cleanValue(decodeIfPercentEncoded(userPass[1]));
					boolean isPlaceholder = extractedPass.equalsIgnoreCase("<PASSWORD>")
							|| extractedPass.equalsIgnoreCase("<your_password>")
							|| extractedPass.equalsIgnoreCase("<your-password>");

					if (!isPlaceholder && (password == null || "root".equals(password) || "DEFAULT_LOCAL".equals(passSource))) {
						password = extractedPass;
						passSource = "EMBEDDED_IN_URL";
					}
				}

				// Rebuild clean URL without user:password@
				trimmed = "jdbc:mysql://" + hostPortDb;
			} catch (Exception e) {
				log.warn("Could not parse embedded credentials from URL: {}", e.getMessage());
			}
		}

		// 3. Ensure jdbc: prefix
		if (!trimmed.startsWith("jdbc:")) {
			trimmed = "jdbc:" + trimmed;
		}

		// 4. Ensure TiDB Cloud SSL parameters
		if (trimmed.contains("tidbcloud.com") && !trimmed.contains("sslMode") && !trimmed.contains("useSSL")) {
			trimmed = trimmed + (trimmed.contains("?") ? "&" : "?") + "sslMode=VERIFY_IDENTITY&enabledTLSProtocols=TLSv1.2,TLSv1.3";
		}

		return new ConnectionInfo(trimmed, username, password, userSource, passSource);
	}

	public static String cleanValue(String val) {
		if (val == null) {
			return null;
		}
		String trimmed = val.trim();
		// Strip surrounding single or double quotes if accidentally included in dashboard
		if ((trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length() >= 2) ||
				(trimmed.startsWith("'") && trimmed.endsWith("'") && trimmed.length() >= 2)) {
			trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
		}
		return trimmed;
	}

	private static String decodeIfPercentEncoded(String str) {
		if (str == null) return null;
		try {
			if (str.contains("%")) {
				return URLDecoder.decode(str, StandardCharsets.UTF_8);
			}
		} catch (Exception ignored) {
		}
		return str;
	}

	private static String getFirstNonBlankEnv(String... names) {
		for (String name : names) {
			String val = System.getenv(name);
			if (val != null && !val.isBlank()) {
				return val;
			}
		}
		return null;
	}

	public static String sanitizeJdbcUrl(String rawUrl) {
		return parseConnectionInfo(rawUrl, "root", "root").jdbcUrl;
	}
}
