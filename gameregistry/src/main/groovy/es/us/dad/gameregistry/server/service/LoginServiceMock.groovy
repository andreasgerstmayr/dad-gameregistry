package es.us.dad.gameregistry.server.service

import com.darylteo.vertx.promises.groovy.Promise

class LoginServiceMock implements ILoginService {

    @Override
    Promise<Boolean> isAuthenticated(String user, String token) {
        final Promise<Boolean> p = new Promise()

        if (user == null || token == null) {
            p.fulfill(false)
        }

        // TODO: REST call to login component
        p.fulfill(true)

        return p
    }

}
