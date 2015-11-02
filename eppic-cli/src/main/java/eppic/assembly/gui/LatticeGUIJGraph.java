package eppic.assembly.gui;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JFrame;

import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.StructureTools;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.ext.JGraphXAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mxgraph.layout.mxFastOrganicLayout;
import com.mxgraph.model.mxICell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;

import eppic.assembly.ChainVertex3D;
import eppic.assembly.InterfaceEdge3D;
import eppic.assembly.LatticeGraph3D;

public class LatticeGUIJGraph {
	private static Logger logger = LoggerFactory.getLogger(LatticeGUIJGraph.class);

	private LatticeGraph3D graph;
	private JGraphXAdapter<ChainVertex3D, InterfaceEdge3D> view;

	public LatticeGUIJGraph(Structure struc) throws StructureException {
		this.graph = new LatticeGraph3D(struc);
		this.view = createView(this.graph.getGraph());
		organicLayout(this.view); //quick initial layout
	}

	/**
	 * Display graph in a JFrame
	 */
	public JFrame display() {
		mxGraphComponent graphComponent = new mxGraphComponent(view);
		graphComponent.setSize(700, 700);

		graphComponent.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		JFrame frame = new JFrame("JGraph");
		frame.getContentPane().add(graphComponent);
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		return frame;
	}

	/**
	 * @param graph
	 * @return
	 */
	private static JGraphXAdapter<ChainVertex3D, InterfaceEdge3D> createView(
			UndirectedGraph<ChainVertex3D, InterfaceEdge3D> graph) {
		JGraphXAdapter<ChainVertex3D, InterfaceEdge3D> jgraph = new JGraphXAdapter<ChainVertex3D,InterfaceEdge3D>(graph);


		//Colors
		for(ChainVertex3D vert: graph.vertexSet()) {
			mxICell cell = jgraph.getVertexToCellMap().get(vert);
			Color color = vert.getColor();
			if(color != null) {
				String hexColor = String.format("#%02x%02x%02x", color.getRed(),color.getGreen(),color.getBlue());
				jgraph.setCellStyles(mxConstants.STYLE_FILLCOLOR, hexColor, new Object[] {cell});
			}
			jgraph.setCellStyles(mxConstants.STYLE_FONTCOLOR, "#FFFFFF", new Object[] {cell});
		}
		for(InterfaceEdge3D edge: graph.edgeSet()) {
			mxICell cell = jgraph.getEdgeToCellMap().get(edge);
			Color color = edge.getColor();
			if(color != null) {
				String hexColor = String.format("#%02x%02x%02x", color.getRed(),color.getGreen(),color.getBlue());
				jgraph.setCellStyles(mxConstants.STYLE_STROKECOLOR, hexColor, new Object[] {cell});
				jgraph.setCellStyles(mxConstants.STYLE_FONTCOLOR, hexColor, new Object[] {cell});
			}
		}
		return jgraph;
	}
	
	private static void organicLayout(JGraphXAdapter<ChainVertex3D, InterfaceEdge3D> jgraph) {

		//Layout
		final mxFastOrganicLayout layout = new mxFastOrganicLayout(jgraph);
		//default 50
		//		layout.setForceConstant(100);
		//		layout.setInitialTemp(500);
		//		layout.setMaxIterations(1000);
		layout.setMaxDistanceLimit(300);
		System.out.format("Force=%f\tTemp=%f\tIter=%f\tlimits=%f-%f%n",layout.getForceConstant(),layout.getInitialTemp(),layout.getMaxIterations(),layout.getMinDistanceLimit(),layout.getMaxDistanceLimit());
		layout.execute(jgraph.getDefaultParent());
	}


	/**
	 * Get the underlying lattice graph datastructure
	 * @return
	 */
	public LatticeGraph3D getGraph() {
		return graph;
	}

	public static void main(String[] args) throws IOException, StructureException {

		if (args.length<1) {
			logger.error("No PDB code or file name given.");
			logger.error("Usage: LatticeGUI <PDB code or file> [comma separated list of interfaces to display]");
			System.exit(1);
		}

		int arg = 0;
		String input = args[arg++];
		List<Integer> interfaceIds = null;

		if (args.length>=2) {
			String interfaceIdsCommaSep = args[arg++];
			// '*' for all interfaces
			if(!interfaceIdsCommaSep.equals("*")) {
				String[] splitIds = interfaceIdsCommaSep.split("\\s*,\\s*");
				interfaceIds = new ArrayList<Integer>(splitIds.length);
				for(String idStr : splitIds) {
					try {
						interfaceIds.add(new Integer(idStr));
					} catch( NumberFormatException e) {
						logger.error("Invalid interface IDs. Expected comma-separated list, got {}",interfaceIdsCommaSep);
						System.exit(1);
					}
				}
			}
		}

		if (args.length>2) {
			logger.error("Expected at most 3 arguments.");
			logger.error("Usage: {} <PDB code or file> <output.js> [comma separated list of interfaces to display]",LatticeGUIJmol.class.getSimpleName());
			System.exit(1);
		}
		// Done parsing

		// Load input structure
		Structure struc = StructureTools.getStructure(input);

		LatticeGUIJGraph gui = new LatticeGUIJGraph(struc);
		if(interfaceIds != null) {
			gui.getGraph().filterEngagedInterfaces(interfaceIds);
		}
		gui.display();
	}

}
