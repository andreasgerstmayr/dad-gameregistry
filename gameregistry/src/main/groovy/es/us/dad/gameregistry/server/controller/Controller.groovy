package es.us.dad.gameregistry.server.controller

import com.darylteo.vertx.promises.groovy.Promise
import es.us.dad.gameregistry.GameRegistryConstants
import es.us.dad.gameregistry.server.domain.DomainObject
import es.us.dad.gameregistry.server.service.ILoginService
import es.us.dad.gameregistry.server.util.Authenticated
import es.us.dad.gameregistry.server.util.DELETE
import es.us.dad.gameregistry.server.util.GET
import es.us.dad.gameregistry.server.util.POST
import es.us.dad.gameregistry.server.util.PUT
import groovy.json.JsonOutput
import io.netty.handler.codec.http.HttpResponseStatus
import org.vertx.groovy.core.http.HttpServerRequest
import org.vertx.groovy.core.http.RouteMatcher

import java.lang.annotation.Annotation
import java.lang.reflect.Method

class Controller {

    // TODO: dependency injection
    private final ILoginService loginService

    public Controller(ILoginService loginService) {
        this.loginService = loginService
    }

    protected static void sendJsonResponse(HttpServerRequest request, DomainObject jsonResponse, HttpResponseStatus responseStatus) {
        request.response.putHeader("Content-Type", "application/json")
        request.response.setStatusCode(responseStatus.code())

        if (jsonResponse)
            request.response.end(JsonOutput.toJson(jsonResponse.toJsonMap()))
        else
            request.response.end()
    }

    protected static void sendJsonResponse(HttpServerRequest request, DomainObject jsonResponse) {
        sendJsonResponse(request, jsonResponse, HttpResponseStatus.OK)
    }

    protected static void sendJsonResponse(HttpServerRequest request, HttpResponseStatus responseStatus) {
        sendJsonResponse(request, null, responseStatus)
    }

    protected void requireAuthentication(HttpServerRequest request, Closure authenticatedFunction) {
        String user = request.headers.get(GameRegistryConstants.GAMEREGISTRY_USER_HEADER)
        String token = request.headers.get(GameRegistryConstants.GAMEREGISTRY_USER_HEADER)

        Promise<Boolean> authenticated = loginService.isAuthenticated(user, token)
        authenticated.then({ boolean isAuthenticated ->
            if (isAuthenticated)
                authenticatedFunction.call()
            else
                sendJsonResponse(request, HttpResponseStatus.FORBIDDEN)
        }, { Exception ex ->
            sendJsonResponse(request, HttpResponseStatus.INTERNAL_SERVER_ERROR)
        })
    }

    public void registerUrls(RouteMatcher routeMatcher) {
        for (Method method : this.class.declaredMethods) {
            Authenticated authenticationRequired = method.getAnnotation(Authenticated.class)

            for (Annotation annotation : method.declaredAnnotations) {
                // create local variable so the method of the current iteration gets captured inside the closure and
                // not the variable of the last loop iteration
                // see http://blog.freeside.co/2013/03/29/groovy-gotcha-for-loops-and-closure-scope/
                Method myMethod = method

                Closure closure = { HttpServerRequest request ->
                    if (authenticationRequired != null) {
                        requireAuthentication(request, {
                            myMethod.invoke(this, request)
                        })
                    }
                    else {
                        // no authentication required
                        myMethod.invoke(this, request)
                    }
                }

                if (annotation instanceof GET) {
                    routeMatcher.get(annotation.value(), closure)
                } else if (annotation instanceof POST) {
                    routeMatcher.post(annotation.value(), closure)
                } else if (annotation instanceof PUT) {
                    routeMatcher.put(annotation.value(), closure)
                } else if (annotation instanceof DELETE) {
                    routeMatcher.delete(annotation.value(), closure)
                }
            }
        }
    }

}
