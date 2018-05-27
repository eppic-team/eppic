package eppic.rest.endpoints;

import eppic.dtomodel.Assembly;
import eppic.dtomodel.Interface;
import eppic.dtomodel.InterfaceCluster;
import eppic.dtomodel.PdbInfo;
import eppic.db.dao.DaoException;
import eppic.rest.service.JobService;
import io.swagger.annotations.Api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;
import java.util.List;

@Path("/job")
@Api(tags = {"job"})
public class JobResource {

    @GET
    @Path("/pdb/" + "{jobId}")
    @Produces({MediaType.APPLICATION_JSON + ";charset=utf-8", MediaType.APPLICATION_XML + ";charset=utf-8"})
    public Response getPdb(
            @Context UriInfo uriInfo,
            @PathParam("jobId") String jobId) throws DaoException {


        PdbInfo pdbInfo = JobService.getResultData(jobId, false, false, false, false);

        Response.ResponseBuilder responseBuilder =  Response
                .status(Response.Status.OK)
                .type(getMediaType(uriInfo))
                .entity(pdbInfo);

        return responseBuilder.build();
    }

    @GET
    @Path("/assemblies/" + "{jobId}")
    @Produces({MediaType.APPLICATION_JSON + ";charset=utf-8", MediaType.APPLICATION_XML + ";charset=utf-8"})
    public Response getAssemblies(
            @Context UriInfo uriInfo,
            @PathParam("jobId") String jobId) throws DaoException {


        List<Assembly> assemblies = JobService.getAssemblyData(jobId);
        // https://stackoverflow.com/questions/6081546/jersey-can-produce-listt-but-cannot-response-oklistt-build
        GenericEntity<List<Assembly>> entity = new GenericEntity<List<Assembly>>(assemblies){};

        Response.ResponseBuilder responseBuilder =  Response
                .status(Response.Status.OK)
                .type(getMediaType(uriInfo))
                .entity(entity);

        return responseBuilder.build();
    }

    @GET
    @Path("/interfaceClusters/" + "{jobId}")
    @Produces({MediaType.APPLICATION_JSON + ";charset=utf-8", MediaType.APPLICATION_XML + ";charset=utf-8"})
    public Response getInterfaceClusters(
            @Context UriInfo uriInfo,
            @PathParam("jobId") String jobId) throws DaoException {


        List<InterfaceCluster> ics = JobService.getInterfaceClusterData(jobId);
        // https://stackoverflow.com/questions/6081546/jersey-can-produce-listt-but-cannot-response-oklistt-build
        GenericEntity<List<InterfaceCluster>> entity = new GenericEntity<List<InterfaceCluster>>(ics){};

        Response.ResponseBuilder responseBuilder =  Response
                .status(Response.Status.OK)
                .type(getMediaType(uriInfo))
                .entity(entity);

        return responseBuilder.build();
    }

    @GET
    @Path("/interfaces/" + "{jobId}")
    @Produces({MediaType.APPLICATION_JSON + ";charset=utf-8", MediaType.APPLICATION_XML + ";charset=utf-8"})
    public Response getInterfaces(
            @Context UriInfo uriInfo,
            @PathParam("jobId") String jobId) throws DaoException {


        List<Interface> ics = JobService.getInterfaceData(jobId);
        // https://stackoverflow.com/questions/6081546/jersey-can-produce-listt-but-cannot-response-oklistt-build
        GenericEntity<List<Interface>> entity = new GenericEntity<List<Interface>>(ics){};

        Response.ResponseBuilder responseBuilder =  Response
                .status(Response.Status.OK)
                .type(getMediaType(uriInfo))
                .entity(entity);

        return responseBuilder.build();
    }

    /**
     * Checks the format url parameter and returns the right mime type (either json or xml).
     * If no format parameter present, then json is returned.
     * @param uriInfo
     * @return
     */
    private static String getMediaType(UriInfo uriInfo) {
        String mediaType = MediaType.APPLICATION_JSON;
        String format = uriInfo.getQueryParameters().getFirst("format");
        if (format!=null && format.equalsIgnoreCase("xml")) {
            mediaType = MediaType.APPLICATION_XML;
        }
        return mediaType;
    }
}
