# Hiring Human In The Loop Quarkus App
This project uses Quarkus, the Supersonic Subatomic Java Framework
to build an application used to manage a hiring process that is using LLM Agents 
for analysis of work items common to hiring.

Main vehicle that allows us to develop this is https://github.com/quarkiverse/quarkus-flow extensions
however the whole application is using solely extensions from Quarkus ecosystem.


## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
./mvnw compile quarkus:dev
```
In order to trigger a new hiring review instance navigate to http://localhost:8080/q/dev/
and using the Quarkus Flow card, start a new workflow. You need to provide a simple json:
```
{
  "candidateId": "Fero",
  "cvData" "<Full CV text, you can copy paste whole document here>",
  "positionRequirements": "<List of requirements for the position - consumed by LLM>"
}
```

Alternatively you can send a POST request like this:
```
TODO
```
Application provides a simple Console to examine analysis of the LLM at http://localhost:8080/console

## Packaging and running the application

The application can be packaged using:
```shell script
./mvnw package
```
It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:
```shell script
./mvnw package -Dquarkus.package.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using: 
```shell script
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: 
```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/hiring-hitl-quarkus-flow-app-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.

## Related Guides

- REST Jackson ([guide](https://quarkus.io/guides/rest#json-serialisation)): Jackson serialization support for Quarkus REST. This extension is not compatible with the quarkus-resteasy extension, or any of the extensions that depend on it

## Provided Code

### REST

Easily start your REST Web Services

[Related guide section...](https://quarkus.io/guides/getting-started-reactive#reactive-jax-rs-resources)
