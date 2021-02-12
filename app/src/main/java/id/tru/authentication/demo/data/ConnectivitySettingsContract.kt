package id.tru.authentication.demo.data

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.RequiresApi

class ConnectivitySettingsContract : ActivityResultContract<Int, Uri?>() {
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun createIntent(context: Context, input: Int) = Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)

    override fun parseResult(resultCode: Int, result: Intent?): Uri? {
        println("Result code $resultCode")

        if (resultCode != Activity.RESULT_OK) {
            return null
        }

        return result?.data
    }

    companion object {
        const val ACTION = "Settings.Panel.ACTION_INTERNET_CONNECTIVITY"
    }
}