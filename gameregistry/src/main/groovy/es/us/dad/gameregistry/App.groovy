package es.us.dad.gameregistry

import org.vertx.groovy.platform.Verticle


class App extends Verticle {

    def start() {

        // start all verticles


        // java toyrestserver
        container.deployVerticle(ToyRestServer.class.getName())

        // groovy test rest server
        container.deployVerticle("groovy:" + RestServer.class.getName())

    }
}