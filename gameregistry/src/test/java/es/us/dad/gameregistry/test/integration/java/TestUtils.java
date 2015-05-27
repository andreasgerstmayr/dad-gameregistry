package es.us.dad.gameregistry.test.integration.java;

import org.vertx.java.core.Vertx;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.vertx.testtools.VertxAssert.assertEquals;
import static org.vertx.testtools.VertxAssert.assertTrue;

public class TestUtils {

    public static void clearDatabase(Vertx vertx, Runnable callback) {
        JsonObject mongoCmd = new JsonObject();
        mongoCmd.putString("action", "drop_collection");
        mongoCmd.putString("collection", "game_session");

        vertx.eventBus().send("gameregistry.db", (Object) mongoCmd, message -> {
            JsonObject messageBody = (JsonObject)message.body();
            assertEquals("ok", messageBody.getString("status"));
            callback.run();
        });
    }

    public static JsonObject readTestConfig(Logger logger) {
        try {
            return new JsonObject(new String(Files.readAllBytes(Paths.get("conf-test.json"))));
        }
        catch (IOException ex) {
            logger.error("Could not read config file");
            assertTrue(false);
            return null;
        }
    }

}
