package com.twitter.configparser.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.twitter.configparser.models.Profile.Value;

/**
 * Configuration class represents Java Object Representation of Configuration
 * File.
 * 
 * Each configuration is consist of multiple Sections and each Section will have
 * there own properties.
 * 
 * Properties can have multiple values depending on profiles.
 * 
 * NOTE: We are not supporting concept as DEFAULT SECTION. But if we are going
 * to add it moving forward, we can easily do that here.
 */
public class Configuration {

	private static final Logger LOGGER = Logger.getLogger(Configuration.class
			.getName());

	/**
	 * Sets override profile priorities. Profile at the end of ArrayList have
	 * more priority.
	 */
	private List<String> overridePriorities = new ArrayList<String>();

	/**
	 * 
	 * Indicates States of Configuration Loading. Here we are Considering
	 * configuration as an entity which can have its own state.
	 *
	 */
	public enum State {
		INITIALIZED, // Configuration object created but nobody has loaded
						// properties yet
		LOADING, // Someone is loading properties
		ERROR, // Configuration loading was failed
		LOADED // Configuration loading was successful
	}

	private Map<String, Section> sections = new HashMap<String, Section>();

	// Singleton
	private static final Configuration INSTANCE = new Configuration();

	private State state;

	// There should be only on object of configuration
	private Configuration() {
		state = State.INITIALIZED;
	}

	public static Configuration getInstance() {
		return INSTANCE;
	}

	/**
	 * Adds warnings and throws exceptions depending on state of Configuration.
	 */
	private void checkConfigStateAndWarning() {
		if (state == State.ERROR)
			throw new IllegalStateException(
					"Looks like configuration load was failed. Configuration is in ERROR State. Please try reloading or correcting config");
		if (state == State.INITIALIZED)
			throw new IllegalStateException(
					"Looks like Config Parser did not run. Configuration is just Initialized but never loaded with properties");
		if (state == State.LOADING)
			LOGGER.warning("Configuration is still loading. You might not get Values on first attempt, try again. ");
	}

	/**
	 * Set Property Value of type V with given key under give sectionName
	 * 
	 * @param key
	 * @param sectionName
	 * @param value
	 */
	public <V> void setProperty(String key, String sectionName, V value) {
		Section section;
		if (sections.containsKey(sectionName))
			section = sections.get(sectionName);
		else {
			section = new Section(sectionName);
			sections.put(sectionName, section);
		}

		section.setValue(key, value);
		LOGGER.info(String
				.format("Added Property to Default Profile with Section %s, KEY %s And Value %s",
						sectionName, key, value));
	}

	/**
	 * Set Property value of type V with given key , under given section and
	 * profile name.
	 * 
	 * @param key
	 * @param sectionName
	 * @param profileName
	 * @param value
	 */
	public <V> void setProperty(String key, String sectionName,
			String profileName, V value) {
		if (profileName == null) {
			setProperty(key, sectionName, value);
			return;
		}
		Section section;
		if (sections.containsKey(sectionName))
			section = sections.get(sectionName);
		else {
			section = new Section(sectionName);
			sections.put(sectionName, section);
		}
		section.setValue(key, profileName, value);
		LOGGER.info(String
				.format("Added Property with Profile %s, Section %s, KEY %s And Value %s",
						profileName, sectionName, key, value));
	}

	/**
	 * Return property with given key under given section name and profile name.
	 * 
	 * @param key
	 * @param sectionName
	 * @param profileName
	 * @return Profile.Value object , value can be retrived from this.
	 */
	public <V> Value<V> getProperty(String key, String sectionName,
			String profileName) {
		checkConfigStateAndWarning();
		if (sections.containsKey(sectionName))
			return null;
		return sections.get(sectionName).getValue(key, profileName);
	}

	/**
	 * Return property value with given key and under section name based on
	 * override priorities
	 * 
	 * @param key
	 * @param sectionName
	 * @return
	 */
	public <V> Value<V> getProperty(String key, String sectionName) {
		checkConfigStateAndWarning();
		if (!sections.containsKey(sectionName))
			return null;

		Section section = sections.get(sectionName);
		Value<V> propertyValue = null;
		// Go throw Priorities defined.
		for (int profileIndex = overridePriorities.size() - 1; profileIndex >= 0; profileIndex--) {
			String profileName = overridePriorities.get(profileIndex).trim();
			Value value = section.getValue(key, profileName);
			if (value != null) {
				propertyValue = value;
				break;
			}
		}
		// if we could not find value in profiles, check in default profile.
		if (propertyValue == null && section.getValue(key) != null) {
			propertyValue = section.getValue(key);
		}

		return propertyValue;
	}

	/**
	 * Return property value as String based on composite key.
	 * 
	 * @param key
	 * @return
	 */
	public String getPropertyAsString(String key) {

		String[] tokens = key.split("\\.");
		if (tokens.length == 1) // only section is required
		{
			Map<String, Value> sectionMap = getSectionConf(key);
			if (sectionMap != null)
				return sectionMap.toString();
			else
				return null;
		}
		String sectionName = tokens[0];
		String propertyKey = tokens[1];
		Value value = getProperty(propertyKey, sectionName);
		if (value == null)
			return null;
		return value.toString();
	}

	/**
	 * Returns complete section configuration considering override priorities.
	 * 
	 * @param sectionName
	 * @return Section Configuration Map
	 */
	public Map<String, Value> getSectionConf(String sectionName) {
		if (!sections.containsKey(sectionName))
			return null;

		return sections.get(sectionName).getCurrentContextConfiguration();
	}

	/**
	 * Update State of Configuration Loading
	 * 
	 * @param state
	 */
	public void changeState(State state) {
		this.state = state;
	}

	/**
	 * Get State of Configuration
	 * 
	 * @return
	 */
	public State getState() {
		return this.state;
	}

	/**
	 * Set Override Priorities.
	 * 
	 * @param overrides
	 */
	public void setOverridePriorities(List<String> overrides) {
		overridePriorities.addAll(overrides);
	}

	/**
	 * Get Override Priorities
	 * 
	 * @return
	 */
	public List<String> getOverridePriorities() {
		return overridePriorities;
	}
}
