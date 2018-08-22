package eppic.rest.filter;

import eppic.EppicParams;
import eppic.db.dao.DaoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.NoResultException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * A custom exception mapper to intercept exceptions and set approppriate response codes and messages
 * when things go wrong in responding to an API request.
 * @param <K>
 *
 * @author Jose Duarte
 */
@Provider
public class CustomExceptionMapper<K> implements ExceptionMapper<Throwable> {

    private static final Logger logger = LoggerFactory.getLogger(CustomExceptionMapper.class);

    @Override
    public Response toResponse(Throwable ex) {

        int response;
        String msg;

        if (ex instanceof DaoException && ex.getCause()!=null && ex.getCause() instanceof NoResultException) {

            // so that a hibernate NoResultException (wrapped inside a DaoException) is converted to a 404 in REST API output

            response = Response.Status.NOT_FOUND.getStatusCode();
            msg = "No results found";
            logger.error(msg + ". Error: "+ex.getMessage());

        } else {

            response = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
            msg = "Internal server error. If the problem persists, please report it to " + EppicParams.CONTACT_EMAIL;
            logger.error(msg + ". Error: "+ex.getMessage());
        }

        return Response.status(response)
                .entity(msg)
                .type(MediaType.TEXT_PLAIN)
                .build();

    }



}