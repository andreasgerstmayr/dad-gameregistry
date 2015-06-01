package es.us.dad.integration.java;

import es.us.dad.gameregistry.client.GameRegistryClient;
import es.us.dad.gameregistry.client.GameRegistryResponse;
import es.us.dad.gameregistry.shared.domain.GameSession;
import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.vertx.testtools.VertxAssert.*;


public class GameRegistryClientExampleTest extends TestVerticle {


    private void listGames(GameRegistryClient client, String user, String token) {
        client.setUser(user).setToken(token);
        client.getSessions(null, new Handler<GameRegistryResponse>() {

            @Override
            public void handle(GameRegistryResponse event) {
                assertEquals(GameRegistryResponse.ResponseType.OK, event.responseType);

                container.logger().info("found " + event.sessions.length + " game sessions:");
                for (GameSession gameSession : event.sessions) {
                    container.logger().info("found game session: " + gameSession);
                }

                testComplete();
            }

        });
    }

    private void finishGame(final GameRegistryClient client, final String user, final String token, GameSession session) {
        // store game result
        Map<String,Object> gameResult = new HashMap<String,Object>();
        gameResult.put("points", 10);

        client.setUser(user).setToken(token);
        client.finishSession(session.getId(), gameResult, new Handler<GameRegistryResponse>() {

            @Override
            public void handle(GameRegistryResponse event) {
                assertEquals(GameRegistryResponse.ResponseType.OK, event.responseType);
                assertNotNull(event.sessions[0].getEnd());

                container.logger().info("game session was updated.");
                listGames(client, user, token);
            }

        });
    }

    private void startGame(final GameRegistryClient client, final String user, final String token) {
        client.setUser(user).setToken(token);
        client.addSession("testGame", new Handler<GameRegistryResponse>() {
            @Override
            public void handle(GameRegistryResponse event) {
                assertEquals(GameRegistryResponse.ResponseType.OK, event.responseType);
                assertEquals(1, event.sessions.length);

                container.logger().info("new game session was created.");

                // this object also contains the newly generated ID of the session
                GameSession createdSession = event.sessions[0];

                // there is no end date because the game is running at the moment
                assertNull(createdSession.getEnd());

                finishGame(client, user, token, createdSession);
            }
        });
    }

    @Test
    public void testMain() {
        container.logger().info("GameRegistryClientExample started");

        InetAddress localhost = null;
        try {
            localhost = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return;
        }

        GameRegistryClient client = new GameRegistryClient(localhost, vertx);
        startGame(client, "testUser", "testToken");
    }

    /**
     * try to deploy GameRegistry module (needed for tests).
     * it's also possible to start the gameregistry server manually
     * @param callback
     */
    private void deployGameRegistryModule(final Handler<Void> callback) {
        JsonObject testConfig = null;
        try {
            testConfig = new JsonObject(new String(Files.readAllBytes(Paths.get("../gameregistry/conf-test.json"))));
        }
        catch (IOException ex) {
            container.logger().info("Could not read config file");
            callback.handle(null);
            return;
        }

        container.deployModule("es.us.dad~gameregistry~0.0.1", testConfig, new Handler<AsyncResult<String>>() {
            @Override
            public void handle(AsyncResult<String> result) {
                if(result.failed()) {
                    container.logger().info("Could not deploy gameregistry module:");
                    container.logger().info(result.cause());
                }
                callback.handle(null);
            }
        });
    }

    @Override
    public void start() {
        initialize();
        deployGameRegistryModule(new Handler<Void>() {
            @Override
            public void handle(Void aVoid) {
                startTests();
            }
        });
    }

}
