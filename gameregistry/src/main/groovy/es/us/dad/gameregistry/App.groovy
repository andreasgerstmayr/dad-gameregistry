package es.us.dad.gameregistry

import org.vertx.groovy.platform.Verticle


class App extends Verticle {

    def start() {
		def mongoConfig = container.getConfig().'mongo-persistor'

        // start all verticles or modules
		container.deployModule("io.vertx~mod-mongo-persistor~2.1.1", mongoConfig)
		
        // java toyrestserver
        container.deployVerticle(ToyRestServer.class.getName())

        // groovy test rest server
        container.deployVerticle("groovy:" + RestServer.class.getName())

    }
}