package es.us.dad.gameregistry

import org.vertx.groovy.platform.Verticle
import org.vertx.java.core.Future


class App extends Verticle {

    def startMongoVerticle(Map<String, Object> mongoConfig, Closure deployResult) {
        // deploy mongodb persistor
        container.deployModule("io.vertx~mod-mongo-persistor~2.1.1", mongoConfig, { asyncResult ->
            if(asyncResult.failed) {
                container.logger.error("Can't deploy mongo db persistor:")
                container.logger.error(asyncResult.cause())
            }

            deployResult.call(asyncResult)
        })
    }

    def startRestServer(Map<String, Object> gameRegistryConfig, Closure deployResult) {
        // deploy groovy test rest server
        container.deployVerticle("groovy:" + RestServer.class.getName(), gameRegistryConfig, { asyncResult ->
            if(asyncResult.failed) {
                container.logger.error("Can't deploy game registry:")
                container.logger.error(asyncResult.cause())
            }

            deployResult.call(asyncResult)
        })
    }

    def start(Future<Void> startedResult) {
        container.logger.info("Starting GameRegistry...")

        Map<String, Object> appConfig = container.config
        Map<String, Object> mongoConfig = appConfig.getOrDefault("mongo-persistor", [:]) as Map<String, Object>
        Map<String, Object> gameRegistryConfig = appConfig.getOrDefault("game-registry", [:]) as Map<String, Object>

        startMongoVerticle(mongoConfig, { asyncResult ->
            if (asyncResult.failed) {
                startedResult.setFailure(asyncResult.result())
            }
            else {
                startRestServer(gameRegistryConfig, { asyncResult2 ->
                    if (asyncResult2.failed)
                        startedResult.setFailure(asyncResult2.result())
                    else
                        startedResult.setResult(Void.TYPE)
                })
            }
        })
    }
}