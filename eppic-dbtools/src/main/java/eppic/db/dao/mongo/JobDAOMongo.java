package eppic.db.dao.mongo;

import eppic.db.dao.DaoException;
import eppic.db.dao.JobDAO;
import eppic.model.db.JobDB;
import eppic.model.db.PdbInfoDB;
import eppic.model.dto.InputWithType;
import eppic.model.shared.StatusOfJob;

import java.util.Date;

public class JobDAOMongo implements JobDAO {
    @Override
    public void insertNewJob(String jobId, String sessionId, String email, String input, String ip, Date submissionDate, int inputType, StatusOfJob status, String submissionId) throws DaoException {

    }

    @Override
    public void updateStatusOfJob(String jobId, StatusOfJob stopped) throws DaoException {

    }

    @Override
    public StatusOfJob getStatusForJob(String jobId) throws DaoException {
        return null;
    }

    @Override
    public int getInputTypeForJob(String jobId) throws DaoException {
        return 0;
    }

    @Override
    public Long getNrOfJobsForIPDuringLastDay(String ip) throws DaoException {
        return null;
    }

    @Override
    public InputWithType getInputWithTypeForJob(String jobId) throws DaoException {
        return null;
    }

    @Override
    public void setPdbScoreItemForJob(String jobId, PdbInfoDB pdbScoreItem) throws DaoException {

    }

    @Override
    public String getSubmissionIdForJobId(String jobId) throws DaoException {
        return null;
    }

    @Override
    public JobDB getJob(String jobId) throws DaoException {
        return null;
    }

    @Override
    public boolean isJobsEmpty() {
        return false;
    }
}
