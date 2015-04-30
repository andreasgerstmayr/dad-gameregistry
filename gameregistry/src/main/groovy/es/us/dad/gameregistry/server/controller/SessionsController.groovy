package es.us.dad.gameregistry.server.controller

import es.us.dad.gameregistry.server.domain.GameSession
import es.us.dad.gameregistry.server.service.ILoginService
import es.us.dad.gameregistry.server.service.LoginServiceMock
import es.us.dad.gameregistry.server.service.SessionService
import es.us.dad.gameregistry.server.util.Authenticated
import es.us.dad.gameregistry.server.util.DELETE
import es.us.dad.gameregistry.server.util.GET
import es.us.dad.gameregistry.server.util.POST
import es.us.dad.gameregistry.server.util.PUT

import io.netty.handler.codec.http.HttpResponseStatus
import org.vertx.groovy.core.http.HttpServerRequest

class SessionsController extends Controller {

    // TODO: dependency injection
    private final SessionService sessionService

    public SessionsController(ILoginService loginService, SessionService sessionService) {
        super(loginService)
        this.sessionService = sessionService
    }

    @GET("/sessions")
    public void getSessions(HttpServerRequest request) {
        // TODO: implement this method
        sendJsonResponse(request, HttpResponseStatus.NOT_IMPLEMENTED)
    }

    @Authenticated
    @POST("/sessions")
    public void createSession(HttpServerRequest request) {
        sessionService.startSession({ GameSession newSession ->
            if (newSession != null)
                sendJsonResponse(request, newSession, HttpResponseStatus.CREATED)
            else
                sendJsonResponse(request, newSession, HttpResponseStatus.INTERNAL_SERVER_ERROR)
        })
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
