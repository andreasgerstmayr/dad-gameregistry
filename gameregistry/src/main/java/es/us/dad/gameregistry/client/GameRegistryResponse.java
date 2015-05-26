package es.us.dad.gameregistry.client;

import java.util.ArrayList;
import java.util.List;

import es.us.dad.gameregistry.shared.domain.GameSession;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * Represents a response from a GameRegistry server.
 * 
 * @see GameRegistryClient
 */
public class GameRegistryResponse {
	/**
	 * Enumeration listing possible request outcomes.
	 */
	public enum ResponseType {
		/**
		 * Request was successful.
		 */
		OK,
		/**
		 * Server says 404 Not Found. Ussually means the sessions the client asked
         * for is not found but it can also means the server is not a GameRegistry
         * compatible server and the resource's path is not recognized.
		 */
		SESSION_NOT_FOUND,
		/**
		 * The request is invalid (e.g. invalid id).
		 */
		INVALID_REQUEST,
		/**
		 * The server did not answer to the request in time.
		 */
		TIMEOUT,
		/**
		 * Connection refused by the server
		 */
		CONNECTION_REFUSED,
		/**
		 * User-token pair invalid
		 */
		TOKEN_ERROR,
		/**
		 * 5xx http response code (ie Gateway Error, Internal Server Error, etc)
		 */
		SERVER_ERROR,
		/**
		 * Connection closed unexpectedly
		 */
		CONNECTION_CLOSED,
		/**
		 * Error while parsing the server's response. An invalid JSon,
         * an unexpected http status code (ie a 405 MethodNotAllowed in
         * a resource's path that should allow that method),.. It can also
         * means the GameRegistryClient has a bug and performed a
         * bad request.
		 */
		INVALID_RESPONSE,
		/**
		 * Unknown error, unexpected redirection, ...
		 */
		UNKNOWN
	}
	
	/**
	 * Indicates response's nature. 
	 */
	public ResponseType responseType;
	/**
	 * Any GameSession object returned by the server will be in this array.
	 */
	public GameSession[] sessions;
	/**
	 * The HttpClientResponse object returned by the server, if any.
     * Otherwise null.
	 */
	public HttpClientResponse innerHttpResponse;
	/**
	 * If the response is not OK and the error is motivated by a throwable
	 * this will have a reference to it. In other case it will be null.
	 */
	public Throwable innerThrowable;
	
	/**
	 * Builds a new GameRegistryResponse and sets it up as an UNKNOWN response type, null sessions 
	 * and null innerHttpResponse.
	 */
	GameRegistryResponse() {
		responseType = ResponseType.UNKNOWN;
		sessions = new GameSession[0];
		innerHttpResponse = null;
		innerThrowable = null;
	}
	
	/**
	 * Parses an HttpClientResponse returned by a GameRegistryServer.
	 * 
	 * The returned response's responseType field signals if the request
	 * was successful or not. If successful and depending on the request the response can
	 * include one or more GameSession objects that will be in the sessions field. 
	 * In the case responseType is not OK the sessions filed will be null.
	 * 
	 * In any case the innerHttpResponse field will contain the HttpClientResponse object
	 * returned by the server with the raw response.
	 * 
	 * @param response Http response returned by the GameRegistry server.
	 * @return Parsed GameRegistryResponse object.
	 */
	static GameRegistryResponse fromHttpResponse(HttpClientResponse response, Buffer body) {
		GameRegistryResponse rval = new GameRegistryResponse();
		rval.innerHttpResponse = response;
		
		// If '200 OK' or '201 Created' or '202 Accepted'...
		if (response.statusCode() >= 200 && response.statusCode() < 300) 
			parseOkResponse(response, body, rval);
		else 
			parseNonOkResponse(response, rval);
		
		return rval;
	}
	
	// 200 OK, 201 Created or 202 Accepted...
	private static void parseOkResponse(HttpClientResponse response, Buffer body, GameRegistryResponse rval) {
		rval.responseType = ResponseType.OK;
		
		if (body.length() != 0) {
			try {
				List<GameSession> sessions = new ArrayList<>();
				JsonObject jsonBody = new JsonObject(body.toString());
			
				// Find GameSessions and add them to response.sessions 
				if (jsonBody.isArray()) {
					// A collection of GameSession objects
					JsonArray jsonArray = jsonBody.asArray();
					for (int i = 0; i < jsonArray.size(); i++) {
						JsonObject jsonSession = jsonArray.get(i);
						sessions.add(new GameSession(jsonSession.toMap()));
					}
				}
				else if (jsonBody.isObject()) {
					// A single GameSession
					sessions.add(new GameSession(jsonBody.toMap()));
				}
				
				rval.sessions = sessions.toArray(new GameSession[sessions.size()]);
			} catch (Exception e) {
				rval.responseType = ResponseType.INVALID_RESPONSE;
				rval.innerThrowable = e;
			}
		}
	}
	
	private static void parseNonOkResponse(HttpClientResponse response, GameRegistryResponse rval) {
		// First narrow our status code
		if (response.statusCode() >= 300 && response.statusCode() < 400) {
			// Redirection
			rval.responseType = ResponseType.UNKNOWN;
		}
		else if (response.statusCode() >= 400 && response.statusCode() < 500) {
			// Client Error
			switch (response.statusCode()) {
                case 400:
                    rval.responseType = ResponseType.INVALID_REQUEST;
                    break;
                case 401:
                case 403:
                    rval.responseType = ResponseType.TOKEN_ERROR;
                    break;
                case 404:
                    rval.responseType = ResponseType.SESSION_NOT_FOUND;
                    break;
                default:
                    rval.responseType = ResponseType.INVALID_RESPONSE;
            }
		}
		else if (response.statusCode() >= 500 && response.statusCode() < 600) {
			// Server Error
            rval.responseType = ResponseType.SERVER_ERROR;
		}
		else {
			// Should never happen
			rval.responseType = ResponseType.UNKNOWN;
		}
	}
}
