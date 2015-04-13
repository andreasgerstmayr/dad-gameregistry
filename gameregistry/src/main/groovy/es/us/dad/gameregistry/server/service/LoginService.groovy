package es.us.dad.gameregistry.server.service

class LoginService {

    public boolean isAuthenticated(String user, String token) {
        if (user == null || token == null)
            return false

        // TODO: REST call to login component
        return true
    }

}
