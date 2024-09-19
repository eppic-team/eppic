package eppic.model.dto;

import eppic.model.shared.StatusOfJob;

public class SubmissionStatus {

    private String submissionId;
    private StatusOfJob status;

    public SubmissionStatus(String submissionId, StatusOfJob status) {
        this.submissionId = submissionId;
        this.status = status;
    }

    public String getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(String submissionId) {
        this.submissionId = submissionId;
    }

    public StatusOfJob getStatus() {
        return status;
    }

    public void setStatus(StatusOfJob status) {
        this.status = status;
    }
}
