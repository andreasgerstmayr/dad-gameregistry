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
