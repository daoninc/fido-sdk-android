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
are up-to-date.

### `app/src/main/java/com/daon/fido/sdk/sample/kt/util/FidoHolder.kt`

This file contains the `FidoHolder` class, which is a singleton that manages the FIDO SDK instance 
and the RPSA/REST server instance.

- **RPSA Server Configuration:**

    ```kotlin
    val rpsaParams = Bundle()
    rpsaParams.putString("server_url", <server-url>)
    return IXUAFRPSAService(context, rpsaParams)
    ```

- **REST Service Configuration:**
-
    ```kotlin
    val restParams = Bundle()
    restParams.putString("appId", <app-id>)
    restParams.putString("regPolicy", <reg-policy>)
    restParams.putString("authPolicy", <auth-policy>)
    restParams.putString("username", <username>)
    restParams.putString("password", <passsword>)
    restParams.putString("server_url", <server-url>)
    restParams.putString("rest_path", <rest-path>)
    return IXUAFRestService(context, restParams)
    ```

## Usage

1. **Create the RPSA server instance:**

    ```kotlin
    val rpsaServer = IXUAFRPSAService(context, rpsaParams)
    ```
2. **Create the IXUAF instance:**

    ```kotlin
    var fido = IXUAF(context, rpsaServer)
    ```
3. **Access the FIDO SDK instance using FidoHolder:**

 ```kotlin
    val fidoHolder = FidoHolder.getInstance(context)
    val fido = fidoHolder.fido
    ```
   
## License
The FIDO SDK requires a license that is bound to an application identifier. This license may in 
turn embed licenses that are required for specific authenticators. Contact Daon Support or Sales to 
request a license.
