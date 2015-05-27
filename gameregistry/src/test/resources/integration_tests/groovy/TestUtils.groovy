package integration_tests.groovy

import org.vertx.groovy.core.Vertx
import org.vertx.groovy.core.eventbus.Message
import org.vertx.java.core.json.JsonObject

import static org.vertx.testtools.VertxAssert.assertEquals

static def clearDatabase(Vertx vertx, Closure callback) {
    vertx.eventBus.send("gameregistry.db", [action: "drop_collection",
                                            collection: "game_session"]) { Message message ->
        Map messageBody = message.body
        assertEquals("ok", messageBody["status"])
        callback.call()
    }
}

static Map<String, Object> readTestConfig() {
    return new JsonObject(new File('conf-test.json').getText('UTF-8')).toMap()
}
