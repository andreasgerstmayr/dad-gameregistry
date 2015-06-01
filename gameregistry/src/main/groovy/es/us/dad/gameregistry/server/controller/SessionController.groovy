package es.us.dad.gameregistry.server.controller

import es.us.dad.gameregistry.server.exception.BadRequestException
import es.us.dad.gameregistry.server.exception.MethodNotAllowedException
import es.us.dad.gameregistry.server.service.ILoginService
import es.us.dad.gameregistry.server.service.SessionService
import es.us.dad.gameregistry.server.util.*
import es.us.dad.gameregistry.shared.domain.GameSession
import io.netty.handler.codec.http.HttpResponseStatus
import org.vertx.groovy.core.http.HttpServerRequest
import org.vertx.java.core.json.JsonObject

class SessionController extends Controller {

    private final SessionService sessionService

    public SessionController(ILoginService loginService, SessionService sessionService) {
        super(loginService)
        this.sessionService = sessionService
    }

    private static UUID convertIdOrSendError(HttpServerRequest request, String id) {
        try {
            return UUID.fromString(id)
        }
        catch (IllegalArgumentException ignored) {
            sendErrorResponse(request, new BadRequestException("The id: '" + id + "' is not valid."))
            return null
        }
    }

    @Authenticated
    @GET("/api/v1/sessions/:id")
    public void getSession(HttpServerRequest request) {
        UUID id = convertIdOrSendError(request, request.params.get("id"))
        if (id == null) {
            return
        }

        sessionService.getSession(id).then({ GameSession session ->
            sendJsonResponse(request, session)
        }).fail({ Exception ex ->
            sendErrorResponse(request, ex)
        })
    }

    @Authenticated
    @POST("/api/v1/sessions/:id")
    public void postSession(HttpServerRequest request) {
        sendErrorResponse(request, new MethodNotAllowedException())
    }

    @Authenticated
    @PUT("/api/v1/sessions/:id")
    public void finishSession(HttpServerRequest request) {
        String user = getCurrentUser(request)
        UUID id = convertIdOrSendError(request, request.params.get("id"))
        if (id == null) {
            return
        }

        getRequestBody(request).then({ JsonObject body ->
            Map<String, Object> resultMap = body != null ? body.toMap() : null
            return sessionService.finishSession(user, id, resultMap)
        }).then({ GameSession session ->
            sendJsonResponse(request, session)
        }).fail({ Exception ex ->
            sendErrorResponse(request, ex)
        })
    }

    @Authenticated
    @DELETE("/api/v1/sessions/:id")
    public void deleteSession(HttpServerRequest request) {
        String user = getCurrentUser(request)
        UUID id = convertIdOrSendError(request, request.params.get("id"))
        if (id == null) {
            return
        }

        sessionService.deleteSession(user, id).then({
            sendJsonResponse(request, [:], HttpResponseStatus.NO_CONTENT)
        }).fail({ Exception ex ->
            sendErrorResponse(request, ex)
        })
    }

}
