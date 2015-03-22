package es.us.dad.gameregistry

import org.vertx.groovy.core.http.HttpServerRequest
import org.vertx.groovy.platform.Verticle


class RestServer extends Verticle {

    def start() {

        vertx.createHttpServer().requestHandler { HttpServerRequest req ->
            req.response.end("hello from groovy code")
        }.listen(8080)

    }
}
