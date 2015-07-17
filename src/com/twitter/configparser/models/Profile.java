package com.twitter.configparser.models;

import java.util.HashMap;
import java.util.Map;

/**
 * Each section in configuration file consist of Key and Value pairs. These
 * values can be overridden and set of such overridden values forms 1 Profile.
 * By default, profile will be default profile.
 * 
 * NOTE: It appears that, instead of creating new Class as Profile to maintain
 * properties Key value relations, we could have used simple Map. But reason
 * behind creating this as a separate class is considering future use case, for
 * example, if we start supporting expression, then we need to have our own
 * logic to parse these expression and return value when interacted.
 * 
 */
public class Profile {

	/**
	 * Structure to store Profile Values.
	 * 
	 * We dont have well defined value type. So having this genering structure
	 * which wrapps around actual value helps us Object of this type actually
	 * know , what type of property it indicates.
	 * 
	 * @param <V>
	 */
	static class Value<V> {

		private V value;

		public Value(V value) {
			this.value = value;
		}

		public V getValue() {
			return value;
		}

		@Override
		public String toString() {
			return value.toString();
		}
	}

	private Map<String, Value> properties = new HashMap<String, Profile.Value>();

	/**
	 * Return value for given Key. if key does not exist, return null;
	 * 
	 * @param key
	 * @return
	 */
	public Value getValue(String key) {
		return properties.get(key);
	}

	/**
	 * Add property to this profile.
	 * 
	 * @param key
	 * @param value
	 */
	public <V> void putValue(String key, V value) {
		// Here you can have authorization, who can insert values in config
		properties.put(key, new Value<V>(value));
	}

	public Map<String, Value> getAllProperties() {
		return properties;
	}
}
