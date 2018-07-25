package eppic.model.dto.views;

import java.io.Serializable;
import java.util.List;

public class LatticeGraphEdge implements Serializable {

    private static final long serialVersionUID = 1L;

    private int interfaceId;
    private int clusterId;
    private String xtalTrans;
    private String color;
    private List<Circle> circles;
    private List<Segment> segments;

    public int getInterfaceId() {
        return interfaceId;
    }

    public void setInterfaceId(int interfaceId) {
        this.interfaceId = interfaceId;
    }

    public int getClusterId() {
        return clusterId;
    }

    public void setClusterId(int clusterId) {
        this.clusterId = clusterId;
    }

    public String getXtalTrans() {
        return xtalTrans;
    }

    public void setXtalTrans(String xtalTrans) {
        this.xtalTrans = xtalTrans;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public List<Circle> getCircles() {
        return circles;
    }

    public void setCircles(List<Circle> circles) {
        this.circles = circles;
    }

    public List<Segment> getSegments() {
        return segments;
    }

    public void setSegments(List<Segment> segments) {
        this.segments = segments;
    }
}
