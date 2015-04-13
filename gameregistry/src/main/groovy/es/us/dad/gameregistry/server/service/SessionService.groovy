package es.us.dad.gameregistry.server.service

import es.us.dad.gameregistry.server.domain.GameSession

class SessionService {

    private Map<UUID, GameSession> database = [:]

    /**
     * retrieves a game session
     * @param id session id
     * @return game session or {@code null} if game session could not be found
     */
    public GameSession getSession(UUID id) {
        GameSession session = database.get(id)
        return session
    }

    /**
     * initializes a new game session
     * @return new game session
     */
    public GameSession startSession() {
        GameSession session = new GameSession()
        session.setId(UUID.randomUUID())
        session.setStart(new Date())

        database.put(session.id, session)
        return session
    }

    /**
     * finishes a game session: sets end date
     * @param id session id
     * @return updated game session or {@code null} if game session couldn't be found
     */
    public GameSession finishSession(UUID id) {
        GameSession session = database.get(id)

        if (session != null) {
            session.setEnd(new Date())
            database.put(session.id, session)
        }

        return session
    }

    /**
     * deletes a game session
     * @param id session id
     * @return true if the session is found and deleted, false otherwise
     */
    public boolean deleteSession(UUID id) {
        if (database.containsKey(id)) {
            database.remove(id)
            return true
        } else {
            return false
        }
    }

}
