package es.us.dad.gameregistry.test.integration.java;

import es.us.dad.gameregistry.client.GameRegistryClient;
import es.us.dad.gameregistry.client.GameRegistryResponse;
import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.testtools.TestVerticle;
import org.vertx.testtools.VertxAssert;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import static org.vertx.testtools.VertxAssert.*;

/**
 * Example Java integration test that deploys the module that this project builds.
 *
 * Quite often in integration tests you want to deploy the same module for all tests and you don't want tests
 * to start before the module has been deployed.
 *
 * This test demonstrates how to do that.
 */
public class ClientIntegrationTest extends TestVerticle {

    @Test
    public void testClientCreateSession() throws UnknownHostException {
        GameRegistryClient client = new GameRegistryClient(InetAddress.getLocalHost(), vertx);
        client.setUser("test");
        client.setToken("test");
        client.addSession(new Date(), new Date(), new Handler<GameRegistryResponse>() {

            @Override
            public void handle(GameRegistryResponse event) {
                testComplete();
            }

        });
    }

    @Override
    public void start() {
        // Make sure we call initialize() - this sets up the assert stuff so assert functionality works correctly
        initialize();
        // Deploy the module - the System property `vertx.modulename` will contain the name of the module so you
        // don't have to hardecode it in your tests
        container.deployModule(System.getProperty("vertx.modulename"), new AsyncResultHandler<String>() {
            @Override
            public void handle(AsyncResult<String> asyncResult) {
                // Deployment is asynchronous and this this handler will be called when it's complete (or failed)
                if (asyncResult.failed()) {
                    container.logger().error(asyncResult.cause());
                }
                assertTrue(asyncResult.succeeded());
                assertNotNull("deploymentID should not be null", asyncResult.result());
                // If deployed correctly then start the tests!
                startTests();
            }
        });
    }

}