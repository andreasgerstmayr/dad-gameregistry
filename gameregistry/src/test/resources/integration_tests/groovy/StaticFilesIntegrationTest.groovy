package integration_tests.groovy

import org.vertx.groovy.core.http.HttpClient
import org.vertx.groovy.core.http.HttpClientResponse
import org.vertx.groovy.testtools.VertxTests
import org.vertx.java.core.json.JsonObject

import static org.vertx.testtools.VertxAssert.*

def testIndexHtml() {
    HttpClient client = vertx.createHttpClient().setPort(8080)
    String path = "/doc/index.html"

    container.logger.info("Performing GET to ${path}")
    client.getNow(path, { response ->
        container.logger.info("Received response: ${response.statusCode}")
        assertEquals(200, response.statusCode)
        testComplete()
    })
}

def testNoIndexHtml() {
    HttpClient client = vertx.createHttpClient().setPort(8080)
    client.getNow("/doc/", { HttpClientResponse response ->
        assertEquals(200, response.statusCode)
        testComplete()
    })
}

def testFileNotFound() {
    HttpClient client = vertx.createHttpClient().setPort(8080)
    client.getNow("/doc/bjerg", { HttpClientResponse response ->
        assertEquals(404, response.statusCode)
        testComplete()
    })
}

def testSubDirectory() {
    HttpClient client = vertx.createHttpClient().setPort(8080)
    client.getNow("/doc/css/screen.css", { HttpClientResponse response ->
        assertEquals(200, response.statusCode)
        testComplete()
    })
}

VertxTests.initialize(this)
Map<String, Object> testConfig = new JsonObject(new File('conf-test.json').getText('UTF-8')).toMap()
container.deployModule(System.getProperty("vertx.modulename"), testConfig, { asyncResult ->
    assertTrue(asyncResult.succeeded)
    assertNotNull("deploymentID should not be null", asyncResult.result())

    VertxTests.startTests(this)
})