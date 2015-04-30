package es.us.dad.gameregistry.server.exception

import io.netty.handler.codec.http.HttpResponseStatus

class MethodNotAllowedException extends RestException {

    public MethodNotAllowedException() {
        super("This method is not allowed.", HttpResponseStatus.METHOD_NOT_ALLOWED)
    }

}
