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

class SessionController extends RestController {

    // TODO: dependency injection
    private final SessionService sessionService

    public SessionController(LoginService loginService, SessionService sessionService) {
        super(loginService)
        this.sessionService = sessionService
    }

    @GET("/sessions/:id")
    public void getSession(HttpServerRequest request) {
        if (!validateUserAuthentication(request)) {
            return
        }

        UUID id = UUID.fromString(request.params.get("id"))
        GameSession session = sessionService.getSession(id)

        if (session != null)
            sendJsonResponse(request, session)
        else
            sendJsonResponse(request, HttpResponseStatus.NOT_FOUND)
    }

    @POST("/sessions/:id")
    public void postSession(HttpServerRequest request) {
        sendJsonResponse(request, null, HttpResponseStatus.METHOD_NOT_ALLOWED)
    }

    @PUT("/sessions/:id")
    public void changeSession(HttpServerRequest request) {
        if (!validateUserAuthentication(request)) {
            return
        }

        UUID id = UUID.fromString(request.params.get("id"))
        GameSession session = sessionService.finishSession(id)

        if (session != null)
            sendJsonResponse(request, session)
        else
            sendJsonResponse(request, HttpResponseStatus.NOT_FOUND)
    }

    @DELETE("/sessions/:id")
    public void deleteSession(HttpServerRequest request) {
        if (!validateUserAuthentication(request)) {
            return
        }

        UUID id = UUID.fromString(request.params.get("id"))
        boolean success = sessionService.deleteSession(id)

        if (success)
            sendJsonResponse(request, HttpResponseStatus.NO_CONTENT)
        else
            sendJsonResponse(request, HttpResponseStatus.NOT_FOUND)
    }

}
