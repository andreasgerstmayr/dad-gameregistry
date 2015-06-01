package es.us.dad.gameregistry.server.exception

import io.netty.handler.codec.http.HttpResponseStatus

class BadRequestException extends RestException {

    public BadRequestException(String message) {
        super(message, HttpResponseStatus.BAD_REQUEST)
    }

}
