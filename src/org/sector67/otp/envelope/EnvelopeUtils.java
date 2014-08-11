package org.sector67.otp.envelope;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

public class EnvelopeUtils {

	private static final String END_OTP_MESSAGE = "====== END OTP MESSAGE =====\n";
	private static final String BEGIN_OTP_MESSAGE = "====== BEGIN OTP MESSAGE =====\n";

	public static String getEnvelopeHeader() {
		return BEGIN_OTP_MESSAGE;
	}
	
	public static String formatHeader(String name, String value) {
		return name + ": " + value + "\n";
	}
	
	public static String getBodySeparator() {
		return "\n";
	}
	
	public static String getEnvelopeFooter() {
		return END_OTP_MESSAGE;
	}
	
	public static String parseHeader(String name, String message) {
		BufferedReader bufReader = new BufferedReader(new StringReader(message));
		String line="";
		try {
			while( (line=bufReader.readLine()) != null )
			{
				if (line.startsWith(name + ": ")) {
					return line.substring(name.length() + 2);
				}
				//could check for the end of the header and end early
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return null;
	}
	
	public static String getBody(String message) {
		BufferedReader bufReader = new BufferedReader(new StringReader(message));
		String line=null;
		//TODO: could implement more strict parsing
		String result = "";
		try {
			while( (line=bufReader.readLine()) != null )
			{
				if (line.startsWith("=====")) {
					//ignore
				} else if (line.contains(": ")) {
					//ignore
				} else if (line.equals("")) {
					//ignore
				} else {
					result = result + line;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return result;
	}
}
