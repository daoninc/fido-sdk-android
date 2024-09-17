# xAuth FIDO SDK

The FIDO (Fast Identity Online) API is a set of protocols and standards developed to provide a secure and easy-to-use method for user authentication.

## FIDO UAF
FIDO UAF (Universal Authentication Framework) is a set of specifications developed by the FIDO Alliance that provides a secure and easy-to-use authentication framework for online services. It is designed to replace traditional password-based authentication methods with more secure and user-friendly alternatives.

FIDO UAF works by using public key cryptography to authenticate users. When a user wants to authenticate themselves to an online service, their device generates a public-private key pair. The private key is stored securely on the device, while the public key is registered with the online service. When the user wants to authenticate themselves, they simply need to provide a signature using their private key, which can be verified by the online service using the registered public key.

One of the key benefits of FIDO UAF is that it is resistant to phishing attacks, since the user's private key is never transmitted over the network. This means that even if an attacker is able to intercept the authentication request, they will not be able to use the user's private key to authenticate themselves to the service.

FIDO UAF also supports a wide range of authentication methods, including biometrics, PINs, and Passkeys. This allows users to choose the authentication method that works best for them, while still maintaining a high level of security.

## License
The SDK requires a license that is bound to an application identifier. This license may in turn embed licenses that are required for specific authenticators. Contact Daon Support or Sales to request a license.

## Samples

The demo sample includes the following:

- **RelyingParty**: A reference sample Relying Party application.

- **AuthBasicFaceInjectionDetection**: Basic sample application with face authentication using IFP.

- **AuthBasic**: Basic sample application for use with the tutorial.


## SDK repository
In your project-level build.gradle file, make sure to include the Daon Maven repository in your buildscript or allprojects sections.

[Daon Maven repository](https://github.com/daoninc/sdk-packages/blob/main/README.md)

## API

Add the following dependencies to the build.gradle file:

```gradle
implementation 'com.daon.sdk:fido-kt:4.8.108'
implementation 'com.daon.sdk:fido-device:4.8.4'
implementation 'com.daon.sdk:fido-crypto:4.8.7'
implementation 'com.daon.sdk:fido-auth-common:4.8.32'
implementation 'com.daon.sdk:fido-auth-authenticator:4.8.32'

// Face authenticator with Injection Attack Detection
implementation 'com.daon.sdk:fido-auth-face-ifp:4.8.32'
implementation 'com.daon.sdk:face:5.3.36'
implementation 'com.daon.sdk:face-quality:3.2.103'
implementation 'com.daon.sdk:face-capture:1.7.36'

// Optional medical mask detection
implementation 'com.daon.sdk:face-mask:1.0.10'

// Optional client side passive liveness V1
implementation 'com.daon.sdk:face-liveness:5.3.36'

// Continuity checks and local face matching
implementation 'com.daon.sdk:face-matcher:1.3.2'

// CameraX core library
// Used by the face capture library

def camerax_version = "1.3.4"

implementation "androidx.camera:camera-core:${camerax_version}"
implementation "androidx.camera:camera-camera2:${camerax_version}"
implementation "androidx.camera:camera-lifecycle:${camerax_version}"
implementation "androidx.camera:camera-video:${camerax_version}"
implementation "androidx.camera:camera-view:${camerax_version}"
implementation "androidx.camera:camera-extensions:${camerax_version}"
```

The face capture library is part of the [xProof Face SDK](https://github.com/daoninc/face-sdk-android) and is used to capture images of the user's face for authentication. 

See included samples for details and additional information.


### Initialize

Initialize a new IXUAF instance using the RPSA server.

```kotlin

val rpsaParams = Bundle()
rpsaParams.putString("server_url", <server-url>)

val rpsaServer = IXUAFRPSAService(context, rpsaParams) 
var fido = IXUAF(context, rpsaServer)

val parameters = Bundle()
parameters.putString("com.daon.sdk.log", "true")
parameters.putString("com.daon.sdk.ados.enabled", "true")

viewModelScope.launch(Dispatchers.Default)  {    
    when (val response = fido.initialize(parameters)) {
        is Success -> {
            // SDK is initialized            
        }

        is Failure -> {
            // SDK failed to initialize
        }
    }
}
```

See included samples and [xAuth FIDO SDK Documentation](https://developer.identityx-cloud.com/client/fido/android/) for details and additional information.

### Register 

Register a new authenticator with the FIDO server.

```kotlin
viewModelScope.launch(Dispatchers.Default) {
    when (val response = fido.register(bundle)) {
        is Success -> {
            // Handle successful registration.            
        }

        is Failure -> {
            // Handle registration failure.            
        }
    }

```

See included samples and [xAuth FIDO SDK Documentation](https://developer.identityx-cloud.com/client/fido/android/) for details and additional information.

### Authenticate

Authenticate the user with the FIDO server. If a username is provided, a step-up authentication is performed.

```kotlin
viewModelScope.launch(Dispatchers.Default) {
    val bundle = Bundle()
    bundle.putString(IXUAF.USERNAME, username)
    
    when (val response = fido.authenticate(bundle)) {
        is Success -> {
            // Handle successful authentication.            
        }

        is Failure -> {
            // Handle authentication failure.
        }
    }
}
```

See included samples and [xAuth FIDO SDK Documentation](https://developer.identityx-cloud.com/client/fido/android/) for details and additional information.



