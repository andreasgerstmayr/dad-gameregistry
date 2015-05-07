package es.us.dad.gameregistry.client;

import java.util.ArrayList;
import java.util.List;

import es.us.dad.gameregistry.server.domain.*;

import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * Represents a response from a GameRegistry server.
 * 
 * @see us.es.dad.gameregistry.client.GameRegistryClient
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
		 * The server did not answer to the request in time.
		 */
		TIMEOUT,
		/**
		 * Server not found
		 */
		UNKNOWN_HOST,
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
		 * Error while parsing the server's response
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
	public GameSession [] sessions;
	/**
	 * The HttpClientResponse object returned by the server, if any.
	 */
	public HttpClientResponse innerHttpResponse;
	/**
	 * If the response is not OK and the error is motivated by an exception
	 * this will have a reference to it. In other case it will be null.
	 */
	public Exception innerException;
	
	/**
	 * Builds a new GameRegistryResponse and sets it up as an UNKNOWN response type, null sessions 
	 * and null innerHttpResponse.
	 */
	GameRegistryResponse() {
		responseType = ResponseType.UNKNOWN;
		sessions = null;
		innerHttpResponse = null;
		innerException = null;
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
		rval.sessions = null;
		
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
				List<GameSession> sessions = new ArrayList<GameSession>();
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
				
				rval.sessions = sessions.toArray(new GameSession[0]);
			} catch (Exception e) {
				rval.responseType = ResponseType.INVALID_RESPONSE;
				rval.innerException = e;
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
			// TODO set responsetype accordingly
			/*
			 * 400 Bad Request (malformed request syntax, invalid request message framing, ...)
			 * 401 Unauthorized (similar to 403 but specifically for use when auth is required
			 *     and has failed or has not yet been provided).
			 * 402 Payment Required (reserved for future use)
			 * 403 Forbidden (Requested resource refuses to respond, but here authentication
			 *     will make no difference).
			 * 404 Not Found (but might be there in the future)
			 * 405 Method Not Allowed (A request was made of a resource using a method not supported
			 *     by that resource).
			 * 406 Not Acceptable (the resource is only available as content not acceptable according
			 *     to the Accept headers in the request)
			 * 407 Proxy Auth Required
			 * 408 Request Timeout (the server timed out waiting for the request. The client did not
			 *     produce a request within the time that the server was prepared to wait)
			 * 409 Conflict (can't execute request because of a conflict in the request... ?)
			 * 410 Gone (resource removed for good, clients should not request it, search engines
			 *     should remove it, ...)
			 * 411 Length Required (The request did not specify the length of its content, which is 
			 *     required by the requested resource)
			 * 412 Preconfition Failed 
			 * 413 Request Entity Too Large
			 * 414 Request-URI Too Long
			 * 415 Unsupported Media Type
			 * 416 Requested Range Not Satisfiable
			 * 417 Expectation Failed (Expect request header related)
			 * 418 I'm a teapot (April's Fools 1998)
			 * 419 Authentication Timeout (auth expired...)
			 * 420 Method Failure (not official (used by Spring), deprecated)
			 * 420 Enhance Your Calm (twitter specific, client being rate-limited)
			 * 422 Unprocessable Entity (webDAV)
			 * 423 Locked (webDAV)
			 * 424 Failed Dependency (webDAV)
			 * 426 Upgrade Required (client should switch to a different protocol)
			 * 428 Precondition Required (origin server required the request to be conditional. Intended
			 *     to prevent "the 'lost update' problem, where a client GETs a resource's state,
			 *     modifies it and PUTs it back to the server when meanwhile a third party has
			 *     modified the state on the server, leading to a conflict")
			 * 429 Too Many Requests
			 * 431 Request Header Fields Too Large
			 */
		}
		else if (response.statusCode() >= 500 && response.statusCode() < 600) {
			// Server Error
			// TODO set responsetype accordingly
			/*
			 * 500 Internal Server Error
			 * 501 Not Implemented
			 * 502 Bad Gateway (the server was acting as a gateway or proxy and received
			 *     an invalid response from the upstream server).
			 * 503 Service Unavailable
			 * 504 Gateway Timeout
			 * 505 Http Version Not Supported
			 * 506 Variant Also Negotiates
			 * 507 Insufficient Storage (webDAV)
			 * 508 Loop Detected (webDAV)
			 * 509 Bandwidth Limit Exceeded 
			 * 510 Not extended
			 * 511 Network Authentication Required
			 */
		}
		else {
			// Should never happen
			rval.responseType = ResponseType.UNKNOWN;
		}
	}
}
