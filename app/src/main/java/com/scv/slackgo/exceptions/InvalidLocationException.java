package com.scv.slackgo.exceptions;

/**
 * Created by scvsoft on 11/15/16.
 */

public class InvalidLocationException extends RuntimeException {
    private String errorMessage;

    public InvalidLocationException(String detailMessage) {
        super(detailMessage);
        this.errorMessage = detailMessage;
    }

    @Override
    public String getMessage() {
        return errorMessage;
    }
}
