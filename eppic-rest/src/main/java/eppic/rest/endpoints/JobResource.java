package eppic.rest.endpoints;

import eppic.db.dao.DaoException;
import eppic.model.dto.*;
import eppic.model.dto.views.AssemblyDiagram;
import eppic.model.dto.views.LatticeGraph;
import eppic.rest.commons.Utils;
import eppic.rest.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;
import java.util.List;
import java.util.SortedSet;

@Path("/job")
public class JobResource {

    @GET
    @Path("/pdb/{jobId}")
    @Produces({MediaType.APPLICATION_JSON})
    @Tag(name = "PDB info service",
            description = "provides general information about PDB structures.")
    @Operation(
            summary = "Get PDB structure description by job id (either PDB id or alphanumerical user job id).",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = PdbInfo.class))),
                    @ApiResponse(responseCode = "404", description = "Not Found")})
    public Response getPdb(
            @Context UriInfo uriInfo,
            @PathParam("jobId") String jobId) throws DaoException {


        PdbInfo pdbInfo = JobService.getResultData(jobId, false, false, false, false);

        Response.ResponseBuilder responseBuilder =  Response
                .status(Response.Status.OK)
                .type(MediaType.APPLICATION_JSON)
                .entity(pdbInfo);

        return responseBuilder.build();
    }

    @GET
    @Path("/interfaceClusters/{jobId}")
    @Produces({MediaType.APPLICATION_JSON})
    @Tag(name = "Interface cluster service",
            description = "provides information about an interface cluster (interface type or unique binding mode).")
    @Operation(
            summary = "Get interface cluster information by job id (either PDB id or alphanumerical user job id).")
    public Response getInterfaceClusters(
            @Context UriInfo uriInfo,
            @PathParam("jobId") String jobId) throws DaoException {


        List<InterfaceCluster> ics = JobService.getInterfaceClusterData(jobId);
        // https://stackoverflow.com/questions/6081546/jersey-can-produce-listt-but-cannot-response-oklistt-build
        GenericEntity<List<InterfaceCluster>> entity = new GenericEntity<List<InterfaceCluster>>(ics){};

        Response.ResponseBuilder responseBuilder =  Response
                .status(Response.Status.OK)
                .type(MediaType.APPLICATION_JSON)
                .entity(entity);

        return responseBuilder.build();
    }

    @GET
    @Path("/interfaces/{jobId}")
    @Produces({MediaType.APPLICATION_JSON})
    @Tag(name = "Interface service",
            description = "provides information about an interface.")
    @Operation(
            summary = "Get interface information by job id (either PDB id or alphanumerical user job id).")
    public Response getInterfaces(
            @Context UriInfo uriInfo,
            @PathParam("jobId") String jobId) throws DaoException {


        List<Interface> ics = JobService.getInterfaceData(jobId);
        // https://stackoverflow.com/questions/6081546/jersey-can-produce-listt-but-cannot-response-oklistt-build
        GenericEntity<List<Interface>> entity = new GenericEntity<List<Interface>>(ics){};

        Response.ResponseBuilder responseBuilder =  Response
                .status(Response.Status.OK)
                .type(MediaType.APPLICATION_JSON)
                .entity(entity);

        return responseBuilder.build();
    }

    @GET
    @Path("/sequences/{jobId}")
    @Produces({MediaType.APPLICATION_JSON})
    @Tag(name = "Sequences service",
            description = "provides sequence information (alignment to reference UniProt and sequence homologs) for all molecular entities of a structure.")
    @Operation(
            summary = "Get sequence information by job id (either PDB id or alphanumerical user job id).")
    public Response getSequences(
            @Context UriInfo uriInfo,
            @PathParam("jobId") String jobId) throws DaoException {


        List<ChainCluster> ics = JobService.getSequenceData(jobId);
        // https://stackoverflow.com/questions/6081546/jersey-can-produce-listt-but-cannot-response-oklistt-build
        GenericEntity<List<ChainCluster>> entity = new GenericEntity<List<ChainCluster>>(ics){};

        Response.ResponseBuilder responseBuilder =  Response
                .status(Response.Status.OK)
                .type(MediaType.APPLICATION_JSON)
                .entity(entity);

        return responseBuilder.build();
    }

    @GET
    @Path("/interfaceResidues/{jobId}/{interfId}")
    @Produces({MediaType.APPLICATION_JSON})
    @Tag(name = "Interface residues service",
            description = "provides information about interface residues.")
    @Operation(
            summary = "Get interface residues information by job id (either PDB id or alphanumerical user job id) and EPPIC interface id.")
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
                .type(MediaType.APPLICATION_JSON)
                .entity(entity);

        return responseBuilder.build();
    }

    @GET
    @Path("/assemblies/{jobId}")
    @Produces({MediaType.APPLICATION_JSON})
    @Tag(name = "Assemblies service",
            description = "provides information about all assemblies of a structure.")
    @Operation(
            summary = "Get assemblies information by job id (either PDB id or alphanumerical user job id).")
    public Response getAssemblies(
            @Context UriInfo uriInfo,
            @PathParam("jobId") String jobId) throws DaoException {


        List<Assembly> assemblies = JobService.getAssemblyDataByPdbAssemblyId(jobId);
        // https://stackoverflow.com/questions/6081546/jersey-can-produce-listt-but-cannot-response-oklistt-build
        GenericEntity<List<Assembly>> entity = new GenericEntity<List<Assembly>>(assemblies){};

        Response.ResponseBuilder responseBuilder =  Response
                .status(Response.Status.OK)
                .type(MediaType.APPLICATION_JSON)
                .entity(entity);

        return responseBuilder.build();
    }

    @GET
    @Path("/contacts/{jobId}/{interfId}")
    @Produces({MediaType.APPLICATION_JSON})
    @Tag(name = "Contacts service",
            description = "provides information about contacts across an interface.")
    @Operation(
            summary = "Get interface contacts information by job id (either PDB id or alphanumerical user job id) and EPPIC interface id.")
    public Response getContacts(
            @Context UriInfo uriInfo,
            @PathParam("jobId") String jobId,
            @PathParam("interfId") String interfId) throws DaoException {

        // TODO validate interfId is int

        List<Contact> cs = JobService.getContactData(jobId, Integer.parseInt(interfId));
        // https://stackoverflow.com/questions/6081546/jersey-can-produce-listt-but-cannot-response-oklistt-build
        GenericEntity<List<Contact>> entity = new GenericEntity<List<Contact>>(cs){};

        Response.ResponseBuilder responseBuilder =  Response
                .status(Response.Status.OK)
                .type(MediaType.APPLICATION_JSON)
                .entity(entity);

        return responseBuilder.build();
    }

    @GET
    @Path("/assemblyByPdbId/{pdbId}/{pdbAssemblyId}")
    @Produces({MediaType.APPLICATION_JSON})
    @Tag(name = "Assembly by PDB id service",
            description = "provides information about an assembly.")
    @Operation(
            summary = "Get EPPIC assembly information by PDB id and PDB assembly id.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = Assembly.class))),
                    @ApiResponse(responseCode = "404", description = "Not Found")})
    public Response getAssemblyByPdbId(
            @Context UriInfo uriInfo,
            @PathParam("pdbId") String jobId,
            @PathParam("pdbAssemblyId") String pdbAssemblyId) throws DaoException {

        // TODO validate pdbAssemblyId is int

        Assembly assembly = JobService.getAssemblyDataByPdbAssemblyId(jobId, Integer.parseInt(pdbAssemblyId));

        Response.ResponseBuilder responseBuilder =  Response
                .status(Response.Status.OK)
                .type(MediaType.APPLICATION_JSON)
                .entity(assembly);

        return responseBuilder.build();
    }

    @GET
    @Path("/assembly/{jobId}/{assemblyId}")
    @Produces({MediaType.APPLICATION_JSON})
    @Tag(name = "Assembly service",
            description = "provides information about an assembly.")
    @Operation(
            summary = "Get assembly information by job id (either PDB id or alphanumerical user job id) and EPPIC assembly id.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = Assembly.class))),
                    @ApiResponse(responseCode = "404", description = "Not Found")})
    public Response getAssembly(
            @Context UriInfo uriInfo,
            @PathParam("jobId") String jobId,
            @PathParam("assemblyId") String assemblyId) throws DaoException {

        // TODO validate assemblyId is int

        Assembly assembly = JobService.getAssemblyData(jobId, Integer.parseInt(assemblyId));

        Response.ResponseBuilder responseBuilder =  Response
                .status(Response.Status.OK)
                .type(MediaType.APPLICATION_JSON)
                .entity(assembly);

        return responseBuilder.build();
    }

    @GET
    @Path("/latticeGraph/{jobId}/{assemblyId}")
    @Produces({MediaType.APPLICATION_JSON})
    @Tag(name = "Lattice graph by assembly id service",
            description = "provides information about the lattice graph.")
    @Operation(
            summary = "Get lattice graph by job id (either PDB id or alphanumerical user job id) and EPPIC assembly id.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = LatticeGraph.class))),
                    @ApiResponse(responseCode = "404", description = "Not Found")})
    public Response getLatticeGraph(
            @Context UriInfo uriInfo,
            @PathParam("jobId") String jobId,
            @PathParam("assemblyId") String assemblyId) throws DaoException {

        // TODO validate assemblyId is int

        LatticeGraph latticeGraph = JobService.getLatticeGraphData(jobId, Integer.parseInt(assemblyId));

        Response.ResponseBuilder responseBuilder =  Response
                .status(Response.Status.OK)
                .type(MediaType.APPLICATION_JSON)
                .entity(latticeGraph);

        return responseBuilder.build();
    }

    @GET
    @Path("/latticeGraphByInterfaceIds/{jobId}/{interfaceIds}")
    @Produces({MediaType.APPLICATION_JSON})
    @Tag(name = "Lattice graph by interface ids service",
            description = "provides information about the lattice graph.")
    @Operation(
            summary = "Get lattice graph by job id (either PDB id or alphanumerical user job id) and EPPIC interface ids.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = LatticeGraph.class))),
                    @ApiResponse(responseCode = "404", description = "Not Found")})
    public Response getLatticeGraphByInterfaceIdList(
            @Context UriInfo uriInfo,
            @PathParam("jobId") String jobId,
            @PathParam("interfaceIds") String interfaceIdString) throws DaoException {

        // TODO convert interfaceIdsString to list
        SortedSet<Integer> interfaceIds = Utils.parseIdsString(interfaceIdString);
        LatticeGraph latticeGraph = JobService.getLatticeGraphDataByInterfaceIds(jobId, interfaceIds);

        Response.ResponseBuilder responseBuilder =  Response
                .status(Response.Status.OK)
                .type(MediaType.APPLICATION_JSON)
                .entity(latticeGraph);

        return responseBuilder.build();
    }

    @GET
    @Path("/latticeGraphByInterfaceClusterIds/{jobId}/{interfaceClusterIds}")
    @Produces({MediaType.APPLICATION_JSON})
    @Tag(name = "Lattice graph by interface cluster ids service",
            description = "provides information about the lattice graph.")
    @Operation(
            summary = "Get lattice graph by job id (either PDB id or alphanumerical user job id) and EPPIC interface cluster ids.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = LatticeGraph.class))),
                    @ApiResponse(responseCode = "404", description = "Not Found")})
    public Response getLatticeGraphByInterfaceClusterIdList(
            @Context UriInfo uriInfo,
            @PathParam("jobId") String jobId,
            @PathParam("interfaceClusterIds") String interfaceClusterIdString) throws DaoException {

        // TODO convert interfaceIdsString to list
        SortedSet<Integer> interfaceClusterIds = Utils.parseIdsString(interfaceClusterIdString);
        LatticeGraph latticeGraph = JobService.getLatticeGraphDataByInterfaceClusterIds(jobId, interfaceClusterIds);

        Response.ResponseBuilder responseBuilder =  Response
                .status(Response.Status.OK)
                .type(MediaType.APPLICATION_JSON)
                .entity(latticeGraph);

        return responseBuilder.build();
    }

    @GET
    @Path("/assemblyDiagram/{jobId}/{assemblyId}")
    @Produces({MediaType.APPLICATION_JSON})
    @Tag(name = "Assembly diagram by assembly id service",
            description = "provides information about the assembly diagram (2D graph).")
    @Operation(
            summary = "Get assembly diagram information by job id (either PDB id or alphanumerical user job id) and EPPIC assembly id.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = AssemblyDiagram.class))),
                    @ApiResponse(responseCode = "404", description = "Not Found")})
    public Response getAssemblyDiagram(
            @Context UriInfo uriInfo,
            @PathParam("jobId") String jobId,
            @PathParam("assemblyId") String assemblyId) throws DaoException {

        // TODO validate assemblyId is int

        AssemblyDiagram assemblyDiagram = JobService.getAssemblyDiagram(jobId, Integer.parseInt(assemblyId));

        Response.ResponseBuilder responseBuilder =  Response
                .status(Response.Status.OK)
                .type(MediaType.APPLICATION_JSON)
                .entity(assemblyDiagram);

        return responseBuilder.build();
    }
}
