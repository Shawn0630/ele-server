package com.ele.server.handlers;

import akka.Done;
import akka.NotUsed;
import akka.actor.Actor;
import akka.actor.ActorSystem;
import akka.japi.function.Procedure;
import akka.japi.function.Procedure2;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.OverflowStrategy;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import com.ele.model.DataModel;
import com.ele.server.handlers.helpers.VertxChunkedOutputStream;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.common.io.ByteStreams;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpConnection;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.impl.HttpServerResponseImpl;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.concurrent.CompletableFuture.completedFuture;


public abstract class ApiHandler {
    protected static final ObjectMapper OM = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .registerModule(new Jdk8Module());
    private static final Logger LOG = LoggerFactory.getLogger(ApiHandler.class);
    protected final Vertx vertx;
    protected final ActorSystem system;

    public ApiHandler(Vertx vertx, ActorSystem system) {
        this.vertx = vertx;
        this.system = system;
    }

    public static void failChunkedResponse(HttpServerResponse response) {
        response.write("ERROR");
        HttpConnection conn;
        try {
            HttpServerResponseImpl responseImpl = (HttpServerResponseImpl) response;
            Field f = responseImpl.getClass().getDeclaredField("conn");
            f.setAccessible(true);
            conn = (HttpConnection) f.get(responseImpl);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        conn.close();
    }

    public abstract Router createSubRouter();

    @NotNull
    protected static <T> Function<T, CompletionStage<T>> futureErrorIf(Predicate<T> predicate, int errorCode, String message) {
        return (T data) -> {
            if (predicate.test(data)) {
                return futureError(errorCode, message);
            } else {
                return completedFuture(data);
            }
        };
    }

    protected static <T> CompletionStage<T> completeExceptionaly(Throwable throwable) {
        CompletableFuture<T> future = new CompletableFuture<>();
        future.completeExceptionally(throwable);
        return future;
    }

    @NotNull
    protected static <T> CompletionStage<T> futureError(int errorCode, String message) {
        return completeExceptionaly(new HandlerException(errorCode, message));
    }

    @NotNull
    protected static <T> Function<T, CompletionStage<T>> futureErrorIfNull(int errorCode, String message) {
        return futureErrorIf(Objects::isNull, errorCode, message);
    }

    protected final void ok(RoutingContext context) {
        context.response().setStatusCode(204).end();
    }

    protected final void ok(RoutingContext context, Object obj) {
        if (obj == null) {
            notFound(context);
        } else {
            String json;
            try {
                if (obj instanceof String) {
                    json = (String) obj;
                } else if (obj instanceof DataModel) {
                    json = ((DataModel) obj).toJSON();
                } else if (obj instanceof GeneratedMessageV3) {
                    json = toJson((GeneratedMessageV3) obj);
                }  else {
                    json = OM.writeValueAsString(obj);
                }
            } catch (JsonProcessingException e) {
                LOG.error("Unable to JSON serialize object", e);
                json = obj.toString();
            }
            context.response()
                    .setStatusCode(200)
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(json);
        }
    }

    protected final <T extends GeneratedMessageV3> CompletionStage<Done> serializationProcedure(
            RoutingContext context,
            Source<T, NotUsed> source
    ) {
        final Materializer materializer = ActorMaterializer.create(system);
        for (String accept : context.request().headers().getAll("Accept")) {
            if (accept.contains("protobuf")) {
                return source.runWith(toProtoSink("", context.response()), materializer);
            } else if (accept.contains("json")) {
                return source.runWith(toJsonSink("", context.response()), materializer);
            }
        }
        return source.runWith(toJsonSink("", context.response()), materializer);
    }

    /**
     * This is used for response of HTTP creation
     * @param context  associated routing context
     * @param content  content to send back to client
     * @param location location where the newly created object can be accessed
     */
    protected final void ok(RoutingContext context, String content, String location) {
        context.response()
                .setStatusCode(201)
                .putHeader("location", location)
                .end(content);
    }

    protected final void badRequest(RoutingContext context, String error) {
        context.response().setStatusCode(400).setStatusMessage(error).end();
    }

    protected final void notFound(RoutingContext context) {
        context.response().setStatusCode(404).setStatusMessage("Required data cannot be found").end();
    }

    protected final void unauthorized(RoutingContext context) {
        context.response().setStatusCode(401).end();
    }
    
    protected final void error(RoutingContext context, String message, Throwable t) {
        HandlerException he = new HandlerException(500, message, t);
        context.fail(he);
    }

    protected final void error(RoutingContext context, String message) {
        HandlerException he = new HandlerException(500, message);
        context.fail(he);
    }

    protected final void getId(RoutingContext context, String param, Consumer<UUID> uuidConsumer) {
        try {
            uuidConsumer.accept(UUID.fromString(context.request().getParam(param)));
        } catch (IllegalArgumentException e) {
            badRequest(context, "Unable to parse id for " + param);
        }
    }

    private final <T extends GeneratedMessageV3> String toJson(T obj) {
        final JsonFormat.Printer printer = JsonFormat.printer().omittingInsignificantWhitespace();
        try {
            return printer.print(obj);
        } catch (InvalidProtocolBufferException e) {
            LOG.error("Error converting to JSON", e);
            return "\"Error converting to JSON. See server log.\"";
        }
    }

    private final <T extends GeneratedMessageV3> Sink<T, CompletionStage<Done>> toJsonSink(
            String name,
            HttpServerResponse response
    ) {
        return Flow.<T>create()
                .async()
                .map(x -> {
                    return toJson(x);
                })
                .buffer(256, OverflowStrategy.backpressure())
                .intersperse("[", ",", "]")
                .toMat(responseSink(
                        response,
                        r -> {
                            r.putHeader("Content-Disposition", "attachment; filename=\"" + name + ".json\"");
                        }, null,
                        (output, value) -> output.write(value.getBytes())
                ), Keep.right());
    }

    private final <T extends GeneratedMessageV3> Sink<T, CompletionStage<Done>> toProtoSink(
            String name,
            HttpServerResponse response
    ) {
        return Flow.<T>create()
                .async()
                .buffer(256, OverflowStrategy.backpressure())
                .toMat(responseSink(
                        response,
                        r -> {
                            r.putHeader("Content-Disposition", "attachment; filename=\"" + name + ".protobuf\"");
                            r.putHeader("Content-Type", "application/x-protobuf");
                        }, null,
                        (output, proto) -> proto.writeDelimitedTo(output)
                ), Keep.right());
    }

    private static <T> Sink<T, CompletionStage<Done>> responseSink(
            HttpServerResponse response,
            Procedure<HttpServerResponse> setHeaders,
            Procedure<OutputStream> writeFirst,
            Procedure2<OutputStream, T> write
    ) {
        return Sink.<T, CompletionStage<Done>>lazyInit(
                first -> {
                    setHeaders.apply(response);
                    response.setChunked(true);
                    final OutputStream output = new BufferedOutputStream(new VertxChunkedOutputStream(response));
                    if (writeFirst != null) {
                        writeFirst.apply(output);
                    }

                    return completedFuture(Sink.<T>foreach(value -> {
                        if (response.closed()) {
                            throw new IOException("Response closed permaturely");
                        }
                        write.apply(output, value);
                    }).mapMaterializedValue(futureDone -> futureDone.whenComplete((done, throwable) -> {
                        try {
                            output.flush();
                            output.close();
                        } catch (IOException e) {
                            LOG.error("Exception during close of output stream", e);
                        }
                        response.end();
                    })));
                },
                () -> {
                    if (writeFirst != null) {
                        setHeaders.apply(response);
                        response.setChunked(true);
                        final OutputStream output = new BufferedOutputStream(new VertxChunkedOutputStream(response));
                        writeFirst.apply(output);
                        try {
                            output.flush();
                            output.close();
                        } catch (IOException e) {
                            LOG.error("Exception during close of output stream", e);
                        }
                        response.end();
                    } else {
                        response.setStatusCode(204).end();
                    }
                    return completedFuture(Done.getInstance());
                }
        ).mapMaterializedValue(futureDone -> futureDone.thenCompose(x -> x));
    }

    protected void sendFile(RoutingContext context, String file) {
        HttpServerResponse response = context.response();
        response.putHeader("Content-Disposition", "attachment; file=\"" + file + "\"");
        response.putHeader("Content-Type", "application/octet-stream");
        response.setChunked(true);
        OutputStream out = new VertxChunkedOutputStream(response);
        InputStream in = getClass().getClassLoader().getResourceAsStream(file);
        if (in == null) {
            LOG.error("Unable to download file");
            response.setStatusCode(500).setStatusMessage("Unable to download file").end();
            return;
        } else {
            try {
                ByteStreams.copy(in, out);
            } catch (IOException e) {
                LOG.error("Error in sending file", e);
            }
            response.end();
        }
    }

}

