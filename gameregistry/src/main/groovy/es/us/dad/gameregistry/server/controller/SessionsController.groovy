package es.us.dad.gameregistry.server.controller

import es.us.dad.gameregistry.GameRegistryConstants
import es.us.dad.gameregistry.server.domain.GameSession
import es.us.dad.gameregistry.server.exception.MethodNotAllowedException
import es.us.dad.gameregistry.server.service.ILoginService
import es.us.dad.gameregistry.server.service.SessionService
import es.us.dad.gameregistry.server.util.*
import io.netty.handler.codec.http.HttpResponseStatus
import org.vertx.groovy.core.buffer.Buffer
import org.vertx.groovy.core.http.HttpServerRequest
import org.vertx.java.core.json.JsonObject

class SessionsController extends Controller {

    private final SessionService sessionService

    public SessionsController(ILoginService loginService, SessionService sessionService) {
        super(loginService)
        this.sessionService = sessionService
    }

    @GET("/sessions")
    public void getSessions(HttpServerRequest request) {
        String user = request.params.get("user")

        sessionService.findSessions(null, user).then ({ List<GameSession> sessions ->
            sendJsonResponse(request, [count: sessions.size(), sessions: sessions])
        }, { Exception ex ->
            sendErrorResponse(request, ex)
        })
    }

    @Authenticated
    @POST("/sessions")
    public void createSession(HttpServerRequest request) {
        String user = request.headers.get(GameRegistryConstants.GAMEREGISTRY_USER_HEADER)
        request.bodyHandler { Buffer buffer ->
            JsonObject data = new JsonObject(buffer.toString())

            String game = data.getString("game")
            sessionService.startSession(user, game).then({ GameSession newSession ->
                sendJsonResponse(request, newSession, HttpResponseStatus.CREATED)
            }, { Exception ex ->
                sendErrorResponse(request, ex)
            })
        }
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
