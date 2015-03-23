package es.us.dad.gameregistry.domain

import org.vertx.java.core.json.JsonObject

interface IJsonSerializable {

    void fromJson(JsonObject json)
    JsonObject toJson()

}
