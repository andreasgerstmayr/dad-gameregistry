package es.us.dad.gameregistry.server.domain

import java.text.DateFormat
import java.text.SimpleDateFormat

abstract class DomainObject {

    final static private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    public static String formatDate(Date date) {
        if (date == null)
            return null
        else
            return dateFormat.format(date)
    }

    public static Date toDate(Object date) {
        if (date == null)
            return null
        else
            return dateFormat.parse(date)
    }

    public DomainObject() {
    }
    public DomainObject(Map<String, Object> jsonMap) {
    }
    abstract Map<String, Object> toJsonMap()

}
