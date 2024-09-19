package eppic.rest.endpoints;

import eppic.model.dto.SubmissionStatus;
import eppic.model.dto.UserJobSubmission;
import eppic.rest.commons.AppConstants;
import eppic.rest.jobs.JobHandlerException;
import eppic.rest.service.SubmitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.PermitAll;
import java.io.IOException;

@RestController
@RequestMapping(AppConstants.ENDPOINTS_COMMON_PREFIX + "v${build.project_major_version}/submit")
@CrossOrigin
public class SubmitResource {

    private static final Logger logger = LoggerFactory.getLogger(SubmitResource.class);

    private final SubmitService submitService;

    @Autowired
    public SubmitResource(SubmitService submitService) {
        this.submitService = submitService;
    }

    @PermitAll
    @PostMapping(value = "new", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
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
            @RequestBody UserJobSubmission userJobSubmission) throws JobHandlerException, IOException {
        logger.info("Got structure upload request - length: {}, email: {}, file_name: {}, skip_entropy: {}",
                userJobSubmission.getData().length(),
                userJobSubmission.getEmail(),
                userJobSubmission.getFileName(),
                userJobSubmission.isSkipEvolAnalysis());
        return submitService.submit(userJobSubmission);
    }

    @PermitAll
    @GetMapping(value = "status/{submissionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Tag(name = "Get status of submission")
    public SubmissionStatus getStatus(@PathVariable("submissionId") String submissionId) throws JobHandlerException {
        return submitService.getStatus(submissionId);
    }
}
