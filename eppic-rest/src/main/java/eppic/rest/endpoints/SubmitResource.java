package eppic.rest.endpoints;

import eppic.rest.commons.FileFormat;
import eppic.rest.service.SubmitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.annotation.security.PermitAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;


@Path("/submit")
public class SubmitResource {

    private final SubmitService submitService;

    public SubmitResource() {
        submitService = new SubmitService();
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
            @FormDataParam("fileFormat") FileFormat fileFormat,
            @FormDataParam("fileName") String fileName,
            @FormDataParam("file") InputStream fileInputStream,
            @FormDataParam("email")String email) {

        return submitService.submit(fileFormat, fileName, fileInputStream, email);
    }

    @PermitAll
    @GET
    @Path("status/{submissionId}")
    @Produces({MediaType.APPLICATION_JSON})
    @Tag(name = "Get status of submission")
    public Response getStatus(@PathParam("submissionId") String submissionId) {
        return submitService.getStatus(submissionId);
    }
}
