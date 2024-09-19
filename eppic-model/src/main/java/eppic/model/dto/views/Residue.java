package eppic.model.dto.views;

import java.io.Serializable;

public class Residue implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean side;
    private double asa;
    private double bsa;
    private double burialFraction;
    private short region;
    private int residueNumber;
    private String residueType;
    private double entropyScore;

    public boolean isSide() {
        return side;
    }

    public void setSide(boolean side) {
        this.side = side;
    }

    public double getAsa() {
        return asa;
    }

    public void setAsa(double asa) {
        this.asa = asa;
    }

    public double getBsa() {
        return bsa;
    }

    public void setBsa(double bsa) {
        this.bsa = bsa;
    }

    public double getBurialFraction() {
        return burialFraction;
    }

    public void setBurialFraction(double burialFraction) {
        this.burialFraction = burialFraction;
    }

    public short getRegion() {
        return region;
    }

    public void setRegion(short region) {
        this.region = region;
    }

    public int getResidueNumber() {
        return residueNumber;
    }

    public void setResidueNumber(int residueNumber) {
        this.residueNumber = residueNumber;
    }

    public String getResidueType() {
        return residueType;
    }

    public void setResidueType(String residueType) {
        this.residueType = residueType;
    }

    public double getEntropyScore() {
        return entropyScore;
    }

    public void setEntropyScore(double entropyScore) {
        this.entropyScore = entropyScore;
    }
}
