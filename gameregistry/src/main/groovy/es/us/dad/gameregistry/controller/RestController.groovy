package es.us.dad.gameregistry.controller

import groovy.json.JsonBuilder
import io.netty.handler.codec.http.HttpResponseStatus
import org.vertx.groovy.core.http.HttpServerRequest

class RestController extends Controller {

    private static void sendJsonResponse(HttpServerRequest request, Object jsonResponse, HttpResponseStatus responseStatus) {
        request.response.putHeader("Content-Type", "application/json")
        request.response.setStatusCode(responseStatus.code())

        if (jsonResponse)
            request.response.end(new JsonBuilder(jsonResponse).toPrettyString())
        else
            request.response.end()
    }

    private static void sendJsonResponse(HttpServerRequest request, Object jsonResponse) {
        sendJsonResponse(request, jsonResponse, HttpResponseStatus.OK)
    }

    private static void sendJsonResponse(HttpServerRequest request, HttpResponseStatus responseStatus) {
        sendJsonResponse(request, null, responseStatus)
    }

}
