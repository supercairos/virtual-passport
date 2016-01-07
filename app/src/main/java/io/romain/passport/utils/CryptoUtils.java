package io.romain.passport.utils;


import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@SuppressWarnings("unused")
public class CryptoUtils {

	public static String sha1(String toHash) {
		String hash = null;
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			byte[] bytes = toHash.getBytes("UTF-8");
			digest.update(bytes, 0, bytes.length);
			bytes = digest.digest();

			// This is ~55x faster than looping and String.formating()
			hash = bytesToHex(bytes);
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			Dog.e(e, "Ex");
		}
		return hash;
	}

	private static String bytesToHex(byte[] data) {
		StringBuilder buf = new StringBuilder();
		for (byte b : data) {
			int halfbyte = (b >>> 4) & 0x0F;
			int two_halfs = 0;
			do {
				buf.append((0 <= halfbyte) && (halfbyte <= 9) ? (char) ('0' + halfbyte) : (char) ('a' + (halfbyte - 10)));
				halfbyte = b & 0x0F;
			} while (two_halfs++ < 1);
		}
		return buf.toString();
	}
}
