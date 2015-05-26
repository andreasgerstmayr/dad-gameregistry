package es.us.dad.gameregistry.shared.domain;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

abstract class DomainObject {

    final static private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    public static String formatDate(Date date) {
        if (date == null)
            return null;
        else
            return dateFormat.format(date);
    }

    public static Date toDate(Object date) {
        try {
            return dateFormat.parse(date.toString());
        } catch (NullPointerException | ParseException ignore) {
            return null;
        }
    }

    public DomainObject() {
    }
    public DomainObject(Map<String, Object> jsonMap) {
    }
    abstract public Map<String, Object> toJsonMap();

}
