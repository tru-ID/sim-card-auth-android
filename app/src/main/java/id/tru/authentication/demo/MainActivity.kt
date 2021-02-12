package id.tru.authentication.demo

import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import id.tru.authentication.demo.api.RedirectManager
import id.tru.authentication.demo.api.RetrofitBuilder
import id.tru.authentication.demo.data.ConnectivitySettingsContract
import id.tru.authentication.demo.data.PhoneCheck
import id.tru.authentication.demo.data.PhoneCheckPost
import id.tru.authentication.demo.data.PhoneCheckResult
import id.tru.authentication.demo.databinding.ActivityMainBinding
import id.tru.authentication.demo.util.isPhoneNumberValid
import id.tru.sdk.TruSDK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private val redirectManager by lazy { RedirectManager() }
    private lateinit var phoneCheck: PhoneCheck

    private var _binding: ActivityMainBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private val startSettingsForResult = registerForActivityResult(ConnectivitySettingsContract()) {
        Log.i(TAG, "Internet Connectivity Setting dismissed")
        initVerification()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        TruSDK.initializeSdk(applicationContext)

        _binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }


    /** Called when the user taps the Sign In button */
    fun initSignIn(view: View) {
        Log.d(TAG, "phoneNumber " + binding.phoneNumber.text)
        // close virtual keyboard when sign in starts
        binding.phoneNumber.onEditorAction(EditorInfo.IME_ACTION_DONE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!isMobileDataConnectivityEnabled()) {
                startSettingsForResult.launch(CONNECTIVITY_SETTINGS_ACTION)
                return
            }
        }
        initVerification()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    // region internal

    private fun initVerification() {
        resetProgress()
        createPhoneCheck()
    }

    private fun resetProgress() {
        binding.loadingLayout.visibility = View.VISIBLE
        binding.progressCheckview.uncheck()
        binding.progressTv.text = getString(R.string.phone_check_step1)
    }

    // Step 1: Create a Phone Check
    private fun createPhoneCheck() {
        if (!isPhoneNumberValid(binding.phoneNumber.text.toString())) {
            binding.progressTv.text = getString(R.string.phone_check_step1_errror)
            return
        }
        binding.progressTv.text = getString(R.string.phone_check_step1)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitBuilder.apiClient.getPhoneCheck(
                        PhoneCheckPost(binding.phoneNumber.text.toString()))

                if (response.isSuccessful && response.body() != null) {
                    phoneCheck = response.body() as PhoneCheck

                    withContext(Dispatchers.Main) {
                        binding.progressTv.text = getString(R.string.phone_check_step2)
                    }

                    // Step 2: Open the check_url
                    openCheckURL()
                } else {
                    // Show API error.
                    updateUIonError("Error Occurred: ${response.message()}")
                }
            } catch (e: Throwable) {
                updateUIonError("exception caught $e")
            }
        }
    }

    // STEP 2: Open checkUrl
    private fun openCheckURL() {
        CoroutineScope(Dispatchers.IO).launch {
            redirectManager.openCheckUrl(phoneCheck.check_url)

            withContext(Dispatchers.Main) {
                binding.progressTv.text = getString(R.string.phone_check_step3)
            }

            // Step 3: Get Phone Check Result
            getPhoneCheckResult()
        }
    }

    // Step 3: Get Phone Check Result
    private fun getPhoneCheckResult() {
        CoroutineScope(Dispatchers.IO).launch {
            val response = RetrofitBuilder.apiClient.getPhoneCheckResult(phoneCheck.check_id)
            if (response.isSuccessful && response.body() != null) {
                val phoneCheckResult = response.body() as PhoneCheckResult

                withContext(Dispatchers.Main) {
                    if (phoneCheckResult.match) {
                        binding.progressCheckview.check()
                        binding.progressTv.text = null
                    } else {
                        binding.progressTv.text = getString(R.string.phone_check_error)
                    }
                }
            }
        }
    }

    private suspend fun updateUIonError(additionalInfo: String) {
        Log.e(TAG, additionalInfo)
        withContext(Dispatchers.Main) {
            binding.phoneNumber.setText("")
            binding.loadingLayout.visibility = View.INVISIBLE
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun isMobileDataConnectivityEnabled(): Boolean {
        val tm = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        return tm.isDataEnabled
    }

    // endregion internal
    companion object {
        private const val TAG = "MainActivity"
        const val CONNECTIVITY_SETTINGS_ACTION = 1
    }
}