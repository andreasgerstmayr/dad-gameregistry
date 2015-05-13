package es.us.dad.gameregistry.server

import org.vertx.groovy.platform.Verticle
import org.vertx.java.core.Future


class App extends Verticle {

    def startMongoVerticle(Map<String, Object> mongoConfig, Closure deployResult) {
		def mod_mongo = "io.vertx~mod-mongo-persistor~2.1.1"
		
		container.logger.info("Deploying module: ${mod_mongo}")
        // deploy mongodb persistor
        container.deployModule(mod_mongo, mongoConfig, { asyncResult ->
            if(asyncResult.failed) {
                container.logger.error("Can't deploy mongo db persistor:")
                container.logger.error(asyncResult.cause())
            }

            deployResult.call(asyncResult)
        })
    }

    def startRestServer(Map<String, Object> gameRegistryConfig, Closure deployResult) {
		def verticle_rest = "groovy:" + RestServer.class.getName()
		
		container.logger.info("Deploying GameRegistry REST server (${verticle_rest})...")
        // deploy groovy test rest server
        container.deployVerticle(verticle_rest, gameRegistryConfig, { asyncResult ->
            if(asyncResult.failed) {
                container.logger.error("Can't deploy ${verticle_rest}:")
                container.logger.error(asyncResult.cause())
            }

            deployResult.call(asyncResult)
        })
    }

    def start(Future<Void> startedResult) {
        container.logger.info("Starting...")

        Map<String, Object> appConfig = container.config
        Map<String, Object> mongoConfig = appConfig.getOrDefault("mongo-persistor", [:]) as Map<String, Object>
        Map<String, Object> gameRegistryConfig = appConfig.getOrDefault("game-registry", [:]) as Map<String, Object>

        startMongoVerticle(mongoConfig, { asyncResult2 ->
            if (asyncResult2.failed) {
                startedResult.setFailure(asyncResult2.result())
            }
            else {
                startRestServer(gameRegistryConfig, { asyncResult3 ->
                    if (asyncResult3.failed)
                        startedResult.setFailure(asyncResult3.result())
                    else
                        startedResult.setResult(null)
                })
            }
        })
    }
}