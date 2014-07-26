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

import java.security.SecureRandom;
import java.util.Random;

/**
 * 
 * @author scott.hasse@gmail.com
 *
 */
public class MultiPassSecureRandomEraser implements KeyEraser {
	
	private int passes = 3;
	Random r = new SecureRandom();

	public void erase(KeyData d, int offset, int length) throws KeyException {
		byte[] data = new byte[length];
		for(int i = 0; i < passes; i++) {
			r.nextBytes(data);
			d.seek(offset);
			d.write(data);
		}
	}	

}
