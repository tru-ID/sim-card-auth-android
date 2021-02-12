# sim-card-auth-android
How to Add SIM Card Based Authentication to your Android App [with tru.ID]

[![License][license-image]][license-url]


## Before you being

You will need:

- Android capable IDE e.g. [Android Studio](https://developer.android.com/studio)
- A [tru.ID Account](https://tru.id)
- The Node.js installed and the [server example](https://github.com/tru-ID/server-example-node) running
    - Follow the instructions within the tru.ID example node.js server README
    - A local tunnel solution such as [ngrok](https://ngrok.com/)
- An Android phone with a SIM card and mobile data connection

## Getting Started

- Get the server example up and running
- Run your local tunnel solution and tunnelling the requests to the running server
- Clone or unzip the PhoneCheckExample into a directory.
- Open the project with your Android Capable IDE
- Once you have your server up and running make a note of your server URL.
Create a file named truid.properties in the root directory of your project and update the configuration value to be the URL of your example server
```code
EXAMPLE_SERVER_BASE_URL="https://example.com"
```
- Connect your phone to your computer so it's used for running the PhoneCheckExample application
- Run the application from your IDE
- Enter the phone number for the mobile device in the UI in the format +{country_code}{number} e.g. `+447900123456`
- Press the done keyboard key or touch the "Verify my phone number" button
- You will see the result of the Phone Check
- Get in touch: please email feedback@tru.id with any questions

## References

- [tru.ID example node.js server](https://github.com/tru-ID/server-example-node)

## Meta

Distributed under the MIT license. See [LICENSE][license-url] for more information.

[https://github.com/tru-ID](https://github.com/tru-ID)

[license-image]: https://img.shields.io/badge/License-MIT-blue.svg
[license-url]: LICENSE.md