/*
 * PNG code thanks to Rob Camick (https://tips4java.wordpress.com/2008/10/13/screen-image/).
 * Public domain.
 */
package eppic.assembly.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Point3d;

import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.align.util.AtomCache;
import org.biojava.nbio.structure.symmetry.axis.AxisAligner;
import org.biojava.nbio.structure.symmetry.core.QuatSymmetryResults;
import org.biojava.nbio.structure.symmetry.core.Rotation;
import org.biojava.nbio.structure.symmetry.core.RotationGroup;
import org.jgrapht.Graph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
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
import eppic.assembly.layout.QuaternaryOrientationLayout;
import eppic.assembly.layout.VertexPositioner;
import eppic.assembly.layout.mxgraph.mxConnectedComponentLayout;
import eppic.assembly.layout.mxgraph.mxStereographicLayout;

/**
 * 
 * @author Spencer Bliven
 *
 */
public class LatticeGUIJGraph {
	private static Logger logger = LoggerFactory.getLogger(LatticeGUIJGraph.class);

	private Graph<ChainVertex3D, InterfaceEdge3D> graph;
	private JGraphXAdapter<ChainVertex3D, InterfaceEdge3D> view;
	private boolean layedOut;
	
	private String name = "JGraph";

	public LatticeGUIJGraph(Structure struc) throws StructureException {
		this(new LatticeGraph3D(struc).getGraph());
	}
	public LatticeGUIJGraph(Graph<ChainVertex3D, InterfaceEdge3D> graph) {
		this.graph = graph;
		this.view = null;
		this.layedOut = false;

	}

	/**
	 * Display graph in a JFrame
	 */
	public JFrame display() {
		if(!layedOut) {
			organicLayout();
		}
		mxGraphComponent graphComponent = new mxGraphComponent(getView());
		graphComponent.setSize(700, 700);

		graphComponent.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JFrame frame = new JFrame(name);
		frame.getContentPane().add(graphComponent);
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		return frame;
	}

	public void writePNG(File out) throws IOException {
		if(!layedOut) {
			organicLayout();
		}
		mxGraphComponent graphComponent = new mxGraphComponent(getView());
		
		JPanel control = new JPanel();
		control.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		control.add(graphComponent.getGraphControl());
		
		BufferedImage image = createImage(control);
		ImageIO.write(image, "PNG", out);
	}
	

	/*
	 *  Create a BufferedImage for Swing components.
	 *  The entire component will be captured to an image.
	 *
	 *  @param  component Swing component to create image from
	 *  @return	image the image for the given region
	*/
	public static BufferedImage createImage(JComponent component)
	{
		Dimension d = component.getSize();

		if (d.width == 0 || d.height == 0)
		{
			d = component.getPreferredSize();
			component.setSize( d );
		}

		Rectangle region = new Rectangle(0, 0, d.width, d.height);
		return createImage(component, region);
	}

	/*
	 *  Create a BufferedImage for Swing components.
	 *  All or part of the component can be captured to an image.
	 *
	 *  @param  component Swing component to create image from
	 *  @param  region The region of the component to be captured to an image
	 *  @return	image the image for the given region
	*/
	public static BufferedImage createImage(JComponent component, Rectangle region)
	{
        //  Make sure the component has a size and has been layed out.
        //  (necessary check for components not added to a realized frame)

		if (! component.isDisplayable())
		{
			Dimension d = component.getSize();

			if (d.width == 0 || d.height == 0)
			{
				d = component.getPreferredSize();
				component.setSize( d );
			}

			layoutComponent( component );
		}

		BufferedImage image = new BufferedImage(region.width, region.height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = image.createGraphics();

		//  Paint a background for non-opaque components,
		//  otherwise the background will be black

		if (! component.isOpaque())
		{
			g2d.setColor( component.getBackground() );
			g2d.fillRect(region.x, region.y, region.width, region.height);
		}

		g2d.translate(-region.x, -region.y);
		component.paint( g2d );
		g2d.dispose();
		return image;
	}
	static void layoutComponent(Component component)
	{
		synchronized (component.getTreeLock())
		{
			component.doLayout();

			if (component instanceof Container)
			{
				for (Component child : ((Container)component).getComponents())
				{
					layoutComponent(child);
				}
			}
		}
	}

	/**
	 * Called to indicate that the underlying graph has changed.
	 * After calling this, any layout and display methods should be re-called.
	 */
	public void update() {
		view = null;
	}

	private JGraphXAdapter<ChainVertex3D, InterfaceEdge3D> getView() {
		if(view == null)
			view = createView(this.graph);
		return view;
	}
	/**
	 * @param graph
	 * @return
	 */
	private static JGraphXAdapter<ChainVertex3D, InterfaceEdge3D> createView(
			Graph<ChainVertex3D, InterfaceEdge3D> graph) {
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
			jgraph.setCellStyles(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_CURVE, new Object[] {cell});
		}
		return jgraph;
	}

	public void organicLayout() {
		organicLayout(getView());
		layedOut = true;
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
	
	public void stereographicLayout(Point3d center, Point3d zenith) {
		stereographicLayout(getView(),center,zenith,ChainVertex3D.getVertexPositioner());
		layedOut = true;
	}
	public void stereographicLayout(Point3d center, Point3d zenith,VertexPositioner<ChainVertex3D> positioner) {
		stereographicLayout(getView(),center,zenith,positioner);
		layedOut = true;
	}
	private static void stereographicLayout(JGraphXAdapter<ChainVertex3D, InterfaceEdge3D> jgraph,
			Point3d center, Point3d zenith, VertexPositioner<ChainVertex3D> positioner) {
		final mxStereographicLayout<ChainVertex3D, InterfaceEdge3D> layout =
				new mxStereographicLayout<ChainVertex3D, InterfaceEdge3D>(
						jgraph,positioner,center, zenith);
		layout.execute(jgraph.getDefaultParent());
		final mxConnectedComponentLayout<ChainVertex3D, InterfaceEdge3D> layout2 = 
				new mxConnectedComponentLayout<ChainVertex3D, InterfaceEdge3D>(jgraph);
		layout2.execute(jgraph.getDefaultParent());
	}


	/**
	 * Get the underlying lattice graph datastructure
	 * @return 
	 * @return
	 */
	public Graph<ChainVertex3D, InterfaceEdge3D> getGraph() {
		return graph;
	}

	public static void main(String[] args) throws IOException, StructureException {

		if (args.length<1) {
			logger.error("No PDB code or file name given.");
			logger.error("Usage: LatticeGUI <PDB code or file> [comma separated list of interfaces to display] [pngfile]");
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
		File pngFile = null;
		if( args.length >= 3) {
			String pngFilename = args[arg++];
			pngFile = new File(pngFilename);
		}

		if (args.length>3) {
			logger.error("Expected at most 3 arguments.");
			logger.error("Usage: {} <PDB code or file> [comma separated list of interfaces to display] [pngfile]",LatticeGUIJmol.class.getSimpleName());
			System.exit(1);
		}
		// Done parsing

		// Load input structure
		AtomCache cache = new AtomCache();
		cache.getFileParsingParams().setAlignSeqRes(true);
		cache.setUseMmCif(true);
		Structure struc = cache.getStructure(input);

		LatticeGraph3D latticeGraph = new LatticeGraph3D(struc);
		
		if(interfaceIds != null) {
			latticeGraph.filterEngagedInterfaces(interfaceIds);
		}
		ConnectivityInspector<ChainVertex3D, InterfaceEdge3D> connectivity = new ConnectivityInspector<ChainVertex3D, InterfaceEdge3D>(latticeGraph.getGraph());
		for( Set<ChainVertex3D> connected : connectivity.connectedSets()) {
			if(!connected.iterator().next().toString().equals("D3")) { //A8
				//continue;
			}
			// Focus on one complex
			UndirectedGraph<ChainVertex3D, InterfaceEdge3D> subgraph = QuaternaryOrientationLayout.getVertexSubgraph(latticeGraph.getGraph(), connected);
			// Orient
			QuatSymmetryResults gSymmetry = QuaternaryOrientationLayout.getQuatSymm(subgraph,ChainVertex3D.getVertexPositioner());
			RotationGroup pointgroup = gSymmetry.getRotationGroup();
			AxisAligner aligner = AxisAligner.getInstance(gSymmetry);
			Point3d center = aligner.getGeometricCenter();
			
			Rotation rotation = pointgroup.getRotation(pointgroup.getHigherOrderRotationAxis());
			AxisAngle4d axis = rotation.getAxisAngle();
			Point3d zenith = new Point3d(axis.x,axis.y,axis.z);
			zenith.add(center);
			
			logger.info("Connected Component containing {} has center {} and zenith {} angle {}",
					connected.iterator().next(), center, zenith, axis.angle);
			
			LatticeGUIJGraph gui = new LatticeGUIJGraph(subgraph);
			gui.setName("Connected Component around "+connected.iterator().next());
			gui.stereographicLayout(center, zenith);
			gui.display();

			//break;
		}
		LatticeGUIJGraph gui = new LatticeGUIJGraph(latticeGraph.getGraph());

//		Point3d center = new Point3d(.5,.5,.5);
//		struc.getCrystallographicInfo().getCrystalCell().transfToOrthonormal(center);
//		Point3d zenith = new Point3d(.5,.5,1);
//		struc.getCrystallographicInfo().getCrystalCell().transfToOrthonormal(zenith);
//		gui.stereographicLayout(center,zenith);

		gui.display();
		if(pngFile != null) {
			gui.writePNG(pngFile);
		}
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

}
