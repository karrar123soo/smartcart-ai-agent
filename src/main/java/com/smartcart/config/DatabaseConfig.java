package com.smartcart.config;

import java.net.URI;
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

	@Bean
	@Primary
	public DataSource dataSource() {
		String finalUrl = sanitizeJdbcUrl(rawUrl);
		String finalUsername = configuredUsername;
		String finalPassword = configuredPassword;

		// Extract credentials if embedded in URI (mysql://user:password@host:port/database)
		if (rawUrl != null && rawUrl.startsWith("mysql://")) {
			try {
				URI uri = URI.create(rawUrl);
				String userInfo = uri.getUserInfo();
				if (userInfo != null && !userInfo.isEmpty()) {
					String[] userPass = userInfo.split(":", 2);
					if (finalUsername == null || "root".equals(finalUsername)) {
						finalUsername = userPass[0];
					}
					if (userPass.length > 1 && (finalPassword == null || "root".equals(finalPassword))) {
						finalPassword = userPass[1];
					}
				}
			} catch (Exception e) {
				log.warn("Could not parse user info from database URI: {}", e.getMessage());
			}
		}

		HikariDataSource dataSource = new HikariDataSource();
		dataSource.setDriverClassName(driverClassName);
		dataSource.setJdbcUrl(finalUrl);
		dataSource.setUsername(finalUsername);
		dataSource.setPassword(finalPassword);

		// Optimized pool settings for free-tier cloud deployment
		dataSource.setMaximumPoolSize(5);
		dataSource.setMinimumIdle(1);
		dataSource.setConnectionTimeout(30000);
		dataSource.setIdleTimeout(600000);
		dataSource.setMaxLifetime(1800000);

		return dataSource;
	}

	public static String sanitizeJdbcUrl(String rawUrl) {
		if (rawUrl == null || rawUrl.isBlank()) {
			return rawUrl;
		}

		String trimmed = rawUrl.trim();

		// Handle cloud URIs starting with mysql://
		if (trimmed.startsWith("mysql://")) {
			try {
				URI uri = URI.create(trimmed);
				String host = uri.getHost();
				int port = uri.getPort() == -1 ? 4000 : uri.getPort();
				String path = uri.getPath() != null && !uri.getPath().isEmpty() ? uri.getPath() : "/test";
				String query = uri.getQuery();

				StringBuilder jdbcUrl = new StringBuilder("jdbc:mysql://")
						.append(host)
						.append(":")
						.append(port)
						.append(path);

				if (query != null && !query.isEmpty()) {
					jdbcUrl.append("?").append(query);
					if (!query.contains("sslMode") && !query.contains("useSSL")) {
						jdbcUrl.append("&sslMode=VERIFY_IDENTITY&enabledTLSProtocols=TLSv1.2,TLSv1.3");
					}
				} else {
					jdbcUrl.append("?sslMode=VERIFY_IDENTITY&enabledTLSProtocols=TLSv1.2,TLSv1.3");
				}
				return jdbcUrl.toString();
			} catch (Exception e) {
				trimmed = "jdbc:" + trimmed;
			}
		}

		// Ensure jdbc: prefix
		if (!trimmed.startsWith("jdbc:")) {
			trimmed = "jdbc:" + trimmed;
		}

		// If connecting to TiDB Cloud, guarantee SSL flags are appended
		if (trimmed.contains("tidbcloud.com") && !trimmed.contains("sslMode") && !trimmed.contains("useSSL")) {
			trimmed = trimmed + (trimmed.contains("?") ? "&" : "?") + "sslMode=VERIFY_IDENTITY&enabledTLSProtocols=TLSv1.2,TLSv1.3";
		}

		return trimmed;
	}
}
