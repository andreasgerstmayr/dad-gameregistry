package integration_tests.groovy

import org.vertx.groovy.core.http.HttpClient
import org.vertx.groovy.core.http.HttpClientResponse
import org.vertx.groovy.testtools.VertxTests
import org.vertx.java.core.json.JsonObject

import static org.vertx.testtools.VertxAssert.*

def testDebugPromiseIsNotBlocking() {
    HttpClient client = vertx.createHttpClient().setPort(8080)
    HttpClient client2 = vertx.createHttpClient().setPort(8080)
    String path = "/debug_promise"

    /*
     * In this test we want to perform a request to our debug_promise url,
     * where the DebugPromiseService will await 20 seconds before answering
     * it with a simple html content and 200 OK. We want to do another
     * request after the initial one started but before it ended and check that
     * it is answered before the original request is finally ended.
     */

    List<Integer> order = []

    // This gets the first request going
    container.logger.info("#1: Launching first request!")
    order.add(1)

    client.getNow(path, { response ->
        // This code will execute around 20 secs after the getNow call was performed
        container.logger.info("#4: First request answered: " + response.getStatusCode())
        order.add(4)

        assertEquals([1,2,3,4], order)
        testComplete()
    })

    // Wait for 2 seconds
    vertx.setTimer(2 * 1000, {long time ->
        // And then do another request that should end before the first one.
        container.logger.info("#2: Launching second request!")
        order.add(2)

        client2.getNow("/doc", { response ->
            container.logger.info("#3: Second request answered.")
            order.add(3)
        })
    })
}

VertxTests.initialize(this)
Map<String, Object> testConfig = new JsonObject(new File('conf-test.json').getText('UTF-8')).toMap()
container.deployModule(System.getProperty("vertx.modulename"), testConfig, { asyncResult ->
    assertTrue(asyncResult.succeeded)
    assertNotNull("deploymentID should not be null", asyncResult.result())

    VertxTests.startTests(this)
})