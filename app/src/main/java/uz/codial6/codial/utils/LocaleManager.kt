package uz.codial6.codial.utils

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import com.orhanobut.hawk.Hawk
import java.util.*

object LocaleManager {

    fun setLocale(mContext: Context): Context {
        Hawk.init(mContext).build()
        val lang = getLanguagePref(mContext)
        return if (lang == "empty") {
            mContext
        } else updateResources(mContext, lang)
    }

    private fun getLanguagePref(mContext: Context?): String {
        return Hawk.get("pref_lang", "empty")
    }

    private fun updateResources(context: Context, language: String): Context {
        var mContext = context
        val locale = Locale(language)
        Locale.setDefault(locale)

        val res: Resources = mContext.resources
        val config = Configuration(res.configuration)
        config.setLocale(locale)
        mContext = mContext.createConfigurationContext(config)
        res.updateConfiguration(config, res.displayMetrics)

        return mContext
    }
}