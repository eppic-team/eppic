package eppic.rest.endpoints;

import com.fasterxml.jackson.databind.node.ObjectNode;
import eppic.rest.service.UtilService;
import io.swagger.v3.oas.annotations.tags.Tag;

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
    @Tag(name = "Alive service")
    @Produces(MediaType.TEXT_PLAIN)
    public String alive() {


        if (UtilService.isDbHealthy() && UtilService.isTempDiskHealthy()) {
            return "true";
        } else {
            return "false";
        }
    }

    @GET
    @Path("/info")
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = "Info service")
    public Response getInfo() {

        ObjectNode jsonObj = UtilService.getInfo();

        return Response
                .ok()
                .entity(jsonObj.toString())
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
