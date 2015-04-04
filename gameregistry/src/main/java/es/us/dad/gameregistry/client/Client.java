package es.us.dad.gameregistry.client;

import org.vertx.java.platform.Verticle;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientRequest;
import org.vertx.java.core.http.HttpClientResponse;

import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import com.hazelcast.util.AddressUtil.InvalidAddressException;

/**
 * Helper class to execute requests on a GameRegistry server.
 * 
 * Intended to be used like this:
 * <pre><code>
 * 	// Create the client
 * 	Client client = new Client(new InetAddress("1.2.3.4", "1080"))
 *                            .setUser(userId)
 *                            .setToken(token);
 *  // Do a request
 *  client.getLastGameSession(new Handler&lt;Response&gt;() {
 *  	&#64;Override
 *  	public void handle(Response event) {
 *  		// TODO handle a response
 *  	}
 *  });
 * </code></pre>                     
 */
public class Client {
	public static final int DEFAULT_PORT = 8080;
	public static final String GAMEREGISTRY_USER_HEADER = "gameregistry-user";
	public static final String GAMEREGISTRY_TOKEN_HEADER = "gameregistry-token";
	
	private HttpClient httpClient = null;
	private InetAddress host = null;
	private int port = -1;
	
	public String userId = "";
	public String token = "";
	
	public Client(InetAddress host, HttpClient httpClient) throws InvalidAddressException {
		Initialize(host, DEFAULT_PORT, httpClient);
	}
	
	public Client(InetAddress host, int port, HttpClient httpClient) throws InvalidAddressException {
		Initialize(host, port, httpClient);
	}
	
	// WARNING: This does a synchronous DNS resolve if address is a host name. Use IP to avoid it.
	// It can be avoided using org.vertx.java.core.dns.DnsClient to resolve a name asynchronously,
	// then using one of the others constructors.
	public Client(String address, HttpClient httpClient) throws URISyntaxException, InvalidAddressException, UnknownHostException {
		// Expects something like 'somehost:port' or '1.2.3.4:port'.
		
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
				// This might do a synchronous DNS resolve
				ip = InetAddress.getByName(address.substring(0, colonsIndex - 1));
			} 
			catch(NumberFormatException e) { 
				throw new URISyntaxException(address, "Invalid port number.");
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
		this.httpClient = httpClient;
	}
	
	public Client setUser(String user) {
		this.userId = (user == null) ? "" : user.trim();
		return this;
	}
	
	public Client setToken(String token) {
		this.token = (token == null) ? "" : token.trim();
		return this;
	}
	
	/*
	 * Next should be methods to perform requests on the server.
	 * 
	 * addSession (GameSession session, ...)
	 * Adds a new session to the system. I think it should have start and end timestamps already set.
	 * 
	 * updateSession (sessionid, GameSession, ...)
	 * Updates a game session with another one. I think this should not be allowed. I think we should
	 * avoid sessions without end timestamp and only allow updates to special users, if any.
	 * 
	 * removeSession (sessionid, ...)
	 * Deletes a game session. Needed? Allowed? To any?
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
	 * ?
	 */
	
	// TODO A mock of a typical request method is something like this
	public Client getLastGameSession(final Handler<Response> handler) {
		// TODO Needs API definition/review
		String url = this.host.toString() + ":" + this.port + "/sessions/last";
		// TODO Change to the desired method (post, delete, ...). HttpResponseHandler
		//      will invoke 'handler' with the parsed http response.
		HttpClientRequest request = httpClient.get(url, new HttpResponseHandler(handler));
		
		// TODO
		// request.exceptionHandler(*an exception event handler that invokes handler with a proper response object*);
		
		// Sets userId and token as headers
		addUserTokenToRequest(request);
		
		// TODO Add any other headers or a body
		
		// Sends the request
		request.end();
		
		return this;
	}
	
	private void addUserTokenToRequest(HttpClientRequest request) {
		if (!this.userId.isEmpty() && !this.token.isEmpty()) {
			request.putHeader(GAMEREGISTRY_USER_HEADER, this.userId)
				   .putHeader(GAMEREGISTRY_TOKEN_HEADER, this.token);
		}
	}
	
	private class HttpResponseHandler implements Handler<HttpClientResponse> {
		private Handler<Response> outerResponseHandler;
		
		public HttpResponseHandler(Handler<Response> handler) {
			this.outerResponseHandler = handler;
		}
		
		@Override
		public void handle(HttpClientResponse event) {
			Response response = Response.fromHttpResponse(event);
			this.outerResponseHandler.handle(response);
		}
	}
}
