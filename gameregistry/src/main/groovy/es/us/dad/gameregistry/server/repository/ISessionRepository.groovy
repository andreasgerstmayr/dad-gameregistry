package es.us.dad.gameregistry.server.repository

import com.darylteo.vertx.promises.groovy.Promise
import es.us.dad.gameregistry.shared.domain.GameSession

interface ISessionRepository {

    Promise<GameSession> create(GameSession session)
    Promise<GameSession> update(GameSession session)
    Promise<Void> delete(UUID id)
    Promise<GameSession> findById(UUID id)
    Promise<List<GameSession>> find(UUID id, String user)
    Promise<Void> cleanup(long maxAge)

}
