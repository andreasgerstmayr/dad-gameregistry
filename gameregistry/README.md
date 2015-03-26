Game Registry
=============

Development
-----------

    # IDE Setup
    ./gradlew eclipse
    ./gradlew idea

    # Start module
    ./gradlew runMod -i

    # Run tests
    ./gradlew clean test


Deployment
----------

    ./gradlew clean modZip
    docker build -t distributedsystems/gameregistry .
    docker run -p 1080:1080 -p 8080:8080 distributedsystems/gameregistry

cURL
----

    # create session
    curl -i -X POST http://localhost:8080/session
    
    # retrieve session (end date = null)
    curl -i -X GET http://localhost:8080/session/<SESSIONID>
    
    # update session (sets end date)
    curl -i -X POST http://localhost:8080/session/<SESSIONID>
    
    # retrieve session (end date is now set)
    curl -i -X GET http://localhost:8080/session/<SESSIONID>
        
    # delete session
    curl -i -X DELETE http://localhost:8080/session/<SESSIONID>
