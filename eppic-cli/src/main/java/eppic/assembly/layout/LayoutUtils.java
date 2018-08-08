package eppic.assembly.layout;

import eppic.assembly.ChainVertex3D;
import eppic.assembly.InterfaceEdge3D;
import eppic.assembly.LatticeGraph;
import eppic.assembly.gui.InterfaceEdge3DSourced;
import org.biojava.nbio.structure.xtal.CrystalCell;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.Pseudograph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A bunch of utilities for laying out assembly graphs.
 * @since 3.1.0
 */
public class LayoutUtils {

    /**
     * Gets the default 2D graph layout.
     * This used to be in LatticeGuiMustache, moved here 2018-05-31 by JD
     * @param cell the crystal cell
     * @return
     */
    public static GraphLayout<ChainVertex3D, InterfaceEdge3D> getDefaultLayout2D(CrystalCell cell) {
        VertexPositioner<ChainVertex3D> vertexPositioner = ChainVertex3D.getVertexPositioner();
        List<GraphLayout<ChainVertex3D,InterfaceEdge3D>> layouts = new ArrayList<>();

        layouts.add( new UnitCellLayout<>(vertexPositioner, cell));
        QuaternaryOrientationLayout<ChainVertex3D,InterfaceEdge3D> stereo = new QuaternaryOrientationLayout<>(vertexPositioner);

//		Point3d center = new Point3d();
//		Point3d zenith = new Point3d(0,0,1);
//		StereographicLayout<ChainVertex3D,InterfaceEdge3D> stereo = new StereographicLayout<>(vertexPositioner , center , zenith));
        layouts.add(stereo);

        ConnectedComponentLayout<ChainVertex3D, InterfaceEdge3D> packer = new ConnectedComponentLayout<>(vertexPositioner);
        packer.setPadding(100);
        layouts.add(packer);
        return new ComboLayout<>(layouts);
    }

    /**
     * Get a version of the graph where all 3D coordinates have z=0.
     * This is achieved by performing a stereographic projection of the 3D
     * coordinates.
     * This is a copy of a similar method in LatticeGuiMustache, moved here 2018-05-31 by JD
     * @param graph3d
     * @param layout2d
     * @return
     */
    public static UndirectedGraph<ChainVertex3D, InterfaceEdge3DSourced<ChainVertex3D>> getGraph2D(UndirectedGraph<ChainVertex3D, InterfaceEdge3D> graph3d,
                                                                                                   GraphLayout<ChainVertex3D,InterfaceEdge3D> layout2d) {

        if (layout2d == null) {
            throw new IllegalArgumentException("No 2D layout set for calculating the 2D graph.");
        }

        UndirectedGraph<ChainVertex3D, InterfaceEdge3DSourced<ChainVertex3D>> graph2d;

        //clone
        UndirectedGraph<ChainVertex3D, InterfaceEdge3D> graph2dUnsorced = cloneGraph3D(graph3d);
        //Filter duplicate components
        graph2dUnsorced = LatticeGraph.filterUniqueStoichiometries(graph2dUnsorced);
        //Layout

        layout2d.projectLatticeGraph(graph2dUnsorced);

        graph2d = InterfaceEdge3DSourced.addSources(graph2dUnsorced);

        return graph2d;
    }

    public static UndirectedGraph<ChainVertex3D, InterfaceEdge3D> cloneGraph3D(
            UndirectedGraph<ChainVertex3D, InterfaceEdge3D> oldGraph) {
        // Mappings from old graph to new
        Map<ChainVertex3D,ChainVertex3D> newVertices = new HashMap<>(oldGraph.vertexSet().size());
        Map<InterfaceEdge3D,InterfaceEdge3D> newEdges = new HashMap<>(oldGraph.edgeSet().size());
        for(ChainVertex3D vert : oldGraph.vertexSet()) {
            ChainVertex3D newVert = new ChainVertex3D(vert);
            newVertices.put(vert,newVert);
        }
        for(InterfaceEdge3D edge :oldGraph.edgeSet()) {
            InterfaceEdge3D newEdge = new InterfaceEdge3D(edge);
            newEdges.put(edge, newEdge);
        }
        // convert old graph to new one
        UndirectedGraph<ChainVertex3D,InterfaceEdge3D> newGraph = new Pseudograph<>(InterfaceEdge3D.class);
        for(ChainVertex3D vert : newVertices.values()) {
            newGraph.addVertex(vert);
        }
        for(InterfaceEdge3D edge : oldGraph.edgeSet()) {
            ChainVertex3D source = newVertices.get( oldGraph.getEdgeSource(edge) );
            ChainVertex3D target = newVertices.get( oldGraph.getEdgeTarget(edge) );
            InterfaceEdge3D newEdge = newEdges.get( edge );
            newGraph.addEdge(source, target, newEdge);
        }
        return newGraph;
    }
}
