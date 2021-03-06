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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import com.google.zxing.common.reedsolomon.GenericGF;
import com.google.zxing.common.reedsolomon.ReedSolomonDecoder;
import com.google.zxing.common.reedsolomon.ReedSolomonEncoder;
import com.google.zxing.common.reedsolomon.ReedSolomonException;

/**
 * A class to encode binary data into base 16 chunks, adding Reed Solomon error correcting bytes
 * to the end of each major chunk.
 * 
 * @author scott.hasse@gmail.com
 *
 */
public class ErrorCorrectingBase16Encoder extends SimpleBase16Encoder {

	private int errorCorrectionBytes = 4;
	private GenericGF gf = GenericGF.QR_CODE_FIELD_256;
	private int maxBytes = 256;
	
	@Override
	public String encode(byte[] input) throws EncodingException {
		// chunk up the input into maxChunkSize - errorCorrectionBytes sized chunks of data, and add error correction to each chunk
	    int ilength = input.length;
	    ByteArrayOutputStream result = new ByteArrayOutputStream();
	    int eccChunkSize = majorChunkSize - errorCorrectionBytes; 

	    for (int i = 0; i < ilength; i += eccChunkSize) {
	    	byte[] noEccChunk = Arrays.copyOfRange(input, i, Math.min(ilength, i + eccChunkSize));
			//System.out.println("noEccChunk [" + super.encode(noEccChunk) + "]");

	    	byte[] errorCorrected = addECC(noEccChunk);
	        try {
				result.write(errorCorrected);
				//System.out.println("chunk range: " + i + " to " + Math.min(ilength, i + eccChunkSize));
				//System.out.println("chunking to [" + super.encode(errorCorrected) + "]");
			} catch (IOException e) {
				// in-memory byte stream should not have this problem
				throw new RuntimeException(e);
			}
	    }
		// then base16 encode and chunk the whole thing
		return super.encode(result.toByteArray());
	}

	@Override
	public byte[] decode(String input) throws EncodingException {
		// chunk up the input into maxChunkSize sized chunks of data, and add error correction to each chunk
		input  = cleanInput(input);
	    int ilength = input.length();
	    StringBuffer result = new StringBuffer();
	    
	    //there are two hex characters for each byte
	    int base16StringMajorChunkSize = majorChunkSize * 2;

	    for (int i = 0; i < ilength; i += base16StringMajorChunkSize) {
	    	String chunk = input.substring(i, Math.min(ilength, i + base16StringMajorChunkSize));
			//System.out.println("dechunking to [" + chunk + "]");

	    	byte[] byteChunk = super.decode(chunk);
			try {
				byte[] corrected = removeECC(byteChunk);
				//append the string data stripped of the ECC characters
				result.append(super.encode(corrected));
			} catch (ReedSolomonException e) {
				throw new EncodingException(e);
			}
	    }
		return super.decode(result.toString());
	}
	
	
	private byte[] addECC(byte[] input) {
		if (input == null) {
			throw new IllegalArgumentException("The input to error correction code cannot be null");
		}
		if (input.length > (maxBytes - errorCorrectionBytes)) {
			throw new IllegalArgumentException("The input to error correction code plus error correction bytes cannot be longer than 256");
		}
		//convert byte[] to int[] into an array large enough to hold the ECC
		int[] ints = new int[input.length + errorCorrectionBytes];
		for (int i = 0; i < input.length; i++) {
			ints[i] = input[i] & 0xFF;
		}
		ReedSolomonEncoder e = new ReedSolomonEncoder(gf);
		e.encode(ints, errorCorrectionBytes);
		//convert int[] to byte[]
		byte[] output = new byte[ints.length];
		for (int i = 0; i < ints.length; i++) {
			output[i] = (byte) ints[i];			
		}
		return output;
	}
	
	private  byte[] removeECC(byte[] input) throws ReedSolomonException {
		if (input == null) {
			throw new IllegalArgumentException("The input to error correction code cannot be null");
		}
		if (input.length > maxBytes) {
			throw new IllegalArgumentException("The input to error correction code plus error correction bytes cannot be longer than 256");
		}
		int[] ints = new int[input.length];
		for (int i = 0; i < ints.length; i++) {
			ints[i] = input[i] & 0xFF;
		}
		ReedSolomonDecoder d = new ReedSolomonDecoder(gf);
		d.decode(ints, errorCorrectionBytes);
		byte[] result = new byte[input.length - errorCorrectionBytes];
		for (int i = 0; i < result.length; i++) {
			result[i] = (byte) ints[i];
		}
		return result;
	}
}
