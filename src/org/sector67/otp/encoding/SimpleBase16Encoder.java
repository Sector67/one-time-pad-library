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

package org.sector67.otp.encoding;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

/**
 * 
 * @author scott.hasse@gmail.com
 *
 */
public class SimpleBase16Encoder implements TextEncoder {

	protected int majorChunkSize = 20; //the size in bytes of major chunks
	protected int minorChunkSize = 1; //the size in bytes of minor chunks
	protected String minorChunkSeparator = "  ";
	protected String majorChunkSeparator = "\n";
	
	/* 
	 * Sets how many bytes in each major chunk
	 */
	public void setMajorChunkSize(int majorChunkSize) {
		this.majorChunkSize = majorChunkSize;
	}

	public void setMinorChunkSize(int minorChunkSize) {
		this.minorChunkSize = minorChunkSize;
	}

	public void setMinorChunkSeparator(String minorChunkSeparator) {
		this.minorChunkSeparator = minorChunkSeparator;
	}

	public void setMajorChunkSeparator(String majorChunkSeparator) {
		this.majorChunkSeparator = majorChunkSeparator;
	}

	@Override
	public String encode(byte[] input) throws EncodingException {
		return getChunkedBase16(input);
	}

	@Override
	public byte[] decode(String input) throws EncodingException {
		byte[] result = null;
		input  = cleanInput(input);
		try {
			result =  Hex.decodeHex(input.toCharArray());
		} catch (DecoderException e) {
			throw new IllegalArgumentException("The provided input is not valid base16.", e);
		}
		return result;
	}
	
	protected String cleanInput(String input) {
		input  = input.replaceAll("[" + minorChunkSeparator + majorChunkSeparator + "]","");
		return input;
	}
	
	protected String bytesToBase16(byte[] input) {
		char[] hex = Hex.encodeHex(input);
		String result = new String(hex);
		return result.toUpperCase();
	}

	/*
	 * 
	 */
	protected String getChunkedBase16(byte[] data) {
		String base16 = bytesToBase16(data);
	    StringBuffer result = new StringBuffer();
	    //For each byte in, there are two characters returned
	    int base16StringMajorChunkSize = majorChunkSize * 2;
	    int base16StringMinorChunkSize = minorChunkSize * 2;
	    int ilength = base16.length();
	    for (int i = 0; i < ilength; i += base16StringMajorChunkSize) {
	    	String line = base16.substring(i, Math.min(ilength, i + base16StringMajorChunkSize));
	    	int jlength = line.length();
	    	for (int j = 0; j < jlength; j += base16StringMinorChunkSize) {
	            result.append(line.substring(j, Math.min(jlength, j + base16StringMinorChunkSize)));
	            if (j + base16StringMinorChunkSize < jlength) {
	            	result.append(minorChunkSeparator);
	            }
	    	}
	        result.append(majorChunkSeparator);
	    }
	    return result.toString();
	}
	
}
