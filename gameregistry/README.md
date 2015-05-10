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

	# Cleans and builds the module
    ./gradlew clean modZip
    
    # Builds the docker container that runs the vertx module
    docker build -t distributedsystems/gameregistry .
    
    # Downloads the docker container that runs the mongoDB server
    # Only needed the first time.
    docker pull mongo:3.0.1
    
    # Starts the mongo container and names it "mongo-server"
    docker run --name mongo-server mongo:3.0.1
    
    # Starts our module container exposing in the host the relevant ports and linking its network stack to mongo-server.
    docker run -p 8080:8080 -p 8081:8081 --link mongo-server:mongo-server distributedsystems/gameregistry
    
    Caveats:
    The mongo image uses a docker volume to persist the database across runs. It might
    be a good idea to run the mongo docker container like this:
    
    docker run -v [local path]:/data/db/ --name mongo-server mongo:3.0.1
    
    The container stores the mongo database in /data/db. Running it this way you
    make docker use a folder 'local path' in the host system as /data/db.
    
    Our module uses the container's hosts file to connect to the mongo server. Docker
    updates the hosts file through the '--link' parameter. It is important that the
    alias of the linked mongo server container is 'mongo-server'.

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
