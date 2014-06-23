/*
 * Copyright 2014 individual contributors as indicated by the @author 
 * tags
 * 
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this software.  If not, see <http://www.gnu.org/licenses/>. 
 */

package org.sector67.otp.key;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * An in-memory implementation of an OTP keystore primarily for testing purposes 
 * 
 * @author scott.hasse@gmail.com
 */
public class InMemoryKeyStore implements TestableKeyStore {

	private Map<String, Integer> offsets = new HashMap<String, Integer>();
	private Map<String, byte[]> keys = new HashMap<String, byte[]>(); 
	Random r = new SecureRandom();

	public void generateKey(String name, int length) {
		if (name == null) {
			throw new IllegalArgumentException("You cannot create a null key name.");			
		}
		if (keys.containsKey(name)) {
			throw new IllegalArgumentException("You cannot create a key that already exists: " + name);
		}
		offsets.put(name, new Integer(0));
		byte[] key = new byte[length];
		r.nextBytes(key);
		keys.put(name, key);
	}
	
	/* 
	 * Put a key in the key store, for testing purposes
	 */
	public void addKey(String name, byte[] key, int offset) {
		if (name == null || key == null) {
			throw new IllegalArgumentException("You cannot create a null key name.");			
		}
		if (keys.containsKey(name)) {
			throw new IllegalArgumentException("You cannot create a key that already exists: " + name);
		}		
		offsets.put(name, new Integer(offset));
		keys.put(name, Arrays.copyOf(key, key.length));
	}
	
	/*
	 * Provides the next bytes from the key and replaces those bytes with random information
	 * updates the current index of the key
	 */
	@Override
	public byte[] nextBytes(String name, int length) throws KeyException {
		if (name == null) {
			throw new KeyException("You cannot use a null key name.");			
		}
		if (!keys.containsKey(name)) {
			throw new KeyException("The requested key does not exist in this key store: " + name);
		}
		byte[] key = keys.get(name);
		int currentOffset = offsets.get(name);
		if (key.length < currentOffset + length) {
			throw new KeyException("The key is not long enough to provide the requested bytes");			
		}
		byte[] result = new byte[length];
		for (int i = 0; i < length; i++) {
			result[i] = key[currentOffset + i];
			byte[] random = new byte[1];
			r.nextBytes(random);
			key[currentOffset + i] = random[0];
		}
		offsets.put(name, currentOffset + length);
		return result;
	}
	
	public Set<String> getKeyNames() {
		return keys.keySet();
	}
	
	public void copyKey(String source, String destination) {
		byte[] key = keys.get(source);
		byte[] copy = Arrays.copyOf(key, key.length);
		int offset = offsets.get(source);
		offsets.put(destination, offset);
		keys.put(destination, copy);
	}

}
