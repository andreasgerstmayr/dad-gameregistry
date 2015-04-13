package es.us.dad.gameregistry

import es.us.dad.gameregistry.controller.SessionController
import es.us.dad.gameregistry.controller.SessionsController
import es.us.dad.gameregistry.service.SessionService
import org.vertx.groovy.core.http.RouteMatcher
import org.vertx.groovy.platform.Verticle

class RestServer extends Verticle {

    private final String DEFAULT_HOST = "localhost"
    private final int DEFAULT_PORT = 8080

    def start() {
        def config = container.config
        String host = config.getOrDefault("host", DEFAULT_HOST) as String
        int port = config.getOrDefault("port", DEFAULT_PORT) as int

        RouteMatcher rm = new RouteMatcher()

        // create instances of all controllers and register the URLs to the RouteMatcher
        SessionService sessionService = new SessionService()
        new SessionsController(sessionService).registerUrls(rm)
        new SessionController(sessionService).registerUrls(rm)

        vertx.createHttpServer().requestHandler(rm.asClosure()).listen(port, host)
        container.logger.info("Started GameRegistry REST Server on ${host}:${port}.")
    }
}
