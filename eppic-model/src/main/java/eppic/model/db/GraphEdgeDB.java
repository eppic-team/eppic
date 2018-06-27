package eppic.model.db;

import java.io.Serializable;

public class GraphEdgeDB implements Serializable {

    private static final long serialVersionUID = 1L;

    private int uid;

    private AssemblyDB assembly;

    // we could store a reference to the intefaceCluster but that would produce a complicated SQL schema
    // instead we store the interfaceCluster id
    private int label;
    private String color;

    private boolean inGraph2d;

    private int xtalTransA;
    private int xtalTransB;
    private int xtalTransC;

    // instead of having references to the nodes (which in SQL would require an extra table for many-to-many relation)
    // we just store the labels (chainId_operatorId)
    private String node1Label;
    private String node2Label;

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public AssemblyDB getAssembly() {
        return assembly;
    }

    public void setAssembly(AssemblyDB assembly) {
        this.assembly = assembly;
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

    public boolean isInGraph2d() {
        return inGraph2d;
    }

    public void setInGraph2d(boolean inGraph2d) {
        this.inGraph2d = inGraph2d;
    }
}
