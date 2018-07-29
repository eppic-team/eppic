package eppic.db.adaptors;

import eppic.assembly.LatticeGraph3D;
import eppic.model.dto.Assembly;
import eppic.model.dto.GraphEdge;
import eppic.model.dto.GraphNode;
import eppic.model.dto.views.*;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adaptor of dto classes to dto view classes.
 * Bridging REST core endpoints to view endpoints.
 * @author Jose Duarte
 * @since 3.1.0
 */
public class ViewsAdaptor {

    /**
     * Convert an Assembly dto object into a LatticeGraph dto view object.
     * Serves as a bridge between assembly REST service and the view REST service latticeGraph.
     * @param assembly the assembly dto object, used to extract edges information
     * @param unitcellAssembly the special unit cell assembly containing the whole unit cell,
     *                         used to extract nodes information 
     * @return the lattice graph view object
     */
    public static LatticeGraph getLatticeGraphView(Assembly assembly, Assembly unitcellAssembly) {
        LatticeGraph latticeGraph = new LatticeGraph();
        Map<String, GraphNode> nodeLookup = new HashMap<>();

        // nodes
        List<LatticeGraphVertex> vertices = new ArrayList<>();
        List<UnitCellTransform> unitCellTransforms = new ArrayList<>();
        for (GraphNode node : unitcellAssembly.getGraphNodes()) {
            nodeLookup.put(node.getLabel(), node);
            LatticeGraphVertex vertex = new LatticeGraphVertex();
            vertex.setChainId(node.getLabel().split("_")[0]);
            vertex.setOpId(Integer.parseInt(node.getLabel().split("_")[1]));
            vertex.setCenter(new Point3D(node.getPos3dX(), node.getPos3dY(), node.getPos3dZ()));
            vertex.setColor(node.getColor());
            vertex.setLabel(node.getLabel());
            vertices.add(vertex);
            if (node.getLabel().split("_")[1].equals("0")) {
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

                unitCellTransforms.add(unitCellTransform);
            }

        }
        latticeGraph.setVertices(vertices);

        List<LatticeGraphEdge> lgEdges =  new ArrayList<>();
        // edges
        for (GraphEdge edge : assembly.getGraphEdges()) {
            LatticeGraphEdge lgEdge = new LatticeGraphEdge();
            lgEdge.setColor(edge.getColor());
            lgEdge.setXtalTrans("("+edge.getXtalTransA()+","+edge.getXtalTransB()+","+edge.getXtalTransC()+")");
            lgEdge.setInterfaceId(edge.getLabel());
            // we don't have cluster id readily available yet
            lgEdge.setClusterId(-1);

            List<Segment> segments = new ArrayList<>();
            Segment segment = new Segment();
            GraphNode node1 = nodeLookup.get(edge.getNode1Label());
            GraphNode node2 = nodeLookup.get(edge.getNode2Label());
            segment.setStart(new Point3D(node1.getPos3dX(), node1.getPos3dY(), node1.getPos3dZ()));
            segment.setEnd(new Point3D(node2.getPos3dX(), node2.getPos3dY(), node2.getPos3dZ()));
            // not setting angles and mid point, they aren't needed
            segments.add(segment);
            lgEdge.setSegments(segments);

            List<Circle> circles = new ArrayList<>();
            Circle circle = new Circle();
            // calculating midpoint
            Point3d start = new Point3d(node1.getPos3dX(), node1.getPos3dY(), node1.getPos3dZ());
            Point3d end = new Point3d(node2.getPos3dX(), node2.getPos3dY(), node2.getPos3dZ());
            Vector3d vec = new Vector3d(end);
            vec.sub(start);
            vec.scale(0.5);
            Point3d center = new Point3d(start);
            center.add(vec);
            circle.setCenter(new Point3D(center.x, center.y, center.z));
            // perpendicular is simply a point on the perpendicular to the circle, e.g the source of the edge
            circle.setPerpendicular(new Point3D(start.x, start.y, start.z));
            circle.setRadius(LatticeGraph3D.defaultInterfaceRadius);
            circles.add(circle);
            lgEdge.setCircles(circles);

            lgEdges.add(lgEdge);
        }
        latticeGraph.setEdges(lgEdges);

        latticeGraph.setUnitCellTransforms(unitCellTransforms);

        return latticeGraph;
    }
}
