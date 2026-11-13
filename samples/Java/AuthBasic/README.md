# AuthBasic

This fragment-based application demonstrates the Java callback API, including registration,
authentication, deregistration, and application-managed face capture. It uses the non-IAD
`fido-auth-face` authenticator; it is not the injection-attack-detection sample.

## Features

- Log in with FIDO
- Register new authenticators
- Step-up authentication
- Deregister authenticators

## Build

- Use JDK 17 and install Android SDK 36. The app requires Android API 24 or later.
- Open this directory as a standalone Android Studio project.
- The committed `gradle/libs.versions.toml` contains the released SDK 4.11.7 dependencies.
  Preserve these versions when sharing or packaging the sample.
- Dependencies are resolved from Google, Maven Central, the local `repository/` directory if
  supplied, GitHub Packages, and Daon's Nexus repository. Configure GitHub Packages credentials
  using the [Daon Maven repository instructions](https://github.com/daoninc/sdk-packages/blob/main/README.md).
  If remote repository access is unavailable, copy the SDK 4.11.7 distribution's `repository/`
  directory into this project.
- Run `./gradlew :app:assembleDebug :app:testDebugUnitTest` in this directory.

## Usage

Access the FIDO SDK instance:

```java
IXUAF fido = Fido.getInstance(getApplicationContext());
```

Create the RPSA server instance:

```java
IXUAFCommService commService = RPSAService.getInstance(getApplicationContext());
```

## Runtime setup

Configure `serverUrl` in `app/src/main/assets/config.properties` for your environment.
The sample account-signup flow is for demonstration only, not production account provisioning.

## License

Obtain a license for `com.daon.fido.sdk.sample.basic` from Daon and place it at
`app/src/main/assets/license.txt`. Configure the server's allowed facets for the certificate that
signs your app. Compilation does not validate the license, server configuration, camera behavior,
or device authentication flows.

Face SDK 5.5 uses passive liveness V2 (`face-liveness-dfl`). Do not add the removed V1 liveness
library or alerts from older tutorials. Preserve the dependency versions from the selected SDK
distribution when sharing or packaging the sample.
