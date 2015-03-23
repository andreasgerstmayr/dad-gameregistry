package es.us.dad.gameregistry.domain

import org.vertx.java.core.json.JsonObject

import java.text.DateFormat
import java.text.SimpleDateFormat

class GameSession implements IJsonSerializable {
    final static private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");


    UUID id
    Date start
    Date end

    @Override
    void fromJson(JsonObject json) {
        // TODO
    }

    @Override
    JsonObject toJson() {
        return new JsonObject([
                id: id.toString(),
                start: dateFormat.format(start),
                end: end ? dateFormat.format(end) : null
        ])
    }
}
