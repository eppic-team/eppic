package eppic.model.dto;

public class UserJobSubmission {


    private String data;
    private String fileName;
    private boolean skipEvolAnalysis;
    private String email;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isSkipEvolAnalysis() {
        return skipEvolAnalysis;
    }

    public void setSkipEvolAnalysis(boolean skipEvolAnalysis) {
        this.skipEvolAnalysis = skipEvolAnalysis;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
