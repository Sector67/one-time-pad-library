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

/**
 * An interface to facilitate erasing key data using different pluggable strategies.
 * Intentionally designed to only allow writing, not reading, key data.
 * 
 * @author scott.hasse@gmail.com
 *
 */
public interface KeyData {
	public void seek(int position) throws KeyException;
	public void write(byte[] data) throws KeyException;
	public void close() throws KeyException;
}
