package id.tru.authentication.demo.api

import android.util.Log
import id.tru.sdk.TruSDK

class RedirectManager {
    private val truSdk = TruSDK.getInstance()

    fun openCheckUrl(checkUrl: String): String {
        Log.d("RedirectManager", "Triggering open check url $checkUrl")
        return truSdk.openCheckUrl(checkUrl)
    }
}