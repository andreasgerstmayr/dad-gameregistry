package es.us.dad.gameregistry.server.controller

import es.us.dad.gameregistry.server.domain.GameSession
import es.us.dad.gameregistry.server.service.LoginService
import es.us.dad.gameregistry.server.service.SessionService
import es.us.dad.gameregistry.server.util.DELETE
import es.us.dad.gameregistry.server.util.GET
import es.us.dad.gameregistry.server.util.POST
import es.us.dad.gameregistry.server.util.PUT

import io.netty.handler.codec.http.HttpResponseStatus
import org.vertx.groovy.core.http.HttpServerRequest

class SessionsController extends RestController {

    // TODO: dependency injection
    private final SessionService sessionService

    public SessionsController(LoginService loginService, SessionService sessionService) {
        super(loginService)
        this.sessionService = sessionService
    }

    @GET("/sessions")
    public void getSessions(HttpServerRequest request) {
        // TODO: implement this method
        sendJsonResponse(request, HttpResponseStatus.NOT_IMPLEMENTED)
    }

    @POST("/sessions")
    public void createSession(HttpServerRequest request) {
        if (!validateUserAuthentication(request)) {
            return
        }

        GameSession newSession = sessionService.startSession()
        sendJsonResponse(request, newSession, HttpResponseStatus.CREATED)
    }

    @PUT("/sessions")
    public void changeSessions(HttpServerRequest request) {
        sendJsonResponse(request, HttpResponseStatus.METHOD_NOT_ALLOWED)
    }

    @DELETE("/sessions")
    public void deleteSessions(HttpServerRequest request) {
        sendJsonResponse(request, HttpResponseStatus.METHOD_NOT_ALLOWED)
    }

}
