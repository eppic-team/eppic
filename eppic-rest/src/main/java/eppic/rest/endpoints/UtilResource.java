package eppic.rest.endpoints;

import com.fasterxml.jackson.databind.node.ObjectNode;
import eppic.db.dao.JobDAO;
import eppic.db.dao.jpa.JobDAOJpa;
import eppic.rest.service.UtilService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Utilities via REST
 *
 * @author Jose Duarte
 * @since 3.2.0
 */
@Path("/util")
public class UtilResource {

    @GET
    @Path("/alive")
    public String alive() {
        JobDAO jobDAO = new JobDAOJpa();

        if (jobDAO.isJobsEmpty()) {
            return "false";
        } else {
            return "true";
        }
    }

    @GET
    @Path("/info")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInfo() {

        ObjectNode jsonObj = UtilService.getInfo();

        return Response
                .ok()
                .entity(jsonObj.toString())
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
