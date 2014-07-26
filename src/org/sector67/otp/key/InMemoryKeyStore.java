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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
	private KeyEraser eraser = new MultiPassSecureRandomEraser();
	
	@Override
	public void init() {
		
	}

	@Override
	public void setKeyEraser(KeyEraser eraser) {
		this.eraser = eraser;
	}

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
	 * Provides the next bytes from the key and erases the data using the provided KeyEraser
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
		}
		//clear the data
		clear(name, currentOffset, length);
		//update the offset
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
	
	private void clear(String keyName, int pos, int length) throws KeyException {
		byte[] key = keys.get(keyName);
		KeyData kd = new InMemoryKeyData(key);
		eraser.erase(kd, pos, length);
	}
	
	public class InMemoryKeyData implements KeyData {
		private int position = 0;
		private byte[] key;
		
		public InMemoryKeyData(byte[] key) {
			this.key = key;
			this.position = 0;
		}

		public void close() {
			//no-op for in-memory
		}

		public void seek(int position) throws KeyException {
			this.position = position;
			
		}

		public void write(byte[] data) throws KeyException {
			//System.out.println("data length: " + data.length + " position: " + position); 
			for (int i = 0; i < data.length; i++) {
				key[position + i] = data[position];
			}
		}
		
	}

	@Override
	public List<String> listKeys() {
		List<String> result = new ArrayList<String>();
		result.addAll(keys.keySet());
		Collections.sort(result);
		return result;
	}
	
	@Override
	public void deleteKey(String name) {
		if (offsets.containsKey(name)) {
			offsets.remove(name);
		}
		if (keys.containsKey(name)) {
			keys.remove(name);
		}
		
	}

}
