package eppic;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MolViewersHelper {

	// PROPERTY FILES
	public static final InputStream COLORS_PROPERTIES_IS = MolViewersHelper.class.getResourceAsStream("/eppic/chain_colors.dat");
	public static final InputStream PYMOL_COLOR_MAPPINGS_IS = MolViewersHelper.class.getResourceAsStream("/eppic/pymol.colors");

	private static final Logger LOGGER = LoggerFactory.getLogger(MolViewersHelper.class);

	

	/**
	 * We use 26 colors corresponding to chain letters A to Z (second 13 are repeated from first 13)
	 */
	private static final String[] DEF_CHAIN_COLORS = 
		{"green","cyan","yellow","white","lightblue","magenta","red","orange","wheat","limon","salmon","palegreen","lightorange",
		"green","cyan","yellow","white","lightblue","magenta","red","orange","wheat","limon","salmon","palegreen","lightorange",};

	//private static final String DEF_SYM_RELATED_CHAIN_COLOR = "grey";

	private static final String DEF_INTERF_COLOR = "red";

	
	
	
	
	private static String[] chainColors;
	//private static String symRelatedColor;
	private static String interf1color;
	private static String interf2color;

	private static HashMap<String, String> colorMappings;

	static {
		try {
			readColorMappingsFromResourceFile(PYMOL_COLOR_MAPPINGS_IS);
			readColorsFromPropertiesFile(COLORS_PROPERTIES_IS);
		} catch (IOException e) {
			LOGGER.warn("Couldn't read color mappings and properties from resource files. Will use defaults.");
			chainColors = DEF_CHAIN_COLORS;
			//symRelatedColor = DEF_SYM_RELATED_CHAIN_COLOR;
			interf1color = DEF_INTERF_COLOR;
			interf2color = DEF_INTERF_COLOR;
			colorMappings = new HashMap<String, String>();
		}
	}

	/**
	 * Reads from properties file the chain colors: 26 colors, one per alphabet letter
	 * and a color for the sym related chain
	 * @param is
	 * @throws IOException
	 */
	private static void readColorsFromPropertiesFile(InputStream is) throws IOException {

		Properties p = new Properties();
		p.load(is);

		chainColors = new String[26];
		char letter = 'A';
		for (int i=0;i<26;i++) {
			chainColors[i] = p.getProperty(Character.toString(letter)); 
			letter++;
		}
		//symRelatedColor = p.getProperty("SYMCHAIN");
		interf1color = p.getProperty("INTERF1");
		interf2color = p.getProperty("INTERF2");
	}	

	/**
	 * Reads from resource file the color mappings of pymol color names to hex RGB color codes
	 * @param is
	 * @throws IOException
	 */
	private static void readColorMappingsFromResourceFile(InputStream is) throws IOException {
		colorMappings = new HashMap<String, String>();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line;
		while ((line=br.readLine())!=null) {
			if (line.isEmpty()) continue;
			if (line.startsWith("#")) continue;
			String[] tokens = line.split(" ");
			colorMappings.put(tokens[0], tokens[1]);
		}
		br.close();
	}

	/**
	 * Given a pymol color name (e.g. "raspberry") returns the hex RGB color code, e.g. #b24c66
	 * @param pymolColor
	 * @return the hex color or #00ffff (cyan) if no such color name exists
	 */
	private static String getHexColorCode(String pymolColor) {
		String col = colorMappings.get(pymolColor);
		if (col==null) return "#00ffff";
		return col;
	}

	/**
	 * Given a pymol color name (e.g. "raspberry") returns the hex RGB color code, e.g. 0xb24c66
	 * @param pymolColor
	 * @return the hex color or 0x00ffff (cyan) if no such color name exists
	 */
	private static String getHexColorCode0x(String pymolColor) {
		String col = colorMappings.get(pymolColor);
		if (col==null) return "0x00ffff";
		col = col.replace("#", "0x");
		return col;
	}

	public static String getHexInterf1Color() {
		return getHexColorCode0x(interf1color);
	}

	public static String getHexInterf2Color() {
		return getHexColorCode0x(interf2color);
	}
	
	public static String getChainColor(int index) {
		return chainColors[index%chainColors.length];
	}
	
	/**
	 * Returns the next letter in alphabet with matching case, or if input is 'z' or 'Z'
	 * returns 'a' or 'A'
	 * @param letter
	 * @return
	 */
	public static char getNextLetter(char letter) {

		char newLetter = letter;
		// if both chains are named equally we want to still named them differently in the output pdb file
		// so that molecular viewers can handle properly the 2 chains as separate entities 

		if (letter!='Z' && letter!='z') {
			newLetter = (char)(letter+1); // i.e. next letter in alphabet
		} else {
			newLetter = (char)(letter-25); //i.e. 'A' or 'a'
		}

		return newLetter;
	}
	
	/**
	 * Get the hex color (0x or #-prefixed) understood by PyMOL or 3Dmol.js given a chainId
	 * of the form &lt;chainIdString&gt;_&lt;opId&gt;, with chainIdString a String of 1 or 
	 * more characters and opId an integer representing the operator identifier within the space group.
	 * The first letter of chainIdString will be used to get one of a standard set of colors used by PyMOL
	 * to color chains, if opId>0 then the color is darkened. 
	 * @param chainId
	 * @param hashPrefixed if true the color is prefixed with a '#', if false with a '0x'
	 * @return
	 */
	public static String getHexChainColor(String chainId, boolean hashPrefixed) {
		
		
		String chain = chainId;
		int opId = -1;
		
		if (chainId.contains("_")) {
			String[] tokens = chainId.split("_");
			if (tokens.length!=2) throw new IllegalArgumentException("Malformed chain identifier: "+chainId);
			chain = tokens[0];
			try {
				opId = Integer.parseInt(tokens[1]);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Malformed chain identifier: "+chainId);
			}
		}
		
		int index = getIndex(chain);
		
		String pymolColString = chainColors[index%chainColors.length];
		String hex = getHexColorCode(pymolColString);

		Color color = Color.decode(hex);
		
		// in all other cases we keep the original: either when no opId given (opId==-1) or when opId==0
		if (opId>0) {
			color = darken(color);
		}  

		if (hashPrefixed) {
			return toHexHash(color);
		} else {
			return toHex0x(color);	
		}
		
		
	}
	
	private static int getIndex(String chain) {
		char c = chain.charAt(0);
		if (Character.isUpperCase(c)) {
			return c - 65; // 65 is the ascii decimal for A
		} 
		
		if (Character.isLowerCase(c)) {
			return c - 97; // 97 is the ascii decimal for a
		}
		
		if (Character.isDigit(c)) {
			return c - 48; // 48 is the ascii decimal for 0
		}
		
		return 0;
		
	}
	
	private static String toHex0x(Color col) {
		return "0x"+Integer.toHexString(col.getRGB() & 0xffffff);
	}
	
	private static String toHexHash(Color col) {
		return "#"+Integer.toHexString(col.getRGB() & 0xffffff);
	}

	private static Color darken(Color col) {
		float[] hsv = Color.RGBtoHSB(col.getRed(), col.getGreen(), col.getBlue(), null);
		hsv[2] = hsv[2] * 0.4f;
		int newCol = Color.HSBtoRGB(hsv[0], hsv[1], hsv[2]);
		return new Color(newCol);
	}

}
