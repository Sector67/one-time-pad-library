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

import java.util.List;

/**
 * This interface defines the required methods of an OTP keystore.
 * 
 * @author scott.hasse@gmail.com
 */
public interface KeyStore {
	/**
	 * This method is used when obtaining key bytes for encryption
	 * @param name
	 * @param length
	 * @return
	 * @throws KeyException
	 */
	public byte[] getKeyBytesForEncryption(String keyName, int length) throws KeyException;
	public void setKeyEraser(KeyEraser eraser);
	public void deleteKey(String name) throws KeyException;
	public List<String> listKeys() throws KeyException;
	public void init() throws KeyException;
	public void generateKey(String name, int length) throws KeyException;
	public void addKey(String name, byte[] key, int offset) throws KeyException;
	public void copyKey(String source, String destination) throws KeyException;
	public int getCurrentOffset(String keyName) throws KeyException;
	public int getSize(String keyName) throws KeyException;
	
	/**
	 * This method is used when obtaining key bytes for decryption
	 * @param name
	 * @param offset
	 * @param length
	 * @return
	 */
	public byte[] getKeyBytesForDecryption(String name, int offset, int length) throws KeyException;
}