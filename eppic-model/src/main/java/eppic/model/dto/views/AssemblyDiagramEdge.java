package eppic.model.dto.views;

import java.io.Serializable;

public class AssemblyDiagramEdge implements Serializable {

    private static final long serialVersionUID = 1L;

    private String from;//	D_0
    private String to;//	A_0
    private String label;//	1
    private String title;//	(0, 0, 0)
    private String color; //	#66c2a5

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
