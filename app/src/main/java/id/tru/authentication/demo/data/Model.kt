package id.tru.authentication.demo.data

import com.google.gson.annotations.SerializedName

data class SubscriberCheckPost(
        @SerializedName("phone_number")
        val phone_number: String
)

data class SubscriberCheck(
        @SerializedName("check_url")
        val check_url: String,
        @SerializedName("check_id")
        val check_id: String
)

data class SubscriberCheckResult(
        @SerializedName("match")
        val match: Boolean,
        @SerializedName("no_sim_change")
        val no_sim_change: Boolean,
        @SerializedName("check_id")
        val check_id: String
)