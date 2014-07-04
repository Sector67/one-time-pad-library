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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import org.sector67.otp.key.InMemoryKeyStore.InMemoryKeyData;
import org.sector67.otp.utils.FileUtils;

/**
 * A file-based implementation of an OTP keystore, primarily for testing
 * purposes.  Uses a properties file to store the key offsets.
 * 
 * @author scott.hasse@gmail.com
 * 
 */
public class FileKeyStore implements TestableKeyStore {

	String OFFSET_FILE_NAME = "offsets.properties";
	String keyDirectory;
	Random r = new SecureRandom();

	private KeyEraser eraser = new MultiPassSecureRandomEraser();
	
	public FileKeyStore(String keyDirectory) {
		this.keyDirectory = keyDirectory;
		File keyDir = new File(keyDirectory);
		if (!keyDir.exists() || !keyDir.isDirectory()) {
			throw new IllegalArgumentException(
					"The configured key directory does not exist or is not a directory: "
							+ keyDirectory);
		}
		File offsetFile = new File(keyDirectory + File.pathSeparator
				+ OFFSET_FILE_NAME);
		if (!offsetFile.exists()) {
			Properties p = new Properties();
			writeOffsetFile(p);
		}
	}

	public void setKeyEraser(KeyEraser eraser) {
		this.eraser = eraser;
	}

	/*
	 * Provides the next bytes from the key and replaces those bytes with random
	 * information updates the current index of the key
	 */
	@Override
	public byte[] nextBytes(String name, int length) throws KeyException {
		if (name == null) {
			throw new KeyException("You cannot use a null key name.");
		}
		Properties p = readOffsetFile();
		if (!p.containsKey(name)) {
			throw new KeyException(
					"The requested key does not exist in this key store: "
							+ name);
		}
		int offset = Integer.parseInt(p.getProperty(name));
		byte[] key = new byte[length];
		try (RandomAccessFile file = new RandomAccessFile(keyDirectory
				+ File.pathSeparator + name, "r");) {
			if (file.length() < offset + length) {
				throw new KeyException(
						"The key is not long enough to provide the requested bytes");
			}
			file.seek(offset);
			file.read(key);
			clear(name, offset, length);
		} catch (IOException e) {
			throw new KeyException(e);
		}

		p = readOffsetFile();
		p.setProperty(name, new Integer(offset + length).toString());
		writeOffsetFile(p);
		return key;
	}

	@Override
	public Set<String> getKeyNames() throws KeyException {
		Properties p = readOffsetFile();
		return p.stringPropertyNames();
	}

	@Override
	public void copyKey(String source, String destination) throws KeyException {
		File input = new File(keyDirectory + File.pathSeparator + source);
		File output = new File(keyDirectory + File.pathSeparator + destination);
		try {
			Files.copy(input.toPath(), output.toPath(), new CopyOption[0]);
		} catch (IOException e) {
			throw new KeyException(e);
		}

		Properties p = readOffsetFile();
		p.setProperty(destination, p.getProperty(source));
		writeOffsetFile(p);

	}

	@Override
	public void addKey(String name, byte[] key, int offset) throws KeyException {
		if (name == null) {
			throw new IllegalArgumentException(
					"You cannot create a null key name.");
		}
		Properties p = readOffsetFile();
		if (p.containsKey(name)) {
			throw new IllegalArgumentException(
					"You cannot create a key that already exists: " + name);
		}
		try {
			FileUtils.write(key, keyDirectory + File.pathSeparator + name);
		} catch (IOException e) {
			throw new KeyException(e);
		}
		p = readOffsetFile();
		p.setProperty(name, new Integer(offset).toString());
		writeOffsetFile(p);
	}

	@Override
	public void generateKey(String name, int length) throws KeyException {
		if (name == null) {
			throw new IllegalArgumentException(
					"You cannot create a null key name.");
		}
		Properties p = readOffsetFile();
		if (p.containsKey(name)) {
			throw new IllegalArgumentException(
					"You cannot create a key that already exists: " + name);
		}
		byte[] b = new byte[length];
		r.nextBytes(b);
		try {
			FileUtils.write(b, keyDirectory + File.pathSeparator + name);
		} catch (IOException e) {
			throw new KeyException(e);
		}
		p = readOffsetFile();
		p.setProperty(name, "0");
		writeOffsetFile(p);
	}

	/*
	 * Destroys this keystore, eliminating all file-based key and index data,
	 * and attempts to delete the keystore directory
	 */
	public void destroy() throws KeyException {
		//TODO: make this use the KeyEraser to clear data before deleting
		Properties p = readOffsetFile();
		Set<String> names = p.stringPropertyNames();
		for (String name : names) {
			FileUtils.deleteFile(keyDirectory + File.pathSeparator + name);
		}
		FileUtils.deleteFile(keyDirectory + File.pathSeparator
				+ OFFSET_FILE_NAME);
		FileUtils.deleteFile(keyDirectory);
	}

	private void writeOffsetFile(Properties props) {
		try {
			File offsetFile = new File(keyDirectory + File.pathSeparator
					+ OFFSET_FILE_NAME);
			OutputStream out = new FileOutputStream(offsetFile);
			props.store(out, "A property file storing the key offsets");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Properties readOffsetFile() throws KeyException {
		Properties props = new Properties();
		InputStream is = null;
		try {
			File offsetFile = new File(keyDirectory + File.pathSeparator
					+ OFFSET_FILE_NAME);
			is = new FileInputStream(offsetFile);
			props.load(is);
			is.close();
		} catch (IOException e) {
			throw new KeyException(e);
		}
		return props;
	}
	
	private void clear(String keyName, int pos, int length) throws KeyException {
		RandomAccessFile key;
		try {
			key = new RandomAccessFile(keyDirectory
					+ File.pathSeparator + keyName, "rw");
			KeyData kd = new FileKeyData(key);
			eraser.erase(kd, pos, length);
		} catch (FileNotFoundException e) {
			throw new KeyException(e);
		}

	}
	
	public class FileKeyData implements KeyData {
		private RandomAccessFile key;
		
		public FileKeyData(RandomAccessFile key) {
			this.key = key;
		}

		public void close() throws KeyException {
			try {
				key.close();
			} catch (IOException e) {
				throw new KeyException(e);
			}
		}

		public void seek(int position) throws KeyException {
			try {
				key.seek(position);
			} catch (IOException e) {
				throw new KeyException(e);
			}
			
		}

		public void write(byte[] data) throws KeyException {
			try {
				key.write(data);
			} catch (IOException e) {
				throw new KeyException(e);
			}
		}
		
	}

}
