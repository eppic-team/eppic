package eppic.model.dto;

import eppic.model.db.AssemblyDB;
import eppic.model.db.GraphEdgeDB;

import java.io.Serializable;

public class GraphEdge implements Serializable {

    private static final long serialVersionUID = 1L;

    private int uid;

    private int label;
    private String color;

    private boolean inGraph2d;

    private int xtalTransA;
    private int xtalTransB;
    private int xtalTransC;

    // labels (chainId_operatorId)
    private String node1Label;
    private String node2Label;

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getLabel() {
        return label;
    }

    public void setLabel(int label) {
        this.label = label;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public boolean isInGraph2d() {
        return inGraph2d;
    }

    public void setInGraph2d(boolean inGraph2d) {
        this.inGraph2d = inGraph2d;
    }

    public int getXtalTransA() {
        return xtalTransA;
    }

    public void setXtalTransA(int xtalTransA) {
        this.xtalTransA = xtalTransA;
    }

    public int getXtalTransB() {
        return xtalTransB;
    }

    public void setXtalTransB(int xtalTransB) {
        this.xtalTransB = xtalTransB;
    }

    public int getXtalTransC() {
        return xtalTransC;
    }

    public void setXtalTransC(int xtalTransC) {
        this.xtalTransC = xtalTransC;
    }

    public String getNode1Label() {
        return node1Label;
    }

    public void setNode1Label(String node1Label) {
        this.node1Label = node1Label;
    }

    public String getNode2Label() {
        return node2Label;
    }

    public void setNode2Label(String node2Label) {
        this.node2Label = node2Label;
    }

    public static GraphEdge create(GraphEdgeDB graphEdgeDB) {
        GraphEdge graphEdge = new GraphEdge();

        graphEdge.setUid(graphEdgeDB.getUid());
        graphEdge.setColor(graphEdgeDB.getColor());
        graphEdge.setInGraph2d(graphEdgeDB.isInGraph2d());
        graphEdge.setLabel(graphEdgeDB.getLabel());

        graphEdge.setNode1Label(graphEdgeDB.getNode1Label());
        graphEdge.setNode2Label(graphEdgeDB.getNode2Label());

        graphEdge.setXtalTransA(graphEdgeDB.getXtalTransA());
        graphEdge.setXtalTransB(graphEdgeDB.getXtalTransB());
        graphEdge.setXtalTransC(graphEdgeDB.getXtalTransC());

        return graphEdge;
    }
}
