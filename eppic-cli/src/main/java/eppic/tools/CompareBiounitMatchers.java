package eppic.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.cluster.SubunitClustererParameters;
import org.biojava.nbio.structure.quaternary.BioAssemblyInfo;
import org.biojava.nbio.structure.quaternary.BiologicalAssemblyBuilder;
import org.biojava.nbio.structure.quaternary.BiologicalAssemblyTransformation;
import org.biojava.nbio.structure.symmetry.core.QuatSymmetryDetector;
import org.biojava.nbio.structure.symmetry.core.QuatSymmetryParameters;
import org.biojava.nbio.structure.symmetry.core.QuatSymmetryResults;

import eppic.DataModelAdaptor;
import eppic.EppicException;
import eppic.EppicParams;
import eppic.Main;
import eppic.analysis.compare.BiounitMatchResult;
import eppic.assembly.Assembly;
import eppic.assembly.AssemblyDescription;
import eppic.assembly.ChainVertex;
import eppic.assembly.CrystalAssemblies;
import eppic.assembly.SubAssembly;
import eppic.cli.CommonCliParams;
import picocli.CommandLine;

/**
 * Benchmark tool: runs EPPIC (no evolutionary scoring) on each entry of a list and then matches every PDB biounit
 * with both the operator-based and the graph-based matcher, on the same in-memory EPPIC result.
 * <p>
 * Usage: CompareBiounitMatchers &lt;entries file: pdbId [tab stratum] per line&gt; &lt;output tsv&gt; &lt;work dir&gt; [nSpherePoints]
 * <p>
 * One output row per biounit per matcher.
 *
 * @author Jose Duarte
 */
public class CompareBiounitMatchers {

	private static final String[] HEADER = {
			"pdbId", "stratum", "biounit", "buChains", "buStoich", "buSymmetry", "matcher", "type", "assemblyId",
			"assemblyClusters", "assemblySize", "assemblyStoich", "assemblySymmetry", "sizeOk", "symmetryOk",
			"buClusters", "numNodes", "labelledEdges", "unlabelledEdges", "minorContacts", "removedEdges",
			"timeMs", "eppicExhaustive", "numValidAssemblies", "reason", "ncs", "xtal"
	};

	public static void main(String[] args) throws IOException {
		if (args.length < 3) {
			System.err.println("Usage: CompareBiounitMatchers <entries file> <output tsv> <work dir> [nSpherePoints]");
			System.exit(1);
		}
		File entriesFile = new File(args[0]);
		File outFile = new File(args[1]);
		File workDir = new File(args[2]);
		int nSpherePoints = args.length > 3 ? Integer.parseInt(args[3]) : 100;

		List<String[]> entries = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(entriesFile))) {
			String line;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.isEmpty() || line.startsWith("#")) continue;
				String[] tokens = line.split("\t");
				entries.add(new String[] { tokens[0].toLowerCase(), tokens.length > 1 ? tokens[1] : "" });
			}
		}

		boolean exists = outFile.exists() && outFile.length() > 0;
		try (PrintWriter pw = new PrintWriter(new FileWriter(outFile, true))) {
			if (!exists) pw.println(String.join("\t", HEADER));
			for (String[] entry : entries) {
				List<String[]> rows;
				try {
					rows = runEntry(entry[0], entry[1], workDir, nSpherePoints);
				} catch (Throwable e) {
					rows = new ArrayList<>();
					String[] row = emptyRow(entry[0], entry[1]);
					row[7] = "ERROR";
					row[24] = clean(e.getClass().getSimpleName() + ": " + e.getMessage());
					rows.add(row);
				}
				for (String[] row : rows) pw.println(String.join("\t", row));
				pw.flush();
				System.out.println("Done " + entry[0] + ": " + rows.size() + " rows");
			}
		}
	}

	private static List<String[]> runEntry(String pdbId, String stratum, File workDir, int nSpherePoints) throws IOException, EppicException {
		File outDir = new File(workDir, pdbId);
		outDir.mkdirs();

		CommonCliParams cliParams = new CommonCliParams();
		new CommandLine(cliParams).parseArgs("-i", pdbId, "-o", outDir.toString(), "-t", outDir.toString());
		EppicParams params = cliParams.toEppicParams();
		params.setInput(pdbId);
		params.setAlphabet(EppicParams.DEF_ENTROPY_ALPHABET);
		params.setnSpherePointsASAcalc(nSpherePoints);

		List<String[]> rows = new ArrayList<>();
		try {
			Main m = new Main();
			m.run(params, false);

			CrystalAssemblies ca = m.getCrystalAssemblies();
			Structure pdb = m.getStructure();
			DataModelAdaptor dm = m.getDataModelAdaptor();
			if (ca == null || pdb == null) {
				String[] row = emptyRow(pdbId, stratum);
				row[7] = "ERROR";
				row[24] = "EPPIC run did not produce assemblies";
				rows.add(row);
				return rows;
			}

			Map<Integer, BioAssemblyInfo> bioAssemblies = pdb.getPDBHeader().getBioAssemblies();
			if (bioAssemblies == null || bioAssemblies.isEmpty()) {
				String[] row = emptyRow(pdbId, stratum);
				row[7] = "NO_BIOUNITS";
				rows.add(row);
				return rows;
			}

			int numValid = ca.getUniqueAssemblies().size();

			for (Map.Entry<Integer, BioAssemblyInfo> e : bioAssemblies.entrySet()) {
				BioAssemblyInfo bu = e.getValue();

				TreeMap<Integer, Integer> buStoich = new TreeMap<>();
				for (BiologicalAssemblyTransformation t : bu.getTransforms()) {
					Chain c = pdb.getPolyChain(t.getChainId());
					if (c == null) continue;
					buStoich.merge(c.getEntityInfo().getMolId(), 1, Integer::sum);
				}
				int buChains = buStoich.values().stream().mapToInt(Integer::intValue).sum();
				String buSym = buChains <= 600 ? getBiojavaSymmetry(pdb, bu) : "NA";

				BiounitMatchResult ops = dm.matchBioUnitByOperators(bu, ca);
				BiounitMatchResult graph = dm.matchBioUnitByGraph(bu, ca);

				for (BiounitMatchResult r : new BiounitMatchResult[] { ops, graph }) {
					String[] row = emptyRow(pdbId, stratum);
					row[2] = String.valueOf(e.getKey());
					row[3] = String.valueOf(buChains);
					row[4] = stoichString(buStoich);
					row[5] = buSym;
					row[6] = r == ops ? "OPERATORS" : "GRAPH";
					row[7] = r.getType().toString();
					Assembly a = r.getAssembly();
					if (a != null) {
						row[8] = String.valueOf(a.getId());
						row[9] = a.toString();
						List<AssemblyDescription> desc = a.getDescription();
						StringBuilder sizes = new StringBuilder();
						StringBuilder syms = new StringBuilder();
						for (AssemblyDescription d : desc) {
							if (sizes.length() > 0) { sizes.append(','); syms.append(','); }
							sizes.append(d.getSize());
							syms.append(d.getSymmetry());
						}
						row[10] = sizes.toString();
						row[11] = assemblyStoichs(a);
						row[12] = syms.toString();
						row[13] = String.valueOf(isSizeOk(a, buStoich));
						row[14] = String.valueOf(syms.toString().equals(buSym));
					}
					row[15] = r.getClusterIds().toString().replace(" ", "");
					row[16] = String.valueOf(r.getNumNodes());
					row[17] = String.valueOf(r.getNumLabelledEdges());
					row[18] = String.valueOf(r.getNumUnlabelledEdges());
					row[19] = String.valueOf(r.getNumMinorContacts());
					row[20] = String.valueOf(r.getNumRemovedEdges());
					row[21] = String.valueOf(r.getTimeMs());
					row[22] = String.valueOf(ca.isExhaustiveEnumeration());
					row[23] = String.valueOf(numValid);
					row[24] = clean(r.getReason());
					row[25] = String.valueOf(dm.getPdbInfo().isNcsOpsPresent());
					row[26] = String.valueOf(pdb.isCrystallographic());
					rows.add(row);
				}
			}
		} finally {
			deleteRecursively(outDir);
		}
		return rows;
	}

	/**
	 * The matched assembly is consistent in size if the biounit's entity counts equal those of one of the assembly's
	 * distinct sub-assemblies.
	 */
	private static boolean isSizeOk(Assembly a, TreeMap<Integer, Integer> buStoich) {
		for (List<SubAssembly> group : a.getAssemblyGraph().getSubAssembliesGroupedByStoichiometries()) {
			if (entityCounts(group.get(0)).equals(buStoich)) return true;
		}
		return false;
	}

	private static String assemblyStoichs(Assembly a) {
		StringBuilder sb = new StringBuilder();
		for (List<SubAssembly> group : a.getAssemblyGraph().getSubAssembliesGroupedByStoichiometries()) {
			if (sb.length() > 0) sb.append('|');
			sb.append(stoichString(entityCounts(group.get(0))));
		}
		return sb.toString();
	}

	private static TreeMap<Integer, Integer> entityCounts(SubAssembly sub) {
		TreeMap<Integer, Integer> counts = new TreeMap<>();
		for (ChainVertex v : sub.getConnectedGraph().vertexSet()) {
			counts.merge(v.getEntityId(), 1, Integer::sum);
		}
		return counts;
	}

	private static String stoichString(Map<Integer, Integer> counts) {
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<Integer, Integer> e : counts.entrySet()) {
			if (sb.length() > 0) sb.append(',');
			sb.append(e.getKey()).append(':').append(e.getValue());
		}
		return sb.toString();
	}

	private static String getBiojavaSymmetry(Structure pdb, BioAssemblyInfo bu) {
		try {
			Structure bioAssembly = new BiologicalAssemblyBuilder().rebuildQuaternaryStructure(pdb, new ArrayList<>(bu.getTransforms()), true, false);
			QuatSymmetryParameters parameters = new QuatSymmetryParameters();
			parameters.setOnTheFly(true);
			QuatSymmetryResults res = QuatSymmetryDetector.calcGlobalSymmetry(bioAssembly, parameters, new SubunitClustererParameters());
			return res == null ? "NA" : res.getSymmetry();
		} catch (Exception e) {
			return "NA";
		}
	}

	private static String[] emptyRow(String pdbId, String stratum) {
		String[] row = new String[HEADER.length];
		java.util.Arrays.fill(row, "");
		row[0] = pdbId;
		row[1] = stratum;
		return row;
	}

	private static String clean(String s) {
		if (s == null) return "";
		return s.replaceAll("[\t\n\r]", " ");
	}

	private static void deleteRecursively(File dir) throws IOException {
		if (!dir.exists()) return;
		try (Stream<java.nio.file.Path> walk = Files.walk(dir.toPath())) {
			walk.sorted(Comparator.reverseOrder()).map(java.nio.file.Path::toFile).forEach(File::delete);
		}
	}
}
