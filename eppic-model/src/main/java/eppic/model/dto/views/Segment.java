package eppic.model.dto.views;

import java.io.Serializable;

public class Segment implements Serializable {

    private static final long serialVersionUID = 1L;

    private double radius;
    private double startAngle;
    private double endAngle;

    private Point3D start;
    private Point3D mid;
    private Point3D end;

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public double getStartAngle() {
        return startAngle;
    }

    public void setStartAngle(double startAngle) {
        this.startAngle = startAngle;
    }

    public double getEndAngle() {
        return endAngle;
    }

    public void setEndAngle(double endAngle) {
        this.endAngle = endAngle;
    }

    public Point3D getStart() {
        return start;
    }

    public void setStart(Point3D start) {
        this.start = start;
    }

    public Point3D getMid() {
        return mid;
    }

    public void setMid(Point3D mid) {
        this.mid = mid;
    }

    public Point3D getEnd() {
        return end;
    }

    public void setEnd(Point3D end) {
        this.end = end;
    }
}
