package es.us.dad.gameregistry.server.exception

import io.netty.handler.codec.http.HttpResponseStatus

class InvalidIdException extends RestException {

    public InvalidIdException(String id) {
        super("The id: '" + id + "' is not valid.", HttpResponseStatus.BAD_REQUEST)
    }

}
