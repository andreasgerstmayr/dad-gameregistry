package es.us.dad.gameregistry.controller

import es.us.dad.gameregistry.domain.GameSession
import es.us.dad.gameregistry.service.SessionService
import es.us.dad.gameregistry.util.DELETE
import es.us.dad.gameregistry.util.GET
import es.us.dad.gameregistry.util.POST
import groovy.json.JsonBuilder
import org.vertx.groovy.core.http.HttpServerRequest

class SessionController extends Controller {

    // TODO: dependency injection
    private SessionService sessionService = new SessionService()

    private static void sendJsonResponse(HttpServerRequest req, Object jsonResponse, int statusCode = 200) {
        req.response.putHeader("Content-Type", "application/json")
        req.response.setStatusCode(statusCode)
        req.response.end(new JsonBuilder(jsonResponse).toPrettyString())
    }

    private static void sendNotFoundResponse(HttpServerRequest req, UUID id) {
        sendJsonResponse(req, [error: "Could not find a game session with id: " + id], 404)
    }

    @GET("/session/:id")
    public void getSession(HttpServerRequest request) {
        UUID id = UUID.fromString(request.params.get("id"))
        GameSession session = sessionService.getSession(id)

        if (session != null)
            sendJsonResponse(request, session)
        else
            sendNotFoundResponse(request, id)
    }

    @POST("/session")
    public void createSession(HttpServerRequest request) {
        GameSession newSession = sessionService.startSession()
        sendJsonResponse(request, newSession, 201)
    }

    @POST("/session/:id")
    public void finishSession(HttpServerRequest request) {
        UUID id = UUID.fromString(request.params.get("id"))
        GameSession session = sessionService.finishSession(id)

        if (session != null)
            sendJsonResponse(request, session)
        else
            sendNotFoundResponse(request, id)
    }

    @DELETE("/session/:id")
    public void deleteSession(HttpServerRequest request) {
        UUID id = UUID.fromString(request.params.get("id"))
        boolean success = sessionService.deleteSession(id)

        if (success)
            sendJsonResponse(request, [status: "Removed session " + id])
        else
            sendNotFoundResponse(request, id)
    }

}
