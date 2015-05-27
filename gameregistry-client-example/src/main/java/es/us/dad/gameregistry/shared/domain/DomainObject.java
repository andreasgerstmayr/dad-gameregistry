package es.us.dad.gameregistry.shared.domain;

import java.util.Date;
import java.util.Map;

abstract public class DomainObject {

    public static Long formatDate(Date date) {
        if (date == null)
            return null;
        else
            return date.getTime();
    }

    public static Date toDate(Object date) {
        try {
            return new Date(Long.parseLong(date.toString()));
        } catch (NullPointerException | NumberFormatException ignore) {
            return null;
        }
    }

    public DomainObject() {
    }

    public DomainObject(Map<String, Object> jsonMap) {
    }

    abstract public Map<String, Object> toJsonMap();

}
