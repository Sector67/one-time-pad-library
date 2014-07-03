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

import org.sector67.otp.EncryptionException;


public class EncodingException extends EncryptionException {

	/**
	 * 
	 * @author scott.hasse@gmail.com
	 */
	private static final long serialVersionUID = -4026645266468683511L;

	public EncodingException(String message) {
		super(message);
	}

	public EncodingException(Exception e) {
		super(e);
	}
}
