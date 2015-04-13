package es.us.dad.gameregistry.server.controller

import es.us.dad.gameregistry.server.util.DELETE
import es.us.dad.gameregistry.server.util.GET
import es.us.dad.gameregistry.server.util.POST
import es.us.dad.gameregistry.server.util.PUT
import org.vertx.groovy.core.http.HttpServerRequest
import org.vertx.groovy.core.http.RouteMatcher

import java.lang.annotation.Annotation
import java.lang.reflect.Method

class Controller {

    public void registerUrls(RouteMatcher routeMatcher) {
        for (Method method : this.class.declaredMethods) {
            for (Annotation annotation : method.declaredAnnotations) {
                // create local variable so the method of the current iteration gets captured inside the closure and
                // not the variable of the last loop iteration
                // see http://blog.freeside.co/2013/03/29/groovy-gotcha-for-loops-and-closure-scope/
                Method myMethod = method

                Closure closure = { HttpServerRequest request ->
                    myMethod.invoke(this, request)
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
