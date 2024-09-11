# AuthBasicFaceInjectionDetection

This sample application demonstrates the use of the FIDO SDK Java APIs, including the new face 
authenticator APIs that utilize face capture APIs internally. The Capture API provides injection 
attack prevention when used with the IdentityX server. The app is built with Java.

## Features

- Log in with FIDO
- Register new authenticators
- Step-up authentication
- Deregister authenticators

## Setup

1. **Open the project in Android Studio:**

    Open Android Studio and select `Open an existing project`. Navigate to the cloned repository and open it.

2. **Build the project:**

    Android Studio will automatically download the required dependencies and build the project.

## Configuration

### `gradle/libs.versions.toml`

This file contains the dependencies and their versions used in the project. Ensure that the versions 
are up-to-date.

## Usage

1. **Access the FIDO SDK instance:**

    ```java
    IXUAF fido = Fido.getInstance(getApplicationContext());
    ```

2. **Create the RPSA server instance:**

    ```java
    IXUAFCommService commService = RPSAService.getInstance(getApplicationContext());
    ```
   
## License
The FIDO SDK requires a license that is bound to an application identifier. This license may in 
turn embed licenses that are required for specific authenticators. Contact Daon Support or Sales to 
request a license.
