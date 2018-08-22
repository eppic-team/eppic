package eppic.model.dto;

import eppic.model.db.GraphEdgeDB;

import java.io.Serializable;

public class GraphEdge implements Serializable {

    private static final long serialVersionUID = 1L;

    private int uid;

    private String label;

    private int interfaceId;
    private int interfaceClusterId;

    private String color;

    private boolean inGraph2d;

    private int xtalTransA;
    private int xtalTransB;
    private int xtalTransC;

    // labels (chainId_operatorId)
    private String node1Label;
    private String node2Label;

    private double startPos3dX;
    private double startPos3dY;
    private double startPos3dZ;

    private double endPos3dX;
    private double endPos3dY;
    private double endPos3dZ;

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
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

    public int getInterfaceId() {
        return interfaceId;
    }

    public void setInterfaceId(int interfaceId) {
        this.interfaceId = interfaceId;
    }

    public int getInterfaceClusterId() {
        return interfaceClusterId;
    }

    public void setInterfaceClusterId(int interfaceClusterId) {
        this.interfaceClusterId = interfaceClusterId;
    }

    public double getStartPos3dX() {
        return startPos3dX;
    }

    public void setStartPos3dX(double startPos3dX) {
        this.startPos3dX = startPos3dX;
    }

    public double getStartPos3dY() {
        return startPos3dY;
    }

    public void setStartPos3dY(double startPos3dY) {
        this.startPos3dY = startPos3dY;
    }

    public double getStartPos3dZ() {
        return startPos3dZ;
    }

    public void setStartPos3dZ(double startPos3dZ) {
        this.startPos3dZ = startPos3dZ;
    }

    public double getEndPos3dX() {
        return endPos3dX;
    }

    public void setEndPos3dX(double endPos3dX) {
        this.endPos3dX = endPos3dX;
    }

    public double getEndPos3dY() {
        return endPos3dY;
    }

    public void setEndPos3dY(double endPos3dY) {
        this.endPos3dY = endPos3dY;
    }

    public double getEndPos3dZ() {
        return endPos3dZ;
    }

    public void setEndPos3dZ(double endPos3dZ) {
        this.endPos3dZ = endPos3dZ;
    }

    public static GraphEdge create(GraphEdgeDB graphEdgeDB) {
        GraphEdge graphEdge = new GraphEdge();

        graphEdge.setUid(graphEdgeDB.getUid());
        graphEdge.setColor(graphEdgeDB.getColor());
        graphEdge.setInGraph2d(graphEdgeDB.isInGraph2d());
        graphEdge.setLabel(graphEdgeDB.getLabel());

        graphEdge.setInterfaceId(graphEdgeDB.getInterfaceId());
        graphEdge.setInterfaceClusterId(graphEdgeDB.getInterfaceClusterId());

        graphEdge.setNode1Label(graphEdgeDB.getNode1Label());
        graphEdge.setNode2Label(graphEdgeDB.getNode2Label());

        graphEdge.setXtalTransA(graphEdgeDB.getXtalTransA());
        graphEdge.setXtalTransB(graphEdgeDB.getXtalTransB());
        graphEdge.setXtalTransC(graphEdgeDB.getXtalTransC());

        graphEdge.setStartPos3dX(graphEdgeDB.getStartPos3dX());
        graphEdge.setStartPos3dY(graphEdgeDB.getStartPos3dY());
        graphEdge.setStartPos3dZ(graphEdgeDB.getStartPos3dZ());

        graphEdge.setEndPos3dX(graphEdgeDB.getEndPos3dX());
        graphEdge.setEndPos3dY(graphEdgeDB.getEndPos3dY());
        graphEdge.setEndPos3dZ(graphEdgeDB.getEndPos3dZ());

        return graphEdge;
    }
}
