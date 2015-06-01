package es.us.dad.gameregistry.server.exception

import io.netty.handler.codec.http.HttpResponseStatus

class ForbiddenException extends RestException {

    public ForbiddenException(String message) {
        super(message, HttpResponseStatus.FORBIDDEN)
    }

}
