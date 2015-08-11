/*

(C) Copyright 2015 Alberto Fernández <infjaf@gmail.com>
(C) Copyright 2014 Jan Schlößin
(C) Copyright 2003-2004 Anil Kumar K <anil@linuxense.com>

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 3.0 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library.  If not, see <http://www.gnu.org/licenses/>.

*/

package com.linuxense.javadbf;

import java.io.DataInput;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Locale;
/**
	Miscelaneous functions required by the JavaDBF package.
*/
public final class Utils {

	public static final int ALIGN_LEFT = 10;
	public static final int ALIGN_RIGHT = 12;
	private static CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder(); 


	private Utils() {
		throw new AssertionError("No instances allowed");
	}

	public static int readLittleEndianInt(DataInput in) throws IOException {
		int bigEndian = 0;
		for (int shiftBy = 0; shiftBy < 32; shiftBy += 8) {
			bigEndian |= (in.readUnsignedByte() & 0xff) << shiftBy;
		}
		return bigEndian;
	}

	public static short readLittleEndianShort(DataInput in) throws IOException {
		int low = in.readUnsignedByte() & 0xff;
		int high = in.readUnsignedByte();
		return (short) (high << 8 | low);
	}

	/**
	 * Remove all spaces (32) found in the data.
	 * @param array the data
	 * @return the data cleared of whitespaces
	 */
	public static byte[] removeSpaces(byte[] array) {
		StringBuilder t_sb = new StringBuilder(array.length);
		for (byte b: array) {
			if (b != ' '){
				t_sb.append( (char) b);
			}
		}
		return t_sb.toString().getBytes();
	}
	

	public static short littleEndian(short value) {

		short num1 = value;
		short mask = (short) 0xff;

		short num2 = (short) (num1 & mask);
		num2 <<= 8;
		mask <<= 8;

		num2 |= (num1 & mask) >> 8;

		return num2;
	}

	public static int littleEndian(int value) {

		int num1 = value;
		int mask = 0xff;
		int num2 = 0x00;

		num2 |= num1 & mask;

		for (int i = 1; i < 4; i++) {
			num2 <<= 8;
			mask <<= 8;
			num2 |= (num1 & mask) >> (8 * i);
		}

		return num2;
	}

	public static byte[] textPadding(String text, String characterSetName, int length)
			throws UnsupportedEncodingException {
		return textPadding(text, characterSetName, length, Utils.ALIGN_LEFT);
	}

	public static byte[] textPadding(String text, String characterSetName, int length, int alignment)
			throws UnsupportedEncodingException {

		return textPadding(text, characterSetName, length, alignment, (byte) ' ');
	}

	public static byte[] textPadding(String text, String characterSetName, int length, int alignment, byte paddingByte)
			throws UnsupportedEncodingException {

		if (text.length() >= length) {
			return text.substring(0, length).getBytes(characterSetName);
		}

		byte byte_array[] = new byte[length];
		Arrays.fill(byte_array, paddingByte);

		switch (alignment) {
		case ALIGN_RIGHT:
			int t_offset = length - text.length();
			System.arraycopy(text.getBytes(characterSetName), 0, byte_array, t_offset, text.length());
			break;
		case ALIGN_LEFT:
		default:
			System.arraycopy(text.getBytes(characterSetName), 0, byte_array, 0, text.length());
			break;
			
		}

		return byte_array;
	}

	public static byte[] doubleFormating(Number num, String characterSetName, int fieldLength, int sizeDecimalPart)
			throws java.io.UnsupportedEncodingException {
		return doubleFormating(num.doubleValue(), characterSetName, fieldLength, sizeDecimalPart);
	}
	
	public static byte[] doubleFormating(Double doubleNum, String characterSetName, int fieldLength, int sizeDecimalPart)
			throws java.io.UnsupportedEncodingException {
		int sizeWholePart = fieldLength - (sizeDecimalPart > 0 ? (sizeDecimalPart + 1) : 0);

		StringBuilder format = new StringBuilder(fieldLength);
		for (int i = 0; i < sizeWholePart; i++) {
			format.append("#");
		}
		if (sizeDecimalPart > 0) {
			format.append(".");
			for (int i = 0; i < sizeDecimalPart; i++) {
				format.append("0");
			}
		}

		DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(Locale.ENGLISH);
		df.applyPattern(format.toString());
		return textPadding(df.format(doubleNum.doubleValue()).toString(), characterSetName, fieldLength, ALIGN_RIGHT);
	}

	/**
	 * Checcks that a byte array contains some byte
	 * @param array The array to search in
	 * @param value The byte to search for
	 * @return
	 */
	public static boolean contains(byte[] array, byte value) {
		if (array != null) {
			for (byte data: array) {
				if (data == value) {
					return true;
				}
			}
		}
		return false;		
	}

	/**
	 * Checks if a string is pure Ascii
	 * @param stringToCheck the string
	 * @return true if is ascci
	 */
	public static boolean isPureAscii(String stringToCheck) {
		if (stringToCheck == null) {
			return true;
		}
		return asciiEncoder.canEncode(stringToCheck);
	}

	/**
	 * Convert LOGICAL (L) byte to boolean value
	 * @param t_logical The byte value as stored in the file
	 * @return The boolean value
	 */
	public static Boolean toBoolean(byte t_logical) {
		if (t_logical == 'Y' || t_logical == 'y' || t_logical == 'T' || t_logical == 't') {
			return Boolean.TRUE;
		} else if (t_logical == 'N' || t_logical == 'n' || t_logical == 'F' || t_logical == 'f'){
			return Boolean.FALSE;
		}
		return null;
	}
	
	/**
	 * 
	 * @param arr
	 * @return
	 * @deprecated this functions really trim all spaces, instead only left spaces
	 */
	@Deprecated
	public static byte[] trimLeftSpaces(byte[] arr) {
		return removeSpaces(arr);
	}

}