package eppic.model.dto.views;

import java.io.Serializable;
import java.util.List;

/**
 * A bean to represent the data needed to render the lattice graph view.
 */
public class LatticeGraph implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<LatticeGraphVertex> vertices;
    private List<LatticeGraphEdge> edges;
    private List<UnitCellTransform> unitCellTransforms;

    public List<LatticeGraphVertex> getVertices() {
        return vertices;
    }

    public void setVertices(List<LatticeGraphVertex> vertices) {
        this.vertices = vertices;
    }

    public List<LatticeGraphEdge> getEdges() {
        return edges;
    }

    public void setEdges(List<LatticeGraphEdge> edges) {
        this.edges = edges;
    }

    public List<UnitCellTransform> getUnitCellTransforms() {
        return unitCellTransforms;
    }

    public void setUnitCellTransforms(List<UnitCellTransform> unitCellTransforms) {
        this.unitCellTransforms = unitCellTransforms;
    }
}
