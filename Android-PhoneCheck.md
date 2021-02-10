# How to Add SIM Card Based Authentication to your Android App [with tru.ID]

## Introduction

Have you ever wanted to authenticate the users of your Android app by using the phone number?
There are many traditional solutions, but these mechanisms still rely on SMS to send a one-time code to the end user.

Here at [tru.ID] we have come up with an innovative solution, called *PhoneCheck*, that confirms the ownership of a mobile phone number by verifying the possession of an active SIM card with the same number.
That's right, no more one-time code exchange, no more logic around PIN mis-type or retry due to delivery failure.
This means with Instant PhoneCheck the user never leaves your app, and the entire verification happens seamlessly. Fewer dependencies, better security and a better experience.

This is what you're going to build:
A basic Android application that implements a phone number authentication/sign-in flow using the PhoneCheck API and SDK.
**Estimated completion time: 20 mins**

The completed Android sample app in the [sim-card-auth-android repo](https://github.com/tru-ID/sim-card-auth-android) on GitHub. The repo includes a README with documentation for running and exploring the code.

Let's run through this application step by step ...

## Before you begin

<details><summary>To complete this tutorial, you’ll need:</summary>

* Basic knowledge in Kotlin programming
* An updated version of [Android Studio](https://developer.android.com/studio "Download Android Studio")
* If you haven't already, register for a [truID account](https://tru.id/)
* Setup a local server using the Node.js [example server](https://tru.id/docs/phone-check/getting-started-android#2-setup-the-nodejs-example-server)
* One more thing to remember is that the PhoneCheck requires a physical device with an active SIM card, and won’t work on an emulator

</details>


## Step 1: Let's start by creating a new project

First, you have to create a new Android application using Android Studio. The app name is SIMAuthentication. The package name is `id.tru.authentication.demo`.
Click through the wizard, ensuring that Empty Activity is selected. Leave the Activity Name set to MainActivity, and leave the Layout Name set to activity_main.


## Step 2: Phone number authentication UI
To allow the end user to input his phone number we use Material Design EditText object.
The first screen will be our Verification screen on which the user has to add his phone number. After adding his phone number, the user will click on the "Verify my phone number" button,

The main screen will look like this:
[Design preview](https://github.com/tru-ID/sim-card-auth-android/tree/main/images/main_layout.png)


## Step 3: PhoneCheck workflow

The sequence diagram below shows the complete PhoneCheck Workflow.
(TODO 1 create small diagram)[...]

In order to communicate with our local server we will integrate Retrofit and relevant JSON converters:

```groovy
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
```

Create a Retrofit instance using Retrofit.Builder and pass your interface to generate an implementation in a new file RetrofitBuilder.kt

```kotlin
object RetrofitBuilder {
    val apiClient: ApiService by lazy {

        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        val okHttpClient = OkHttpClient()
            .newBuilder()
            //httpLogging interceptor for logging network requests
            .addInterceptor(httpLoggingInterceptor)
            .build()

        Retrofit.Builder()
            .baseUrl(BuildConfig.SERVER_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
```
We have added the HttpLoggingInterceptor just so we can check what is going on every step of the way.

Create an implementation of the API endpoints defined by the Service interface in a new file ApiService.kt
This interface is empty at the moment
```kotlin
interface ApiService {
}
```

To perform network operations in your application, your manifest must include the following permissions:
```code
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```
Note: Both the Internet and ACCESS_NETWORK_STATE permissions are [normal permissions](https://developer.android.com/guide/topics/permissions/overview#normal-dangerous), which means they're granted at install time and don't need to be requested at runtime.

### 3.1 Creating a PhoneCheck
To start the phone number verification flow in an Android app, you send the phone number to your verification server, that will create a PhoneCheck resource on the tru.ID platform.
In order to do this you need the phone number that you want to perform the check for.

```kotlin
interface ApiService {

    @Headers("Content-Type: application/json")
    @POST("/check")
    suspend fun getPhoneCheck(@Body user: PhoneCheckPost): Response<PhoneCheck>
}
```

Let's create the models for our network operations in a new file Model.kt
```kotlin
data class PhoneCheckPost(
    @SerializedName("phone_number")
    val phone_number: String
)

data class PhoneCheck(
    @SerializedName("check_url")
    val check_url: String,
    @SerializedName("check_id")
    val check_id: String
)
```

Now we are ready to create the PhoneCheck using the provided phone number:

```kotlin
    private fun createPhoneCheck() {
        CoroutineScope(Dispatchers.IO).launch {
            val phoneCheck = RetrofitBuilder.apiClient.getPhoneCheck(PhoneCheckPost(phone_number.text.toString()))
        }
    }
```

### 3.2 Navigate to the PhoneCheck URL
At this point you're going to add `tru-sdk-android` SDK to your project.
The purpose of using the SDK is to make sure execution of a phone check verification request is done against the mobile data connection.

Edit the build.gradle for your project (at your project's root) and add the following code snippet to the allprojects/repositories section:
The app uses Maven to load the tru.ID Android SDK:

```groovy
    maven {
        url "https://gitlab.com/api/v4/projects/22035475/packages/maven"
    }
 ```

 Now add the dependency, and don't forget to Sync the project.
```groovy
implementation 'id.tru.sdk:tru-sdk-android:0.0.1'
```

** The `tru-sdk-android` is available on Android devices with minimum Android SDK Version 21 (Lollipop), therefore we have set minSdkVersion = 21.

Write a small RedirectManager in order to keep things tidy, and obtain a TruSDK instance that will be used to perform the network request with PhoneCheck URL over mobile data connection.

```kotlin
class RedirectManager {
    private val truSdk = TruSDK.getInstance()

    fun openCheckUrl(phoneCheckUrl: String) {
        truSdk.openCheckUrl(phoneCheckUrl)
    }
}
```

Now that the mobile device has the PhoneCheck URL it must request that URL. This enables the mobile network operator to identify the phone number associated with the mobile data session.

```kotlin
   private val redirectManager by lazy { RedirectManager() }

    private fun openCheckURL(phoneCheck: PhoneCheck) {
        CoroutineScope(Dispatchers.IO).launch {
            redirectManager.openCheckUrl(phoneCheck.check_url)
        }
    }
```

### 3.3 Get the PhoneCheck Results
The tru.ID platform now knows whether the PhoneCheck Match has been performed. Your mobile application and the application server should now query the result. This will in turn trigger the tru.ID platform to fetch the PhoneCheck Match result from the MNO.


Add the PhoneCheckResult model to Model.kt
```kotlin
data class PhoneCheckResult(
    @SerializedName("match")
    val match: Boolean,
    @SerializedName("check_id")
    val check_id: String
)
```

The Service interface will look like this:
```kotlin
interface ApiService {

    @Headers("Content-Type: application/json")
    @POST("/check")
    suspend fun getPhoneCheck(@Body user: PhoneCheckPost): Response<PhoneCheck>

    @GET("/check_status")
    suspend fun getPhoneCheckResult(@Query(value = "check_id") checkId: String): Response<PhoneCheckResult>
}
```

Now it's time to execute the request and find out if the phone number was verified successfully.
```kotlin
    private fun getPhoneCheckResult() {
        CoroutineScope(Dispatchers.IO).launch {
            val phoneCheckResult = RetrofitBuilder.apiClient.getPhoneCheckResult(phoneCheck.check_id)

                withContext(Dispatchers.Main) {
                    if (phoneCheckResult.match) {
                        resultIndicator.text = "Phone verification complete"
                    } else {
                        resultIndicator.text = "Phone verification failed"
                    }
                }
    }
```
There you go, based on the PhoneCheckResult you may notify the user that the authentication has been complete, and probably jump on to the next screen.

### Step 4: To test this end to end, run the server locally
[example server](https://tru.id/docs/phone-check/getting-started-android#2-setup-the-nodejs-example-server)

Once you have your server up and running make a copy of the app/tru.properties.example file 

```code
cp app/tru.properties.example app/tru.properties
```
and update the configuration value to be the URL of your example server.

```code
tru.properties:
EXAMPLE_SERVER_BASE_URL="https://example.com"
```

Let's try out what we have so far!

Now that your code is complete, you can run the application on a real device. Bear in mind that SIM card based authentication would not be possible against an emulator.

[TODO 2 include gif]()

**Troubleshooting**
Don't forget the PhoneCheck validation requires the device to enable Mobile Data.
Because we have attached a *HttpLoggingInterceptor* you can use adb logs to debug your PhoneCheck:
```code
etc..
```

Congratulations! You've finished PhoneCheck Tutorial for Android.
You can continue to play with and adjust the code you've developed here, or check out the Next Steps below.


## Where next?

You can view a completed version of this sample app in the [sim-card-auth-android repo](https://github.com/tru-ID/sim-card-auth-android) on GitHub. This completed version adds code to validate phone number format with `libphonenumber`, shows Internet Connectivity settings dialog prior to Login, and Dagger Hilt because nobody wants to do manual dependency injection anymore.

#### Programmatically reading the device Phone Number, if you wish to leave the phone number input as part of app's responsibility

[Play Services auth-api-phone library](https://developers.google.com/identity/sms-retriever/request#1_obtain_the_users_phone_number) hint picker prompts the user to choose from the phone numbers stored on the device and thereby avoid having to manually type a phone number. 


What if my device supports multiple SIM cards?
Alternatively, if you just want to use platform features and no extra dependencies, Android 5.1 adds [support](https://developer.android.com/about/versions/android-5.1.html#multisim) for using more than one cellular carrier SIM card at a time. This feature lets users activate and use additional SIMs on devices that have two or more SIM card slots, but required READ_PHONE_STATE permission.

```kotlin
    @RequiresApi(Build.VERSION_CODES.M)
    fun readPhoneNumbers() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val subscriptions =
                getSystemService(SubscriptionManager::class.java).activeSubscriptionInfoList

            for (item in subscriptions) {
                Log.d(TAG, "number " + item.number)
                Log.d(TAG, "network name " + item.carrierName)
                Log.d(TAG, "country iso " + item.countryIso)
            }
        }
    }
```

#### Accessibility
If your application targets **Accessibility**, you will find the phone number verification flow perfectly clear for those users that rely on TalkBack. There is no need to be pasting digits into small boxes, or attempt to replay the code from another application.






 