package es.us.dad.gameregistry.server.service

import com.darylteo.vertx.promises.groovy.Promise
import es.us.dad.gameregistry.shared.domain.GameSession
import es.us.dad.gameregistry.server.repository.ISessionRepository
import org.vertx.groovy.core.Vertx
import org.vertx.java.core.logging.Logger

class SessionService {

    private final Vertx vertx
    private final Logger logger
    private final ISessionRepository sessionRepository

    public SessionService(Vertx vertx, Logger logger, ISessionRepository sessionRepository) {
        this.vertx = vertx
        this.logger = logger
        this.sessionRepository = sessionRepository
    }

    /**
     * retrieves a game session
     * @param id session id
     * @return game session or {@code null} if game session could not be found
     */
    public Promise<GameSession> getSession(UUID id) {
        return sessionRepository.findById(id)
    }

    public Promise<List<GameSession>> findSessions(UUID id, String user) {
        return sessionRepository.find(id, user)
    }

    /**
     * initializes a new game session
     * @return new game session
     */
    public Promise<GameSession> startSession(String user, String game) {
        GameSession session = new GameSession()
        session.setId(UUID.randomUUID())
        session.setUser(user)
        session.setGame(game)
        session.setStart(new Date())

        return sessionRepository.create(session)
    }

    /**
     * finishes a game session: sets end date
     * @param id session id
     * @return updated game session or {@code null} if game session couldn't be found
     */
    public Promise<GameSession> finishSession(UUID id) {
        Promise<GameSession> p = new Promise()

        sessionRepository.findById(id).then({ GameSession session ->
            session.end = new Date()
            return sessionRepository.update(session)
        }, { Exception ex ->
            p.reject(ex)
        }).then({ GameSession session ->
            p.fulfill(session)
        }, { Exception ex ->
            p.reject(ex)
        })

        return p
    }

    /**
     * deletes a game session
     * @param id session id
     * @return true if the session is found and deleted, false otherwise
     */
    public Promise<Void> deleteSession(UUID id) {
        return sessionRepository.delete(id)
    }

}
