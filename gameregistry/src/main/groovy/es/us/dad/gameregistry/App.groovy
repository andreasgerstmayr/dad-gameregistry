package es.us.dad.gameregistry

import org.vertx.groovy.platform.Verticle


class App extends Verticle {

    def start() {
        container.logger.info("Starting GameRegistry...")

        Map<String, Object> appConfig = container.config
        Map<String, Object> mongoConfig = appConfig.getOrDefault("mongo-persistor", [:]) as Map<String, Object>
        Map<String, Object> gameRegistryConfig = appConfig.getOrDefault("game-registry", [:]) as Map<String, Object>

        // deploy mongodb persistor
		container.deployModule("io.vertx~mod-mongo-persistor~2.1.1", mongoConfig, { asyncResult ->
            if(asyncResult.failed) {
                container.logger.error("Can't deploy mongo db persistor:")
                container.logger.error(asyncResult.cause())
            }
        })
		
        // java toyrestserver
        container.deployVerticle(ToyRestServer.class.getName())

        // deploy groovy test rest server
        container.deployVerticle("groovy:" + RestServer.class.getName(), gameRegistryConfig, { asyncResult ->
            if(asyncResult.failed) {
                container.logger.error("Can't deploy game registry:")
                container.logger.error(asyncResult.cause())
            }
        })

    }
}