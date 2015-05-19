package es.us.dad.gameregistry.client;

import es.us.dad.gameregistry.GameRegistryConstants;
import es.us.dad.gameregistry.server.domain.GameSession;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.dns.DnsClient;
import org.vertx.java.core.dns.DnsException;
import org.vertx.java.core.dns.DnsResponseCode;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientRequest;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.Vertx;

import java.net.*;
import java.util.UUID;

import com.hazelcast.util.AddressUtil.InvalidAddressException;

/**
 * Helper class to execute requests on a GameRegistry server.
 * 
 * This class is coded with a fluent interface in mind, as is the HttpClient from vertx. It is
 * also coded to be used asynchronously.
 * 
 * Intended to be used like this:
 * <pre><code>
 *  // In verticle's Start() method probably:
 * 	// Create the client 
 *  // In this example the GameRegistry server ip is 1.2.3.4 and it is listening in the default port.
 *  InetAddress addr = InetAddress.getByName("1.2.3.4");
 * 	GameRegistryClient client = new GameRegistryClient(addr, vertx)
 *                                  .setUser(userId)
 *                                  .setToken(token);
 *  ...
 *  
 *  // Somewhere in your code, an example method that retrieves a session
 *  // and 'doSomethingWith' it will look like this:
 *  void requestSomeSession(UUID sessionId) {
 *    // Do a request
 *    client.getSession(id, new Handler&lt;GameRegistryResponse&gt;() {
 *  	&#64;Override
 *  	public void handle(GameRegistryResponse response) {
 *        if (response.responseType == ResponseType.OK) {
 *          // Request was successful
 *          doSomethingWith(response.sessions[0]);  
 *        }
 *        else {
 *          // Error happened. Check response.responseType value and handle the error
 *          handleError(response);
 *        }
 *  	}
 *    });
 *  }
 * </code></pre> 
 * 
 * Every request made to the GameRegistry server should contain an User-Token pair that
 * will be validated by the server through a LoginServer. Set them up using setUser and 
 * setToken methods before doing any request or an IllegalArgumentException will be raised.
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
	private String basepath = "/api/v1";
	private String user = "";
	private String token = "";

    /**
     * Creates a GameRegistryClient performing an asynchronous DNS lookup.
     *
     * This method is usefull if you want to create a new GameResgitryClient but only
     * knows its host name, not its ip address. This case requires an asynchronous DNS
     * lookup.
     *
     * Example follows:
     *
     * <pre><code>
     *     GameRegistryClient.createFromAddress("gameregistry.cloudapp.net:8080", new Handler&lt;AsyncResult&lt;GameRegistryClient&gt;&gt;() {
     *         &#64;Override
     *         public void handle(AsyncResult&lt;GameRegistryClient&gt; result) {
     *             if (result.succeeded()) {
     *                 GameRegistryClient myClient = result.result();
     *                 // Do stuff with the client.
     *             }
     *             else {
     *                 // result.cause() returns an exception describing the problem.
     *             }
     *         }
     *     }
     * </code></pre>
     *
     * @param address String with the GameRegistry server's address (ie "gameregistry,cloudapp.net:8080")
     * @param vertx A Vertx instance to create the Dns client and the GameRegistryClient.
     * @param resultHandler To handle the result
     * @throws UnknownHostException
     * @throws URISyntaxException
     */
    public static void createFromAddress(String address, Vertx vertx, Handler<AsyncResult<GameRegistryClient>> resultHandler) {
        int port;
        int colonsIndex = address.indexOf(':');
        String hostname = address;

        if (colonsIndex == -1) {
            port = DEFAULT_PORT;
            hostname = address;
        }
        else {
            try {
                port = Integer.parseInt(address.substring(colonsIndex + 1));
				hostname = address.substring(0, colonsIndex);
            } catch (Exception e) {
                // Fail the async result becouse port is not a number
                resultHandler.handle(new AsyncResultImpl<>(e));
                return;
            }
        }

        DnsClient dnsClient = vertx.createDnsClient(new InetSocketAddress("8.8.8.8", 53), new InetSocketAddress("8.8.4.4", 53));
        dnsClient.lookup(hostname, new Handler<AsyncResult<InetAddress>>() {
            @Override
            public void handle(AsyncResult<InetAddress> lookUpResult) {
                // DnsClient documentation states that when a host can't be resolved the AsyncResult will be succeeded
                // and the result() call will be null.
                // But what actually seems to happen is that the AsyncResult is failed and the cause is a DnsException
                // with code 3 (NXDOMAIN)
                if (lookUpResult.succeeded() && lookUpResult.result() != null) {
                    // Succeed our async result with a new GameRegistryClienet
                    resultHandler.handle(new AsyncResultImpl<>(new GameRegistryClient(lookUpResult.result(), port, vertx)));
                } else {
                    // Either is succeeded but null or failed
                    if (lookUpResult.succeeded())
                        resultHandler.handle(new AsyncResultImpl<>(new UnknownHostException("Couldn't resolve address (result is null): " + address)));
                    else {
                        // It failed. Is it an NXDOMAIN error?
                        if (lookUpResult.cause() instanceof DnsException && ((DnsException) lookUpResult.cause()).code() == DnsResponseCode.NXDOMAIN) {
                            resultHandler.handle(new AsyncResultImpl<>(new UnknownHostException("Couldn't resolve address (NXDOMAIN error):" + address)));
                        }
                        // If it isnt just fail with whatever the cause of the async result was.
                        else
                            resultHandler.handle(new AsyncResultImpl<>(lookUpResult.cause()));
                    }
                }
            }
        });
    }

	/**
	 * Builds a new GameRegistryClient.
	 * 
	 * The port will be the default GameRegistry port, which is 8080.
	 * @param host Address where the GameRegistry server is hosted.
	 * @param vertx The Vertx instance, used to create an HttpClient.
	 * @throws InvalidAddressException
	 */
	public GameRegistryClient(InetAddress host, Vertx vertx) throws InvalidAddressException {
		initialize(host, DEFAULT_PORT, vertx.createHttpClient());
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
		initialize(host, port, vertx.createHttpClient());
	}
	
	private void initialize(InetAddress host, int port, HttpClient httpClient) throws InvalidAddressException {
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

	/**
	 * Sets the base path to use to make requests to the server.
	 *
	 * The base path is the route inside the path where the API lives.
	 * For example, if the GameRegistry responds to requests to the path
	 * "/api/v1" (ie "/api/v1/sessions/" to request a collection of sessions)
	 * the base path would be "/api/v1".
	 * @param basePath
	 * @return
	 */
	public GameRegistryClient setBasePath(String basePath) {
		if (basePath.charAt(basePath.length()-1) =='/')
			this.basepath = basepath.substring(0, basePath.length()-1);
        else
		    this.basepath = basePath;

		return this;
	}

    /**
     * Returns the current user string.
     * @return String used as user identifier (for the login server).
     */
	public String getUser() {
		return this.user;
	}

    /**
     * Returns the current token string.
     * @return String used as a token (for the login server).
     */
	public String getToken() {
		return this.token;
	}

    /**
     * Returns the current base path used to access the GameRegistry REST api.
     * @return String with the base path (ie "/api/v1").
     */
	public String getBasePath() {
		return this.basepath;
	}

    /**
     * Returns the current port used for the GameRegistry requests.
     * @return An integer representing the port of the remote GameRegistry server.
     */
    public int getPort() { return this.port; }

	/**
	 * Creates an HttpClientRequest object and sets it up for the GameRegistryServer.
	 *  
	 * @param path Route of the resource. For example "/sessions/1".
	 * @param method Http method to use in the request (GET/POST/PUT/DELETE).
	 * @param gameRegistryResponseHandler The handler in charge of the response.
	 * @return The newly created request.
	 */
	private HttpClientRequest createHttpRequest(String path, String method, Handler<GameRegistryResponse> gameRegistryResponseHandler) {
		if (!method.equals("GET") && !method.equals("POST") && !method.equals("PUT") && !method.equals("DELETE"))
			throw new IllegalArgumentException("Unsuported method: " + method);

		HttpClientHandlers handlers = new HttpClientHandlers(gameRegistryResponseHandler);
		HttpClientRequest req = httpClient.request(method, path, handlers.httpHandler());
		req.exceptionHandler(handlers.exceptionHandler());
		this.addUserTokenToRequest(this.user, this.token, req);
		
		return req;
	}
	
	// GET /sessions
	/**
	 * Requests a collection of GameSessions from the GameRegistry server.
	 * 
	 * @param filterParams Filtering options.
	 * @param responseHandler The handler that will process the response.
	 * @return This client (fluent interface).
	 */
	public GameRegistryClient getSessions(Object filterParams, Handler<GameRegistryResponse> responseHandler) {
		// TODO filterParams needs the protocol to be defined to be more specific
		String url = basepath + "/sessions";
		HttpClientRequest req = createHttpRequest(url, "GET", responseHandler);
		// TODO Add filtering parameters
		
		req.end();
		
		return this;
	}
	
	// POST /sessions
	/**
	 * Adds a new session to the GameRegistry server.
	 * 
	 * The session to be added will get a new identifier when added to the server.
	 * The previous session id will be ignored.
	 * 
	 * The response will contain the new game session with its new UUID.
	 * 
	 * Intended use:
	 * <pre><code>
	 *  GameRegistryClient client = new GameRegistryClient("1.2.3.4", "8080");
	 *  
	 *  GameSession newSession = new GameSession();
	 *  newSession.game = "MyFunnyGame";
	 *  newSession.start = new Date(...);
	 *  newSession.end = new Date(...);
	 *  newSession.user = userId;
	 *  
	 *  client.addSession(session, new Handler&lt;GameRegistryResponse&gt;() {
	 *    @Override
	 *    void handle(GameRegistryResponse response) {
	 *      // Add here code to handle the response (check response.responseType, etc)
	 *    }
	 *  });
	 * </code></pre>
	 * 
	 * @param session Session to add.
	 * @param responseHandler A handler for the server's response.
	 * @return This client (fluent interface).
	 */
	public GameRegistryClient addSession(GameSession session, Handler<GameRegistryResponse> responseHandler) {
		String url = basepath + "/sessions";
		HttpClientRequest req = createHttpRequest(url, "POST", responseHandler);
		req.headers().set("Content-Type", "application/json");

		// The JsonObject sent to the server should not contain an id, it will get
		// a new id when added to the collection.
		JsonObject jsonSession = new JsonObject(session.toJsonMap());
		jsonSession.removeField("id");
		req.end(jsonSession.encodePrettily());
		
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
		String url = basepath + "/sessions/" + sessionId.toString();
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
		String url = basepath + "/sessions/" + session.getId();
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
		String url = basepath + "/sessions/" + sessionId;
		HttpClientRequest req = createHttpRequest(url, "DELETE", responseHandler);
		req.end();
		
		return this;
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
         * @see es.us.dad.gameregistry.client.GameRegistryClient.HttpClientHandlers
		 */
		private class ResponseHandler implements Handler<HttpClientResponse> {
			private HttpClientHandlers handlers;
			
			ResponseHandler(HttpClientHandlers handlers) {
				this.handlers = handlers;
			}
			
			@Override
			public void handle(final HttpClientResponse httpResponse) {
				// We need to parse the response when all the response body is received
				httpResponse.bodyHandler(new Handler<Buffer>() {
					@Override
					public void handle(Buffer body) {
						GameRegistryResponse response = GameRegistryResponse.fromHttpResponse(httpResponse, body);
						handlers.gameRegistryResponseHandler.handle(response);
					}
				});
			}
		}
		
		/**
		 * A Throwable handler that creates a GameRegistryResponse object signaling an
		 * error and calls a handler for it.
         * @see es.us.dad.gameregistry.client.GameRegistryClient.HttpClientHandlers
		 */
		private class ExceptionHandler implements Handler<java.lang.Throwable> {
			private HttpClientHandlers handlers;
			
			ExceptionHandler(HttpClientHandlers handlers) {
				this.handlers = handlers;
			}
			
			@Override
			public void handle(java.lang.Throwable throwable) {
				GameRegistryResponse rval = new GameRegistryResponse();
				
				switch (throwable.getClass().getName()) {
				// TODO Set rval.responsetype accordingly based on throwable's class name and info
				}
				
				handlers.gameRegistryResponseHandler.handle(rval);
			}
		}
	}

    /**
     * Minimal AsyncResult implementation.
     * @param <T> Whatever the result's type should be.
     */
    static class AsyncResultImpl<T> implements AsyncResult<T> {
        private T _result;
        private Throwable _exception;
        private boolean _succeded;

        /**
         * The resulting AsyncResult will be successful, its parameter the result of the operation.
         * @param t
         */
        public AsyncResultImpl(T t) {
            _result = t;
            _succeded = true;
            _exception = null;
        }

        /**
         * The resulting AsyncResult will be failed, its parameter the cause of the failure.
         * @param e
         */
        public AsyncResultImpl(Throwable e) {
            _result = null;
            _succeded = false;
            _exception = e;
        }

        @Override
        public T result() {
            return _result;
        }

        @Override
        public Throwable cause() {
            return _exception;
        }

        @Override
        public boolean succeeded() {
            return _succeded;
        }

        @Override
        public boolean failed() {
            return !_succeded;
        }
    }
}
