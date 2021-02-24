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
import id.tru.authentication.demo.data.*
import id.tru.authentication.demo.databinding.ActivityMainBinding
import id.tru.authentication.demo.util.isPhoneNumberValid
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

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        TruSDK.initializeSdk(applicationContext)

        _binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.verify.setOnClickListener {
            initVerification()
        }
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
            if (!isMobileDataConnectivityEnabled()) {
                startSettingsForResult.launch(CONNECTIVITY_SETTINGS_ACTION)
                return
            }
        }
        startVerification()
    }

    private fun startVerification() {
        invalidateUI()
        createSubscriberCheck()
    }

    private fun invalidateUI() {
        binding.loadingLayout.visibility = View.VISIBLE
        binding.progressCheckview.uncheck()
        binding.progressTv.text = getString(R.string.phone_check_step1)
    }

    // Step 1: Create a Subscriber Check
    private fun createSubscriberCheck() {
        if (!isPhoneNumberValid(binding.phoneNumber.text.toString())) {
            binding.progressTv.text = getString(R.string.phone_check_step1_errror)
            return
        }
        binding.progressTv.text = getString(R.string.phone_check_step1)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitBuilder.apiClient.getSubscriberCheck(
                        SubscriberCheckPost(binding.phoneNumber.text.toString()))

                if (response.isSuccessful && response.body() != null) {
                    subscriberCheck = response.body() as SubscriberCheck

                    withContext(Dispatchers.Main) {
                        binding.progressTv.text = getString(R.string.phone_check_step2)
                    }

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

            withContext(Dispatchers.Main) {
                binding.progressTv.text = getString(R.string.phone_check_step3)
            }

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
            }
        }
    }

    private suspend fun updateUI(subscriberCheckResult: SubscriberCheckResult) {
        withContext(Dispatchers.Main) {
            if (subscriberCheckResult.match && subscriberCheckResult.no_sim_change) {
                binding.progressCheckview.check()
                binding.progressTv.text = getString(R.string.phone_check_complete)
            } else {
                binding.progressTv.text = getString(R.string.phone_check_error)
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