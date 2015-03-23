package integration_tests.groovy

import org.vertx.groovy.core.buffer.Buffer
import org.vertx.groovy.core.http.HttpClient
import org.vertx.groovy.core.http.HttpClientResponse
import org.vertx.java.core.json.JsonObject

import static org.vertx.testtools.VertxAssert.*
import org.vertx.groovy.testtools.VertxTests

import es.us.dad.gameregistry.RestServer


def createSession(HttpClient client, Closure handler) {
    client.post("/session", { HttpClientResponse resp ->
        resp.bodyHandler { Buffer content ->
            handler.call(resp.statusCode, new JsonObject(content.toString()))
        }
    }).end()
}

def retrieveSession(HttpClient client, String id, Closure handler) {
    client.getNow("/session/${id}", { HttpClientResponse resp ->
        resp.bodyHandler { Buffer content ->
            handler.call(resp.statusCode, new JsonObject(content.toString()))
        }
    })
}

def updateSession(HttpClient client, String id, Closure handler) {
    client.post("/session/${id}", { HttpClientResponse resp ->
        resp.bodyHandler { Buffer content ->
            handler.call(resp.statusCode, new JsonObject(content.toString()))
        }
    }).end()
}

def deleteSession(HttpClient client, String id, Closure handler) {
    client.delete("/session/${id}", { HttpClientResponse resp ->
        resp.bodyHandler { Buffer content ->
            handler.call(resp.statusCode, new JsonObject(content.toString()))
        }
    }).end()
}

def testNotFound() {
    HttpClient client = vertx.createHttpClient().setPort(8080)
    retrieveSession(client, "18aef6a4-d415-4a19-8261-fe6c18d8bac0", { int statusCode, JsonObject data ->
        assertEquals(404, statusCode)
        assertEquals("Could not find a game session with id: 18aef6a4-d415-4a19-8261-fe6c18d8bac0", data.getString("error"))
        testComplete()
    })
}

def testCreate() {
    HttpClient client = vertx.createHttpClient().setPort(8080)
    createSession(client, { int statusCode, JsonObject data ->
        assertEquals(200, statusCode)

        retrieveSession(client, data.getString("id"), { int statusCode2, JsonObject data2 ->
            assertEquals(200, statusCode2)
            assertNull(data2.getValue("end"))
            testComplete()
        })
    })
}

def testUpdate() {
    HttpClient client = vertx.createHttpClient().setPort(8080)
    createSession(client, { int statusCode, JsonObject data ->
        assertEquals(200, statusCode)

        updateSession(client, data.getString("id"), { int statusCode2, JsonObject data2 ->
            assertEquals(200, statusCode2)
            assertNotNull(data2.getValue("end"))
            testComplete()
        })
    })
}

def testDelete() {
    HttpClient client = vertx.createHttpClient().setPort(8080)
    createSession(client, { int statusCode, JsonObject data ->
        assertEquals(200, statusCode)

        deleteSession(client, data.getString("id"), { int statusCode2, JsonObject data2 ->
            assertEquals(200, statusCode2)

            retrieveSession(client, data.getString("id"), { int statusCode3, JsonObject data3 ->
                assertEquals(404, statusCode3)
                testComplete()
            })
        })
    })
}

VertxTests.initialize(this)
container.deployVerticle("groovy:" + RestServer.class.getName(), { asyncResult ->
    assertTrue(asyncResult.succeeded)
    assertNotNull("deploymentID should not be null", asyncResult.result())
    VertxTests.startTests(this)
})
