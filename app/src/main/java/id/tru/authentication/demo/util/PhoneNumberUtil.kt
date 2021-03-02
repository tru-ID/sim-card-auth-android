package id.tru.authentication.demo.util

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber

fun isPhoneNumberFormatValid(phoneNumber: String): Boolean {
    val phoneNumberUtil = PhoneNumberUtil.getInstance()
    return try {
        phoneNumberUtil.isValidNumber(phoneNumberUtil.parse(
                phoneNumber, Phonenumber.PhoneNumber.CountryCodeSource.UNSPECIFIED.name))
    } catch (e: NumberParseException) {
        false
    }
}