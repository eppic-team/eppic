package eppic.model.db;

import java.io.Serializable;

public class GraphNodeDB implements Serializable {

    private static final long serialVersionUID = 1L;

    private int uid;

    private AssemblyDB assembly;

    // label is: chainId_operatorId
    private String label;
    private String color;

    private boolean inGraph2d;

    private double pos2dX;
    private double pos2dY;

    private double pos3dX;
    private double pos3dY;
    private double pos3dZ;

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

    public double getPos2dX() {
        return pos2dX;
    }

    public void setPos2dX(double pos2dX) {
        this.pos2dX = pos2dX;
    }

    public double getPos2dY() {
        return pos2dY;
    }

    public void setPos2dY(double pos2dY) {
        this.pos2dY = pos2dY;
    }

    public double getPos3dX() {
        return pos3dX;
    }

    public void setPos3dX(double pos3dX) {
        this.pos3dX = pos3dX;
    }

    public double getPos3dY() {
        return pos3dY;
    }

    public void setPos3dY(double pos3dY) {
        this.pos3dY = pos3dY;
    }

    public double getPos3dZ() {
        return pos3dZ;
    }

    public void setPos3dZ(double pos3dZ) {
        this.pos3dZ = pos3dZ;
    }

    public boolean isInGraph2d() {
        return inGraph2d;
    }

    public void setInGraph2d(boolean inGraph2d) {
        this.inGraph2d = inGraph2d;
    }
}
