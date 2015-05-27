package integration_tests.groovy

import com.darylteo.vertx.promises.groovy.Promise
import org.vertx.groovy.testtools.VertxTests


import static org.vertx.testtools.VertxAssert.*

def testMultipleThenStaticValue() {
    Promise<String> p = new Promise<String>()
    p.fulfill("first")

    p.then({ String val ->
        println "#1: " + val
        assertEquals("first", val)
        return "second"
    }).then({ String val ->
        println "#2: " + val
        assertEquals("second", val)
        testComplete()
    })
}

def testMultipleThenPromise() {
    Promise<String> p = new Promise<String>()
    p.fulfill("first")

    p.then({ String val ->
        println "#1: " + val

        Promise<String> p2 = new Promise<String>()
        vertx.setTimer(1000, { long time ->
            p2.fulfill("inner promise")
        })

        return p2
    }).then({ String val ->
        println "#2: " + val
        assertEquals("inner promise", val)
        testComplete()
    })
}

def testMultipleThenPromiseReturnType() {
    Promise<String> p = new Promise<String>()
    p.fulfill("first")

    // this method returns Promise<Promise<String>>
    p.then({ String val ->
        println "#1: " + val

        Promise<String> p2 = new Promise<String>()
        vertx.setTimer(1000, { long time ->
            p2.fulfill("promise from first .then()")
        })

        return p2
    })

    p.then({ String val ->
        // returns "first", NOT the inner promise!
        println "#2: " + val
        assertEquals("first", val)
        testComplete()
    })
}

def testPropagateFail() {
    Promise<String> p = new Promise<String>()

    p.fulfill("first")

    p.then({ String val ->
        println "#1: " + val
        throw new RuntimeException("some error")
    }).then({ String val ->
        // skipped because of error
        println "#2: " + val
        return "second"
    }).fail({ Exception e ->
        println "#3 error: " + e.message
        assertEquals("some error", e.message)
        return "error occured"
    }).then({ String val ->
        println "#4: " + val
        assertEquals("error occured", val)
        return "number 4"
    }).then({ String val ->
        println "#5: " + val
        assertEquals("number 4", val)
        throw new RuntimeException("sorry")
    }).fail({ Exception e ->
        println "#6: second error occured: " + e.message
        assertEquals("sorry", e.message)
        testComplete()
    })
}

def testFail() {
    Promise<String> p = new Promise<String>()

    p.fulfill("first")

    p.then({ String val ->
        throw new RuntimeException("some error")
    }, { Exception e ->
        // not reached!
        println "error occured: " + e.message
        throw new Exception("this code shouldn't be reached")
    }).then {
        println "done"
        throw new Exception("this code shouldn't be reached")
    }.fail { Exception e ->
        println "failed: " + e.message
        assertEquals("some error", e.message)
        testComplete()
    }
}

VertxTests.initialize(this)
VertxTests.startTests(this)
