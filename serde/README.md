# DataCater SerDe

This folder contains our (De)serializers used for deserializing records when consuming data from a stream, i.e., Kafka
Topic, or serializing records when publishing data to a stream.

Internally, we use JSON as data format.

## File Structure

    ├── build             - [ignored]
    ├── src               - Main folder of the library
    └── build.gradle      - Dependency management file

## Usage
The project dependencies can be added to other projects in this repo by adding:

``` implementation project(':serde') ```

to the build.gradle file of the respective project.
