package integration_tests

import org.vertx.groovy.core.buffer.Buffer
import org.vertx.groovy.core.http.HttpClientResponse

import static org.vertx.testtools.VertxAssert.*
import org.vertx.groovy.testtools.VertxTests

import es.us.dad.gameregistry.ToyRestServer


def testRestServer() {
    // deploy only the ToyRestServer
    container.deployVerticle("groovy:" + ToyRestServer.class.getName())

    vertx.createHttpClient().setPort(1080).getNow("/", { HttpClientResponse resp ->
        assertEquals(200, resp.statusCode)

        resp.bodyHandler { Buffer content ->
            assertEquals("Hello world.", content.toString())

            // tests are async, we have to notify the test runner that the test has finished
            testComplete()
        }
    })
}

VertxTests.initialize(this)
VertxTests.startTests(this)
