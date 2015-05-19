package integration_tests.groovy

import org.vertx.groovy.core.http.HttpClient
import org.vertx.groovy.core.http.HttpClientResponse
import org.vertx.groovy.testtools.VertxTests
import org.vertx.java.core.json.JsonObject

import static org.vertx.testtools.VertxAssert.*

def testDebugPromiseIsNotBlocking() {
    HttpClient client = vertx.createHttpClient().setPort(8080)
    String path = "/debug_promise"
    //final Map<String, Boolean> myMap = new HashMap<>().put("secondRequestEnded", false)

    /*
     * In this test we want to perform a request to our debug_promise url,
     * where the DebugPromiseService will await 20 seconds before answering
     * it with a simple html content and 200 OK. We want to do another
     * request after the initial one started but before it ended and check that
     * it is answered before the original request is finally ended.
     */

    /*
    TODO: This doesnt work for some reason, but manually checking works.
    What I mean is that if someone just runs the server with "debug_promise" : true
    then make a request to /debug_promise, it will wait 20 secs for an answer but the
    server is responsive in the meantime (any other request work while waiting).
    But this tests does not reflect that behavior: the log shows
    'Launching first request!', then 'Launching second request!', then waits,
    then the first request is answered and then sometimes the second request is
    answered an other times it spits an exception about a closed channel (probably
    testCompleted() closes any pending request without subtleness).

    Until I find an explanation I will mark this test as fail(), just to remember
    there is something fishy going on and would be cool to know what.
     */

    fail("TODO")
    return

    // This gets the first request going
    container.logger.info("Launching first request!")
    client.getNow(path, { response ->
        container.logger.info("First request answered.")
        // This code will execute around 20 secs after the getNow call was performed
        //assertTrue(myMap.get("secondRequestEnded"))
        testComplete()
    })

    // Wait for 2 seconds
    vertx.setTimer(2 * 1000, {long time ->
        // And then do another request that should end before the first one.
        container.logger.info("Launching second request!")
        client.getNow("/doc", { response ->
            container.logger.info("Second request answered.")
            //myMap.put("secondRequestEnded", true)
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