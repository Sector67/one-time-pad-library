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

package org.sector67.otp.utils;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

/**
 * This class provides simple interfaces to work with binary data in various bases.
 * 
 * @author scott.hasse@gmail.com
 */
public class BaseUtils {

	private static String HEADER="------START DATA-----";
	private static String FOOTER="------END DATA-----";
	private static int MAJOR_CHUNK_SIZE=40;
	private static int MINOR_CHUNK_SIZE=2;
	private static String MINOR_CHUNK_SEPARATOR="  ";
	private static String MAJOR_CHUNK_SEPARATOR="\n";

	public static String bytesToBase64(byte[] input) {
		return Base64.encodeBase64String(input);
	}

	public static byte[] base64ToBytes(String input) {
		if (!Base64.isBase64(input)) {
			throw new IllegalArgumentException("The provided input is not valid base64.");
		}
		return Base64.decodeBase64(input);
	}
	

	
	/*
	 * Chunks the byte[] data into base64 chunks of the configured length.  Chunks are separated
	 * with line terminators, including the last chunk.
	 */
	public static String getChunkedBase64(byte[] data) {
		String base64 = bytesToBase64(data);
	    StringBuffer result = new StringBuffer();
	    int length = base64.length();
	    for (int i = 0; i < length; i += MAJOR_CHUNK_SIZE) {
	        result.append(base64.substring(i, Math.min(length, i + MAJOR_CHUNK_SIZE)));
	        result.append(MAJOR_CHUNK_SEPARATOR);
	    }
	    return result.toString();
	}
	
	public static String getFooter() {
		return FOOTER;
	}
	
	public static String getHeader() {
		return HEADER;
	}
	
	public static String bytesToBase32(byte[] input) {
		Base32 b = new Base32();
		return b.encodeToString(input);
	}

	public static byte[] base32ToBytes(String input) {
		Base32 b = new Base32();
		if (!b.isInAlphabet(input)) {
			throw new IllegalArgumentException("The provided input is not valid base32.");
		}
		return b.decode(input);
	}

	/*
	 * Chunks the byte[] data into base64 chunks of the configured length.  Chunks are separated
	 * with line terminators, including the last chunk.
	 */
	public static String getChunkedBase32(byte[] data) {
		String base32 = bytesToBase32(data);
	    StringBuffer result = new StringBuffer();
	    int length = base32.length();
	    for (int i = 0; i < length; i += MAJOR_CHUNK_SIZE) {
	        result.append(base32.substring(i, Math.min(length, i + MAJOR_CHUNK_SIZE)));
	        result.append(MAJOR_CHUNK_SEPARATOR);
	    }
	    return result.toString();
	}


}
