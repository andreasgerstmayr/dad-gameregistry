package gameregistry;

import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.platform.Verticle;

public class ToyRestServer extends Verticle {
  public void start() {
    vertx.createHttpServer().requestHandler(new Handler<HttpServerRequest>() {
      @Override
      public void handle(HttpServerRequest httpServerRequest) {
        httpServerRequest.response().end("Hello world.");
      }
    }).listen(1080);

    container.logger().info("Rest server started, listening on 1080.");
  }
}
