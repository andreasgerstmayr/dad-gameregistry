package es.us.dad.gameregistry.server.exception

import io.netty.handler.codec.http.HttpResponseStatus

class ObjectNotFoundException extends RestException {

    public ObjectNotFoundException(String message) {
        super(message, HttpResponseStatus.NOT_FOUND)
    }

}
