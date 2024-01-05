package org.swisspush.gateleen.core.resource;

import io.vertx.core.MultiMap;

/**
 * Represents a copy task.
 * 
 * @author https://github.com/ljucam [Mario Ljuca]
 */
public class CopyTask {
    private final String sourceUri;
    private final String destinationUri;
    private final MultiMap headers;

    public CopyTask(String sourceUri, String destinationUri, MultiMap headers) {
        this.sourceUri = sourceUri;
        this.destinationUri = destinationUri;
        this.headers = headers;
    }

    public String getSourceUri() {
        return sourceUri;
    }

    public String getDestinationUri() {
        return destinationUri;
    }

    public MultiMap getHeaders() {
        return headers;
    }

}
