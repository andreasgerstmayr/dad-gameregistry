package es.us.dad.gameregistry.util

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface GET {
    String value()
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface POST {
    String value()
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface PUT {
    String value()
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface DELETE {
    String value()
}
