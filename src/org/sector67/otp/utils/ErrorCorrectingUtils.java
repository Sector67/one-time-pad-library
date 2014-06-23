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

import com.google.zxing.common.reedsolomon.GenericGF;
import com.google.zxing.common.reedsolomon.ReedSolomonDecoder;
import com.google.zxing.common.reedsolomon.ReedSolomonEncoder;
import com.google.zxing.common.reedsolomon.ReedSolomonException;

/**
 * A class to encapsulate static convenience methods for interfacing with
 * error correction code.
 * 
 * @author scott.hasse@gmail.com
 */

public class ErrorCorrectingUtils {
	private static int ERROR_CORRECTION_BYTES = 8;
	private static GenericGF DEFAULT_GF = GenericGF.QR_CODE_FIELD_256;
	private static int MAX_BYTES=256;
	
	public static byte[] encode(byte[] input) {
		if (input == null) {
			throw new IllegalArgumentException("The input to error correction code cannot be null");
		}
		if (input.length > (MAX_BYTES - ERROR_CORRECTION_BYTES)) {
			throw new IllegalArgumentException("The input to error correction code plus error correction bytes cannot be longer than 256");
		}
		//convert byte[] to int[] into an array large enough to hold the ECC
		int[] ints = new int[input.length + ERROR_CORRECTION_BYTES];
		for (int i = 0; i < input.length; i++) {
			ints[i] = input[i] & 0xFF;
		}
		ReedSolomonEncoder e = new ReedSolomonEncoder(DEFAULT_GF);
		e.encode(ints, ERROR_CORRECTION_BYTES);
		//convert int[] to byte[]
		byte[] output = new byte[ints.length];
		for (int i = 0; i < ints.length; i++) {
			output[i] = (byte) ints[i];			
		}
		return output;
	}
	
	public static byte[] decode(byte[] input) throws ReedSolomonException {
		if (input == null) {
			throw new IllegalArgumentException("The input to error correction code cannot be null");
		}
		if (input.length > MAX_BYTES) {
			throw new IllegalArgumentException("The input to error correction code plus error correction bytes cannot be longer than 256");
		}
		int[] ints = new int[input.length];
		for (int i = 0; i < ints.length; i++) {
			ints[i] = input[i] & 0xFF;
		}
		ReedSolomonDecoder d = new ReedSolomonDecoder(DEFAULT_GF);
		d.decode(ints, ERROR_CORRECTION_BYTES);
		byte[] result = new byte[input.length - ERROR_CORRECTION_BYTES];
		for (int i = 0; i < result.length; i++) {
			result[i] = (byte) ints[i];
		}
		return result;
	}
}
