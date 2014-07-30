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
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import org.sector67.otp.utils.FileUtils;

/**
 * A file-based implementation of an OTP keystore, primarily for testing
 * purposes. Uses a properties file to store the key offsets.
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
	}

	@Override
	public void init() throws KeyException {
		File keyDir = new File(keyDirectory);
		if (!keyDir.exists()) {
			boolean result = keyDir.mkdirs();
			if (result == false) {
				throw new KeyException(
						"The configured key directory does not exist, and could not be created: ["
								+ keyDirectory + "]");
			}

		}
		if (!keyDir.isDirectory()) {
			throw new KeyException(
					"The configured key directory is not a directory: "
							+ keyDirectory);
		}
		File offsetFile = new File(keyDirectory + File.separator
				+ OFFSET_FILE_NAME);
		if (!offsetFile.exists()) {
			Properties p = new Properties();
			writeOffsetFile(p);
		}
	}

	@Override
	public void setKeyEraser(KeyEraser eraser) {
		this.eraser = eraser;
	}

	/*
	 * Provides the next bytes from the key and replaces those bytes with random
	 * information updates the current index of the key
	 */
	@Override
	public byte[] getKeyBytesForEncryption(String name, int length)
			throws KeyException {
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
				+ File.separator + name, "r");) {
			if (file.length() < offset + length) {
				throw new KeyException(
						"The key is not long enough to provide the requested bytes");
			}
			file.seek(offset);
			file.read(key);
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
		File input = new File(keyDirectory + File.separator + source);
		File output = new File(keyDirectory + File.separator + destination);
		try {
			copy(input, output);
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
			FileUtils.write(key, keyDirectory + File.separator + name);
		} catch (IOException e) {
			throw new KeyException(e);
		}
		p = readOffsetFile();
		p.setProperty(name, new Integer(offset).toString());
		writeOffsetFile(p);
	}

	@Override
	public void deleteKey(String name) throws KeyException {
		Properties p = readOffsetFile();
		if (p.containsKey(name)) {
			// TODO: wipe file using the appropriate strategy
			FileUtils.deleteFile(keyDirectory + File.separator + name);
			p.remove(name);
			writeOffsetFile(p);
		}
	}

	@Override
	public List<String> listKeys() throws KeyException {
		Properties p = readOffsetFile();
		List<String> result = new ArrayList<String>();
		Set<Object> keys = p.keySet();
		for (Iterator<Object> iterator = keys.iterator(); iterator.hasNext();) {
			String keyName = (String) iterator.next();
			result.add(keyName);
		}
		Collections.sort(result);
		return result;
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
			FileUtils.write(b, keyDirectory + File.separator + name);
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
		// TODO: make this use the KeyEraser to clear data before deleting
		Properties p = readOffsetFile();
		Set<String> names = p.stringPropertyNames();
		for (String name : names) {
			FileUtils.deleteFile(keyDirectory + File.separator + name);
		}
		FileUtils.deleteFile(keyDirectory + File.separator + OFFSET_FILE_NAME);
		FileUtils.deleteFile(keyDirectory);
	}

	private void writeOffsetFile(Properties props) {
		try {
			File offsetFile = new File(keyDirectory + File.separator
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
			File offsetFile = new File(keyDirectory + File.separator
					+ OFFSET_FILE_NAME);
			is = new FileInputStream(offsetFile);
			props.load(is);
			is.close();
		} catch (IOException e) {
			throw new KeyException(e);
		}
		return props;
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

	private void copy(File src, File dst) throws IOException {
		InputStream in = new FileInputStream(src);
		OutputStream out = new FileOutputStream(dst);

		// Transfer bytes from in to out
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
	}

	@Override
	public int getCurrentOffset(String keyName) throws KeyException {
		int offset = 0;
		Properties p = readOffsetFile();
		if (p.containsKey(keyName)) {
			String offsetString = p.getProperty(keyName);
			try {
				offset = Integer.parseInt(offsetString);
			} catch (NumberFormatException e) {
				throw new KeyException(e);
			}
		} else {
			throw new KeyException("The key does not exist: [" + keyName + "]");
		}
		return offset;
	}

	@Override
	public int getSize(String keyName) throws KeyException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public byte[] getKeyBytesForDecryption(String name, int offset, int length)
			throws KeyException {
		if (name == null) {
			throw new KeyException("You cannot use a null key name.");
		}
		Properties p = readOffsetFile();
		if (!p.containsKey(name)) {
			throw new KeyException(
					"The requested key does not exist in this key store: "
							+ name);
		}
		byte[] key = new byte[length];
		try (RandomAccessFile file = new RandomAccessFile(keyDirectory
				+ File.separator + name, "r");) {
			if (file.length() < offset + length) {
				throw new KeyException(
						"The key is not long enough to provide the requested bytes");
			}
			file.seek(offset);
			file.read(key);
		} catch (IOException e) {
			throw new KeyException(e);
		}
		return key;
	}

	public void eraseKeyBytes(String keyName, int pos, int length) throws KeyException {
		if (keyName == null) {
			throw new KeyException("You cannot use a null key name.");
		}
		RandomAccessFile key;
		try {
			key = new RandomAccessFile(keyDirectory + File.separator + keyName,
					"rw");
			KeyData kd = new FileKeyData(key);
			eraser.erase(kd, pos, length);
		} catch (FileNotFoundException e) {
			throw new KeyException(e);
		}

	}

}
