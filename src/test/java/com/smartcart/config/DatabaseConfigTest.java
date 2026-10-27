package com.smartcart.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DatabaseConfigTest {

	@Test
	void parseConnectionInfo_shouldExtractCredentialsFromMysqlUri() {
		String raw = "mysql://aTeXxfqU7Q8ZvKJ.root:secret123@gateway01.ap-northeast-1.prod.aws.tidbcloud.com:4000/test";
		DatabaseConfig.ConnectionInfo info = DatabaseConfig.parseConnectionInfo(raw, "root", "root");

		assertEquals(
				"jdbc:mysql://gateway01.ap-northeast-1.prod.aws.tidbcloud.com:4000/test?sslMode=VERIFY_IDENTITY&enabledTLSProtocols=TLSv1.2,TLSv1.3",
				info.jdbcUrl
		);
		assertEquals("aTeXxfqU7Q8ZvKJ.root", info.username);
		assertEquals("secret123", info.password);
	}

	@Test
	void parseConnectionInfo_shouldExtractCredentialsFromJdbcMysqlUri() {
		String raw = "jdbc:mysql://aTeXxfqU7Q8ZvKJ.root:secret123@gateway01.ap-northeast-1.prod.aws.tidbcloud.com:4000/test";
		DatabaseConfig.ConnectionInfo info = DatabaseConfig.parseConnectionInfo(raw, "root", "root");

		assertEquals(
				"jdbc:mysql://gateway01.ap-northeast-1.prod.aws.tidbcloud.com:4000/test?sslMode=VERIFY_IDENTITY&enabledTLSProtocols=TLSv1.2,TLSv1.3",
				info.jdbcUrl
		);
		assertEquals("aTeXxfqU7Q8ZvKJ.root", info.username);
		assertEquals("secret123", info.password);
	}

	@Test
	void parseConnectionInfo_shouldPreserveExistingQueryParameters() {
		String raw = "mysql://user:pass@host:4000/test?sslMode=REQUIRED";
		DatabaseConfig.ConnectionInfo info = DatabaseConfig.parseConnectionInfo(raw, "root", "root");

		assertEquals("jdbc:mysql://host:4000/test?sslMode=REQUIRED", info.jdbcUrl);
		assertEquals("user", info.username);
		assertEquals("pass", info.password);
	}

	@Test
	void parseConnectionInfo_shouldLeaveProperJdbcUrlUntouched() {
		String jdbcUrl = "jdbc:mysql://localhost:3306/smartcart_db";
		DatabaseConfig.ConnectionInfo info = DatabaseConfig.parseConnectionInfo(jdbcUrl, "root", "root");

		assertEquals(jdbcUrl, info.jdbcUrl);
		assertEquals("root", info.username);
		assertEquals("root", info.password);
	}

	@Test
	void parseConnectionInfo_shouldAddSslToTidbUrlIfMissing() {
		String raw = "jdbc:mysql://gateway01.ap-northeast-1.prod.aws.tidbcloud.com:4000/test";
		DatabaseConfig.ConnectionInfo info = DatabaseConfig.parseConnectionInfo(raw, "root", "root");

		assertTrue(info.jdbcUrl.contains("sslMode=VERIFY_IDENTITY"));
		assertTrue(info.jdbcUrl.startsWith("jdbc:mysql://"));
	}
}
