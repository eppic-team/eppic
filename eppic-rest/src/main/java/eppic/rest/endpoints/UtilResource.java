package eppic.rest.endpoints;

import com.fasterxml.jackson.databind.node.ObjectNode;
import eppic.rest.service.UtilService;
import io.swagger.v3.oas.annotations.tags.Tag;
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
// TODO base path v${project.artifact.selectedVersion.majorVersion}
@RestController
@RequestMapping("/util")
public class UtilResource {

    @GetMapping(value = "/alive", produces = MediaType.TEXT_PLAIN_VALUE)
    @Tag(name = "Alive service")
    public String alive() {

        if (UtilService.isDbHealthy() && UtilService.isTempDiskHealthy()) {
            return "true";
        } else {
            return "false";
        }
    }

    @GetMapping(value = "/info", produces = MediaType.APPLICATION_JSON_VALUE)
    @Tag(name = "Info service")
    public ObjectNode getInfo() {

        return UtilService.getInfo();

    }
}
