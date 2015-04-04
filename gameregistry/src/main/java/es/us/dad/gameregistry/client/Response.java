package es.us.dad.gameregistry.client;

import es.us.dad.gameregistry.domain.*;
import org.vertx.java.core.http.HttpClientResponse;

public class Response {
	// TODO 
	public enum ResponseType {
		OK,
		TIMEOUT,
		TOKEN_ERROR,
		USER_NOT_FOUND
	}
	
	public ResponseType responseType;
	public GameSession [] sessions;
	public HttpClientResponse innerHttpResponse;
	
	public static Response fromHttpResponse(HttpClientResponse response) {
		Response rval = new Response();
		rval.innerHttpResponse = response;
		
		// If '200 OK' or '201 Created' or '202 Accepted'...
		if (response.statusCode() >= 200 && response.statusCode() < 300) 
			parseOkResponse(response, rval);
		else 
			parseNonOkResponse(response, rval);
		
		return rval;
	}
	
	// 200 OK, 201 Created or 202 Accepted...
	private static void parseOkResponse(HttpClientResponse response, Response rval) {
		// TODO
		/* From Wikipedia (http://en.wikipedia.org/wiki/List_of_HTTP_status_codes):
		 * 200 OK
		 * 201 Created (new resource has been created)
		 * 202 Accepted (request accepted for processing but it has not been completed.
		 * 203 Non-Authoritative Information (Success, returning info from another source)
		 * 204 No Content (usual answer to a delete request)
		 * 205 Reset Content (same as 204 but requires the requester to reset the document view (?))
		 * 206 Partial Content (only part of the resource served; typical answer to a request 
		 *     with range header)
		 * 207 Multi-Status (part of WebDAV; body will be an XML with a number of separate 
		 *     responses)
		 * 208 Already Reported (part of WebDAV; members of a DAV binding have already been 
		 *     enumerated in a previous reply to this request and are not included again)
		 * 226 IM Used (The server has fulfilled a request for the resource, and the response is a 
		 *     representation of the result of one or more instance-manipulations applied to the 
		 *     current instance)
		 */
	}
	
	private static void parseNonOkResponse(HttpClientResponse response, Response rval) {
		// First narrow our status code
		if (response.statusCode() >= 300 && response.statusCode() < 400) {
			// Redirection
			// TODO
			/*
			 * 300 Multiple Choices (Indicates multiple options for the resource that the client 
			 *     may follow. It, for instance, could be used to present different format 
			 *     options for video, list files with different extensions, or word sense 
			 *     disambiguation)
			 * 301 Moved Permanently (This and all future requests should be directed to the given URI)
			 * 302 Found (should not be used but some web servers still do... see wiki and 303/307)
			 * 303 See Other (response to request can be found under another URI using GET)
			 * 304 Not Modified (resource not modified since the version specified in the request)
			 * 305 Use Proxy (Requested resource is only available through the provided proxy;
			 *     Dangerous for security reasons).
			 * 306 Switch Proxy (no longer used).
			 * 307 Temporary Redirect (request should be repited with another URI but only this time)
			 * 308 Permanent Redirect (experimental; all future requests should be to this URI).
			 */
		}
		else if (response.statusCode() >= 400 && response.statusCode() < 500) {
			// Client Error
			// TODO
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
			 * 409 Conflict (can't execute request becouse of a conflict in the request... ?)
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
			// TODO
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
			// TODO
		}
	}
}
