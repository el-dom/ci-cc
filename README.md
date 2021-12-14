# CI-CC

Simple REST API for TODO CRUD operations using Ktor, Koin and Exposed

## Prerequisits
* JDK 11
* Docker compose for starting the PostgreSQL container 
* (alternatively use a running PostgreSQL elsewhere and adjust [hikari.properties](src/main/resources/db/hikari.properties)) accordingly

## DB Startup
```
docker compose -p ci-cc -f docker/cicc.yml up -d
```

## Application startup
```
./gradlew run
```

##API Docs
Available after startup on http://localhost:8080/docs/index.html