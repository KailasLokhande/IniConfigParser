package com.twitter.configparser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * 
 * ConfigParser is entity responsible starting configuration file loading and
 * parsing. ConfigParser should be invoked by Boot Loader of application with
 * path of configuration file. ConfigParser starts {@link ConfigParsingWorker} ,
 * which asynchronously loads configuration from file to memory
 *
 */
public class ConfigParser {
	private static Logger logger = Logger.getLogger(ConfigParser.class
			.getName());

	/**
	 * Load multiple File paths.
	 * 
	 * @param filePaths
	 * @param overrideNames
	 */
	public void load(String[] filePaths, String... overrideNames) {
		List<File> files = new ArrayList<File>();
		for (String filePath : filePaths) {
			File file = new File(filePath);
			if (!file.exists()) {
				logger.severe(String.format("Config file does not exists %s",
						file.getAbsoluteFile()));
				throw new IllegalArgumentException(String.format(
						"Config file does not exists on path %s",
						file.getAbsolutePath()));
			}

			files.add(file);
		}

		File[] filesArray = new File[files.size()];
		filesArray = files.toArray(filesArray);
		load(filesArray, overrideNames);
	}

	/**
	 * Function to start loading config file.
	 * 
	 * @param filePath
	 * @param overrideNames
	 */
	public void load(String filePath, String... overrideNames) {
		File file = new File(filePath);
		if (!file.exists()) {
			logger.severe(String.format("Config file does not exists %s",
					file.getAbsoluteFile()));
			throw new IllegalArgumentException(String.format(
					"Config file does not exists on path %s",
					file.getAbsolutePath()));
		}
		load(new File[] { file }, overrideNames);
	}

	/**
	 * Load multiple config files into memory.
	 * 
	 * @param files
	 * @param overrideNames
	 */
	public void load(File[] files, String... overrideNames) {

		startParser(files, overrideNames);
	}

	/**
	 * Method to start worker thread for parsing.
	 * 
	 * @param files
	 * @param overrideNames
	 */
	private void startParser(File[] files, String... overrideNames) {
		ConfigParsingWorker configParsingWorker = new ConfigParsingWorker(
				files, overrideNames);
		new Thread(configParsingWorker).start();
	}
}
