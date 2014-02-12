package eppic.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import eppic.ChainEvolContextList;
import owl.core.structure.ChainInterfaceList;
import owl.core.util.Goodies;

public class Utils {

	/**
	 * Reads a list file containing pdb codes and interface ids one per line and tab/space separated.
	 * Following formats are allowed:
	 *  1bxy 1 2  -- specifies several interfaces for 1bxy
	 *  2bxy 1    -- specifies only 1 interface for 1bxy
	 *  3bxy      -- no interface specified, 1 is assumed
	 * @param list
	 * @return a map of pdb codes to list of interface ids 
	 * @throws IOException
	 */
	public static TreeMap<String,List<Integer>> readListFile(File list) throws IOException {
		TreeMap<String,List<Integer>> map = new TreeMap<String, List<Integer>>();
		BufferedReader br = new BufferedReader(new FileReader(list));
		String line;
		while ((line=br.readLine())!=null){
			if (line.startsWith("#")) continue;
			if (line.trim().isEmpty()) continue;
			String[] fields = line.trim().split("\\s+");
			String pdbId = fields[0].toLowerCase();
			
			List<Integer> interfaces = new ArrayList<Integer>();

			if (fields.length>1) {
				for (int i=1;i<fields.length;i++) {
					interfaces.add(Integer.parseInt(fields[i]));
				}
			} else {
				interfaces.add(1);
			}
			map.put(pdbId,interfaces);
			
		}
		br.close();
		return map;
	}
	
	public static ChainInterfaceList readChainInterfaceList(File file) throws IOException, ClassNotFoundException {
		return (ChainInterfaceList)Goodies.readFromFile(file);
	}
	
	public static ChainEvolContextList readChainEvolContextList(File file) throws IOException, ClassNotFoundException {
		return (ChainEvolContextList)Goodies.readFromFile(file);
	}
	
	/**
	 * Produces a file list prepending given directory to each combination of pdbCode+interface ids
	 * present in given list file (format is as in {@link #readListFile(File)})
	 * @param dir
	 * @param list
	 * @param suffix
	 * @throws IOException
	 */
	public static void produceFileList(File dir, File list, String suffix) throws IOException {
		TreeMap<String,List<Integer>> map = readListFile(list);
		for (String pdbCode:map.keySet()) {
			for (int id:map.get(pdbCode)) {
				System.out.println(new File(dir,pdbCode+"."+id+"."+suffix+".pdb"));
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		produceFileList(new File(args[0]),new File(args[1]),args[2]);
	}
}
