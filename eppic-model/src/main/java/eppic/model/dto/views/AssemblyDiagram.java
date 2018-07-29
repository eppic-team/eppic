package eppic.model.dto.views;

import java.io.Serializable;
import java.util.List;

/**
 * A bean to represent the data needed to render the assembly diagram view.
 * @since 3.1.0
 */
public class AssemblyDiagram implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<AssemblyDiagramNode> nodes;
    private List<AssemblyDiagramEdge> edges;

    public List<AssemblyDiagramNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<AssemblyDiagramNode> nodes) {
        this.nodes = nodes;
    }

    public List<AssemblyDiagramEdge> getEdges() {
        return edges;
    }

    public void setEdges(List<AssemblyDiagramEdge> edges) {
        this.edges = edges;
    }
}
