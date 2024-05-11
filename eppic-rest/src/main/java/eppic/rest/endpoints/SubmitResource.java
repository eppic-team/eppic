package eppic.rest.endpoints;

import eppic.model.dto.SubmissionStatus;
import eppic.rest.jobs.JobHandlerException;
import eppic.rest.service.SubmitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.PermitAll;
import java.io.IOException;
import java.io.InputStream;

// TODO base path v${project.artifact.selectedVersion.majorVersion}
@RestController
@RequestMapping("/submit")
public class SubmitResource {

    private final SubmitService submitService;

    @Autowired
    public SubmitResource(SubmitService submitService) {
        this.submitService = submitService;
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
    public SubmissionStatus submitStructure(
            @RequestParam("fileName") String fileName,
            @RequestParam("file") InputStream fileInputStream,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "skipEvolAnalysis", defaultValue = "false", required = false) boolean skipEvolAnalysis) throws JobHandlerException, IOException {

        return submitService.submit(fileName, fileInputStream, email, skipEvolAnalysis);
    }

    @PermitAll
    @GetMapping(value = "status/{submissionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Tag(name = "Get status of submission")
    public SubmissionStatus getStatus(@PathVariable("submissionId") String submissionId) throws JobHandlerException {
        return submitService.getStatus(submissionId);
    }
}
