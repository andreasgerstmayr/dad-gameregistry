package es.us.dad.gameregistry.server.exception

import io.netty.handler.codec.http.HttpResponseStatus

class DatabaseException extends RestException {

    public DatabaseException(String message) {
        super(message, HttpResponseStatus.INTERNAL_SERVER_ERROR)
    }

}
