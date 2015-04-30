package es.us.dad.gameregistry.server.service

import com.darylteo.vertx.promises.groovy.Promise
import es.us.dad.gameregistry.server.domain.DomainObject
import es.us.dad.gameregistry.server.domain.GameSession
import es.us.dad.gameregistry.server.exception.DatabaseException
import es.us.dad.gameregistry.server.exception.ObjectNotFoundException
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
    public Promise<GameSession> getSession(UUID id) {
        Promise<GameSession> p = new Promise()

        vertx.eventBus.send("gameregistry.db", [action: "find",
                                                collection: "game_session",
                                                matcher: [id: id.toString()]]) { Message message ->
            Map messageBody = message.body

            if (messageBody["status"].equals("ok")) {
                List<Map> results = messageBody["results"]

                if (results.isEmpty())
                    p.reject(new ObjectNotFoundException("GameSession not found."))
                else
                    p.fulfill(new GameSession(results.first()))
            }
            else {
                logger.error("Error finding GameSession:")
                logger.error(messageBody)
                p.reject(new DatabaseException(messageBody["message"]))
            }
        }

        return p
    }

    /**
     * initializes a new game session
     * @return new game session
     */
    public Promise<GameSession> startSession() {
        Promise<GameSession> p = new Promise()

        GameSession session = new GameSession()
        session.setId(UUID.randomUUID())
        session.setStart(new Date())

        vertx.eventBus.send("gameregistry.db", [action: "save",
                                                collection: "game_session",
                                                document: session.toJsonMap()]) { Message message ->
            Map messageBody = message.body

            if (messageBody["status"].equals("ok")) {
                p.fulfill(session)
            }
            else {
                logger.error("Error saving GameSession:")
                logger.error(messageBody)
                p.reject(new DatabaseException(messageBody["message"]))
            }
        }

        return p
    }

    /**
     * finishes a game session: sets end date
     * @param id session id
     * @return updated game session or {@code null} if game session couldn't be found
     */
    public Promise<GameSession> finishSession(UUID id) {
        Promise<GameSession> p = new Promise()

        vertx.eventBus.send("gameregistry.db", [action: "update",
                                                collection: "game_session",
                                                criteria: [id: id.toString()],
                                                objNew: ['$set': [end: DomainObject.formatDate(new Date())]],
        upsert: true, multi: false]) { Message message ->
            Map messageBody = message.body

            if (messageBody["status"].equals("ok")) {
                // successfully updated. now retrieve full object
                // TODO: optimizable, two db queries aren't very good :)
                getSession(id).then({ GameSession session ->
                    p.fulfill(session)
                }, { Exception ex ->
                    p.reject(ex)
                })
            }
            else {
                logger.error("Error updating GameSession:")
                logger.error(messageBody)
                p.reject(new DatabaseException(messageBody["message"]))
            }
        }

        return p
    }

    /**
     * deletes a game session
     * @param id session id
     * @return true if the session is found and deleted, false otherwise
     */
    public Promise<Void> deleteSession(UUID id) {
        Promise<Void> p = new Promise()

        vertx.eventBus.send("gameregistry.db", [action: "delete",
                                                collection: "game_session",
                                                matcher: [id: id.toString()]]) { Message message ->
            Map messageBody = message.body

            if (messageBody["status"].equals("ok")) {
                p.fulfill(null)
            }
            else {
                logger.error("Error finding GameSession:")
                logger.error(messageBody)
                p.reject(new DatabaseException(messageBody["message"]))
            }
        }

        return p
    }

}
