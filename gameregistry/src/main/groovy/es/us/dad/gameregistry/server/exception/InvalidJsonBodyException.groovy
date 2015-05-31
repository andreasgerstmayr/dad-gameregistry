package es.us.dad.gameregistry.server.exception

import io.netty.handler.codec.http.HttpResponseStatus

class InvalidJsonBodyException extends RestException {

    public InvalidJsonBodyException() {
        super("The supplied request body is not valid JSON.", HttpResponseStatus.BAD_REQUEST)
    }

}
