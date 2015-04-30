package es.us.dad.gameregistry.server.exception

import io.netty.handler.codec.http.HttpResponseStatus

class AuthenticationException extends RestException {

    public AuthenticationException() {
        super("Invalid user or token", HttpResponseStatus.FORBIDDEN)
    }

}
