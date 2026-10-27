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
		assertEquals("EMBEDDED_IN_URL", info.usernameSource);
		assertEquals("EMBEDDED_IN_URL", info.passwordSource);
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
	void parseConnectionInfo_shouldIgnorePlaceholderPasswordInUrlWhenDefaultPassPresent() {
		String raw = "mysql://aTeXxfqU7Q8ZvKJ.root:<PASSWORD>@gateway01.ap-northeast-1.prod.aws.tidbcloud.com:4000/test";
		DatabaseConfig.ConnectionInfo info = DatabaseConfig.parseConnectionInfo(raw, "root", "myRealPass");

		assertEquals("aTeXxfqU7Q8ZvKJ.root", info.username);
		assertEquals("myRealPass", info.password);
		assertEquals("APPLICATION_PROPERTIES", info.passwordSource);
	}

	@Test
	void parseConnectionInfo_shouldDecodeUrlEncodedPassword() {
		String raw = "mysql://aTeXxfqU7Q8ZvKJ.root:my%23secret%40pass@gateway01.ap-northeast-1.prod.aws.tidbcloud.com:4000/test";
		DatabaseConfig.ConnectionInfo info = DatabaseConfig.parseConnectionInfo(raw, "root", "root");

		assertEquals("my#secret@pass", info.password);
	}

	@Test
	void parseConnectionInfo_shouldCleanSurroundingQuotesAndWhitespace() {
		String raw = "mysql://\"aTeXxfqU7Q8ZvKJ.root\":\"myPass \"@gateway01.ap-northeast-1.prod.aws.tidbcloud.com:4000/test";
		DatabaseConfig.ConnectionInfo info = DatabaseConfig.parseConnectionInfo(raw, "root", "root");

		assertEquals("aTeXxfqU7Q8ZvKJ.root", info.username);
		assertEquals("myPass", info.password);
	}

	@Test
	void cleanValue_shouldStripQuotesAndTrim() {
		assertEquals("cleanPass", DatabaseConfig.cleanValue("  \"cleanPass\"  "));
		assertEquals("cleanPass", DatabaseConfig.cleanValue(" 'cleanPass' "));
		assertEquals("cleanPass", DatabaseConfig.cleanValue("cleanPass"));
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
		assertEquals("DEFAULT_LOCAL", info.usernameSource);
		assertEquals("DEFAULT_LOCAL", info.passwordSource);
	}

	@Test
	void parseConnectionInfo_shouldAddSslToTidbUrlIfMissing() {
		String raw = "jdbc:mysql://gateway01.ap-northeast-1.prod.aws.tidbcloud.com:4000/test";
		DatabaseConfig.ConnectionInfo info = DatabaseConfig.parseConnectionInfo(raw, "root", "root");

		assertTrue(info.jdbcUrl.contains("sslMode=VERIFY_IDENTITY"));
		assertTrue(info.jdbcUrl.startsWith("jdbc:mysql://"));
	}

	@Test
	void parseConnectionInfo_shouldExtractCredentialsFromQueryParameters() {
		String raw = "jdbc:mysql://gateway01.ap-northeast-1.prod.aws.tidbcloud.com:4000/test?user=aTeXxfqU7Q8ZvKJ.root&password=secretQueryPass&useSSL=true";
		DatabaseConfig.ConnectionInfo info = DatabaseConfig.parseConnectionInfo(raw, "root", "root");

		assertEquals("aTeXxfqU7Q8ZvKJ.root", info.username);
		assertEquals("secretQueryPass", info.password);
		assertEquals("URL_QUERY_PARAM", info.usernameSource);
		assertEquals("URL_QUERY_PARAM", info.passwordSource);
		assertTrue(info.jdbcUrl.contains("useSSL=true"));
		assertTrue(!info.jdbcUrl.contains("user="));
		assertTrue(!info.jdbcUrl.contains("password="));
	}

	@Test
	void parseConnectionInfo_shouldIgnorePlaceholderPasswordInQueryParameters() {
		String raw = "jdbc:mysql://gateway01.ap-northeast-1.prod.aws.tidbcloud.com:4000/test?user=aTeXxfqU7Q8ZvKJ.root&password=<PASSWORD>&useSSL=true";
		DatabaseConfig.ConnectionInfo info = DatabaseConfig.parseConnectionInfo(raw, "root", "myFallbackPass");

		assertEquals("aTeXxfqU7Q8ZvKJ.root", info.username);
		assertEquals("myFallbackPass", info.password);
		assertEquals("APPLICATION_PROPERTIES", info.passwordSource);
		assertTrue(!info.jdbcUrl.contains("<PASSWORD>"));
	}
}
