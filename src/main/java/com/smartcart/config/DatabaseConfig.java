package com.smartcart.config;

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

		public ConnectionInfo(String jdbcUrl, String username, String password) {
			this.jdbcUrl = jdbcUrl;
			this.username = username;
			this.password = password;
		}
	}

	@Bean
	@Primary
	public DataSource dataSource() {
		ConnectionInfo info = parseConnectionInfo(rawUrl, configuredUsername, configuredPassword);

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

	public static ConnectionInfo parseConnectionInfo(String rawUrl, String defaultUser, String defaultPass) {
		if (rawUrl == null || rawUrl.isBlank()) {
			return new ConnectionInfo(rawUrl, defaultUser, defaultPass);
		}

		String trimmed = rawUrl.trim();
		String username = defaultUser;
		String password = defaultPass;

		// Check environment variables first
		String envUser = getFirstNonBlankEnv("SPRING_DATASOURCE_USERNAME", "DB_USERNAME", "DATABASE_USERNAME", "MYSQLUSER");
		String envPass = getFirstNonBlankEnv("SPRING_DATASOURCE_PASSWORD", "DB_PASSWORD", "DATABASE_PASSWORD", "MYSQLPASSWORD");

		if (envUser != null && !envUser.isBlank()) {
			username = envUser;
		}
		if (envPass != null && !envPass.isBlank()) {
			password = envPass;
		}

		// Normalize: check if credentials are embedded (user:password@)
		String withoutJdbc = trimmed.startsWith("jdbc:") ? trimmed.substring(5) : trimmed;

		if (withoutJdbc.contains("@")) {
			try {
				int schemeIdx = withoutJdbc.indexOf("://");
				String remainder = schemeIdx != -1 ? withoutJdbc.substring(schemeIdx + 3) : withoutJdbc;

				int atIdx = remainder.lastIndexOf("@");
				String userInfo = remainder.substring(0, atIdx);
				String hostPortDb = remainder.substring(atIdx + 1);

				String[] userPass = userInfo.split(":", 2);
				if (username == null || "root".equals(username)) {
					username = userPass[0];
				}
				if (userPass.length > 1 && (password == null || "root".equals(password))) {
					password = userPass[1];
				}

				// Rebuild clean URL without user:password@
				trimmed = "jdbc:mysql://" + hostPortDb;
			} catch (Exception e) {
				log.warn("Could not parse embedded credentials from URL: {}", e.getMessage());
			}
		}

		// Ensure jdbc: prefix
		if (!trimmed.startsWith("jdbc:")) {
			trimmed = "jdbc:" + trimmed;
		}

		// Ensure TiDB Cloud SSL parameters
		if (trimmed.contains("tidbcloud.com") && !trimmed.contains("sslMode") && !trimmed.contains("useSSL")) {
			trimmed = trimmed + (trimmed.contains("?") ? "&" : "?") + "sslMode=VERIFY_IDENTITY&enabledTLSProtocols=TLSv1.2,TLSv1.3";
		}

		return new ConnectionInfo(trimmed, username, password);
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
