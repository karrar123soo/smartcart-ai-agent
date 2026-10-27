package com.smartcart.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DatabaseConfigTest {

	@Test
	void sanitizeJdbcUrl_shouldConvertRawMysqlUriToJdbc() {
		String raw = "mysql://aTeXxfqU7Q8ZvKJ.root:secret123@gateway01.ap-northeast-1.prod.aws.tidbcloud.com:4000/test";
		String result = DatabaseConfig.sanitizeJdbcUrl(raw);

		assertEquals(
				"jdbc:mysql://gateway01.ap-northeast-1.prod.aws.tidbcloud.com:4000/test?sslMode=VERIFY_IDENTITY&enabledTLSProtocols=TLSv1.2,TLSv1.3",
				result
		);
	}

	@Test
	void sanitizeJdbcUrl_shouldPreserveExistingQueryParameters() {
		String raw = "mysql://user:pass@host:4000/test?sslMode=REQUIRED";
		String result = DatabaseConfig.sanitizeJdbcUrl(raw);

		assertEquals("jdbc:mysql://host:4000/test?sslMode=REQUIRED", result);
	}

	@Test
	void sanitizeJdbcUrl_shouldLeaveProperJdbcUrlUntouched() {
		String jdbcUrl = "jdbc:mysql://localhost:3306/smartcart_db";
		String result = DatabaseConfig.sanitizeJdbcUrl(jdbcUrl);

		assertEquals(jdbcUrl, result);
	}

	@Test
	void sanitizeJdbcUrl_shouldAddSslToTidbUrlIfMissing() {
		String raw = "jdbc:mysql://gateway01.ap-northeast-1.prod.aws.tidbcloud.com:4000/test";
		String result = DatabaseConfig.sanitizeJdbcUrl(raw);

		assertTrue(result.contains("sslMode=VERIFY_IDENTITY"));
		assertTrue(result.startsWith("jdbc:mysql://"));
	}
}
