package es.us.dad.gameregistry.server.controller

import com.darylteo.vertx.promises.groovy.Promise
import es.us.dad.gameregistry.server.exception.AuthenticationException
import es.us.dad.gameregistry.server.exception.InvalidJsonBodyException
import es.us.dad.gameregistry.server.exception.RestException
import es.us.dad.gameregistry.server.service.ILoginService
import es.us.dad.gameregistry.server.util.*
import es.us.dad.gameregistry.shared.GameRegistryConstants
import es.us.dad.gameregistry.shared.domain.DomainObject
import groovy.json.JsonOutput
import io.netty.handler.codec.http.HttpResponseStatus
import org.vertx.groovy.core.buffer.Buffer
import org.vertx.groovy.core.http.HttpServerRequest
import org.vertx.groovy.core.http.RouteMatcher
import org.vertx.java.core.json.DecodeException
import org.vertx.java.core.json.JsonObject

import java.lang.annotation.Annotation
import java.lang.reflect.Method

class Controller {

    private final ILoginService loginService

    public Controller(ILoginService loginService) {
        this.loginService = loginService
    }

    protected
    static void sendJsonResponse(HttpServerRequest request, Object jsonResponse, HttpResponseStatus responseStatus) {
        request.response.putHeader("Content-Type", "application/json")
        request.response.setStatusCode(responseStatus.code())

        if (jsonResponse)
            request.response.end(JsonOutput.toJson(jsonResponse))
        else
            request.response.end()
    }

    protected
    static void sendJsonResponse(HttpServerRequest request, DomainObject jsonResponse, HttpResponseStatus responseStatus) {
        sendJsonResponse(request, jsonResponse?.toJsonMap(), responseStatus)
    }

    protected
    static void sendJsonResponse(HttpServerRequest request, Exception exception, HttpResponseStatus responseStatus) {
        sendJsonResponse(request, ["error": exception.message], responseStatus)
    }

    protected static void sendJsonResponse(HttpServerRequest request, DomainObject jsonResponse) {
        sendJsonResponse(request, jsonResponse?.toJsonMap(), HttpResponseStatus.OK)
    }

    protected static void sendJsonResponse(HttpServerRequest request, Map jsonResponse) {
        sendJsonResponse(request, jsonResponse, HttpResponseStatus.OK)
    }

    protected static void sendErrorResponse(HttpServerRequest request, Exception ex) {
        if (ex instanceof RestException)
            sendJsonResponse(request, ex, ex.responseStatus)
        else
            sendJsonResponse(request, ex, HttpResponseStatus.INTERNAL_SERVER_ERROR)
    }

    protected String getCurrentUser(HttpServerRequest request) {
        return request.headers.get(GameRegistryConstants.GAMEREGISTRY_USER_HEADER)
    }

    protected String getCurrentToken(HttpServerRequest request) {
        return request.headers.get(GameRegistryConstants.GAMEREGISTRY_TOKEN_HEADER)
    }

    protected void requireAuthentication(HttpServerRequest request, Closure authenticatedFunction) {
        String user = getCurrentUser(request)
        String token = getCurrentToken(request)

        Promise<Boolean> authenticated = loginService.isAuthenticated(user, token)
        authenticated.then({ boolean isAuthenticated ->
            if (isAuthenticated)
                authenticatedFunction.call()
            else
                sendErrorResponse(request, new AuthenticationException())
        }).fail({ Exception ex ->
            sendErrorResponse(request, ex)
        })
    }

    protected Promise<JsonObject> getRequestBody(HttpServerRequest request) {
        Promise<JsonObject> p = new Promise<JsonObject>()

        request.bodyHandler { Buffer buffer ->
            try {
                JsonObject body = new JsonObject(buffer.toString())
                p.fulfill(body)
            }
            catch(DecodeException ignored) {
                p.reject(new InvalidJsonBodyException())
            }
        }

        return p
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
                    } else {
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
