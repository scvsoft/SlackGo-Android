package com.scv.slackgo.exceptions;

/**
 * Created by scvsoft on 11/16/16.
 */

public class GeocoderException extends RuntimeException {
    private String errorMessage;

    public GeocoderException(String detailMessage) {
        super(detailMessage);
        this.errorMessage = detailMessage;
    }

    @Override
    public String getMessage() {
        return errorMessage;
    }
}
