package software.coley.recaf.web;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic tests for WebServer.
 */
class WebServerTest {
	
	@Test
	void testHealthResponseCreation() {
		WebServer.HealthResponse response = new WebServer.HealthResponse("ok", "test message");
		
		assertEquals("ok", response.status);
		assertEquals("test message", response.message);
	}
}
