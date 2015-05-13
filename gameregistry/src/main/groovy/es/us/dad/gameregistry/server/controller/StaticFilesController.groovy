package es.us.dad.gameregistry.server.controller

import com.darylteo.vertx.promises.groovy.Promise
import es.us.dad.gameregistry.server.service.StaticFilesService
import org.vertx.groovy.core.http.HttpServerRequest
import org.vertx.groovy.core.http.RouteMatcher
import org.vertx.java.core.logging.Logger

class StaticFilesController {
    final private StaticFilesService fileService
    final private String base_path = "/api/v1/api-docs"
    final private Logger logger

    public StaticFilesController(String base_path, StaticFilesService fileService, Logger logger) {
        this.fileService = fileService
        this.base_path = base_path
        this.logger = logger

        if (this.base_path[this.base_path.length()-1] == '/')
            this.base_path = this.base_path.substring(this.base_path.length()-2)
    }

    public StaticFilesController(StaticFilesService fileService) {
        this.fileService = fileService
    }

    public void registerUrls(RouteMatcher routeMatcher) {
        String regexp = base_path.replaceAll("\\/", "\\\\\\/") + "\\/(.*)"
        //String regexp = "\\/api\\/v1\\/api-doc\\/(.*)"
        logger.info("Static file server bound to ${regexp}")
        routeMatcher.allWithRegEx(regexp, { HttpServerRequest request ->
            logger.info("Received static file request: " + request.path)
            String withoutBasePath = request.path.substring(base_path.length())

            fileService.getSystemPathOf(withoutBasePath).then({ String system_path ->
                request.response.sendFile(system_path)
            }, { Throwable ex ->
                request.response.setStatusCode(404)
                                .end()
            })
        })

        routeMatcher.get("/api/v2/api-docs/index.html", { HttpServerRequest request ->
            request.response.sendFile("web/index.html")
        })
    }
}
