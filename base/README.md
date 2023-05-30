# DataCater Base extension

This quarkus extension contains shareable code and resources for DataCater. Quarkus extensions allow
to manipulate build and runtime of quarkus applications like DataCater.

# Extension basics
Quarkus extensions have three different phases [Ref 1]
1. **Augmentation** handles annotation processing, configuration and class initialisation.
2. **Static Init** handles bytecode recording and inlining to main, referenced by `@Record(ExecutionTime.STATIC_INIT)`
3. **Runtime Init** SHOULD be avoided unless OS-Level work is required like opening a port

Different programming models than in the main application. Working towards a more generic appraoch is
encouraged by extensions. Therefore, using Annotations like `javax.ws.rs.Path` require more set-up, then
using interfaces provided `io.vertx.core.Handler`, which maps 1-1 to `io.quarkus.vertx.core.Handler`.
See the extensions directory for more examples of this [Ref 2].

Our multiple module structure contained in this repository is based on Quarkus primitives to optimise for
quick startup and build time optimisation. The `datacater-ee-deployment` module contains `Processors`
and declarative descriptions of what should be built in which manner. This leads to _more_ effort to
register endpoints. First, the `runtime` implementation needs to provided and then the `@BuildStep`
declaration gives Quarkus directions on how to build this artifact into an application [Ref 3 & 4]

Refs:
1. https://quarkus.io/guides/writing-extensions#bootstrap-three-phases
2. https://github.com/quarkusio/quarkus/tree/main/extensions
3. https://quarkus.io/guides/building-my-first-extension
4. https://quarkus.io/guides/all-builditems

# FAQ

## How can I locally develop with this extension?

Clone this repository and publish the extension to your local maven registry by applying. A Flag is needed to skip the
tests so that the extension isn't fully verified at build time. This is needed because some configurations 
are added in the application, not the extension.
```
mvn install -DskipTests
```
add the dependency to your `build.gradle` file in your Quarkus application.
```
implementation 'io.datacater:base-deployment:1.0-SNAPSHOT'
```
Gradlew searches your local mvn repository for dependencies, hence you are good to go.

## How do I setup my developer environment?

We recommend to install the Java Tools with [SDKman](sdkman.io/):
```
sdk install java 17.0.3-zulu
sdk install maven
```