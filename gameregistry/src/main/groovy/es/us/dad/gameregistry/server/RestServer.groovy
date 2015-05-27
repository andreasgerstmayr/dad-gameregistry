package es.us.dad.gameregistry.server

import es.us.dad.gameregistry.server.controller.SessionController
import es.us.dad.gameregistry.server.controller.SessionsController
import es.us.dad.gameregistry.server.controller.StaticFilesController
import es.us.dad.gameregistry.server.repository.ISessionRepository
import es.us.dad.gameregistry.server.repository.MongoSessionRepository
import es.us.dad.gameregistry.server.service.DebugPromiseService
import es.us.dad.gameregistry.server.service.ILoginService
import es.us.dad.gameregistry.server.service.LoginServiceMock
import es.us.dad.gameregistry.server.service.SessionService
import es.us.dad.gameregistry.server.service.StaticFilesService
import org.vertx.groovy.core.http.RouteMatcher
import org.vertx.groovy.platform.Verticle

class RestServer extends Verticle {

    private final String DEFAULT_HOST = "localhost"
    private final int DEFAULT_PORT = 8080
    private final String DEFAULT_STATIC_WEB = "/doc";

    def start() {
        def config = container.config
        String host = config.getOrDefault("host", DEFAULT_HOST) as String
        int port = config.getOrDefault("port", DEFAULT_PORT) as int
        String staticWebBasePath = config.getOrDefault("static_web_basepath", DEFAULT_STATIC_WEB)
        boolean debug_promise = config.getOrDefault("debug_promise", false)
        long cleanup_interval = config.getOrDefault("cleanup_interval", 60*60) as long // seconds
        long gamesession_maxage = config.getOrDefault("gamesession_maxage", 60*60*24) as long // seconds

        RouteMatcher rm = new RouteMatcher()

        ILoginService loginService = new LoginServiceMock()
        ISessionRepository sessionRepository = new MongoSessionRepository(vertx, container.logger)
        SessionService sessionService = new SessionService(vertx, container.logger, sessionRepository)
        StaticFilesService fileService = new StaticFilesService("web", vertx)

        // This was asked by Pablo (the boss). He wants to see a test where a promise
        // is fullfilled after an artificial an exagerated wait time (around 20 secs)
        // and see if the server is able to answer requests while waiting. He wants to
        // ensure Promise is not blocking, basically.
        // If the conf.json doesnt say otherwise it wont even register its url.
        if (debug_promise)
            new DebugPromiseService(20, vertx).registerUrls(rm)

        // create instances of all controllers and register the URLs to the RouteMatcher
        new SessionsController(loginService, sessionService).registerUrls(rm)
        new SessionController(loginService, sessionService).registerUrls(rm)
        // The StaticFilesController should be the last registered controller becouse regular
        // expressions and might be bound to '/', catching any request even if a more specific
        // route exists (but was registered afterward).
        new StaticFilesController(staticWebBasePath, fileService, container.logger).registerUrls(rm)

        // start periodic cleanup task
        vertx.setPeriodic(cleanup_interval * 1000, {
            sessionService.cleanup(gamesession_maxage)
        })

        vertx.createHttpServer().requestHandler(rm.asClosure()).listen(port, host)
        container.logger.info("GameRegistry REST Server ready, listening on ${host}:${port}.")
    }
}
