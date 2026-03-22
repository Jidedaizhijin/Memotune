package com.jidedaizhijin.myapplication

import android.content.Context

object ApiSettingsStore {

    private const val PREFS_NAME = "api_settings"

    private const val KEY_PROVIDER = "provider"
    private const val KEY_API_KEY = "api_key"
    private const val KEY_BASE_URL = "base_url"
    private const val KEY_MODEL = "model"

    const val PROVIDER_DEEPSEEK = "deepseek"
    const val PROVIDER_OPENAI = "openai"
    const val PROVIDER_QIANWEN = "qianwen"
    const val PROVIDER_CUSTOM = "custom"

    fun saveProvider(context: Context, provider: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PROVIDER, provider)
            .apply()
    }

    fun getProvider(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_PROVIDER, PROVIDER_DEEPSEEK)
            ?: PROVIDER_DEEPSEEK
    }

    fun saveApiKey(context: Context, apiKey: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_API_KEY, apiKey)
            .apply()
    }

    fun getApiKey(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_API_KEY, "")
            ?: ""
    }

    fun saveBaseUrl(context: Context, baseUrl: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_BASE_URL, baseUrl)
            .apply()
    }

    fun getBaseUrl(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_BASE_URL, defaultBaseUrl(getProvider(context)))
            ?: defaultBaseUrl(getProvider(context))
    }

    fun saveModel(context: Context, model: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_MODEL, model)
            .apply()
    }

    fun getModel(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_MODEL, defaultModel(getProvider(context)))
            ?: defaultModel(getProvider(context))
    }

    fun defaultBaseUrl(provider: String): String {
        return when (provider) {
            PROVIDER_DEEPSEEK ->
                "https://api.deepseek.com/chat/completions"

            PROVIDER_OPENAI ->
                "https://api.openai.com/v1/chat/completions"

            PROVIDER_QIANWEN ->
                "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions"

            else -> ""
        }
    }

    fun defaultModel(provider: String): String {
        return when (provider) {
            PROVIDER_DEEPSEEK -> "deepseek-chat"
            PROVIDER_OPENAI -> "gpt-4o-mini"
            PROVIDER_QIANWEN -> "qwen-turbo"
            else -> ""
        }
    }

    fun applyProviderDefaults(context: Context, provider: String) {

        saveProvider(context, provider)

        if (provider != PROVIDER_CUSTOM) {

            saveBaseUrl(context, defaultBaseUrl(provider))
            saveModel(context, defaultModel(provider))

        }
    }
}


