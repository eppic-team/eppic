package eppic.model.dto.views;

import java.io.Serializable;

public class Circle implements Serializable {

    private static final long serialVersionUID = 1L;

    private Point3D center;
    private Point3D perpendicular;
    private double radius;

    public Point3D getCenter() {
        return center;
    }

    public void setCenter(Point3D center) {
        this.center = center;
    }

    public Point3D getPerpendicular() {
        return perpendicular;
    }

    public void setPerpendicular(Point3D perpendicular) {
        this.perpendicular = perpendicular;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }
}
