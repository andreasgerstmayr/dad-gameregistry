/*
 * Example Groovy integration test that deploys the module that this project builds.
 *
 * Quite often in integration tests you want to deploy the same module for all tests and you don't want tests
 * to start before the module has been deployed.
 *
 * This test demonstrates how to do that.
 */
import org.vertx.groovy.core.http.HttpClientResponse
import org.vertx.groovy.testtools.VertxTests
import org.vertx.java.core.json.JsonObject

import static org.vertx.testtools.VertxAssert.*

def testRestServerIsRunning() {
    // our module is deployed; no need to start the verticle here

    vertx.createHttpClient().setPort(8080).getNow("/", { HttpClientResponse resp ->
        assertEquals(404, resp.statusCode)
        testComplete()
    })
}


// Make sure you initialize
VertxTests.initialize(this)

Map<String, Object> testConfig = new JsonObject(new File('conf-test.json').getText('UTF-8')).toMap()

// The script is execute for each test, so this will deploy the module for each one
// Deploy the module - the System property `vertx.modulename` will contain the name of the module so you
// don't have to hardecode it in your tests
container.deployModule(System.getProperty("vertx.modulename"), testConfig, { asyncResult ->
    // Deployment is asynchronous and this this handler will be called when it's complete (or failed)
    assertTrue(asyncResult.succeeded)
    assertNotNull("deploymentID should not be null", asyncResult.result())
    // If deployed correctly then start the tests!
    VertxTests.startTests(this)
})
