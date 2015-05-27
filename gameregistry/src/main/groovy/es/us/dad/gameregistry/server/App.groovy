package es.us.dad.gameregistry.server

import com.darylteo.vertx.promises.groovy.Promise
import org.vertx.groovy.platform.Verticle
import org.vertx.java.core.Future


class App extends Verticle {

    Promise<Void> startMongoVerticle(Map<String, Object> mongoConfig) {
		def mod_mongo = "io.vertx~mod-mongo-persistor~2.1.1"
        Promise<Void> p = new Promise<Void>()
		
		container.logger.info("Deploying module: ${mod_mongo}")
        // deploy mongodb persistor
        container.deployModule(mod_mongo, mongoConfig, { asyncResult ->
            if(asyncResult.failed)
                p.reject(asyncResult.cause())
            else
                p.fulfill(null)
        })

        return p
    }

    def startRestServer(Map<String, Object> gameRegistryConfig) {
		def verticle_rest = "groovy:" + RestServer.class.getName()
        Promise<Void> p = new Promise<Void>()

		container.logger.info("Deploying GameRegistry REST server (${verticle_rest})...")
        // deploy groovy test rest server
        container.deployVerticle(verticle_rest, gameRegistryConfig, { asyncResult ->
            if(asyncResult.failed)
                p.reject(asyncResult.cause())
            else
                p.fulfill(null)
        })

        return p
    }

    def start(Future<Void> startedResult) {
        container.logger.info("Starting...")

        Map<String, Object> appConfig = container.config
        Map<String, Object> mongoConfig = appConfig.getOrDefault("mongo-persistor", [:]) as Map<String, Object>
        Map<String, Object> gameRegistryConfig = appConfig.getOrDefault("game-registry", [:]) as Map<String, Object>

        startMongoVerticle(mongoConfig).then({
            return startRestServer(gameRegistryConfig)
        }).then({
            startedResult.setResult(null)
        }).fail({Exception ex ->
            container.logger.error("Error when starting app:")
            container.logger.error(ex)
            startedResult.setFailure(ex)
        })
    }
}