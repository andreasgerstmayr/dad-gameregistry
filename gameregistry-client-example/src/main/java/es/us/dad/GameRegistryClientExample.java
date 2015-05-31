package es.us.dad;

import es.us.dad.gameregistry.client.GameRegistryClient;
import es.us.dad.gameregistry.client.GameRegistryResponse;
import es.us.dad.gameregistry.shared.domain.GameSession;
import org.vertx.java.core.Handler;
import org.vertx.java.platform.Verticle;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class GameRegistryClientExample extends Verticle {
    private void finishGame(GameRegistryClient client, String user, String token, GameSession session) {
        // store game result
        Map<String,Object> gameResult = new HashMap<String,Object>();
        gameResult.put("points", 10);

        client.setUser(user).setToken(token);
        container.logger().info("update game session...");
        client.finishSession(session.getId(), gameResult, new Handler<GameRegistryResponse>() {

            @Override
            public void handle(GameRegistryResponse event) {
                container.logger().info("response: " + event.responseType);
            }

        });
    }

    private void startGame(final GameRegistryClient client, final String user, final String token) {
        GameSession session = new GameSession();
        session.setStart(new Date());
        session.setGame("testGame");

        client.setUser(user).setToken(token);
        container.logger().info("create new game session...");
        client.addSession(session, new Handler<GameRegistryResponse>() {
            @Override
            public void handle(GameRegistryResponse event) {
                container.logger().info("response: "+event.responseType);

                // this object also contains the newly generated ID of the session
                GameSession createdSession = event.sessions[0];

                finishGame(client, user, token, createdSession);
            }
        });
    }

    @Override
    public void start() {
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

}
