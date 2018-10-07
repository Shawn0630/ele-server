package com.ele.server.handlers;

import akka.actor.ActorSystem;
import com.ele.server.config.SystemConfig;
import com.google.common.collect.Iterators;
import com.google.inject.Inject;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.vertx.core.Vertx;
import io.vertx.core.file.FileSystem;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.impl.FileUploadImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.File;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class FileHandler extends ApiHandler {

    private static final Logger LOG = LoggerFactory.getLogger(FileHandler.class);
    private final SystemConfig config;

    @Inject
    public FileHandler(final Vertx vertx,
                        final ActorSystem system,
                        final SystemConfig config) {
        super(vertx, system);
        this.config = config;
    }

    @Override
    public Router createSubRouter() {
        Router subRouter = Router.router(vertx);

        subRouter.get("/:filename").handler(this::getFile);
        subRouter.post("/").handler(this::upload);

        return subRouter;
    }


    private void getFile(RoutingContext context) {
        String filename = context.request().getParam("filename");
        final String uploadsDir = config.getUploadsDir();
        if (filename == null) {
            badRequest(context, "Bad Request");
            LOG.error("Bad Request from client");
            return;
        }
        sendFile(context, uploadsDir + "/" + filename);
    }

    private static boolean isMultipart(RoutingContext context) {
        return Optional.ofNullable(context.request().getHeader(HttpHeaders.CONTENT_TYPE))
                .map(String::toLowerCase)
                .map(contentType -> contentType.startsWith(HttpHeaderValues.MULTIPART_FORM_DATA.toString()))
                .orElse(false);
    }

    private void upload(RoutingContext context) {
        if (!isMultipart(context)) {
            context.fail(415);
        } else {
            final HttpServerRequest request = context.request();
            final HttpServerResponse response = context.response();

            final String uploadsDir = config.getUploadsDir();
            final Set<FileUpload> fileUploads = context.fileUploads();
            final String[] filename = new String[1];

            FileSystem fileSystem = context.vertx().fileSystem();
            if(!fileSystem.existsBlocking(uploadsDir)) {
                fileSystem.mkdirBlocking(uploadsDir);
            }
            request.setExpectMultipart(true);

            request.uploadHandler(upload -> {
                if (!upload.contentType().startsWith("image/")) {
                    error(context, "Unsupported upload type");
                    return;
                }
                String extension = upload.filename().lastIndexOf('.') < 0 ? "" : upload.filename().substring(upload.filename().lastIndexOf('.'));
                filename[0] = UUID.randomUUID().toString().concat(extension);
                String uploadFilePath = new File(uploadsDir, filename[0]).getPath();
                upload.streamToFileSystem(uploadFilePath);
                FileUploadImpl fileUpload = new FileUploadImpl(uploadFilePath, upload);
                fileUploads.add(fileUpload);
            });


            request.endHandler(v -> {
                if (response.ended()) {
                    return;
                }
                FileUpload uploaded = Iterators.getOnlyElement(context.fileUploads().iterator());
                LOG.info("File {} uploaded to Vertx as {}", uploaded.fileName(), uploaded.uploadedFileName());
                ok(context, filename[0]);
            });
        }
    }


}
