package com.kaplat.bookserver;

public class ResponseObject<T> {
    private T result;
    private String errorMessage;

    public ResponseObject() {}

    public ResponseObject(T result, String errorMessage) {
        this.result = result;
        this.errorMessage = errorMessage;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
