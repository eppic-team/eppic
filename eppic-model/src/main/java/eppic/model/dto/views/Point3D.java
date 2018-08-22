package eppic.model.dto.views;

import java.io.Serializable;

public class Point3D implements Serializable {

    private static final long serialVersionUID = 1L;

    private double x;
    private double y;
    private double z;

    public Point3D() {

    }

    public Point3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }
}
