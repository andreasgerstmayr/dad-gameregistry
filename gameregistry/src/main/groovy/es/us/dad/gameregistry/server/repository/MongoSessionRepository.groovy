package es.us.dad.gameregistry.server.repository

import com.darylteo.vertx.promises.groovy.Promise
import es.us.dad.gameregistry.shared.domain.GameSession
import es.us.dad.gameregistry.server.exception.DatabaseException
import es.us.dad.gameregistry.server.exception.ObjectNotFoundException
import org.vertx.groovy.core.Vertx
import org.vertx.groovy.core.eventbus.Message
import org.vertx.java.core.logging.Logger

class MongoSessionRepository implements ISessionRepository {

    private final Vertx vertx
    private final Logger logger

    public MongoSessionRepository(Vertx vertx, Logger logger) {
        this.vertx = vertx
        this.logger = logger
    }

    private DatabaseException prepareAndLogException(Map messageBody) {
        DatabaseException ex = new DatabaseException(messageBody["message"].toString())
        logger.error("Database Error:")
        logger.error(ex.message)
        return ex
    }

    @Override
    Promise<GameSession> create(GameSession session) {
        Promise<GameSession> p = new Promise()

        vertx.eventBus.send("gameregistry.db", [action    : "save",
                                                collection: "game_session",
                                                document  : session.toJsonMap()]) { Message message ->
            Map messageBody = message.body

            if (messageBody["status"].equals("ok")) {
                p.fulfill(session)
            } else {
                DatabaseException ex = prepareAndLogException(messageBody)
                p.reject(ex)
            }
        }

        return p
    }

    @Override
    Promise<GameSession> update(GameSession session) {
        Promise<GameSession> p = new Promise()

        vertx.eventBus.send("gameregistry.db", [action    : "update",
                                                collection: "game_session",
                                                criteria  : [id: session.id.toString()],
                                                objNew    : session.toJsonMap(),
                                                upsert    : true, multi: false]) { Message message ->
            Map messageBody = message.body

            if (messageBody["status"].equals("ok")) {
                p.fulfill(session)
            } else {
                DatabaseException ex = prepareAndLogException(messageBody)
                p.reject(ex)
            }
        }

        return p
    }

    @Override
    Promise<Void> delete(UUID id) {
        Promise<Void> p = new Promise()

        vertx.eventBus.send("gameregistry.db", [action    : "delete",
                                                collection: "game_session",
                                                matcher   : [id: id.toString()]]) { Message message ->
            Map messageBody = message.body

            if (messageBody["status"].equals("ok")) {
                if (messageBody["number"] == 1)
                    p.fulfill(null)
                else
                    p.reject(new ObjectNotFoundException("GameSession not found."));
            } else {
                DatabaseException ex = prepareAndLogException(messageBody)
                p.reject(ex)
            }
        }

        return p
    }

    @Override
    Promise<GameSession> findById(UUID id) {
        Promise<GameSession> p = new Promise()

        find(id, null).then({ List<GameSession> sessions ->
            if (sessions.isEmpty())
                p.reject(new ObjectNotFoundException("GameSession not found."))
            else
                p.fulfill(sessions.first())
        }).fail({ Exception ex ->
            p.reject(ex)
        })

        return p
    }

    @Override
    Promise<List<GameSession>> find(UUID id, String user) {
        Promise<List<GameSession>> p = new Promise()

        Map matcher = [:]
        if (id != null)
            matcher['id'] = id.toString()
        if (user != null)
            matcher['user'] = user

        vertx.eventBus.send("gameregistry.db", [action    : "find",
                                                collection: "game_session",
                                                matcher   : matcher]) { Message message ->
            Map messageBody = message.body

            if (messageBody["status"].equals("ok")) {
                List<Map> results = messageBody["results"] as List<Map>
                List<GameSession> sessions = results.collect { Map json ->
                    new GameSession(json)
                }

                p.fulfill(sessions)
            } else {
                DatabaseException ex = prepareAndLogException(messageBody)
                p.reject(ex)
            }
        }

        return p
    }
}
