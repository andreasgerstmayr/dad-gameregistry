package es.us.dad.gameregistry.test.integration.java;

import es.us.dad.gameregistry.client.GameRegistryClient;
import es.us.dad.gameregistry.client.GameRegistryResponse;
import es.us.dad.gameregistry.client.GameRegistryResponse.ResponseType;
import es.us.dad.gameregistry.server.domain.GameSession;
import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.Handler;
import org.vertx.testtools.TestVerticle;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.UUID;

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

    // TODO Test createFromAddress
    @Test
    public void testCreateFromAddressWithPort() {
        GameRegistryClient.createFromAddress("gameregistry.cloudapp.net:8080", vertx, event -> {
            if (event.succeeded()) {
                GameRegistryClient client = event.result();
                assertEquals(client.getBasePath(), "/api/v1");
                assertEquals(8080, client.getPort());

                testComplete();
            }
            else {
                fail("Failed to resolve DNS name: " + event.cause().toString());
            }
        });
    }

    @Test
    public void testCreateFromAddressWithoutPort() {
        GameRegistryClient.createFromAddress("gameregistry.cloudapp.net", vertx, event -> {
            if (event.succeeded()) {
                GameRegistryClient client = event.result();
                assertEquals(8080, client.getPort());

                testComplete();
            }
            else {
                fail("Failed to resolve DNS name: " + event.cause().toString());
            }
        });
    }

    @Test
    public void testCreateFromWrongAddress() {
        GameRegistryClient.createFromAddress("something.that.dont.exists.wrong", vertx, event -> {
            if (event.succeeded()) {
                fail("Somehow 'something.that.dont.exists.wrong' was resolved?");
            }
            else {
                assertTrue(event.cause() instanceof UnknownHostException);
                testComplete();
            }
        });
    }

    @Test
    public void testBasicProperties() {
        GameRegistryClient client = new GameRegistryClient(InetAddress.getLoopbackAddress(), vertx)
                .setUser("myUser")
                .setToken("myToken")
                .setBasePath("/v1");

        assertEquals(8080, client.getPort());
        assertEquals("myUser", client.getUser());
        assertEquals("myToken", client.getToken());
        assertEquals("/v1", client.getBasePath());

        client.setBasePath("/v1/");
        assertEquals("/v1", client.getBasePath());

        testComplete();
    }

    // TODO setToken, setUser, setBasePath

    @Test
    public void testClientCreateSession() throws UnknownHostException {
        GameRegistryClient client = new GameRegistryClient(InetAddress.getLoopbackAddress(), vertx);
        client.setUser("testUser");
        client.setToken("test");
        
        final GameSession session = new GameSession();
        session.setStart(new Date());
        session.setEnd(new Date(session.getStart().getTime() + 1000*60*10)); // Ten minutes after start
        session.setGame("testGame");
        //session.setUser("testUser");
        
        client.addSession(session, event -> {
            assertEquals(ResponseType.OK, event.responseType);
            assertNotNull(event.sessions);
            assertEquals(1, event.sessions.length);
            assertEquals("testGame", event.sessions[0].getGame());
            assertEquals("testUser", event.sessions[0].getUser());

            testComplete();
        });
    }
    
    @Test
    public void testClientGetSession() throws UnknownHostException {
    	GameRegistryClient client = new GameRegistryClient(InetAddress.getLocalHost(), vertx)
    		.setUser("testUser")
    		.setToken("test");

        // Create a sesion
        final GameSession session = new GameSession();
        session.setStart(new Date());
        session.setEnd(new Date(session.getStart().getTime() + 1000 * 60 * 10)); // Ten minutes after start
        session.setGame("testGame");

        // Add the session
        client.addSession(session, event -> {
            // And ask the server for a session with the id of the added session
            assertEquals(ResponseType.OK, event.responseType);
            assertEquals(1, event.sessions.length);
            UUID id = event.sessions[0].getId();
            client.getSession(id, event2 -> {
                assertEquals(ResponseType.OK, event2.responseType);
                assertNotNull(event2.sessions);
                assertEquals(1, event2.sessions.length);

                GameSession receivedSession = event2.sessions[0];
                assertEquals(id, receivedSession.getId());
                assertEquals("testUser", receivedSession.getUser());
                assertEquals("testGame", receivedSession.getGame());

                testComplete();
            });
        });
    }

    @Test
    public void testClientGetSessionNotFound() throws UnknownHostException {
        GameRegistryClient client = new GameRegistryClient(InetAddress.getLocalHost(), vertx)
                .setUser("testUser")
                .setToken("test");

        UUID id = UUID.randomUUID();
        client.getSession(id, event -> {
            assertEquals(ResponseType.SESSION_NOT_FOUND, event.responseType);
            assertEquals(0, event.sessions.length);

            testComplete();
        });
    }

    @Test
    public void testClientDeleteSession() throws UnknownHostException {
        GameRegistryClient client = new GameRegistryClient(InetAddress.getLocalHost(), vertx)
                .setUser("testUser")
                .setToken("test");

        // Create a sesion
        final GameSession session = new GameSession();
        session.setStart(new Date());
        session.setEnd(new Date(session.getStart().getTime() + 1000 * 60 * 10)); // Ten minutes after start
        session.setGame("testGame");

        // Add the session
        client.addSession(session, event -> {
            // And ask the server for a session with the id of the added session
            assertEquals(ResponseType.OK, event.responseType);
            assertEquals(1, event.sessions.length);
            UUID id = event.sessions[0].getId();
            client.deleteSession(id, event2 -> {
                assertEquals(ResponseType.OK, event2.responseType);
                assertNotNull(event2.sessions);
                assertEquals(0, event2.sessions.length);

                testComplete();
            });
        });
    }

    @Test
    public void testClientDeleteSessionNotFound() throws UnknownHostException {
        GameRegistryClient client = new GameRegistryClient(InetAddress.getLocalHost(), vertx)
                .setUser("testUser")
                .setToken("test");

        UUID id = UUID.randomUUID();
        client.deleteSession(id, event -> {
            assertEquals(ResponseType.SESSION_NOT_FOUND, event.responseType);
            assertEquals(0, event.sessions.length);

            testComplete();
        });
    }

    private void clearDatabase(Runnable callback) {
        JsonObject mongoCmd = new JsonObject();
        mongoCmd.putString("action", "drop_collection");
        mongoCmd.putString("collection", "game_session");

        vertx.eventBus().send("gameregistry.db", (Object)mongoCmd, message -> {
            JsonObject messageBody = (JsonObject)message.body();
            assertEquals("ok", messageBody.getString("status"));
            callback.run();
        });
    }

    @Override
    public void start() {
        // Make sure we call initialize() - this sets up the assert stuff so assert functionality works correctly
        initialize();
        // Deploy the module - the System property `vertx.modulename` will contain the name of the module so you
        // don't have to hardecode it in your tests

        JsonObject testConfig = null;
        try {
            testConfig = new JsonObject(new String(Files.readAllBytes(Paths.get("conf-test.json"))));
        }
        catch (IOException ex) {
            container.logger().error("Could not read config file");
            assertTrue(false);
        }

        container.deployModule(System.getProperty("vertx.modulename"), testConfig, new AsyncResultHandler<String>() {
            @Override
            public void handle(AsyncResult<String> asyncResult) {
                // Deployment is asynchronous and this this handler will be called when it's complete (or failed)
                if (asyncResult.failed()) {
                    container.logger().error("Failed to deploy " + System.getProperty("vertx.modulename") + ": " + asyncResult.cause());
                }
                assertTrue(asyncResult.succeeded());
                assertNotNull("deploymentID should not be null", asyncResult.result());

                clearDatabase(() -> startTests());
            }
        });
    }

}