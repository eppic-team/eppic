package eppic.model.dto;

import eppic.model.db.AssemblyDB;
import eppic.model.db.GraphNodeDB;

import java.io.Serializable;

public class GraphNode implements Serializable {

    private static final long serialVersionUID = 1L;

    private int uid;

    // label is: chainId_operatorId
    private String label;
    private String color;

    /**
     * Whether the node is one of those chosen for laying out the 2D assembly graph.
     */
    private boolean inGraph2d;

    private double pos2dX;
    private double pos2dY;

    private double pos3dX;
    private double pos3dY;
    private double pos3dZ;

    /**
     * Whether the node is one of those chosen for laying out the structure's assembly
     * in 3D (not the 3D lattice graph but the assembly's structure).
     */
    private boolean in3dStructure;

    private double rxx;
    private double rxy;
    private double rxz;
    private double ryx;
    private double ryy;
    private double ryz;
    private double rzx;
    private double rzy;
    private double rzz;

    // translation
    private double tx;
    private double ty;
    private double tz;

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

    public boolean isIn3dStructure() {
        return in3dStructure;
    }

    public void setIn3dStructure(boolean in3dStructure) {
        this.in3dStructure = in3dStructure;
    }

    public double getRxx() {
        return rxx;
    }

    public void setRxx(double rxx) {
        this.rxx = rxx;
    }

    public double getRxy() {
        return rxy;
    }

    public void setRxy(double rxy) {
        this.rxy = rxy;
    }

    public double getRxz() {
        return rxz;
    }

    public void setRxz(double rxz) {
        this.rxz = rxz;
    }

    public double getRyx() {
        return ryx;
    }

    public void setRyx(double ryx) {
        this.ryx = ryx;
    }

    public double getRyy() {
        return ryy;
    }

    public void setRyy(double ryy) {
        this.ryy = ryy;
    }

    public double getRyz() {
        return ryz;
    }

    public void setRyz(double ryz) {
        this.ryz = ryz;
    }

    public double getRzx() {
        return rzx;
    }

    public void setRzx(double rzx) {
        this.rzx = rzx;
    }

    public double getRzy() {
        return rzy;
    }

    public void setRzy(double rzy) {
        this.rzy = rzy;
    }

    public double getRzz() {
        return rzz;
    }

    public void setRzz(double rzz) {
        this.rzz = rzz;
    }

    public double getTx() {
        return tx;
    }

    public void setTx(double tx) {
        this.tx = tx;
    }

    public double getTy() {
        return ty;
    }

    public void setTy(double ty) {
        this.ty = ty;
    }

    public double getTz() {
        return tz;
    }

    public void setTz(double tz) {
        this.tz = tz;
    }

    public static GraphNode create(GraphNodeDB graphNodeDB) {
        GraphNode graphNode = new GraphNode();

        graphNode.setUid(graphNodeDB.getUid());
        graphNode.setColor(graphNodeDB.getColor());
        graphNode.setIn3dStructure(graphNodeDB.isIn3dStructure());
        graphNode.setInGraph2d(graphNodeDB.isInGraph2d());
        graphNode.setLabel(graphNodeDB.getLabel());

        graphNode.setPos2dX(graphNodeDB.getPos2dX());
        graphNode.setPos2dY(graphNodeDB.getPos2dY());

        graphNode.setPos3dX(graphNodeDB.getPos3dX());
        graphNode.setPos3dY(graphNodeDB.getPos3dY());
        graphNode.setPos3dZ(graphNodeDB.getPos3dZ());

        graphNode.setRxx(graphNodeDB.getRxx());
        graphNode.setRxy(graphNodeDB.getRxy());
        graphNode.setRxz(graphNodeDB.getRxz());

        graphNode.setRyx(graphNodeDB.getRyx());
        graphNode.setRyy(graphNodeDB.getRyy());
        graphNode.setRyz(graphNodeDB.getRyz());

        graphNode.setRzx(graphNodeDB.getRzx());
        graphNode.setRzy(graphNodeDB.getRzy());
        graphNode.setRzz(graphNodeDB.getRzz());

        graphNode.setTx(graphNodeDB.getTx());
        graphNode.setTy(graphNodeDB.getTy());
        graphNode.setTz(graphNodeDB.getTz());

        return graphNode;
    }
}
