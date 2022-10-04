# DataCater UI

This folder contains our ReactJS-based web application that operates on
top of DataCater's API.

## File Structure

    ├── build             - [ignored] compiled stylesheets etc
    ├── node_modules      - [ignored] external npm dependencies
    ├── public            - public assets like CSS, fonts etc.
    ├── src               - Main folder of the ReactJS application
    ├── README.md         - The file you are reading
    ├── package-lock.json - Automatically generated with exact dependency versions
    └── package.json      - Dependency management file

**Important**: Component file names match class names.

## Usage

Start the frontend with the command:

```
npm install       # Fetch all dependencies
npm start         # Start the web application and open a web browser
```

## Development

Run `npm test` to run the test suite.

Run `npx cypress run-ct` to run component tests (ct).

Run `npx cypress open-ct` to open the test suite for components and manually trigger the component test.

### Release
```
npm install       # Fetch dependencies
npm test
npm run cypress:run
npm run build
```

See https://docs.npmjs.com/cli/v8/commands/npm-version

## FAQ

### I get SSL issues when I run `npm start` how can I fix them?

Start the command like this:

    NODE_OPTIONS=--openssl-legacy-provider npm start

See https://exerror.com/opensslerrorstack-error03000086digital-envelope-routinesinitialization-error/
