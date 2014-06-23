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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.Random;

/**
 * A class to encapsulate static convenience methods for interfacing with
 * file system calls.
 * 
 * @author scott.hasse@gmail.com
 */
public class FileUtils {

	public static void copyFile(String inputName, String outputName)
			throws IOException {
		File input = new File(inputName);
		File output = new File(outputName);

		Files.copy(input.toPath(), output.toPath(), new CopyOption[0]);
	}

	public static void deleteFile(String filename) {
		File f = new File(filename);
		f.delete();
	}

	/** Read the given binary file, and return its contents as a byte array. 
	 * @throws IOException */
	public static byte[] read(String aInputFileName) throws IOException {
		// log("Reading in binary file named : " + aInputFileName);
		File file = new File(aInputFileName);
		// log("File size: " + file.length());
		byte[] result = new byte[(int) file.length()];
			InputStream input = null;
			try {
				int totalBytesRead = 0;
				input = new BufferedInputStream(new FileInputStream(file));
				while (totalBytesRead < result.length) {
					int bytesRemaining = result.length - totalBytesRead;
					// input.read() returns -1, 0, or more :
					int bytesRead = input.read(result, totalBytesRead,
							bytesRemaining);
					if (bytesRead > 0) {
						totalBytesRead = totalBytesRead + bytesRead;
					}
				}
				/*
				 * the above style is a bit tricky: it places bytes into the
				 * 'result' array; 'result' is an output parameter; the while
				 * loop usually has a single iteration only.
				 */
				// log("Num bytes read: " + totalBytesRead);
			} finally {
				// log("Closing input stream.");
				input.close();
			}

		return result;
	}

	public static byte[] readFromFile(String filePath, long position, int size)
			throws IOException {
		RandomAccessFile file = new RandomAccessFile(filePath, "r");
		file.seek(position);
		byte[] bytes = new byte[size];
		file.read(bytes);
		file.close();
		return bytes;
	}

	/*
	 * Fill the used bytes in the key with random bytes.  This is not necessarily secure
	 * random data, but it is safer to have random data rather than all zeros as accidentally
	 * using an all-zero key would result in the plaintext being sent.
	 */
	public static void randomBytesInFile(String filePath, long position,
			int size) throws IOException {
		RandomAccessFile file = new RandomAccessFile(filePath, "rw");
		file.seek(position);
		Random r = new SecureRandom();
		byte[] bytes = new byte[size];
		r.nextBytes(bytes);
		file.write(bytes);
		file.close();
	}

	/**
	 * Write a byte array to the given file. Writing binary data is
	 * significantly simpler than reading it.
	 * @throws IOException 
	 */
	public static void write(byte[] aInput, String aOutputFileName) throws IOException {
		// log("Writing binary file...");
			OutputStream output = null;
			try {
				output = new BufferedOutputStream(new FileOutputStream(
						aOutputFileName));
				output.write(aInput);
			} finally {
				output.close();
			}

	}

}