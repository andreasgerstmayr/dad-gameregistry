package es.us.dad.gameregistry;

import org.vertx.java.core.json.JsonObject;

public interface IJsonSerializable {
	void fromJSon(JsonObject o);
	JsonObject toJson();
}
