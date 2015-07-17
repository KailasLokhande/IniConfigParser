package com.twitter.configparser.models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.twitter.configparser.models.Profile.Value;

/**
 * Each Configuration File is divided into multiple Sections. These sections
 * contains key value pairs. Which have few overrides as well. Each unique set
 * of override forms a Profile. i.e. Each section is collection of multiple
 * Profiles. If there are no overrides for keys in a Section, then Section will
 * have only one Profile which is "Default Profile" with key as null.
 * 
 */
public class Section {

	private static final Logger LOGGER = Logger.getLogger(Section.class
			.getName());
	private final String DEFAULT_PROFILE_KEY = null;

	private Map<String, Profile> profiles = new HashMap<String, Profile>();

	private Map<String, Value> currentContextConfig = null;
	private String name;

	public Section(String name) {
		this.name = name;
	}

	/**
	 * Returns value from default profile. If key does not exist in default
	 * profile, then we will return null. As we are not sure about which profile
	 * to consider.
	 * 
	 * @param key
	 * @return Value from Default Profile.
	 */
	public <V> Value<V> getValue(String key) {
		return profiles.get(DEFAULT_PROFILE_KEY).getValue(key);
	}

	/**
	 * Returns value from given profile (override) name. If key does not exist
	 * in given profile, then we will return key from default profile. As all
	 * profiles have default profile as subset. If default profile also does not
	 * contain value, we will return null.
	 * 
	 * @param key
	 * @param profileName
	 * @return Value from given profile, if does not exist then value from
	 *         default profile
	 */
	public <V> Value<V> getValue(String key, String profileName) {
		Profile profile = profiles.get(profileName);
		if (profile == null) {
			LOGGER.info(String.format(
					"Profile %s is not registered for section %s", profileName,
					this.name));
			return null;
		}

		Value value = profile.getValue(key);
		if (value == null) {
			value = profiles.get(DEFAULT_PROFILE_KEY).getValue(key);
		}
		return value;
	}

	/**
	 * Adds property to default profile.
	 * 
	 * @param key
	 * @param value
	 */
	public <V> void setValue(String key, V value) {
		setValue(key, DEFAULT_PROFILE_KEY, value);
	}

	/**
	 * Adds override for given property
	 * 
	 * @param key
	 * @param profileName
	 * @param value
	 */
	public <V> void setValue(String key, String profileName, V value) {
		Profile profile;
		if (profiles.containsKey(profileName))
			profile = profiles.get(profileName);
		else {
			profile = new Profile();
			profiles.put(profileName, profile);
		}
		profile.putValue(key, value);
	}

	/**
	 * Provides this section's actual resulting configuraiton map based on
	 * COnfiguration override priorities.
	 * 
	 * @return
	 */
	public Map<String, Value> getCurrentContextConfiguration() {

		if (currentContextConfig != null)
			return currentContextConfig;

		// Else prepare current context considering override priority
		List<String> overrides = Configuration.getInstance()
				.getOverridePriorities();

		currentContextConfig = new HashMap<String, Profile.Value>();
		// Load default first
		Profile profile = profiles.get(null);
		if (profile != null)
			currentContextConfig.putAll(profile.getAllProperties());

		for (String string : overrides) {
			profile = profiles.get(string);
			if (profile != null)
				currentContextConfig.putAll(profile.getAllProperties());
		}
		return currentContextConfig;
	}
}
