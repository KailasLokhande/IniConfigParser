package com.twitter.configparser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

import com.twitter.configparser.models.Configuration;
import com.twitter.configparser.models.Configuration.State;

/**
 * 
 * Actual worker who parses and loads configuration.
 * 
 * This worker can accept 1 or more config files and overrides.
 *
 */
public class ConfigParsingWorker implements Runnable {

	private static final Logger LOGGER = Logger
			.getLogger(ConfigParsingWorker.class.getName());
	private File[] configFiles;
	private List<String> overrideNames;

	public ConfigParsingWorker(File[] files, String... overrideNames) {
		configFiles = files;
		if (overrideNames != null)
			this.overrideNames = Arrays.asList(overrideNames);
	}

	public ConfigParsingWorker(File file, String... overrideNames) {
		this(new File[] { file }, overrideNames);
	}

	@Override
	public void run() {
		Configuration configuration = Configuration.getInstance();
		configuration.setOverridePriorities(overrideNames);
		configuration.changeState(State.LOADING);

		for (File configFile : configFiles) {

			LOGGER.fine("Staring Configuration Parsing Work . Config File is located at: "
					+ configFile.getAbsolutePath());
			boolean failed = false;
			try {
				Scanner scanner = new Scanner(configFile);
				String currentSection = "";
				while (scanner.hasNextLine()) {
					String line = scanner.nextLine().trim();
					if (line.isEmpty() || line == null)
						continue;
					// Ignore the line if it starts with ";" -- COMMENT IN
					// CONFIG
					if (line.startsWith(";"))
						continue;
					// remove Comment part from current line
					if (line.indexOf(";") > -1)
						line = line.split(";")[0];

					// Check for section start -- Starts with [ , then word
					// characters , then end with ]
					if (line.matches("(\\[)([\\w]+)(\\])")) {
						currentSection = line.replace("[", "");
						currentSection = currentSection.replace("]", "");
						LOGGER.info("Started Parsing Section " + currentSection);
						continue;
					}

					// Now these lines must be property values
					String[] tokens = line.split("=");
					if (tokens.length != 2) {
						configuration.changeState(State.ERROR);
						LOGGER.severe("Invalid Config Entry Found on Line : "
								+ line);
						throw new IllegalArgumentException(
								"Invalid Config entry found : " + line);
					}

					String key = tokens[0].trim();
					String value = tokens[1].trim();

					// Check for possiblities in KEY
					// 1. Key can belong to default profile
					// 2. Key can belong to overriden profile
					String[] keyTokens = key.split("<");
					String profileName = null;
					if (keyTokens.length == 2) {
						key = keyTokens[0];
						profileName = keyTokens[1].replace(">", "");
						if (overrideNames != null
								&& !overrideNames.contains(profileName)) {
							LOGGER.info(String.format(
									"Skipping Key %s for Profile %s", key,
									profileName));
							continue;
						}
					}

					// Check for possibilities in Value
					// 1. Value can be simple boolean
					// 2. Value can be simple String
					// 3. Value can be an Array / List

					if (value.toLowerCase().equals("yes")
							|| value.toLowerCase().equals("true")
							|| value.toLowerCase().equals("1")) {
						configuration.setProperty(key, currentSection,
								profileName, true);
						continue;
					}

					if (value.toLowerCase().equals("no")
							|| value.toLowerCase().equals("false")
							|| value.toLowerCase().equals("0")) {
						configuration.setProperty(key, currentSection,
								profileName, false);
						continue;
					}

					if (!value.matches("(\")(.*)(\")")) {
						String[] valueTokens = value.split(",");
						if (valueTokens.length > 1) {
							configuration.setProperty(key, currentSection,
									profileName, Arrays.asList(valueTokens));
							continue;
						}
					}

					configuration.setProperty(key, currentSection, profileName,
							value);
				}

			} catch (FileNotFoundException e) {
				LOGGER.severe(String.format(
						"File %s was not found at desired location",
						configFile.getAbsolutePath()));
				configuration.changeState(State.ERROR);
				failed = true;
			}

			if (failed)
				configuration.changeState(State.ERROR);
			else
				configuration.changeState(State.LOADED);
		}
	}
}
