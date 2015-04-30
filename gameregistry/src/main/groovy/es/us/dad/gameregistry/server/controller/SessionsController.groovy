package es.us.dad.gameregistry.server.controller

import com.darylteo.vertx.promises.groovy.Promise
import es.us.dad.gameregistry.server.domain.GameSession
import es.us.dad.gameregistry.server.exception.MethodNotAllowedException
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
        sendErrorResponse(request, new MethodNotAllowedException())
    }

    @Authenticated
    @POST("/sessions")
    public void createSession(HttpServerRequest request) {
        Promise<GameSession> p = sessionService.startSession()

        p.then({ GameSession newSession ->
            sendJsonResponse(request, newSession, HttpResponseStatus.CREATED)
        }, { Exception ex ->
            sendErrorResponse(request, ex)
        })
    }

    @PUT("/sessions")
    public void changeSessions(HttpServerRequest request) {
        sendErrorResponse(request, new MethodNotAllowedException())
    }

    @DELETE("/sessions")
    public void deleteSessions(HttpServerRequest request) {
        sendErrorResponse(request, new MethodNotAllowedException())
    }

}
