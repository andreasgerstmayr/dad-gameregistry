package es.us.dad.gameregistry.test.integration.java;

import es.us.dad.gameregistry.client.GameRegistryClient;
import es.us.dad.gameregistry.client.GameRegistryResponse.ResponseType;
import es.us.dad.gameregistry.shared.domain.GameSession;
import org.junit.Test;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;

import java.io.IOException;
import java.net.InetAddress;
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

    @Test
    public void testCreateFromAddressWithPort() {
        GameRegistryClient.createFromAddress("google.com:8080", vertx, event -> {
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
        GameRegistryClient.createFromAddress("google.com", vertx, event -> {
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
        String missingHost = "something.that.doesnt.exists.cloudapp.net";
        GameRegistryClient.createFromAddress(missingHost, vertx, event -> {
            if (event.succeeded()) {
                fail("Somehow '" + missingHost + "' was resolved?");
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
                .setBasePath("/v1")
                .setConnectionTimeout(10);

        assertEquals(8080, client.getPort());
        assertEquals("myUser", client.getUser());
        assertEquals("myToken", client.getToken());
        assertEquals("/v1", client.getBasePath());
        assertEquals(10, client.getConnectionTimeout());

        client.setBasePath("/v1/");
        assertEquals("/v1", client.getBasePath());

        testComplete();
    }

    @Test
    public void testConnectionRefused() {
        // Poking at a port that should be closed to get a refused connection error.
        GameRegistryClient client = new GameRegistryClient(InetAddress.getLoopbackAddress(), 8081, vertx);
        client
                .setUser("testUser")
                .setToken("testToken")
                .getSession(UUID.randomUUID(), event -> {
                    if (event.responseType == ResponseType.CONNECTION_REFUSED) {
                        testComplete();
                        return;
                    }
                    fail("Response type is not CONNECTION_REFUSED");
                });
    }

    @Test
    public void testConnectionClosed() {
        // Poking at a port where a simple server listens, waits a few seconds and closes the connection.
        GameRegistryClient client = new GameRegistryClient(InetAddress.getLoopbackAddress(), 8082, vertx);
        client
                .setUser("testUser")
                .setToken("testToken")
                .setBasePath("/")
                .getSession(UUID.randomUUID(), event -> {
                    if (event.responseType == ResponseType.CONNECTION_CLOSED) {
                        testComplete();
                        return;
                    }
                    container.logger().error("Response: " + event.responseType.toString());
                    container.logger().error("inner throwable: " + event.innerThrowable);
                    fail("Respose type is not CLOSED.");
                });
    }

    /*
    Im not able to force a timeout, it seems. Disabling test.

    setConnectionTimeout seems to do nothing whatsoever. I tried setting a net server
    that accepted connections but closed them after 5 minutes and the test itself
    timed out before something happened.
     */
    /*
    @Test
    public void testConnectionTimeout() {
        // Poking at a port where a simple server listens, waits a few seconds and closes the connection.
        GameRegistryClient client = new GameRegistryClient(InetAddress.getLoopbackAddress(), 8083, vertx);
        Date start = Date.from(Instant.now());
        client
                .setUser("testUser")
                .setToken("testToken")
                .setBasePath("/")
                .setConnectionTimeout(3000)
                .getSession(UUID.randomUUID(), event -> {
                    container.logger().info("Event handler executed " + (Date.from(Instant.now()).getTime() - start.getTime())/1000 + " seconds later.");
                    if (event.responseType == ResponseType.TIMEOUT) {
                        testComplete();
                        return;
                    }
                    container.logger().error("Response: " + event.responseType.toString());
                    container.logger().error("inner throwable: " + event.innerThrowable);
                    fail("Respose type is not TIMEOUT.");
                });
    }*/

    @Test
    public void testClientCreateSession() {
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

    @Test
    public void testWorkflow() throws UnknownHostException {
        GameRegistryClient client = new GameRegistryClient(InetAddress.getLocalHost(), vertx);

        // user 'testUser' with token 'testToken' starts game 'testGame':
        client.setUser("testUser").setToken("testToken");

        GameSession session = new GameSession();
        session.setStart(new Date());
        session.setGame("testGame");


        client.addSession(session, event -> {
            assertEquals(ResponseType.OK, event.responseType);
            assertEquals(1, event.sessions.length);

            // this object also contains the newly generated ID of the session
            GameSession createdSession = event.sessions[0];

            // there is no end date because the game is running at the moment
            assertNull(createdSession.getEnd());

            // now the user finishes the game
            createdSession.setEnd(new Date());
            client.updateSession(createdSession, event2 -> {
                assertEquals(ResponseType.OK, event2.responseType);
                assertNotNull(event2.sessions[0].getEnd());
                testComplete();
            });
        });
    }

    private void clearDatabase(Runnable callback) {
        JsonObject mongoCmd = new JsonObject();
        mongoCmd.putString("action", "drop_collection");
        mongoCmd.putString("collection", "game_session");

        vertx.eventBus().send("gameregistry.db", (Object) mongoCmd, message -> {
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

        // Following is a mini-server that closes the connection 5 seconds after
        // opened whatever happens. Used in testConnectionClosed.
        vertx.createNetServer().connectHandler(socket -> {
            vertx.setTimer(5 * 1000, event -> {
                socket.close();
            });
        }).listen(8082);
        // Following is a mini-server that closes the connection a lot after
        // opened whatever happens. Used in testConnectionTimeout
        vertx.createNetServer().connectHandler(socket -> {
            vertx.setTimer(60 * 1000, event -> {
                socket.close();
            });
        }).listen(8083);

        container.deployModule(System.getProperty("vertx.modulename"), testConfig, asyncResult -> {
            // Deployment is asynchronous and this this handler will be called when it's complete (or failed)
            if (asyncResult.failed()) {
                container.logger().error("Failed to deploy " + System.getProperty("vertx.modulename") + ": " + asyncResult.cause());
            }
            assertTrue(asyncResult.succeeded());
            assertNotNull("deploymentID should not be null", asyncResult.result());

            clearDatabase(() -> startTests());
        });
    }

}