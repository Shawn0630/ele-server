package com.ele.server.handlers.helpers;

import com.google.common.util.concurrent.RateLimiter;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

public class VertxChunkedOutputStream extends OutputStream {
    private static final Logger LOG = LoggerFactory.getLogger(VertxChunkedOutputStream.class);

    private final HttpServerResponse response;
    private boolean isClosed;
    private RateLimiter bufferFullLogRateLimiter = RateLimiter.create(2);

    public VertxChunkedOutputStream(HttpServerResponse response) {
        response.exceptionHandler(responseException -> {
            LOG.error("Response exception. Closing Stream.", responseException);
            isClosed = true;
        });
        response.endHandler(end -> {
            LOG.info("Request End. Closing Stream.");
            isClosed = true;
        });
        response.closeHandler(close -> {
            LOG.info("Request Close. Closing Stream.");
            isClosed = true;
        });
        response.setWriteQueueMaxSize(2 << 24);
        this.response = response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(int b) throws IOException {
        checkState();
        Buffer buffer = Buffer.buffer();
        buffer.appendByte((byte) b);
        response.write(buffer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(byte[] b) throws IOException {
        checkState();
        response.write(Buffer.buffer(b));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        checkState();
        Buffer buffer = Buffer.buffer();
        if (off == 0 && len == b.length) {
            buffer.appendBytes(b);
        } else {
            buffer.appendBytes(Arrays.copyOfRange(b, off, off + len));
        }
        response.write(buffer);
    }

    @Override
    public void close() throws IOException {
        isClosed = true;
    }

    void checkState() {
        if (isClosed) {
            throw new RuntimeException("Stream is closed");
        }
        while (response.writeQueueFull()) {
            if (response.closed()) {
                isClosed = true;
                throw new RuntimeException("Response is closed");
            }
            try {
                if (bufferFullLogRateLimiter.tryAcquire()) {
                    LOG.info("Write buffer full.");
                }
                Thread.sleep(10);
            } catch (InterruptedException e) {
                // Simply continue.
            }
            if (isClosed) {
                throw new RuntimeException("Stream is closed");
            }
        }
    }
}
