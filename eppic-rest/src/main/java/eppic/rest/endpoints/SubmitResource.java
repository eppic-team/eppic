package eppic.rest.endpoints;

import eppic.rest.jobs.JobHandlerException;
import eppic.rest.service.SubmitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.PermitAll;
import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/submit")
public class SubmitResource {

    // note this config injection only works after construction (i.e. don't try to call it in constructor because it'll be null)
    @Context
    private Configuration config;

    public SubmitResource() {
    }

    @PermitAll
    @PostMapping(value = "new", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Tag(name = "Submit user-provided structure",
            description = "Submit a user-provided structure to perform full EPPIC analysis on it")
    @Operation(
            summary = "Submit a user-provided structure to perform full EPPIC analysis on it. Supported coordinates formats: PDBx/mmCIF, BCIF or PDB",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "OK",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)), // TODO document that it returns a submission id
                    @ApiResponse(responseCode = "404",
                            description = "Not Found")})
    public Response submitStructure(
            @NotNull @RequestParam("fileName") String fileName,
            @NotNull @RequestParam("file") InputStream fileInputStream,
            @RequestParam("email") String email,
            @RequestParam("skipEvolAnalysis") @DefaultValue("false") boolean skipEvolAnalysis) throws JobHandlerException, IOException {

        SubmitService submitService = new SubmitService(config.getProperties());
        return submitService.submit(fileName, fileInputStream, email, skipEvolAnalysis);
    }

    @PermitAll
    @GetMapping(value = "status/{submissionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Tag(name = "Get status of submission")
    public Response getStatus(@PathVariable("submissionId") String submissionId) throws JobHandlerException {
        SubmitService submitService = new SubmitService(config.getProperties());
        return submitService.getStatus(submissionId);
    }
}
