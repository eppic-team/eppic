package eppic.rest.endpoints;

import com.fasterxml.jackson.databind.node.ObjectNode;
import eppic.rest.commons.AppConstants;
import eppic.rest.service.UtilService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * Utilities via REST
 *
 * @author Jose Duarte
 * @since 3.2.0
 */
@RestController
@RequestMapping(AppConstants.ENDPOINTS_COMMON_PREFIX + "v${build.project_major_version}/util")
public class UtilResource {

    private final UtilService utilService;

    @Autowired
    public UtilResource(UtilService utilService) {
        this.utilService = utilService;
    }

    @GetMapping(value = "/alive", produces = MediaType.TEXT_PLAIN_VALUE)
    @Tag(name = "Alive service")
    public String alive() {

        if (false) { //(utilService.isDbHealthy() && utilService.isTempDiskHealthy()) {
            return "true";
        } else {
            // this should achieve sending out a 500, so that we are k8s compliant
            throw new IllegalStateException("Service is not healthy");
        }
    }

    @GetMapping(value = "/info", produces = MediaType.APPLICATION_JSON_VALUE)
    @Tag(name = "Info service")
    public ObjectNode getInfo() {

        return utilService.getInfo();

    }
}
