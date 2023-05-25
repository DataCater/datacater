# DataCater SerDe

This folder contains all shared entities to be used in multiple projects in order to ensure
a single source of truth and higher maintainability throughout the DataCater product suite.

## File Structure

    ├── build             - [ignored]
    ├── src               - Main folder of the library
    └── build.gradle      - Dependency management file

## Usage
The project dependencies can be added to other projects in this repo by adding:

``` implementation project(':base') ```

to the build.gradle file of the respective project.