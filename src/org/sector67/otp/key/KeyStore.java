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
	public byte[] nextBytes(String name, int length) throws KeyException;
	public void setKeyEraser(KeyEraser eraser);
	public void deleteKey(String name) throws KeyException;
	public List<String> listKeys() throws KeyException;
	public void init() throws KeyException;
}