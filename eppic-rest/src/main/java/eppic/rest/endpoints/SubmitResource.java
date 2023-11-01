package eppic.rest.endpoints;

import eppic.rest.jobs.JobHandlerException;
import eppic.rest.service.SubmitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.annotation.security.PermitAll;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;


@Path("/submit")
public class SubmitResource {

    // note this config injection only works after construction (i.e. don't try to call it in constructor because it'll be null)
    @Context
    private Configuration config;

    public SubmitResource() {
    }

    @PermitAll
    @POST
    @Path("new")
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    @Produces({MediaType.APPLICATION_JSON})
    @Tag(name = "Submit user-provided structure",
            description = "Submit a user-provided structure to perform full EPPIC analysis on it")
    @Operation(
            summary = "Submit a user-provided structure to perform full EPPIC analysis on it. Supported coordinates formats: PDBx/mmCIF, BCIF or PDB",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "OK",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON)), // TODO document that it returns a submission id
                    @ApiResponse(responseCode = "404",
                            description = "Not Found")})
    public Response submitStructure(
            @NotNull @FormDataParam("fileName") String fileName,
            @NotNull @FormDataParam("file") InputStream fileInputStream,
            @FormDataParam("email")String email) throws JobHandlerException, IOException {

        SubmitService submitService = new SubmitService(config.getProperties());
        return submitService.submit(fileName, fileInputStream, email);
    }

    @PermitAll
    @GET
    @Path("status/{submissionId}")
    @Produces({MediaType.APPLICATION_JSON})
    @Tag(name = "Get status of submission")
    public Response getStatus(@PathParam("submissionId") String submissionId) throws JobHandlerException {
        SubmitService submitService = new SubmitService(config.getProperties());
        return submitService.getStatus(submissionId);
    }
}
