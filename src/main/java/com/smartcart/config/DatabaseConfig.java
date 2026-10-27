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
		String effectiveUrl = rawUrl;
		String envUrl = cleanValue(getFirstNonBlankEnv("SPRING_DATASOURCE_URL", "DATABASE_URL", "DB_URL", "MYSQL_URL", "TIDB_URL"));
		if (envUrl != null && !envUrl.isBlank()) {
			effectiveUrl = envUrl;
		}

		ConnectionInfo info = parseConnectionInfo(effectiveUrl, configuredUsername, configuredPassword);

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

		// Pre-flight connection validation
		validateConnection(dataSource, info);

		return dataSource;
	}

	private void validateConnection(HikariDataSource dataSource, ConnectionInfo info) {
		try (var conn = dataSource.getConnection()) {
			String successMsg = "\n============================================================\n" +
					"SMARTCART DATABASE CONNECTION SUCCESSFUL!\n" +
					"Target JDBC URL      : " + maskUrlForLogs(info.jdbcUrl) + "\n" +
					"Target DB Username   : " + info.username + " (source: " + info.usernameSource + ")\n" +
					"============================================================";
			System.out.println(successMsg);
			log.info(successMsg);
		} catch (Exception e) {
			String passAlert;
			if ("root".equals(info.password)) {
				passAlert = "Password is still the local default 'root'! Set SPRING_DATASOURCE_PASSWORD in Render.";
			} else if (info.password == null || info.password.isBlank()) {
				passAlert = "Password is EMPTY! Please set SPRING_DATASOURCE_PASSWORD in Render.";
			} else {
				passAlert = "Password was supplied (length: " + info.password.length() + " chars, source: " + info.passwordSource + "), but was rejected by TiDB Cloud.";
			}

			String report = "\n============================================================\n" +
					"SMARTCART DATABASE CONNECTION FAILURE REPORT\n" +
					"Target JDBC URL      : " + maskUrlForLogs(info.jdbcUrl) + "\n" +
					"Target DB Username   : " + info.username + " (source: " + info.usernameSource + ")\n" +
					"Password Source      : " + info.passwordSource + "\n" +
					"Password Length      : " + (info.password != null ? info.password.length() : 0) + " chars\n" +
					"Password is 'root'   : " + "root".equals(info.password) + "\n" +
					"Diagnosis            : " + passAlert + "\n" +
					"Underlying Exception : " + e.getMessage() + "\n" +
					"============================================================";

			System.err.println(report);
			log.error(report);
			throw new IllegalStateException("Database connection failed: " + passAlert + " (" + e.getMessage() + ")", e);
		}
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
				String alert = "CRITICAL CONFIGURATION ERROR: TiDB Cloud target detected, but Username is still default 'root'!\n" +
						"TiDB Cloud requires your cluster username (e.g. '<prefix>.root'). Set SPRING_DATASOURCE_USERNAME in Render.";
				System.err.println(alert);
				log.error(alert);
			}
			if ("root".equals(info.password) || info.password == null || info.password.isBlank()) {
				String alert = "CRITICAL CONFIGURATION ERROR: TiDB Cloud target detected, but Password is the local default 'root' or empty!\n" +
						"TiDB Cloud rejected the connection with MySQL Error 1045 because the password was not supplied or is still 'root'.\n" +
						"Please set the SPRING_DATASOURCE_PASSWORD environment variable in the Render Dashboard to your cluster root password.";
				System.err.println(alert);
				log.error(alert);
			}
			if (info.password != null && info.password.contains("<PASSWORD>")) {
				String alert = "CRITICAL CONFIGURATION ERROR: Password contains literal placeholder '<PASSWORD>'! Set real password in Render.";
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
		String envUser = cleanValue(getFirstNonBlankEnv(
				"SPRING_DATASOURCE_USERNAME", "DB_USERNAME", "DATABASE_USERNAME", "MYSQL_USER", "MYSQLUSER", "TIDB_USER", "DB_USER"));
		String envPass = cleanValue(getFirstNonBlankEnv(
				"SPRING_DATASOURCE_PASSWORD", "DB_PASSWORD", "DATABASE_PASSWORD", "MYSQL_PASSWORD", "MYSQLPASSWORD", "TIDB_PASSWORD", "DB_PASS", "DATABASE_PASS", "SPRING_DATASOURCE_PASS"));

		if (envUser != null && !envUser.isBlank() && !"root".equals(envUser)) {
			username = envUser;
			userSource = "ENVIRONMENT_VARIABLE";
		}
		if (envPass != null && !envPass.isBlank() && !"root".equals(envPass)) {
			password = envPass;
			passSource = "ENVIRONMENT_VARIABLE";
		}

		// 2. Check if credentials are embedded in URL authority (user:password@)
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

		// 3. Check if credentials are provided in URL query parameters (?user=...&password=...)
		if (trimmed.contains("?")) {
			try {
				int qIdx = trimmed.indexOf("?");
				String base = trimmed.substring(0, qIdx);
				String query = trimmed.substring(qIdx + 1);
				String[] params = query.split("&");
				StringBuilder remainingParams = new StringBuilder();

				for (String param : params) {
					if (param.isBlank()) continue;
					int eqIdx = param.indexOf("=");
					String key = eqIdx != -1 ? param.substring(0, eqIdx).trim() : param.trim();
					String value = eqIdx != -1 ? param.substring(eqIdx + 1).trim() : "";

					if ("user".equalsIgnoreCase(key) || "username".equalsIgnoreCase(key)) {
						String extractedUser = cleanValue(decodeIfPercentEncoded(value));
						if (extractedUser != null && !extractedUser.isBlank()) {
							if (username == null || "root".equals(username) || "DEFAULT_LOCAL".equals(userSource)) {
								username = extractedUser;
								userSource = "URL_QUERY_PARAM";
							}
						}
						// Do not keep user credential in JDBC URL
						continue;
					}

					if ("password".equalsIgnoreCase(key) || "pwd".equalsIgnoreCase(key)) {
						String extractedPass = cleanValue(decodeIfPercentEncoded(value));
						boolean isPlaceholder = extractedPass != null && (
								extractedPass.equalsIgnoreCase("<PASSWORD>")
								|| extractedPass.equalsIgnoreCase("<your_password>")
								|| extractedPass.equalsIgnoreCase("<your-password>"));

						if (!isPlaceholder && extractedPass != null && !extractedPass.isBlank()) {
							if (password == null || "root".equals(password) || "DEFAULT_LOCAL".equals(passSource)) {
								password = extractedPass;
								passSource = "URL_QUERY_PARAM";
							}
						}
						// Do not keep password credential in JDBC URL
						continue;
					}

					if (remainingParams.length() > 0) {
						remainingParams.append("&");
					}
					remainingParams.append(param);
				}

				trimmed = base + (remainingParams.length() > 0 ? "?" + remainingParams.toString() : "");
			} catch (Exception e) {
				log.warn("Could not parse query credentials from URL: {}", e.getMessage());
			}
		}

		// 4. Ensure jdbc: prefix
		if (!trimmed.startsWith("jdbc:")) {
			trimmed = "jdbc:" + trimmed;
		}

		// 5. Ensure TiDB Cloud connection parameters (TLS, native authentication plugin, public key retrieval)
		if (trimmed.contains("tidbcloud.com") || trimmed.contains(":4000")) {
			if (!trimmed.contains("sslMode") && !trimmed.contains("useSSL")) {
				trimmed = trimmed + (trimmed.contains("?") ? "&" : "?") + "sslMode=VERIFY_IDENTITY&enabledTLSProtocols=TLSv1.2,TLSv1.3";
			}
			if (!trimmed.contains("defaultAuthenticationPlugin")) {
				trimmed = trimmed + (trimmed.contains("?") ? "&" : "?") + "defaultAuthenticationPlugin=com.mysql.cj.protocol.a.authentication.MysqlNativePasswordPlugin";
			}
			if (!trimmed.contains("allowPublicKeyRetrieval")) {
				trimmed = trimmed + (trimmed.contains("?") ? "&" : "?") + "allowPublicKeyRetrieval=true";
			}
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
