# SIM Card Based Mobile Authentication with Android

How to Add SIM Card Based Mobile Authentication to your Android App with [tru.ID](https://tru.id).

[![License][license-image]][license-url]


## Before you being

You will need:

- Android capable IDE e.g. [Android Studio](https://developer.android.com/studio)
- A [tru.ID Account](https://tru.id)
- An Android phone with a SIM card and mobile data connection

## Getting Started

Install the [tru.ID CLI]() and set it up using the command provided in the [tru.ID console](https://developer.tru.id/console):

```bash
$ tru setup:credentials {client_id} {client_secret} {data_residency}
```

Install the development server plugin:

```bash
$ tru plugins:install @tru_id/cli-plugin-dev-server@canary
```

Create a **tru.ID** project:

```bash
$ tru projects:create authsome-android
```

Run the development server and take a note of the local tunnel URL:

```bash
$ tru server --project-dir ./authsome-android
```

Clone or this repo:

```bash
$ git clone git@github.com:tru-ID/sim-card-auth-android.git
```

Open the project with your Android Capable IDE.

Create a file `app/tru-id.properties` in your project and add configuration to identify the local tunnel URL of the development server:

```
EXAMPLE_SERVER_BASE_URL="https://example.com"
```

Connect your phone to your computer so it's used for running the application and run the application from your IDE.

Enter the phone number for the mobile device in the UI in the format +{country_code}{number} e.g. `+447900123456`. Press the done keyboard key or touch the "Verify my phone number" button and you will see the result of the check.

Get in touch: please email [feedback@tru.id](mailto:feedback@tru.id) with any questions.

## References

- [tru.ID example node.js server](https://github.com/tru-ID/server-example-node)
- [tru.ID docs](https://developer.tru.id/docs)

## Meta

Distributed under the MIT license. See [LICENSE][license-url] for more information.

[https://github.com/tru-ID](https://github.com/tru-ID)

[license-image]: https://img.shields.io/badge/License-MIT-blue.svg
[license-url]: LICENSE.md