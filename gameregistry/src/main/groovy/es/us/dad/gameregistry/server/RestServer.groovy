package es.us.dad.gameregistry.server

import es.us.dad.gameregistry.server.controller.SessionController
import es.us.dad.gameregistry.server.controller.SessionsController
import es.us.dad.gameregistry.server.controller.StaticFilesController
import es.us.dad.gameregistry.server.repository.ISessionRepository
import es.us.dad.gameregistry.server.repository.MongoSessionRepository
import es.us.dad.gameregistry.server.service.ILoginService
import es.us.dad.gameregistry.server.service.LoginServiceMock
import es.us.dad.gameregistry.server.service.SessionService
import es.us.dad.gameregistry.server.service.StaticFilesService
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

        ILoginService loginService = new LoginServiceMock()
        ISessionRepository sessionRepository = new MongoSessionRepository(vertx, container.logger)
        SessionService sessionService = new SessionService(vertx, container.logger, sessionRepository)
        StaticFilesService fileService = new StaticFilesService("web", vertx)

        // create instances of all controllers and register the URLs to the RouteMatcher
        new SessionsController(loginService, sessionService).registerUrls(rm)
        new SessionController(loginService, sessionService).registerUrls(rm)
        new StaticFilesController("/doc", fileService, container.logger).registerUrls(rm)

        vertx.createHttpServer().requestHandler(rm.asClosure()).listen(port, host)
        container.logger.info("GameRegistry REST Server ready, listening on ${host}:${port}.")
    }
}
