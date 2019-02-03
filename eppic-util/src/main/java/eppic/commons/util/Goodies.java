package eppic.commons.util;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Collection of small useful helper methods.
 */
public class Goodies {
	
	public static final boolean ASCENDING = true;
	public static final boolean DESCENDING = false;
	
	public static final String MD5_ALGORITHM = "MD5";

	/**
	 * Compute the MD5 sum of a given input String using the java.security library.
	 * @param input
	 * @return
	 */
	public static final String computeMD5Sum(String input) {

		if (input == null) {
			throw new IllegalArgumentException("Input cannot be null!");
		}

		StringBuffer sbuf = new StringBuffer();
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance(MD5_ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			System.err.println("Unexpected error while computing md5 hash");
			System.err.println(e.getMessage());
			System.exit(1);
		}
		byte [] raw = md.digest(input.getBytes());

		for (int i = 0; i < raw.length; i++) {
			int c = (int) raw[i];
			if (c < 0) {
				c = (Math.abs(c) - 1) ^ 255;
			}
			String block = toHex(c >>> 4) + toHex(c & 15);
			sbuf.append(block);
		}

		return sbuf.toString();

	}

	private static final String toHex(int s) {
		if (s < 10) {
			return new StringBuffer().
			append((char)('0' + s)).
			toString();
		} else {
			return new StringBuffer().
			append((char)('A' + (s - 10))).
			toString();
		}
	}

	/**
	 * To serialize to file a given Serializable object
	 * @param serializedFile
	 * @param obj
	 * @throws IOException
	 */
	public static void serialize(File serializedFile, Object obj) throws IOException {
		FileOutputStream fileOut = new FileOutputStream(serializedFile);
		ObjectOutputStream out = new ObjectOutputStream(fileOut);
		out.writeObject(obj);
		out.close();
		fileOut.close();
	}

	/**
	 * To deserialize from file a Serializable object. The returned object must be cast to the appropriate class.
	 * @param serialized
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static Object readFromFile(File serialized) throws IOException, ClassNotFoundException {
		FileInputStream fileIn = new FileInputStream(serialized);
		ObjectInputStream in = new ObjectInputStream(fileIn);
		Object obj = in.readObject();
		in.close();
		fileIn.close();
		return obj;
	}
	
	/**
	 * Gunzips (decompresses gzip) given gzFile into outFile using java.util gzip implementation
	 * @param inFile
	 * @param outFile
	 * @throws IOException
	 */
	public static void gunzipFile(File inFile, File outFile) throws IOException {
		GZIPInputStream zis = new GZIPInputStream(new FileInputStream(inFile));
		FileOutputStream os = new FileOutputStream(outFile);
		int b;
		while ( (b=zis.read())!=-1) {
			os.write(b);
		}
		zis.close();
		os.close();
	}
	
	/**
	 * Gzips given inFile into outFile gzip file using java.util gzip implementation
	 * @param inFile
	 * @param outFile
	 * @throws IOException
	 */
	public static void gzipFile(File inFile, File outFile) throws IOException {
		GZIPOutputStream zos = new GZIPOutputStream(new FileOutputStream(outFile));
		FileInputStream is = new FileInputStream(inFile);

		int b;
		while ( (b=is.read())!=-1) {
			zos.write(b);
		}
		zos.close();
		is.close();
	}
}
