package com.powsybl.network.store.server.exceptions;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.powsybl.network.store.model.ErrorObject;
import com.powsybl.network.store.model.TopLevelError;

public class JsonApiErrorResponseException extends RuntimeException {

    private final TopLevelError topLevelError;

    public JsonApiErrorResponseException(ErrorObject error) {
        this(ImmutableList.of(error));
    }

    public JsonApiErrorResponseException(List<ErrorObject> errors) {
        this(errors, null);
    }

    public JsonApiErrorResponseException(List<ErrorObject> errors, Map<String, String> meta) {
        this(new TopLevelError(errors, meta));
    }

    public JsonApiErrorResponseException(TopLevelError topLevelError) {
        this(topLevelError, null);
    }

    public JsonApiErrorResponseException(TopLevelError topLevelError, Throwable cause) {
        super(computeMessage(topLevelError), cause);
        this.topLevelError = topLevelError;
    }

    public TopLevelError getTopLevelError() {
        return topLevelError;
    }

    private static final String computeMessage(TopLevelError topLevelError) {
        Map<String, String> meta = topLevelError.getMeta();
        if (meta != null && meta.get(TopLevelError.META_MESSAGE) != null) {
            return meta.get(TopLevelError.META_MESSAGE);
        } else {
            return topLevelError.getErrors().stream()
                    .map(errorObject -> errorObject.getCode() + ": " + errorObject.getTitle())
                    .collect(Collectors.joining(", "));
        }
    }
}
