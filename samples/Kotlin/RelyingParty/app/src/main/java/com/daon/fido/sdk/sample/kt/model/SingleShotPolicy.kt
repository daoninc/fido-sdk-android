package com.daon.fido.sdk.sample.kt.model

import androidx.annotation.StringRes
import com.daon.fido.sdk.sample.kt.R

enum class SingleShotPolicy(@param:StringRes val labelResId: Int) {
    ALL_REGISTERED(R.string.single_shot_policy_all_registered),
    SILENT(R.string.single_shot_policy_silent),
    FINGERPRINT(R.string.single_shot_policy_fingerprint),
    SILENT_AND_FINGERPRINT(R.string.single_shot_policy_and);

    companion object {
        // Sample-only service parameter; not part of the SDK API.
        const val PARAMETER_KEY = "sample.singleShotPolicy"

        fun fromParameter(value: String?): SingleShotPolicy =
            if (value == null) ALL_REGISTERED else valueOf(value)
    }
}
