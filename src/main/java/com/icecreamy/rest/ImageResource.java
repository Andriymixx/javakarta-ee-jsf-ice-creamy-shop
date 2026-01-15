package com.icecreamy.rest;

import com.icecreamy.util.AppConstants;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import java.io.File;
import java.nio.file.Files;

@Path("/admin/images")
public class ImageResource {

    @GET
    @Path("/{filename}")
    @PermitAll
    public Response getImage(@PathParam("filename") String filename) {
        File file = new File(AppConstants.UPLOAD_FILE_PATH, filename);

        if (!file.exists()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        try {
            String contentType = Files.probeContentType(file.toPath());
            if (contentType == null) {
                contentType = filename.toLowerCase().endsWith(".png") ? "image/png" : "image/jpeg";
            }

            return Response.ok(file).type(contentType).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().build();
        }
    }
}