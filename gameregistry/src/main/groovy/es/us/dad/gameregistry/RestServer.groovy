package es.us.dad.gameregistry

import es.us.dad.gameregistry.domain.GameSession
import org.vertx.groovy.core.http.HttpServerRequest
import org.vertx.groovy.core.http.RouteMatcher
import org.vertx.groovy.platform.Verticle
import org.vertx.java.core.json.JsonObject


class RestServer extends Verticle {

    private Map<UUID,GameSession> database = [:]

    private static void sendJsonResponse(HttpServerRequest req, JsonObject response, int statusCode = 200) {
        req.response.putHeader("Content-Type", "application/json")
        req.response.setStatusCode(statusCode)
        req.response.end(response.encodePrettily())
    }

    private static void sendNotFoundResponse(HttpServerRequest req, UUID id) {
        sendJsonResponse(req, new JsonObject([error: "Could not find a game session with id: " + id]), 404)
    }

    def start() {
        RouteMatcher rm = new RouteMatcher()

        rm.get("/session/:id", { HttpServerRequest req ->
            UUID id = UUID.fromString(req.params.get("id"))

            GameSession session = database.get(id)
            if (session != null)
                sendJsonResponse(req, session.toJson())
            else
                sendNotFoundResponse(req, id)
        })

        rm.post("/session", { HttpServerRequest req ->
            GameSession session = new GameSession()
            session.setId(UUID.randomUUID())
            session.setStart(new Date())

            database.put(session.id, session)
            sendJsonResponse(req, session.toJson())
        })

        rm.post("/session/:id", { HttpServerRequest req ->
            UUID id = UUID.fromString(req.params.get("id"))

            GameSession session = database.get(id)
            if (session != null) {
                session.setEnd(new Date())
                database.put(session.id, session)
                sendJsonResponse(req, session.toJson())
            }
            else {
                sendNotFoundResponse(req, id)
            }
        })

        rm.delete("/session/:id", { HttpServerRequest req ->
            UUID id = UUID.fromString(req.params.get("id"))

            JsonObject response
            if (database.containsKey(id)) {
                database.remove(id)
                response = new JsonObject([status: "Removed session " + id])
                sendJsonResponse(req, response)
            }
            else {
                sendNotFoundResponse(req, id)
            }
        })

        vertx.createHttpServer().requestHandler(rm.asClosure()).listen(8080)
    }
}
