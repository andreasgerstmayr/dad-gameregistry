package es.us.dad.gameregistry.server.service

import com.darylteo.vertx.promises.groovy.Promise

interface ILoginService {

    Promise<Boolean> isAuthenticated(String user, String token)

}