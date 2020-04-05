/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.traceindia.covid.client.android.shared.util

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.telephony.TelephonyManager
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

object PhoneNumberAuthUtils {
    fun isPhoneNumberValid(number: String): Boolean {
        return android.util.Patterns.PHONE.matcher(number).matches()
    }

    fun getDeviceCallingCode(context: Context?) : Int{
        return PhoneNumberUtil.createInstance(context).getCountryCodeForRegion(getDeviceCountryCode(context))

    }

    private fun getDeviceCountryCode(context: Context?): String? {
        var countryCode: String?
        // try to get country code from TelephonyManager service
        val tm = context?.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        if (tm != null) { // query first getSimCountryIso()
            countryCode = tm.simCountryIso
            if (countryCode != null && countryCode.length == 2) return countryCode.toUpperCase()
            countryCode = if (tm.phoneType == TelephonyManager.PHONE_TYPE_CDMA) { // special case for CDMA Devices
                getCDMACountryIso()
            } else { // for 3G devices (with SIM) query getNetworkCountryIso()
                tm.networkCountryIso
            }
            if (countryCode != null && countryCode.length == 2) return countryCode.toUpperCase()
        }
        // if network country not available (tablets maybe), get country code from Locale class
        countryCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.getResources().getConfiguration().getLocales().get(0).getCountry()
        } else {
            context.getResources().getConfiguration().locale.getCountry()
        }
        return if (countryCode != null && countryCode.length == 2) countryCode.toUpperCase() else "IN"
        // general fallback to "us"
    }

    @SuppressLint("PrivateApi")
    private fun getCDMACountryIso(): String? {
        try { // try to get country code from SystemProperties private class
            val systemProperties = Class.forName("android.os.SystemProperties")
            val get: Method = systemProperties.getMethod("get", String::class.java)
            // get homeOperator that contain MCC + MNC
            val homeOperator = get.invoke(systemProperties,
                    "ro.cdma.home.operator.numeric") as String
            // first 3 chars (MCC) from homeOperator represents the country code
            val mcc = homeOperator.substring(0, 3).toInt()
            when (mcc) {
                330 -> return "PR"
                310 -> return "US"
                311 -> return "US"
                312 -> return "US"
                316 -> return "US"
                283 -> return "AM"
                460 -> return "CN"
                455 -> return "MO"
                414 -> return "MM"
                619 -> return "SL"
                450 -> return "KR"
                634 -> return "SD"
                434 -> return "UZ"
                232 -> return "AT"
                204 -> return "NL"
                262 -> return "DE"
                247 -> return "LV"
                255 -> return "UA"
            }
        } catch (ignored: ClassNotFoundException) {
        } catch (ignored: NoSuchMethodException) {
        } catch (ignored: IllegalAccessException) {
        } catch (ignored: InvocationTargetException) {
        } catch (ignored: NullPointerException) {
        }
        return null
    }
}



