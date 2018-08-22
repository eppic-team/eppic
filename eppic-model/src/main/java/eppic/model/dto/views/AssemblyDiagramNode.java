package eppic.model.dto.views;

import java.io.Serializable;

public class AssemblyDiagramNode implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;//	A_0
    private String label;//	A_0
    private double x; //	223
    private double y; //	267
    private String color;//	#1b9e77

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
