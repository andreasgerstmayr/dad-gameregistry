package integration_tests.groovy

import es.us.dad.gameregistry.server.RestServer
import org.vertx.groovy.core.buffer.Buffer
import org.vertx.groovy.core.http.HttpClient
import org.vertx.groovy.core.http.HttpClientResponse
import org.vertx.groovy.testtools.VertxTests
import org.vertx.java.core.json.JsonObject

import static org.vertx.testtools.VertxAssert.*

def jsonOrNull(Buffer content) {
    if (content.length > 0)
        return new JsonObject(content.toString())
    else
        return null
}

def createSession(HttpClient client, Closure handler) {
    client.post("/sessions", { HttpClientResponse resp ->
        resp.bodyHandler { Buffer content ->
            handler.call(resp.statusCode, jsonOrNull(content))
        }
    }).putHeader("gameregistry-user", "testuser").putHeader("gameregistry-token", "testtoken").end()
}

def retrieveSession(HttpClient client, String id, Closure handler) {
    client.get("/sessions/${id}", { HttpClientResponse resp ->
        resp.bodyHandler { Buffer content ->
            handler.call(resp.statusCode, jsonOrNull(content))
        }
    }).putHeader("gameregistry-user", "testuser").putHeader("gameregistry-token", "testtoken").end()
}

def updateSession(HttpClient client, String id, Closure handler) {
    client.put("/sessions/${id}", { HttpClientResponse resp ->
        resp.bodyHandler { Buffer content ->
            handler.call(resp.statusCode, jsonOrNull(content))
        }
    }).putHeader("gameregistry-user", "testuser").putHeader("gameregistry-token", "testtoken").end()
}

def deleteSession(HttpClient client, String id, Closure handler) {
    client.delete("/sessions/${id}", { HttpClientResponse resp ->
        resp.bodyHandler { Buffer content ->
            handler.call(resp.statusCode, jsonOrNull(content))
        }
    }).putHeader("gameregistry-user", "testuser").putHeader("gameregistry-token", "testtoken").end()
}

def testNotFound() {
    HttpClient client = vertx.createHttpClient().setPort(8080)
    retrieveSession(client, "18aef6a4-d415-4a19-8261-fe6c18d8bac0", { int statusCode, JsonObject data ->
        assertEquals(404, statusCode)
        testComplete()
    })
}

def testCreate() {
    HttpClient client = vertx.createHttpClient().setPort(8080)
    createSession(client, { int statusCode, JsonObject data ->
        assertEquals(201, statusCode)

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
        assertEquals(201, statusCode)

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
        assertEquals(201, statusCode)

        deleteSession(client, data.getString("id"), { int statusCode2, JsonObject data2 ->
            assertEquals(204, statusCode2)

            retrieveSession(client, data.getString("id"), { int statusCode3, JsonObject data3 ->
                assertEquals(404, statusCode3)
                testComplete()
            })
        })
    })
}

def testNotAuthenticated() {
    HttpClient client = vertx.createHttpClient().setPort(8080)
    client.post("/sessions", { HttpClientResponse resp ->
        resp.bodyHandler { Buffer content ->
            assertEquals(403, resp.statusCode)
            assertEquals("""{"error":"Invalid user or token"}""", content.toString())
            testComplete()
        }
    }).end()
}

VertxTests.initialize(this)
Map<String, Object> testConfig = new JsonObject(new File('conf-test.json').getText('UTF-8')).toMap()
container.deployModule(System.getProperty("vertx.modulename"), testConfig, { asyncResult ->
    assertTrue(asyncResult.succeeded)
    assertNotNull("deploymentID should not be null", asyncResult.result())
    VertxTests.startTests(this)
})
