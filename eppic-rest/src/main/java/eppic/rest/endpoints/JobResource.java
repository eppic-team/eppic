package eppic.rest.endpoints;

import eppic.db.dao.DaoException;
import eppic.model.dto.*;
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

    @GET
    @Path("/sequences/" + "{jobId}")
    @Produces({MediaType.APPLICATION_JSON + ";charset=utf-8", MediaType.APPLICATION_XML + ";charset=utf-8"})
    public Response getSequences(
            @Context UriInfo uriInfo,
            @PathParam("jobId") String jobId) throws DaoException {


        List<ChainCluster> ics = JobService.getSequenceData(jobId);
        // https://stackoverflow.com/questions/6081546/jersey-can-produce-listt-but-cannot-response-oklistt-build
        GenericEntity<List<ChainCluster>> entity = new GenericEntity<List<ChainCluster>>(ics){};

        Response.ResponseBuilder responseBuilder =  Response
                .status(Response.Status.OK)
                .type(getMediaType(uriInfo))
                .entity(entity);

        return responseBuilder.build();
    }

    @GET
    @Path("/interfaceResidues/" + "{jobId}/{interfId}")
    @Produces({MediaType.APPLICATION_JSON + ";charset=utf-8", MediaType.APPLICATION_XML + ";charset=utf-8"})
    public Response getInterfaceResidues(
            @Context UriInfo uriInfo,
            @PathParam("jobId") String jobId,
            @PathParam("interfId") String interfId) throws DaoException {

        // TODO validate interfId is int

        List<Residue> ics = JobService.getResidueData(jobId, Integer.parseInt(interfId));
        // https://stackoverflow.com/questions/6081546/jersey-can-produce-listt-but-cannot-response-oklistt-build
        GenericEntity<List<Residue>> entity = new GenericEntity<List<Residue>>(ics){};

        Response.ResponseBuilder responseBuilder =  Response
                .status(Response.Status.OK)
                .type(getMediaType(uriInfo))
                .entity(entity);

        return responseBuilder.build();
    }

    @GET
    @Path("/assembly/" + "{jobId}/{pdbAssemblyId}")
    @Produces({MediaType.APPLICATION_JSON + ";charset=utf-8", MediaType.APPLICATION_XML + ";charset=utf-8"})
    public Response getAssembly(
            @Context UriInfo uriInfo,
            @PathParam("jobId") String jobId,
            @PathParam("pdbAssemblyId") String pdbAssemblyId) throws DaoException {


        Assembly assembly = JobService.getAssemblyData(jobId, Integer.parseInt(pdbAssemblyId));

        Response.ResponseBuilder responseBuilder =  Response
                .status(Response.Status.OK)
                .type(getMediaType(uriInfo))
                .entity(assembly);

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
