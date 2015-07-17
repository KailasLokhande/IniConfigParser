/**
 * 
 */
package com.twitter.configparser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.twitter.configparser.models.Configuration;
import com.twitter.configparser.models.Configuration.State;

public class ConfigParserTest {

	private Configuration config = Configuration.getInstance();

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		// Application Boot Program will use this code to start parsing and
		// loading configuration
		// Boot program just spawns a process which starts parsing and does not
		// block itself from doing other activities.
		// Boot program may take decision of waiting or not waiting till
		// application
		// Config is successfully Loaded
		ConfigParser configParser = new ConfigParser();
		configParser.load("src/resources/Config.ini", new String[] { "ubuntu",
				"production" });
		// Wait till ConfigParserWorker completes its task and moves
		// configuration state to either ERROR or LOADED
		while (config.getState() == State.LOADING
				|| config.getState() == State.INITIALIZED) {
			Thread.currentThread().sleep(2000);
		}

		if (config.getState() == State.ERROR)
			fail("Configuration loading failed");
	}

	public void check(String key, String expectedResult) {
		String value = config.getPropertyAsString(key);
		assertEquals(expectedResult, value);
	}

	@Test
	public void testFTPName() {
		check("ftp.name", "\"hello there, ftp uploading\"");
	}

	@Test
	public void testHTTPParams() {
		check("http.params", "[array, of, values]");
	}

	@Test
	public void testFTPLastName() {
		check("ftp.lastname", null);
	}

	@Test
	public void testFTPEnabled() {
		check("ftp.enabled", "false");
	}

	@Test
	public void testFTPPath() {
		check("ftp.path", "/srv/var/tmp/");
	}

	@Test
	public void testSection() {
		Map<String, String> ftpMap = new HashMap<String, String>();
		ftpMap.put("name", "\"hello there, ftp uploading\"");
		ftpMap.put("path", "/srv/var/tmp/");
		ftpMap.put("enabled", "false");

		check("ftp", ftpMap.toString());
	}

	@Test
	public void testCommonPaidUserSizeLimit() {

		check("common.paid_users_size_limit", "2147483648");
		// > Config.get("ftp
		//
		// # returns a symbolized hashmap:
		//
		// {
		//
		// “name” == “http uploading”,
		//
		// “path” == “/etc/var/uploads”,
		//
		// “enabled” == false

		// }
	}

}
