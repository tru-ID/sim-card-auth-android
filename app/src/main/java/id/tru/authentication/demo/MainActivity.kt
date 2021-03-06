package id.tru.authentication.demo

import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.inputmethod.EditorInfo
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import id.tru.authentication.demo.api.RedirectManager
import id.tru.authentication.demo.api.RetrofitBuilder
import id.tru.authentication.demo.data.ConnectivitySettingsContract
import id.tru.authentication.demo.data.SubscriberCheck
import id.tru.authentication.demo.data.SubscriberCheckPost
import id.tru.authentication.demo.data.SubscriberCheckResult
import id.tru.authentication.demo.databinding.ActivityMainBinding
import id.tru.authentication.demo.util.isPhoneNumberFormatValid
import id.tru.sdk.TruSDK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity() {

    private val redirectManager by lazy { RedirectManager() }
    private lateinit var subscriberCheck: SubscriberCheck

    private var _binding: ActivityMainBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private val startSettingsForResult = registerForActivityResult(ConnectivitySettingsContract()) {
        Log.i(TAG, "Internet Connectivity Setting dismissed")
        initVerification()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        TruSDK.initializeSdk(applicationContext)

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        toggleVerifyButton()

        binding.verify.setOnClickListener {
            initVerification()
        }
    }

    // ensure phone number input is provided before verify button is enabled
    private fun toggleVerifyButton() {
        binding.phoneNumber.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                binding.verify.isEnabled = (s.length == 13)
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    // region internal

    /** Called when the user taps the verify button */
    private fun initVerification() {
        Log.d(TAG, "phoneNumber " + binding.phoneNumber.text)
        // close virtual keyboard when sign in starts
        binding.phoneNumber.onEditorAction(EditorInfo.IME_ACTION_DONE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!isMobileDataEnabled()) {
                startSettingsForResult.launch(CONNECTIVITY_SETTINGS_ACTION)
                return
            }
        }

        invalidateVerificationUI(false)
        createSubscriberCheck()
    }

    private fun invalidateVerificationUI(isEnabled: Boolean) {
        if (isEnabled) {
            binding.progressBar.hide()
            binding.phoneNumber.setText("")
        } else {
            binding.progressBar.show()
        }
        binding.verify.isEnabled = isEnabled
        binding.phoneNumber.isEnabled = isEnabled
    }

    // Step 1: Create a Subscriber Check
    private fun createSubscriberCheck() {
        if (!isPhoneNumberFormatValid(binding.phoneNumber.text.toString())) {
            invalidateVerificationUI(true)
            Snackbar.make(binding.container, "Phone number invalid", Snackbar.LENGTH_LONG).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitBuilder.apiClient.createSubscriberCheck(
                        SubscriberCheckPost(binding.phoneNumber.text.toString()))

                if (response.isSuccessful && response.body() != null) {
                    subscriberCheck = response.body() as SubscriberCheck

                    // Step 2: Open the check_url
                    openCheckURL(subscriberCheck)
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
    private fun openCheckURL(check: SubscriberCheck) {
        CoroutineScope(Dispatchers.IO).launch {
            redirectManager.openCheckUrl(check.check_url)

            // Step 3: Get Phone Check Result
            getSubscriberCheckResult()
        }
    }

    // Step 3: Get Phone Check Result
    private fun getSubscriberCheckResult() {
        CoroutineScope(Dispatchers.IO).launch {
            val response = RetrofitBuilder.apiClient.getSubscriberCheckResult(subscriberCheck.check_id)
            if (response.isSuccessful && response.body() != null) {
                val subscriberCheckResult = response.body() as SubscriberCheckResult

                updateUI(subscriberCheckResult)
            } else {
                // Show API error.
                updateUIonError("Error Occurred: ${response.message()}")
            }
        }
    }

    private suspend fun updateUI(subscriberCheckResult: SubscriberCheckResult) {
        withContext(Dispatchers.Main) {
            if (subscriberCheckResult.match && subscriberCheckResult.no_sim_change) {
                Snackbar.make(binding.container, "Phone verification complete", Snackbar.LENGTH_LONG).show()
            } else {
                Snackbar.make(binding.container, "Phone verification failed", Snackbar.LENGTH_LONG).show()
            }

            invalidateVerificationUI(true)
        }
    }

    private suspend fun updateUIonError(additionalInfo: String) {
        Log.e(TAG, additionalInfo)
        withContext(Dispatchers.Main) {
            invalidateVerificationUI(true)
            Snackbar.make(binding.container, "Could not verify your number", Snackbar.LENGTH_LONG).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun isMobileDataEnabled(): Boolean {
        val tm = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        return tm.isDataEnabled
    }

    // endregion internal
    companion object {
        private const val TAG = "MainActivity"
        const val CONNECTIVITY_SETTINGS_ACTION = 1
    }
}