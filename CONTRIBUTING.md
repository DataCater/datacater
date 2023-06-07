# Overview

This file explains how to setup your local development environment to be able to contribute to the DataCater platform.

Follow these steps to setup your development environment.

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/.

## Requirements

- [Homebrew](https://brew.sh/) 3.5+
- [Docker](https://docker.io/) 20.10+
- [SDKman](sdkman.io/) to install Java Tools: 5.15+
- [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0-beta.2/#summary)

## Setup

- The following command ensures that you use the supported tool versions as defined in `.sdkmanrc`:
```
sdk env
```

- Now install the pre-commit for easy linting:
```
brew install pre-commit
pre-commit install
pre-commit install-hooks
```

Before committing your changes it's a good idea to run this command to find potential issues early:
```
pre-commit run --all-files
```

- Verify that you can build and run the application locally:
```
# Run tests
./gradlew :platform-api:clean test

# Run app locally
./gradlew :platform-api:quarkusDev

# Smoke test
curl localhost:8080/api/filters
```

- Once this succeeds you are ready to contribute! 🎉

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/api/q/dev/.

## Remote-dev

Set docker registry to minikube registry.
```shell
eval $(minikube -p minikube docker-env)
```

Build an deploy mutable jar docker container.
```shell
./gradlew :platform-api:clean build -x test \
  -Dquarkus.kubernetes.deploy=true \
  -Dquarkus.package.type=mutable-jar
```

Load image to minikube

Connect remote dev via gradlew:

```shell
./gradlew :platform-api:
```
## Next Steps

We highly recommend to familiarize yourself with the frameworks and libraries used in DataCater:

- [Jackson](https://github.com/FasterXML/jackson-docs)
- [Hibernate Reactive](https://hibernate.org/reactive/)
- [SmallRye Metrics, Health, RESTEasy Rest Client](https://smallrye.io/projects/)
- [SmallRye Mutiny](https://smallrye.io/smallrye-mutiny/)
- [SmallRye Reactive Messaging](https://github.com/smallrye/smallrye-reactive-messaging)

Because DataCater is utilising Kafka we also recommend to dig into the Kafka Resources:

- [Getting Started](https://kafka.apache.org/documentation.html#gettingStarted)
- [Architecture, Design and Components](https://kafka.apache.org/documentation.html#design)

We find these Quarkus Related Guides worth reading because the application builds upon these components:

- RESTEasy Reactive ([guide](https://quarkus.io/guides/resteasy-reactive)): Reactive implementation of JAX-RS with
  additional features. This extension is not compatible with the quarkus-resteasy extension, or any of the extensions
  that depend on it.
- SmallRye Reactive Messaging - Kafka Connector ([guide](https://quarkus.io/guides/kafka-reactive-getting-started)):
  Connect to Kafka with Reactive Messaging
- YAML Configuration ([guide](https://quarkus.io/guides/config#yaml)): Use YAML to configure your Quarkus application
- SmallRye Health ([guide](https://quarkus.io/guides/microprofile-health)): Monitor service health

## Support

If you need support, feel free to join our [Community Slack](https://join.slack.com/t/datacater/shared_invite/zt-17cga6jg3-rGdgQZU6iX~mJGC8j~UNlw).

## FAQ

### How do I package the application?

The application can be packaged using:

```shell script
./gradlew :platform-api:build
```

It produces the `quarkus-run.jar` file in the `build/quarkus-app/` directory. Be aware that it’s not an _über-jar_ as
the dependencies are copied into the `build/quarkus-app/lib/` directory.

The application is now runnable using `java -jar build/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./gradlew :platform-api:build -Dquarkus.package.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar build/*-runner.jar`.

### How do I create a native executable?

You can create a native executable using:

```shell script
./gradlew :platform-api:build -Dquarkus.package.type=native
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./gradlew build -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./build/platform-api-1.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/gradle-tooling.

### How do I write tests for the Helm chart?

- Create a file in `helm-charts/datacater/templates/tests`

We recommend to prefix the file with `test-` and use a descriptive name.

The YAML definition must contain the following annotations:

- `helm.sh/hook: test`

These annotations ensure that the tests run as part of the deployment as well.

See https://helm.sh/docs/topics/chart_tests/ for more details.

### How do I test Helm charts locally?
```
brew install chart-testing yamllint

# Lint the chart(s) to detect syntax errors
ct lint --all --config helm-charts/ct.yaml
<start local K8 cluster e.g. with minikube>
# Install the chart and run tests if available
ct install --namespace default --all --config helm-charts/ct.yaml
```
See https://github.com/helm/chart-testing#usage for details

### How do I update the self-signed certificates?

For testing and development you can find self-signed certificates under
`platform-api/src/main/resources/`. To update these run ...

```bash
 openssl req -nodes -x509 -newkey rsa:4096 -keyout key.pem -out cert.pem -sha256 -days 365 -subj '/CN=localhost'
```

## Logging Principles
Logging plays a crucial role for administrators, users and developers of our tool, which is why it
is important to have certain standards in regards on when and how to use which type of logger.

The following sections will outline
- what information should be logged.
- when information should be logged.
- how information should be logged.

### What information should be logged.
#### Unknown-Error Log Message
These types of messages are things such as an unexpected exception was thrown, which should
provider relative information and context on the exception.
The following questions should be answered with these log messages.
- What error happened
- what data was used to cause this error
- when did the error happen
- ...


#### Informational Log Message
These types of log messages are used to give application administrators contexts to user errors,
users errors can be for example, a stream could not be created due to incorrect user data.
The following questions should be answered with these log messages.
- when did the error occur
- what data was used to cause the error
- what api path did the user call
- ...

#### Exclude sensitive information
Be careful when working on parts of the application that could internationally contain sensitive data.
In these cases, it could be useful to not include the data payload when logging.
In some cases, an object id could be provided for more context, such as a Tenant/UserID instead
of a username but only where the id belongs to a persisted object that an administrator can access.

#### Avoid adding too much Context
Having too much information in a log message is a thing. Image a deploymentID being logged for
an object before it has been saved to the database. The error causes it to not be persisted, but the
admin assumes it should be in the DB. This would waste time in searching for the object
and potentially cause the assumption of an error that doesn't actually exist.

### When information should be logged.
Information doesn't need to be logged everywhere in the application.
```
{
  "level": "info",
  "message": "User logged in succesfully",
  "timestamp": "2021-11-03T20:44:08.460Z"
}
```
just doesn't need to pop up in the application logs.
Information SHOULD be logged when it could potentially provide extra context when an error occurs.

Some examples of when to log information:
- An unexpected exception has occurred (e.g. a timeout exception)
  - Provide more context: Where did the timeout occur? What data payload caused the timeout?
- Authentication Exception (don't log an unauthorized exception, don't log because of incorrect credentials)
  - Provide more context: What provider was used?
- Too many failed attempts
  - You have a process that sometimes fails because a service hasn't started yet so your code
     retries periodically. After `X`-amount of attempt a message could be logged, since the service
     might actually be unavailable.

some example of when to NOT log information:
- An entity could not be updated when rest endpoint is called
  - this information should be returned to the user
- An object was created successfully
- A standard object has been created (such as a pipeline object)
- A standard application process has begun (such as `starting validation process`)

### How information should be logged.
#### Log in JSON Format
We should aim to always log messages in JSON format. JSON format is not only easily user readable
it also allows for seamless integrations with external log management tools for extra flexibility
and a higher observability.
The logging framework we use at DataCater automatically appends the logger messages into JSON format.


#### Use the correct logging level:
Using the correct logging levels gives a much better context when manually handling error
or debugging processes. It allows for a developer to filter by error type
FATAL - Critical service failure. These errors will block the application from further processing any requests.
ERROR - disruption in a request or the ability to service a request. These errors indicate a problem in the software, without knowing what the actual error was. Otherwise, the error should be handled accordingly.
WARN - non-critical service error. These errors are non-critical but could be useful information for making improvements.
INFO - Non-error debugging statements.
DEBUG - extra information regarding life-cycle events. These messages should provide information that can be useful for a developer.
