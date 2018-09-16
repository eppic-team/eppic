package eppic.db.adaptors;

import eppic.model.dto.Assembly;
import eppic.model.dto.GraphEdge;
import eppic.model.dto.GraphNode;
import eppic.model.dto.views.*;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.util.*;

/**
 * Adaptor of dto classes to dto view classes.
 * Bridging REST core endpoints to view endpoints.
 * @author Jose Duarte
 * @since 3.1.0
 */
public class ViewsAdaptor {
	
	/**
	 * Same as in LatticeGraph3D
	 */
	public static final double defaultInterfaceRadius = 2.5;

    /**
     * Convert Assembly/GraphEdge dto objects into a LatticeGraph dto view object.
     * Serves as a bridge between assembly REST service and the view REST service latticeGraph.
     * @param graphEdges the edges to display
     * @param unitcellAssembly the special unit cell assembly containing the whole unit cell,
     *                         used to extract nodes information
     * @return the lattice graph view object
     */
    public static LatticeGraph getLatticeGraphView(List<GraphEdge> graphEdges, Assembly unitcellAssembly) {
        LatticeGraph latticeGraph = new LatticeGraph();

        // nodes
        List<LatticeGraphVertex> vertices = new ArrayList<>();
        Map<Integer, UnitCellTransform> uniqueTransforms = new TreeMap<>();
        for (GraphNode node : unitcellAssembly.getGraphNodes()) {
            LatticeGraphVertex vertex = new LatticeGraphVertex();
            vertex.setChainId(node.getLabel().split("_")[0]);
            vertex.setOpId(Integer.parseInt(node.getLabel().split("_")[1]));
            vertex.setCenter(new Point3D(node.getPos3dX(), node.getPos3dY(), node.getPos3dZ()));
            vertex.setColor(node.getColor());
            vertex.setLabel(node.getLabel());
            vertices.add(vertex);

            // the transforms are redundant (repeated for each chain), like this we get just one 1 per operator id
            int opId = Integer.parseInt(node.getLabel().split("_")[1]);
            if (!uniqueTransforms.containsKey(opId)) {

                UnitCellTransform unitCellTransform = new UnitCellTransform();
                unitCellTransform.setM00(node.getRxx());
                unitCellTransform.setM01(node.getRxy());
                unitCellTransform.setM02(node.getRxz());

                unitCellTransform.setM10(node.getRyx());
                unitCellTransform.setM11(node.getRyy());
                unitCellTransform.setM12(node.getRyz());

                unitCellTransform.setM20(node.getRzx());
                unitCellTransform.setM21(node.getRzy());
                unitCellTransform.setM22(node.getRzz());

                unitCellTransform.setM03(node.getTx());
                unitCellTransform.setM13(node.getTy());
                unitCellTransform.setM23(node.getTz());

                unitCellTransform.setM30(0);
                unitCellTransform.setM31(0);
                unitCellTransform.setM32(0);
                unitCellTransform.setM33(1);

                uniqueTransforms.put(opId, unitCellTransform);
            }

        }
        latticeGraph.setVertices(vertices);
        List<UnitCellTransform> unitCellTransforms = new ArrayList<>(uniqueTransforms.values());
        latticeGraph.setUnitCellTransforms(unitCellTransforms);

        List<LatticeGraphEdge> lgEdges =  new ArrayList<>();
        // edges
        for (GraphEdge edge : graphEdges) {
            LatticeGraphEdge lgEdge = new LatticeGraphEdge();
            lgEdge.setColor(edge.getColor());
            lgEdge.setXtalTrans("("+edge.getXtalTransA()+","+edge.getXtalTransB()+","+edge.getXtalTransC()+")");
            lgEdge.setInterfaceId(edge.getInterfaceId());
            lgEdge.setClusterId(edge.getInterfaceClusterId());

            List<Segment> segments = new ArrayList<>();
            Segment segment = new Segment();
            segment.setStart(new Point3D(edge.getStartPos3dX(), edge.getStartPos3dY(), edge.getStartPos3dZ()));
            segment.setEnd(new Point3D(edge.getEndPos3dX(), edge.getEndPos3dY(), edge.getEndPos3dZ()));
            // not setting angles and mid point, they aren't needed
            segments.add(segment);
            lgEdge.setSegments(segments);

            List<Circle> circles = new ArrayList<>();
            Circle circle = new Circle();
            // calculating midpoint
            Point3d start = new Point3d(edge.getStartPos3dX(), edge.getStartPos3dY(), edge.getStartPos3dZ());
            Point3d end = new Point3d(edge.getEndPos3dX(), edge.getEndPos3dY(), edge.getEndPos3dZ());
            Vector3d vec = new Vector3d(end);
            vec.sub(start);
            vec.scale(0.5);
            Point3d center = new Point3d(start);
            center.add(vec);
            circle.setCenter(new Point3D(center.x, center.y, center.z));
            // perpendicular is simply a point on the perpendicular to the circle, e.g the source of the edge
            circle.setPerpendicular(new Point3D(start.x, start.y, start.z));
            circle.setRadius(defaultInterfaceRadius);
            circles.add(circle);
            lgEdge.setCircles(circles);

            lgEdges.add(lgEdge);
        }
        latticeGraph.setEdges(lgEdges);

        return latticeGraph;
    }

    /**
     * Convert Assembly dto objects into a LatticeGraph dto view object.
     * Serves as a bridge between assembly REST service and the view REST service latticeGraph.
     * @param assembly the assembly dto object, used to extract edges information
     * @param unitcellAssembly the special unit cell assembly containing the whole unit cell,
     *                         used to extract nodes information
     * @return the lattice graph view object
     */
    public static LatticeGraph getLatticeGraphView(Assembly assembly, Assembly unitcellAssembly) {
        List<GraphEdge> graphEdges = assembly.getGraphEdges();
        return getLatticeGraphView(graphEdges, unitcellAssembly);
    }

    /**
     * Convert Assembly dto object into an AssemblyDiagram dto view object.
     * Serves as a bridge between assembly REST service and the view REST service assemblyDiagram.
     * @param assembly the assembly dto object
     * @return
     */
    public static AssemblyDiagram getAssemblyDiagram(Assembly assembly) {

        AssemblyDiagram assemblyDiagram = new AssemblyDiagram();
        List<AssemblyDiagramNode> nodes = new ArrayList<>();
        List<AssemblyDiagramEdge> edges = new ArrayList<>();
        assemblyDiagram.setNodes(nodes);
        assemblyDiagram.setEdges(edges);

        for (GraphNode graphNode : assembly.getGraphNodes()) {
            if (graphNode.isInGraph2d()) {
                AssemblyDiagramNode node = new AssemblyDiagramNode();
                node.setId(graphNode.getLabel());
                node.setLabel(graphNode.getLabel());
                node.setColor("#" + graphNode.getColor());
                node.setX(graphNode.getPos2dX());
                node.setY(graphNode.getPos2dY());

                nodes.add(node);
            }
        }

        for (GraphEdge graphEdge : assembly.getGraphEdges()) {
            if (graphEdge.isInGraph2d()) {
                AssemblyDiagramEdge edge = new AssemblyDiagramEdge();
                edge.setColor("#" + graphEdge.getColor());
                edge.setFrom(graphEdge.getNode1Label());
                edge.setTo(graphEdge.getNode2Label());
                edge.setLabel(graphEdge.getInterfaceId() + "(" + graphEdge.getInterfaceClusterId() + ")");
                edge.setTitle("(" + graphEdge.getXtalTransA() + "," + graphEdge.getXtalTransB() + "," + graphEdge.getXtalTransC() + ")");

                edges.add(edge);
            }
        }
        return assemblyDiagram;
    }
}
