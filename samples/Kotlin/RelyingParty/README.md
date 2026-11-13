# RelyingParty

This sample application demonstrates the use of the FIDO SDK Kotlin APIs, including the new face
authenticator APIs that utilize face capture APIs internally. The Capture API provides injection
attack prevention when used with the IdentityX server. The app is built using Kotlin.

## Features

- Log in with FIDO
- Register new authenticators
- Step-up authentication
- Deregister authenticators
- Support for RPSA and REST services

## Setup

1. **Open the project in Android Studio:**

    Open Android Studio and select `Open an existing project`. Navigate to the cloned repository and open it.

2. **Build the project:**

    Android Studio will automatically download the required dependencies and build the project.

## Configuration

### `gradle/libs.versions.toml`

This file contains the dependencies and their versions used in the project. Ensure that the versions 
are up-to-date. Face SDK 5.5 artifacts must support `compileSdk` 36. Keep `face` and
`face-capture` aligned to the same upstream build. Passive face liveness
uses `com.daon.sdk:face-liveness-dfl`; the legacy `com.daon.sdk:face-liveness` artifact
is no longer available.

### Face capture orientation and lifecycle

Face capture supports portrait, reverse portrait, and both landscape orientations. The screen
temporarily requests `fullSensor` and restores the previous orientation policy when it is disposed.
`IntroActivity` handles `orientation|screenSize` configuration changes, allowing Compose to update
the layout without restarting the camera or the in-flight FIDO operation.

The camera is started only after permission is granted, using the preview's window context and
lifecycle owner. It is closed when the screen stops or is disposed. A captured photo and a pending
registration/authentication are retained across backgrounding and account selection; only an
explicit retake or validation retry starts a new capture. If another configuration change recreates
the activity, an unfinished capture is rebound to the new preview and lifecycle owner.

Face SDK 5.5's upright check accepts all four display rotations; it still rejects a device held too
flat. Photo callbacks already contain upright pixels and must not be rotated again by the app.

## Usage

1. **Create the RPSA server instance:**

    ```kotlin
    val rpsaServer = IXUAFRPSAService(context, rpsaParams)
    ```
2. **Create the IXUAF instance:**

    ```kotlin
    var fido = IXUAF(context, rpsaServer)
    ```
   
## License
The FIDO SDK requires a license that is bound to an application identifier. This license may in 
turn embed licenses that are required for specific authenticators. Contact Daon Support or Sales to 
request a license.
