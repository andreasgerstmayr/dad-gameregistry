package es.us.dad.gameregistry

import es.us.dad.gameregistry.controller.SessionController
import org.vertx.groovy.core.http.RouteMatcher
import org.vertx.groovy.platform.Verticle

class RestServer extends Verticle {

    def start() {
        RouteMatcher rm = new RouteMatcher()

        // create instances of all controllers and register the URLs to the RouteMatcher
        new SessionController().registerUrls(rm)

        vertx.createHttpServer().requestHandler(rm.asClosure()).listen(8080)
        container.logger.info("Started GameRegistry REST Server on port 8080.")
    }
}
