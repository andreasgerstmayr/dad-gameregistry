package integration_tests.groovy

import com.darylteo.vertx.promises.groovy.Promise
import es.us.dad.gameregistry.server.repository.ISessionRepository
import es.us.dad.gameregistry.server.repository.MongoSessionRepository
import es.us.dad.gameregistry.server.service.SessionService
import es.us.dad.gameregistry.shared.domain.GameSession
import org.vertx.groovy.testtools.VertxTests

import static org.vertx.testtools.VertxAssert.assertEquals
import static org.vertx.testtools.VertxAssert.assertNotNull
import static org.vertx.testtools.VertxAssert.assertTrue
import static org.vertx.testtools.VertxAssert.testComplete

def testCleanup() {
    ISessionRepository sessionRepository = new MongoSessionRepository(vertx, container.logger)
    SessionService sessionService = new SessionService(vertx, container.logger, sessionRepository)

    sessionService.startSession("testUser", "testGame").then({ GameSession gameSession ->
        return sessionService.finishSession(gameSession.id)
    }).then({
        return sessionService.startSession("testUser2", "testGame2")
    }).then({ GameSession gameSession ->
        Promise<Void> p = new Promise<Void>()
        vertx.setTimer(5 * 1000, {
            p.fulfill(null)
        })
        return p
    }).then({ GameSession gameSession ->
        return sessionService.startSession("testUser3", "testGame3")
    }).then({ GameSession gameSession ->
        return sessionService.findSessions(null, null)
    }).then({ List<GameSession> gameSessions ->
        // s1: started 5s ago, ended 5s ago
        // s2: started 5s ago
        // s3: started now
        assertEquals(3, gameSessions.size())
        for(GameSession gameSession: gameSessions) {
            container.logger.info(gameSession)
        }
        return sessionService.cleanup(3)
    }).then({
        return sessionService.findSessions(null, null)
    }).then({ List<GameSession> gameSessions ->
        assertEquals(2, gameSessions.size())
        for(GameSession gameSession: gameSessions) {
            container.logger.info(gameSession)
        }
        testComplete()
    }).fail({ Exception ex ->
        container.logger.info("Error: " + ex)
        assertTrue(false)
    })
}

VertxTests.initialize(this)
container.deployModule(System.getProperty("vertx.modulename"), TestUtils.readTestConfig(), { asyncResult ->
    assertTrue(asyncResult.succeeded)
    assertNotNull("deploymentID should not be null", asyncResult.result())

    // clear database before starting each test
    TestUtils.clearDatabase (vertx, {
        VertxTests.startTests(this)
    })
})
