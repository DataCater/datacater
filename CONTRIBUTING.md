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
