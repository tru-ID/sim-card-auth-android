package id.tru.authentication.demo.api

import android.util.Log
import id.tru.sdk.TruSDK
import org.json.JSONObject

class RedirectManager {
    private val truSdk = TruSDK.getInstance()

    fun openCheckUrl(checkUrl: String) {
        Log.d("RedirectManager", "Triggering open check url $checkUrl")
        truSdk.openCheckUrl(checkUrl)
    }

    suspend fun getJsonResponse(url: String): JSONObject? {
        Log.d("RedirectManager", "Execute GET request")
        return truSdk.getJsonResponse(url)
    }

    suspend fun getJsonPropertyValue(url: String) : String? {
        Log.d("RedirectManager", "Execute GET request")
        return truSdk.getJsonPropertyValue(url, "ip_address")
    }
}