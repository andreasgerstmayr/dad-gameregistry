package es.us.dad.gameregistry.controller

import es.us.dad.gameregistry.GameRegistryConstants
import es.us.dad.gameregistry.service.LoginService
import groovy.json.JsonBuilder
import io.netty.handler.codec.http.HttpResponseStatus
import org.vertx.groovy.core.http.HttpServerRequest

class RestController extends Controller {

    // TODO: dependency injection
    private final LoginService loginService

    public RestController(LoginService loginService) {
        this.loginService = loginService
    }

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

    protected boolean validateUserAuthentication(HttpServerRequest request) {
        String user = request.headers.get(GameRegistryConstants.GAMEREGISTRY_USER_HEADER)
        String token = request.headers.get(GameRegistryConstants.GAMEREGISTRY_USER_HEADER)

        if (loginService.isAuthenticated(user, token)) {
            // all good, do nothing
            return true
        }
        else {
            sendJsonResponse(request, HttpResponseStatus.FORBIDDEN)
            return false
        }
    }
}
