package eppic.rest.endpoints;

import eppic.db.dao.DaoException;
import eppic.model.db.AssemblyDB;
import eppic.model.db.ChainClusterDB;
import eppic.model.db.ContactDB;
import eppic.model.db.InterfaceClusterDB;
import eppic.model.db.InterfaceDB;
import eppic.model.db.PdbInfoDB;
import eppic.model.dto.views.AssemblyDiagram;
import eppic.model.dto.views.LatticeGraph;
import eppic.model.dto.views.Residue;
import eppic.rest.commons.Utils;
import eppic.rest.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.Produces;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

@RestController
@RequestMapping("/job")
@CrossOrigin
public class JobResource {

    private static final Logger logger = LoggerFactory.getLogger(JobResource.class);

    @Context
    private Configuration config;

    private final JobService jobService;

    @Autowired
    public JobResource(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping(value = "/pdb/{jobId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Tag(name = "PDB info service",
            description = "provides general information about PDB structures.")
    @Operation(
            summary = "Get PDB structure description by job id (either PDB id or alphanumerical user job id).",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = PdbInfoDB.class))),
                    @ApiResponse(responseCode = "404", description = "Not Found")})
    public PdbInfoDB getPdb(
            @PathVariable("jobId") String jobId) throws DaoException {
        
        PdbInfoDB pdbInfo = jobService.getResultData(jobId, false, false, false, false);

        return pdbInfo;
    }

    @GetMapping(value = "/interfaceClusters/{jobId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Tag(name = "Interface cluster service",
            description = "provides information about an interface cluster (interface type or unique binding mode).")
    @Operation(
            summary = "Get interface cluster information by job id (either PDB id or alphanumerical user job id).")
    public List<InterfaceClusterDB> getInterfaceClusters(
            @PathVariable("jobId") String jobId) throws DaoException {

        List<InterfaceClusterDB> ics = jobService.getInterfaceClusterData(jobId);

        return ics;
    }
    
    @GetMapping(value = "/interfaces/{jobId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Tag(name = "Interface service",
            description = "provides information about an interface.")
    @Operation(
            summary = "Get interface information by job id (either PDB id or alphanumerical user job id).")
    public List<InterfaceDB> getInterfaces(
            @PathVariable("jobId") String jobId) throws DaoException {
        
        List<InterfaceDB> ics = jobService.getInterfaceData(jobId);
        
        return ics;
    }
    
    @GetMapping(value = "/sequences/{jobId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Tag(name = "Sequences service",
            description = "provides sequence information (alignment to reference UniProt and sequence homologs) for all molecular entities of a structure.")
    @Operation(
            summary = "Get sequence information by job id (either PDB id or alphanumerical user job id).")
    public List<ChainClusterDB> getSequences(
            @PathVariable("jobId") String jobId) throws DaoException {
        
        List<ChainClusterDB> ics = jobService.getSequenceData(jobId);
        return ics;
    }
    
    @GetMapping(value = "/interfaceResidues/{jobId}/{interfId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Tag(name = "Interface residues service",
            description = "provides information about interface residues.")
    @Operation(
            summary = "Get interface residues information by job id (either PDB id or alphanumerical user job id) and EPPIC interface id.")
    public List<Residue> getInterfaceResidues(
            @PathVariable("jobId") String jobId,
            @PathVariable("interfId") String interfId) throws DaoException {

        // TODO validate interfId is int

        List<Residue> ics = jobService.getResidueData(jobId, Integer.parseInt(interfId));

        return ics;
    }
    
    @GetMapping(value = "/assemblies/{jobId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Tag(name = "Assemblies service",
            description = "provides information about all assemblies of a structure.")
    @Operation(
            summary = "Get assemblies information by job id (either PDB id or alphanumerical user job id).")
    public List<AssemblyDB> getAssemblies(
            @PathVariable("jobId") String jobId) throws DaoException {
        
        List<AssemblyDB> assemblies = jobService.getAssemblyDataByPdbAssemblyId(jobId);

        return assemblies;
    }
    
    @GetMapping(value = "/contacts/{jobId}/{interfId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Tag(name = "Contacts service",
            description = "provides information about contacts across an interface.")
    @Operation(
            summary = "Get interface contacts information by job id (either PDB id or alphanumerical user job id) and EPPIC interface id.")
    public List<ContactDB> getContacts(
            @PathVariable("jobId") String jobId,
            @PathVariable("interfId") String interfId) throws DaoException {

        // TODO validate interfId is int

        List<ContactDB> cs = jobService.getContactData(jobId, Integer.parseInt(interfId));

        return cs;
    }
    
    @GetMapping(value = "/assemblyByPdbId/{pdbId}/{pdbAssemblyId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Tag(name = "Assembly by PDB id service",
            description = "provides information about an assembly.")
    @Operation(
            summary = "Get EPPIC assembly information by PDB id and PDB assembly id.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = AssemblyDB.class))),
                    @ApiResponse(responseCode = "404", description = "Not Found")})
    public AssemblyDB getAssemblyByPdbId(
            @PathVariable("pdbId") String jobId,
            @PathVariable("pdbAssemblyId") String pdbAssemblyId) throws DaoException {

        // TODO validate pdbAssemblyId is int

        AssemblyDB assembly = jobService.getAssemblyDataByPdbAssemblyId(jobId, Integer.parseInt(pdbAssemblyId));
        
        return assembly;
    }

    @GetMapping(value = "/assembly/{jobId}/{assemblyId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Tag(name = "Assembly service",
            description = "provides information about an assembly.")
    @Operation(
            summary = "Get assembly information by job id (either PDB id or alphanumerical user job id) and EPPIC assembly id.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = AssemblyDB.class))),
                    @ApiResponse(responseCode = "404", description = "Not Found")})
    public AssemblyDB getAssembly(
            @PathVariable("jobId") String jobId,
            @PathVariable("assemblyId") String assemblyId) throws DaoException {

        // TODO validate assemblyId is int

        AssemblyDB assembly = jobService.getAssemblyData(jobId, Integer.parseInt(assemblyId));
        
        return assembly;
    }

    @GetMapping(value = "/latticeGraph/{jobId}/{assemblyId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Tag(name = "Lattice graph by assembly id service",
            description = "provides information about the lattice graph.")
    @Operation(
            summary = "Get lattice graph by job id (either PDB id or alphanumerical user job id) and EPPIC assembly id.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = LatticeGraph.class))),
                    @ApiResponse(responseCode = "404", description = "Not Found")})
    public LatticeGraph getLatticeGraph(
            @PathVariable("jobId") String jobId,
            @PathVariable("assemblyId") String assemblyId) throws DaoException {

        // TODO validate assemblyId is int

        LatticeGraph latticeGraph = jobService.getLatticeGraphData(jobId, Integer.parseInt(assemblyId));

        return latticeGraph;
    }

    @GetMapping(value = "/latticeGraphByInterfaceIds/{jobId}/{interfaceIds}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Tag(name = "Lattice graph by interface ids service",
            description = "provides information about the lattice graph.")
    @Operation(
            summary = "Get lattice graph by job id (either PDB id or alphanumerical user job id) and EPPIC interface ids.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = LatticeGraph.class))),
                    @ApiResponse(responseCode = "404", description = "Not Found")})
    public LatticeGraph getLatticeGraphByInterfaceIdList(
            @PathVariable("jobId") String jobId,
            @PathVariable("interfaceIds") String interfaceIdString) throws DaoException {

        // TODO convert interfaceIdsString to list
        SortedSet<Integer> interfaceIds = Utils.parseIdsString(interfaceIdString);
        LatticeGraph latticeGraph = jobService.getLatticeGraphDataByInterfaceIds(jobId, interfaceIds);

        return latticeGraph;
    }

    @GetMapping(value = "/latticeGraphByInterfaceClusterIds/{jobId}/{interfaceClusterIds}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Tag(name = "Lattice graph by interface cluster ids service",
            description = "provides information about the lattice graph.")
    @Operation(
            summary = "Get lattice graph by job id (either PDB id or alphanumerical user job id) and EPPIC interface cluster ids.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = LatticeGraph.class))),
                    @ApiResponse(responseCode = "404", description = "Not Found")})
    public LatticeGraph getLatticeGraphByInterfaceClusterIdList(
            @PathVariable("jobId") String jobId,
            @PathVariable("interfaceClusterIds") String interfaceClusterIdString) throws DaoException {

        // TODO convert interfaceIdsString to list
        SortedSet<Integer> interfaceClusterIds = Utils.parseIdsString(interfaceClusterIdString);
        LatticeGraph latticeGraph = jobService.getLatticeGraphDataByInterfaceClusterIds(jobId, interfaceClusterIds);

        return latticeGraph;
    }

    @GetMapping(value = "/assemblyDiagram/{jobId}/{assemblyId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Tag(name = "Assembly diagram by assembly id service",
            description = "provides information about the assembly diagram (2D graph).")
    @Operation(
            summary = "Get assembly diagram information by job id (either PDB id or alphanumerical user job id) and EPPIC assembly id.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = AssemblyDiagram.class))),
                    @ApiResponse(responseCode = "404", description = "Not Found")})
    public AssemblyDiagram getAssemblyDiagram(
            @PathVariable("jobId") String jobId,
            @PathVariable("assemblyId") String assemblyId) throws DaoException {

        // TODO validate assemblyId is int

        AssemblyDiagram assemblyDiagram = jobService.getAssemblyDiagram(jobId, Integer.parseInt(assemblyId));

        return assemblyDiagram;
    }

    @GetMapping(value = "/interfaceCifFile/{jobId}/{interfId}", produces = "chemical/x-cif")
    @Tag(name = "Interface coordinate file service", description = "Returns the CIF format coordinates of the interface")
    public ResponseEntity<?> getInterfaceCoordinateFile(
            @PathVariable("jobId") String jobId,
            @PathVariable("interfId") String interfId) throws DaoException, IOException {

        String outputFileName = jobId + ".interface." + interfId + ".cif";
        ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                .filename(outputFileName)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(contentDisposition);

        byte[] os = jobService.getCoordinateFile(jobId, interfId, null, config.getProperties());
        return ResponseEntity.status(HttpStatus.OK).headers(headers).body(os);
    }

    @GetMapping(value = "/assemblyCifFile/{jobId}/{assemblyId}", produces = "chemical/x-cif")
    @Tag(name = "Assembly coordinate file service", description = "Returns the CIF format coordinates of the assembly")
    public ResponseEntity<?> getAssemblyCoordinateFile(
            @PathVariable("jobId") String jobId,
            @PathVariable("assemblyId") String assemblyId) throws DaoException, IOException {

        String outputFileName = jobId + ".assembly." + assemblyId + ".cif";
        ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                .filename(outputFileName)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(contentDisposition);

        byte[] os = jobService.getCoordinateFile(jobId, null, assemblyId, config.getProperties());
        return ResponseEntity.status(HttpStatus.OK).headers(headers).body(os);;
    }

    @GetMapping(value = "/msaFastaFile/{jobId}/{repChainId}", produces = MediaType.TEXT_PLAIN_VALUE)
    @Tag(name = "Alignment file service",
            description = "returns the Multiple Sequence Alignment used to find evolutionary scores in FASTA format.")
    public String getAlignmentFastaFile(
            @PathVariable("jobId") String jobId,
            @PathVariable("repChainId") String repChainId) throws DaoException, IOException {
        Map<String, String> sequences = jobService.getAlignment(jobId, repChainId);
        return jobService.serializeToFasta(sequences);
    }

    @GetMapping(value = "/image/{jobId}/{type}/{id}", produces = MediaType.IMAGE_PNG_VALUE)
    @Produces("image/png")
    public byte[] getFullImage(
            @PathVariable("jobId") String jobId,
            @PathVariable("type") String type,
            @PathVariable("id") String id) throws IOException {

        byte[] os = jobService.getImageFile(jobId, type, id, config.getProperties());
        return os;
    }
}
