# tru.ID Node Server Example

The tru.ID Node Server provides basic example code to be used with client application.

## Before you being

You will need:

- Node.JS installed
- a [tru.ID](https://tru.id) account
- a local tunnel solution if you wish to recieve Phone Check callbacks

## Getting Started

Clone or unzip the tru.ID Node Server into a directory.

### Create a tru.ID Project

- Install the [tru.ID CLI](https://tru.id/docs/reference/cli)
- Setup the CLI with the `client_id` and `client_secret` from the [tru.ID Console](https://tru.id/console)
- Create a project with the CLI pointing to the tru.ID Node Server directory `$ tru projects:create --project-dir PATH_TO_SERVER_DIR`. This will create a `tru.json` file in the directory.

### Install dependencies:

```
npm install
```

or

```
yarn install
```

## Configuration

If required, you can make configuration changes with the following environment variables:

- `PORT` : change the port that the server listens on
- `DEBUG` : determines whether debug information is logged via `console.log`
- `CONFIG_PATH` : the path to the `tru.json` configuration file for the tru.ID project
- `API_BASE_URL` : the tru.ID base URL. Defaults to `https://eu.api.tru.id`

The server will attempt to load environment variables from a `.env` file in the root directory of the server.

### Phone Check callbacks

If you wish to receive Phone Check HTTP callbacks when the Phone Check enters and end state, you must:

1. Run a local tunnel solution to expose your local running server to the Internet
2. Configure your Project to have a `phonecheck_callback_url

From the project directory run the following command replacing `{local_tunnel_url}` with the URL exposing your localhost server to the Internet:

```
$ tru projects:update --phonecheck-callback {local_tunnel_url}/phone-check/callback
```

### Run the server

```
npm start
```

or 

```
yarn start
```

### Explore

- View the code in [blob/main/src/index.js](blob/main/src/index.js)
- The server exposes a basic homepage with some test pages that allow you to try out some functionality# sim-card-auth-android
