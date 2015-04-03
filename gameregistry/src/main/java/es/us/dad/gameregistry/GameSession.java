package es.us.dad.gameregistry;

import org.vertx.java.core.json.JsonObject;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class GameSession implements IJsonSerializable {
	private final static DateFormat m_dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
	
	UUID id; // UUID
	Date start;
	Date end;
	
	
	@Override
	public void fromJSon(JsonObject o) {
		// TODO
	}
	
	@Override
	public JsonObject toJson() {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("id", id.toString());
		m.put("start", m_dateFormat.format(start));
		m.put("end", end == null ? m_dateFormat.format(end) : null);
		
		return new JsonObject(m);
	}
	
	
}
