package es.us.dad.gameregistry.server.service

import es.us.dad.gameregistry.server.domain.DomainObject
import es.us.dad.gameregistry.server.domain.GameSession
import org.vertx.groovy.core.Vertx
import org.vertx.groovy.core.eventbus.Message
import org.vertx.java.core.logging.Logger

class SessionService {

    private final Vertx vertx
    private final Logger logger

    public SessionService(Vertx vertx, Logger logger) {
        this.vertx = vertx
        this.logger = logger
    }

    /**
     * retrieves a game session
     * @param id session id
     * @return game session or {@code null} if game session could not be found
     */
    public void getSession(UUID id, Closure handler) {
        vertx.eventBus.send("gameregistry.db", [action: "find",
                                                collection: "game_session",
                                                matcher: [id: id.toString()]]) { Message message ->
            Map messageBody = message.body

            if (messageBody["status"].equals("ok")) {
                List<Map> results = messageBody["results"]

                if (results.isEmpty())
                    handler.call(null)
                else
                    handler.call(new GameSession(results.first()))
            }
            else {
                logger.error("Error finding GameSession:")
                logger.error(messageBody)
                handler.call(null)
            }
        }
    }

    /**
     * initializes a new game session
     * @return new game session
     */
    public void startSession(Closure handler) {
        GameSession session = new GameSession()
        session.setId(UUID.randomUUID())
        session.setStart(new Date())

        vertx.eventBus.send("gameregistry.db", [action: "save",
                                                collection: "game_session",
                                                document: session.toJsonMap()]) { Message message ->
            Map messageBody = message.body

            if (messageBody["status"].equals("ok")) {
                handler.call(session)
            }
            else {
                logger.error("Error saving GameSession:")
                logger.error(messageBody)
                handler.call(null)
            }
        }
    }

    /**
     * finishes a game session: sets end date
     * @param id session id
     * @return updated game session or {@code null} if game session couldn't be found
     */
    public void finishSession(UUID id, Closure handler) {
        vertx.eventBus.send("gameregistry.db", [action: "update",
                                                collection: "game_session",
                                                criteria: [id: id.toString()],
                                                objNew: ['$set': [end: DomainObject.formatDate(new Date())]],
        upsert: true, multi: false]) { Message message ->
            Map messageBody = message.body

            if (messageBody["status"].equals("ok")) {
                // successfully updated. now retrieve full object
                // TODO: optimizable, two db queries aren't very good :)
                getSession(id, { GameSession session ->
                    handler.call(session)
                })
            }
            else {
                logger.error("Error updating GameSession:")
                logger.error(messageBody)
                handler.call(null)
            }
        }
    }

    /**
     * deletes a game session
     * @param id session id
     * @return true if the session is found and deleted, false otherwise
     */
    public void deleteSession(UUID id, Closure handler) {
        vertx.eventBus.send("gameregistry.db", [action: "delete",
                                                collection: "game_session",
                                                matcher: [id: id.toString()]]) { Message message ->
            Map messageBody = message.body

            if (messageBody["status"].equals("ok")) {
                handler.call(true)
            }
            else {
                logger.error("Error finding GameSession:")
                logger.error(messageBody)
                handler.call(false)
            }
        }
    }

}
