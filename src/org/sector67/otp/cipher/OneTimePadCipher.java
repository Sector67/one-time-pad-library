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

package org.sector67.otp.cipher;

import java.io.UnsupportedEncodingException;

import org.sector67.otp.key.KeyException;
import org.sector67.otp.key.KeyStore;

/**
 * A one-time pad cipher implementation.
 * 
 * This particular implementation is meant for encrypting relatively small values and thus loads the 
 * appropriate portion of the key directly into memory.  If there was a need to encrypt longer sequences 
 * of bytes, a streaming solution would be more appropriate.
 * 
 * The Java Cryptography Extension (JCE) API was considered as a framework for implementing this cipher, but 
 * was rejected for a few reasons:
 * 
 * 1) JCE is not well designed for long symmetric key encryption like OTP.  Although it could be made to work,
 *    for a simple algorithm like OTP it is overkill for no apparent benefit. 
 * 2) Using JCE properly requires installing a signed provider jar and modification of the system's java.security 
 *    file.  This introduces additional complexity with no substantial gain.
 * 3) Some of the goals of this project are transparency and verifiability, implementing a CipherSpi that is
 *    then wrapped in a Cipher instance reduces both of those, again with no apparent upside. 
 * 
 * @author scott.hasse@gmail.com
 */

public class OneTimePadCipher {
	private String CHARSET = "UTF-8";
	private KeyStore store;

	public OneTimePadCipher(KeyStore store) {
		this.store = store;
	}
	/*
	 * Encrypts a String as UTF-8 bytes given the filename of a key.
	 */
	public byte[] encrypt(String keyname, String input) throws KeyException, CipherException {
		if (input == null) {
			throw new CipherException("Cannot encrypt null plain text");
		}
		if (keyname == null) {
			throw new CipherException("Cannot encrypt with a null key name");
		}
		//convert String to UTF-8 bytes
		byte[] inputBytes = null;
		try {
			inputBytes = input.getBytes(CHARSET);
		} catch (UnsupportedEncodingException e) {
			throw new CipherException(e);
		}
		//get the appropriate key
		byte[] key = store.nextBytes(keyname, inputBytes.length);
		byte[] encrypted = encrypt(inputBytes, key);
		return encrypted;
	}

	/*
	 * Decrypts the provided bytes into a Java String given the name of a key.  Assumes the data UTF-8 bytes. 
	 */
	public String decrypt(String keyname, byte[] input) throws KeyException, CipherException {
		//decrypt
		//get the appropriate key data
		byte[] key = store.nextBytes(keyname, input.length);
		byte[] decrypted = decrypt(input, key);
		//interpret the result as UTF-8 bytes 
		String result = null;
		try {
			result = new String(decrypted, CHARSET);
		} catch (UnsupportedEncodingException e) {
			throw new CipherException(e);
		}
		return result;
	}
	
	/* Encrypts the String input as UTF-8 binary data */
	public byte[] encryptString(String input, byte[] key) throws CipherException {
		if (input == null) {
			throw new CipherException("Cannot encrypt null plain text");
		}
		if (key == null) {
			throw new CipherException("Cannot encrypt with a null key");
		}
		byte[] b = null;
		try {
			b = input.getBytes(CHARSET);
		} catch (UnsupportedEncodingException e) {
			throw new CipherException(e);
		}
		byte[] encrypted = encrypt(b, key);
		
		return encrypted;
	}
	
	/* Decrypts an input byte[] into a Java String
	 * This process expects the binary data to be a UTF-8 encoding
	 */
	public String decryptToString(byte[] input, byte[] key) throws CipherException {
		if (input == null) {
			throw new CipherException("Cannot decrypt null cipher text");
		}
		if (key == null) {
			throw new CipherException("Cannot decrypt with a null key");
		}
		String s = null;
		byte[] b = decrypt(input, key);
		try {
			s = new String(b, CHARSET);
		} catch (UnsupportedEncodingException e) {
			throw new CipherException(e);
		}
		return s;
	}
	
	public byte[] encrypt(byte[] plaintext, byte[] key) throws CipherException {
		if (plaintext == null) {
			throw new CipherException("Cannot encrypt null plain text");
		}
		if (key == null) {
			throw new CipherException("Cannot encrypt with a null key");
		}
		if (plaintext.length != key.length) {
			throw new CipherException("Cannot encrypt, input byte length [" + plaintext.length + "] is not the same as the key length [" + key.length + "]");
		}
		byte[] result = new byte[plaintext.length];
		for (int i = 0; i < key.length; i++) {
			result[i] = (byte) (plaintext[i] ^ key[i]);
		}
		return result;
	}

	public byte[] decrypt(byte[] ciphertext, byte[] key) throws CipherException {
		if (ciphertext == null) {
			throw new CipherException("Cannot decrypt null cipher text");
		}
		if (key == null) {
			throw new CipherException("Cannot decrypt with a null key");
		}
		if (ciphertext.length != key.length) {
			throw new CipherException("Cannot decrypt, input byte length [" + ciphertext.length + "] is not the same as the key length [" + key.length + "]");
		}
		byte[] result = new byte[ciphertext.length];
		for (int i = 0; i < key.length; i++) {
			result[i] = (byte) (ciphertext[i] ^ key[i]);
		}
		return result;
	}
}
