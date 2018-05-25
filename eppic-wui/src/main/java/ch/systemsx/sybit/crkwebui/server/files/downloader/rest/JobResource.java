package ch.systemsx.sybit.crkwebui.server.files.downloader.rest;

import ch.systemsx.sybit.crkwebui.server.files.downloader.servlets.DataDownloadServlet;
import ch.systemsx.sybit.crkwebui.shared.exceptions.DaoException;
import ch.systemsx.sybit.crkwebui.shared.model.PdbInfo;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/job")
public class JobResource {

    @GET
    @Path("/pdb/" + "{jobId}")
    @Produces({MediaType.APPLICATION_JSON + ";charset=utf-8", MediaType.APPLICATION_XML + ";charset=utf-8"})
    public Response getPdb(
            @Context UriInfo uriInfo,
            @PathParam("jobId") String jobId) throws DaoException {


        String mediaType = MediaType.APPLICATION_JSON;
        String format = uriInfo.getQueryParameters().getFirst("format");
        if (format!=null && format.equalsIgnoreCase("xml")) {
            mediaType = MediaType.APPLICATION_XML;
        }
        PdbInfo pdbInfo = DataDownloadServlet.getResultData(jobId, null, null, null, false, false);
        Response.ResponseBuilder responseBuilder =  Response
                .status(Response.Status.OK)
                .type(mediaType)
                .entity(pdbInfo);
        return responseBuilder.build();
    }
}
