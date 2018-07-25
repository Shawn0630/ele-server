package com.ele.server.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.sun.istack.internal.NotNull;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpConnection;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.impl.HttpServerResponseImpl;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;



public abstract class ApiHandler {
    protected static final ObjectMapper OM = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .registerModule(new Jdk8Module());
    private static final Logger LOG = LoggerFactory.getLogger(ApiHandler.class);
    protected final Vertx vertx;

    public ApiHandler(Vertx vertx) {
        this.vertx = vertx;
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
                return CompletableFuture.completedFuture(data);
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
                } else {
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
}

