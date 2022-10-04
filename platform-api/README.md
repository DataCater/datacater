# DataCater API

This project is the core of DataCater and provides a RESTful API for
interacting with the different resources, e.g., Streams, Pipelines, etc.

The application is implemented as a reactive Quarkus application and uses
PostgreSQL for persistence internally.

## File Structure

    ├── build             - [ignored]
    ├── src               - Main folder of the application
    ├── build.gradle      - Dependency management file
    └── sample-env        - Exemplary environment variables useful for development

## Structure of Endpoints and Data Model

Each endpoint is considered as a self-contained service, which can be found in a
single directory.

Exemplary services are [deployment](https://github.com/DataCater/datacater/tree/main/api/src/main/java/io/datacater/core/deployment), [pipeline](https://github.com/DataCater/datacater/tree/main/api/src/main/java/io/datacater/core/pipeline), or [stream](https://github.com/DataCater/datacater/tree/main/api/src/main/java/io/datacater/core/stream).

We use the following schema (or structure) for the entities:

| id  | createdAt | updatedAt | spec |     |
|-----|-----------|-----------|------|-----|


Where\

**id**         = unique id as UUID\
**createdAt**  = createdAt immutable Datetime describing creation time\
**updatedAt**  = updatedAt Datetime describing last update\
**spec**       = jsonb describing the spec of the given resource\
