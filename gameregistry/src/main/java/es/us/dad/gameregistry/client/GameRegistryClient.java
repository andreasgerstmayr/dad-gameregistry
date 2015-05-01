package es.us.dad.gameregistry.client;

import es.us.dad.gameregistry.GameRegistryConstants;
import es.us.dad.gameregistry.server.domain.GameSession;

import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientRequest;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.Vertx;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.UUID;

import com.hazelcast.util.AddressUtil.InvalidAddressException;

/**
 * Helper class to execute requests on a GameRegistry server.
 * 
 * This class is coded with a fluent interface in mind, as is the HttpClient from vertx.
 * 
 * Intended to be used like this:
 * <pre><code>
 * 	// Create the client (in verticle's Start() method, maybe, making client a member variable)
 *  // In this example the GameRegistry server ip is 1.2.3.4 and it is listening in the port 8080.
 *  InetAddress addr = InetAddress.getByName("1.2.3.4");
 * 	GameRegistryClient client = new GameRegistryClient(addr, 8080, vertx)
 *                                  .setUser(userId)
 *                                  .setToken(token);
 *  ...
 *  
 *  // Somewhere in your code
 *  UUID id = &lt;some session id&gt;;
 *  // Do a request
 *  client.getSession(id, new Handler&lt;GameRegistryResponse&gt;() {
 *  	&#64;Override
 *  	public void handle(GameRegistryResponse response) {
 *  		// Handle a response
 *  	}
 *  });
 * </code></pre> 
 * 
 * Every request made to the GameRegistry server should contain a User-Token pair that
 * will be validated by the server. Set them up using setUser and setToken methods before
 * doing any request or an IllegalArgumentException will be raised
 * 
 * @see GameRegistryResponse
 */
public class GameRegistryClient {
	/**
	 * Default client port.
	 */
	public static final int DEFAULT_PORT = 8080;
	
	private HttpClient httpClient = null;
	private InetAddress host = null;
	private int port = -1;
	private String user = "";
	private String token = "";
	
	/**
	 * Builds a new GameRegistryClient.
	 * 
	 * The port will be the default GameRegistry port, which is 8080.
	 * @param host Address where the GameRegistry server is hosted.
	 * @param vertx The Vertx instance, used to create an HttpClient.
	 * @throws InvalidAddressException
	 */
	public GameRegistryClient(InetAddress host, Vertx vertx) throws InvalidAddressException {
		Initialize(host, DEFAULT_PORT, vertx.createHttpClient());
	}
	
	/**
	 * Builds a new GameRegistryClient.
	 * 
	 * @param host Address where the GameRegistry server is hosted.
	 * @param port Port number where the server is listening to.
	 * @param vertx The vertx instance, used to create an HttpClient.
	 * @throws InvalidAddressException
	 */
	public GameRegistryClient(InetAddress host, int port, Vertx vertx) throws InvalidAddressException {
		Initialize(host, port, vertx.createHttpClient());
	}
	
	/**
	 * Builds a new GameRegistryClient.
	 * 
	 * This constructor expects the host address as a string of the form "&lt;host&gt;:&lt;port&gt;",
	 * where host is the textual representation of the ip address (ie "1.2.3.4") or a host name
	 * like "gameregistry.cloudall.net" and port is a number between 0 and 65535 (by default, 
	 * GameRegistry servers listen on 8080).
	 * 
	 * Warning: this constructor does a synchronous DNS resolve if the address parameter
	 * contains an unresolved host name. Use an IP to avoid it, or use 
	 * org.vertx.java.core.dns.DnsClient to do a DNS resolve of name asynchronously.
	 * 
	 * @param address Textual representation of the host address.
	 * @param vertx Vertx instance, used to create an HttpClient.
	 * @throws URISyntaxException Invalid port
	 * @throws InvalidAddressException Something is wrong with the address
	 * @throws UnknownHostException Unable to resolve the host name
	 */
	public GameRegistryClient(String address, Vertx vertx) throws URISyntaxException, InvalidAddressException, UnknownHostException {
		int port;
		InetAddress ip;
		
		int colonsIndex = address.indexOf(':');
		if (colonsIndex == -1) {
			port = DEFAULT_PORT;
			ip = InetAddress.getByName(address);
		}
		else {
			try {
				port = Integer.parseInt(address.substring(colonsIndex + 1));
				// This might do a synchronous DNS resolve if contains an unresolved host name
				ip = InetAddress.getByName(address.substring(0, colonsIndex - 1));
			} 
			catch(NumberFormatException e) { 
				throw new URISyntaxException(address, "Invalid port number.");
			}
			catch(Exception e) {
				throw new InvalidAddressException(e.toString());
			}
		}
		
		Initialize(ip, port, httpClient);
	}
	
	private void Initialize(InetAddress host, int port, HttpClient httpClient) throws InvalidAddressException {
		if (port < 0 || port > 65535) {
			throw new InvalidAddressException("Invalid port: " + port);
		}
		
		this.port = port;
		this.host = host;
		this.httpClient = httpClient.setPort(port);
	}
	
	/**
	 * Sets the user identifier to be used in the request.
	 * @param user identifier of the user.
	 * @return This client (fluent interface).
	 */
	public GameRegistryClient setUser(String user) {
		this.user = user;
		return this;
	}
	
	/**
	 * Sets the token to be used in the request.
	 * @param token String containing the token
	 * @return This client (fluent interface).
	 */
	public GameRegistryClient setToken(String token) {
		this.token = token;
		return this;
	}
	
	/* TODO more request methods
	 * Next should be methods to perform requests on the server. Needs more work, like
	 * 
	 * getUserLastSession (user, ...)
	 * Get last session of a given user.
	 * 
	 * getUserSessions (user, begin = 0, count = 20, ...)
	 * Get the first 'count' sessions after session number 'begin' of 'user'.
	 * 
	 * getUserSession (user, sessionid, ...)
	 * Get a specific session given a session id
	 * 
	 * Probably all will be helper methods that will do a 'GET /sessions' with some
	 * filter parameters under the hood.
	 * ?
	 */
	
	/**
	 * Creates an HttpClientRequest object and sets it up for the GameRegistryServer.
	 *  
	 * @param path Route of the resource. For example "/sessions/1".
	 * @param method Http method to use in the request.
	 * @param gameRegistryResponseHandler The handler in charge of the response.
	 * @return The newly created request.
	 */
	private HttpClientRequest createHttpRequest(String path, String method, Handler<GameRegistryResponse> gameRegistryResponseHandler) {
		HttpClientHandlers handlers = new HttpClientHandlers(gameRegistryResponseHandler);
		HttpClientRequest req = httpClient.request(method, path, handlers.httpHandler());
		req.exceptionHandler(handlers.exceptionHandler());
		this.addUserTokenToRequest(this.user, this.token, req);
		
		return req;
	}
	
	// GET /sessions
	/**
	 * Requests a collection of GameSessions from the GameRegistry server.
	 * @param filterParams Filtering options.
	 * @param responseHandler The handler that will process the response.
	 * @return This client (fluent interface).
	 */
	public GameRegistryClient getSessions(Object filterParams, Handler<GameRegistryResponse> responseHandler) {
		// TODO filterParams needs the protocol to be defined to be more specific
		String url = hostString() + "/sessions";
		HttpClientRequest req = createHttpRequest(url, "GET", responseHandler);
		// TODO Add filtering parameters
		
		req.end();
		
		return this;
	}
	
	// POST /sessions
	/**
	 * Adds a new session to the GameRegistry server.
	 * @param start Start date of the session
	 * @param end End date of the session
	 * @param responseHandler A handler for the server's response.
	 * @return This client (fluent interface).
	 */
	public GameRegistryClient addSession(Date start, Date end, Handler<GameRegistryResponse> responseHandler) {
		String url = hostString() + "/sessions";
		HttpClientRequest req = createHttpRequest(url, "POST", responseHandler);
		req.headers().set("Content-Type", "application/json");
		
		req.end(/* TODO Encode new game session as Json */);
		
		return this;
	}
	
	// GET /session/:sessionID
	/**
	 * Requests a single GameSession to the GameRegistry server.
	 * @param sessionId Identifier of the GameSession object to retrieve.
	 * @param responseHandler A handler for the server's response.
	 * @return This client.
	 */
	public GameRegistryClient getSession(UUID sessionId, Handler<GameRegistryResponse> responseHandler) {
		String url = hostString() + "/session/" + sessionId;
		HttpClientRequest req = createHttpRequest(url, "GET", responseHandler);
		req.end();
		
		return this;
	}
	
	// PUT /session/:session.id
	/**
	 * Replaces a GameSession in the GameRegistryServer with a new one.
	 * 
	 * The GameRegistry server will find a session with an identifier equal to the
	 * session provided to this method and replace that session with the new one.
	 * 
	 * @param session The new version of the GameSession to be replaced. 
	 * @param responseHandler
	 * @return This client.
	 */
	public GameRegistryClient updateSession(GameSession session, Handler<GameRegistryResponse> responseHandler) {
		String url = hostString() + "/session/" + session.getId();
		HttpClientRequest req = createHttpRequest(url, "PUT", responseHandler);
		req.headers().set("Content-Type", "application/json");
		req.end(new JsonObject(session.toJsonMap()).toString());
		
		return this;
	}
	
	// DELETE /session/:session.id
	/**
	 * Removes a GameSession from the GameRegistryServer.
	 * @param sessionId Identifier of the session to be removed.
	 * @param responseHandler Handler of the GameRegistry server's response.
	 * @return This client.
	 */
	public GameRegistryClient deleteSession(UUID sessionId, Handler<GameRegistryResponse> responseHandler) {
		String url = hostString() + "/session/" + sessionId;
		HttpClientRequest req = createHttpRequest(url, "DELETE", responseHandler);
		req.end();
		
		return this;
	}
	
	private String hostString() {
		return this.host.toString() + ":" + this.port;
	}
	
	private void addUserTokenToRequest(String user, String token, HttpClientRequest request) throws IllegalArgumentException {
		if (user == null || token == null || user.isEmpty() || token.isEmpty())
			throw new IllegalArgumentException("At least one of the parameters is null or empty.");
		else {
			request.putHeader(GameRegistryConstants.GAMEREGISTRY_USER_HEADER, user)
				   .putHeader(GameRegistryConstants.GAMEREGISTRY_TOKEN_HEADER, token);
		}
	}
	
	/**
	 * Provides handlers for a GameRegistry Http request.
	 * 
	 * This class is only used internally in the GameRegistryClient to encapsulate repetitive
	 * code related to Http and exception handlers in the HttpClientRequest involved in the
	 * GameRegistry request.
	 */
	private class HttpClientHandlers {
		private Handler<GameRegistryResponse> gameRegistryResponseHandler;
		
		HttpClientHandlers(Handler<GameRegistryResponse> gameRegistryHandler) {
			this.gameRegistryResponseHandler = gameRegistryHandler;
		}
		
		/**
		 * Returns an HttpClientResponse handler that will parse the http response into a 
		 * GameRegistryResponse and then call the GameRegistryResponse handler from this class.
		 * @return An HttpClientResponse handler.
		 */
		public Handler<HttpClientResponse> httpHandler() {
			return new ResponseHandler(this);
		}
		
		/**
		 * Returns a Throwable handler that will create an according GameRegistryResponse
		 * object signaling an error. Then it will call the GameRegistryResponse handler
		 * from this class.
		 * @return A Throwable handler.
		 */
		public Handler<java.lang.Throwable> exceptionHandler() {
			return new ExceptionHandler(this);
		}
		
		/**
		 * An HttpClientResponse handler that parses the response into a GameRegistryResponse
		 * and call a handler for that. 
		 * @see GameRegistryClient.HttpClientHandlers.httpHandler
		 */
		private class ResponseHandler implements Handler<HttpClientResponse> {
			private HttpClientHandlers handlers;
			
			ResponseHandler(HttpClientHandlers handlers) {
				this.handlers = handlers;
			}
			
			@Override
			public void handle(HttpClientResponse httpResponse) {
				GameRegistryResponse response = GameRegistryResponse.fromHttpResponse(httpResponse);
				this.handlers.gameRegistryResponseHandler.handle(response);
			}
		}
		
		/**
		 * A Throwable handler that creates a GameRegistryResponse object signaling an
		 * error and calls a handler for it.
		 * @see GameRegistryClient.HttpClientHandlers.exceptionHandler
		 */
		private class ExceptionHandler implements Handler<java.lang.Throwable> {
			private HttpClientHandlers handlers;
			
			ExceptionHandler(HttpClientHandlers handlers) {
				this.handlers = handlers;
			}
			
			@Override
			public void handle(java.lang.Throwable throwable) {
				// TODO Handle the exception, create an according GameRegistryResponse and
				// call handlers.gameRegistryHandler.handle(...)
			}
		}
	}
}
