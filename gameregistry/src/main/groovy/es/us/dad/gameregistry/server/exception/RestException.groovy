package es.us.dad.gameregistry.server.exception

import io.netty.handler.codec.http.HttpResponseStatus

class RestException extends Exception {

    private final HttpResponseStatus responseStatus

    public RestException(String message) {
        super(message)
        this.responseStatus = HttpResponseStatus.INTERNAL_SERVER_ERROR
    }

    public RestException(String message, HttpResponseStatus responseStatus) {
        super(message)
        this.responseStatus = responseStatus
    }

    public HttpResponseStatus getResponseStatus() {
        return responseStatus
    }

}
