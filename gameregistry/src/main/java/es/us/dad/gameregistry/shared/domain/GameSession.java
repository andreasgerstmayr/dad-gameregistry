package es.us.dad.gameregistry.shared.domain;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GameSession extends DomainObject {

    private UUID id;
    private String user;
    private String game;
    private Date start;
    private Date end;

    public GameSession() {
        super();
    }

    public GameSession(Map<String, Object> jsonMap) {
        super(jsonMap);

        id = UUID.fromString(jsonMap.get("id").toString());
        user = jsonMap.containsKey("user") ? jsonMap.get("user").toString() : "";
        game = jsonMap.containsKey("game") ? jsonMap.get("game").toString() : "";
        start = toDate(jsonMap.get("start"));
        end = toDate(jsonMap.get("end"));
    }

    @Override
    public Map<String, Object> toJsonMap() {
        Map<String, Object> json = new HashMap<String, Object>();

        json.put("id", id != null ? id.toString() : null);
        json.put("user", user);
        json.put("game", game);
        json.put("start", formatDate(start));
        json.put("end", formatDate(end));

        return json;
    }

    @Override
    public String toString() {
        return String.format("<GameSession #%s: %s/%s, %s - %s>", id, user, game, start, end);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getGame() {
        return game;
    }

    public void setGame(String game) {
        this.game = game;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

}
