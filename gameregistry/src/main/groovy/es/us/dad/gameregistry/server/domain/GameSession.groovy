package es.us.dad.gameregistry.server.domain

class GameSession extends DomainObject {

    UUID id
    String user
    String game
    Date start
    Date end

    public GameSession() {
        super()
    }

    public GameSession(Map<String, Object> jsonMap) {
        super(jsonMap)

        id = UUID.fromString(jsonMap.get("id").toString())
        user = jsonMap.get("user")
        game = jsonMap.get("game")
        start = toDate(jsonMap.get("start"))
        end = toDate(jsonMap.get("end"))
    }

    @Override
    Map<String, Object> toJsonMap() {
        Map<String, Object> json = new HashMap<String, Object>()

        json.put("id", id.toString())
        json.put("user", user)
        json.put("game", game)
        json.put("start", formatDate(start))
        json.put("end", formatDate(end))

        return json
    }
}
