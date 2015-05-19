package es.us.dad.gameregistry.server.service

import com.darylteo.vertx.promises.groovy.Promise
import org.vertx.groovy.core.Vertx
import org.vertx.groovy.core.http.HttpServerRequest
import org.vertx.groovy.core.http.HttpServerResponse
import org.vertx.groovy.core.http.RouteMatcher

/**
 * This has to wait artificially a lot (20 secs) before fulfilling the
 * promise of some method. See RestServer.
 */
class DebugPromiseService {
    private int wait_time = 20
    private Vertx vertx

    DebugPromiseService(int wait_time, Vertx vertx) {
        this.wait_time = wait_time
        this.vertx = vertx
    }

    Promise<String> doSomething(HttpServerRequest request) {
        final Promise<HttpServerResponse> p = new Promise()

        vertx.setTimer(wait_time * 1000, { time ->
            p.fulfill("<html><body>Waited " + wait_time + "seconds to give this answer.</body></html>")
        })

        return p;
    }

    void registerUrls(RouteMatcher rm) {
        rm.get("/debug_promise", { HttpServerRequest request ->
            this.doSomething().then({ String html ->
                HttpServerResponse response = request.response.setStatusCode(200)
                response.headers.set("content-type", "text/html")
                response.end(html)
            })
        })
    }
}
