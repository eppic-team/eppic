package eppic.rest.filter;

import eppic.db.dao.DaoException;
import eppic.rest.jobs.JobHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import javax.persistence.NoResultException;
import javax.ws.rs.WebApplicationException;

/**
 * A custom exception mapper to intercept exceptions and set approppriate response codes and messages
 * when things go wrong in responding to an API request.
 *
 * @author Jose Duarte
 */
@ControllerAdvice
public class CustomExceptionMapper {

    private static final Logger logger = LoggerFactory.getLogger(CustomExceptionMapper.class);

    private static final String APIDOCS_URL = "https://eppic-rest.rcsb.org/";
    private static final String INTERNAL_SERVER_ERROR_MSG = "Internal server error. If the problem persists, please report it to " + "info@rcsb.org";

    /**
     * The structure for error message.
     * @param status the redundant HTTP error status code
     * @param message short description of the error
     * @param link points to an online resource, such as API spec in swagger ui
     */
    private record ErrorMessage(int status, String message, String link) {
        @Override
        public String toString() {
            return "ErrorMessage{" +
                    "status=" + status +
                    ", message='" + message + '\'' +
                    ", link='" + link + '\'' +
                    '}';
        }
    }

    @ExceptionHandler(DaoException.class)
    public ResponseEntity<?> handleDao(DaoException e) {
        HttpStatus status;
        String msg;
        if (e.getCause() != null && e.getCause() instanceof NoResultException) {
            status = HttpStatus.NOT_FOUND;
            msg = "No results found";
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            msg = INTERNAL_SERVER_ERROR_MSG;
        }

        logger.info("{}. Exception: {}. Error: {}", msg, e.getClass().getName(), e.getMessage());
        return ResponseEntity.status(status).body(getErrorMessage(status.value(), e.getMessage()));
    }

    @ExceptionHandler(NoResultException.class)
    public ResponseEntity<?> handleNoResult(NoResultException e) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        String msg = "No results found";
        logger.info("{}. Exception: {}. Error: {}", msg, e.getClass().getName(), e.getMessage());
        return ResponseEntity.status(status).body(getErrorMessage(status.value(), e.getMessage()));
    }

    @ExceptionHandler(WebApplicationException.class)
    public ResponseEntity<?> handleWebApplication(WebApplicationException e) {
        HttpStatus status = HttpStatus.valueOf(e.getResponse().getStatus());
        String msg = e.getMessage();
        logger.info("{}. Exception: {}. Error: {}", msg, e.getClass().getName(), e.getMessage());
        return ResponseEntity.status(status).body(getErrorMessage(status.value(), e.getMessage()));
    }

    @ExceptionHandler(JobHandlerException.class)
    public ResponseEntity<?> handleJobHandler(JobHandlerException e) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String msg = e.getMessage();
        logger.info("{}. Exception: {}. Error: {}", msg, e.getClass().getName(), e.getMessage());
        return ResponseEntity.status(status).body(getErrorMessage(status.value(), e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String msg = e.getMessage();
        logger.info("{}. Exception: {}. Error: {}", msg, e.getClass().getName(), e.getMessage());
        return ResponseEntity.status(status).body(getErrorMessage(status.value(), e.getMessage()));
    }

    // anything else will be caught by Spring and converted into a 500

    private ErrorMessage getErrorMessage(int statusCode, String message) {
        return new ErrorMessage(statusCode, message, APIDOCS_URL);
    }

}