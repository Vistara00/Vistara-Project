package com.example.vistaraapp.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import org.json.JSONObject

object TokenManager {
    private const val PREFS_NAME = "vistara_auth_prefs"
    private const val TOKEN_KEY = "auth_token"
    
    private var prefs: SharedPreferences? = null
    
    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    fun saveToken(token: String) {
        prefs?.edit()?.putString(TOKEN_KEY, token)?.apply()
    }
    
    fun getToken(): String? {
        return prefs?.getString(TOKEN_KEY, null)
    }
    
    fun clearToken() {
        prefs?.edit()?.remove(TOKEN_KEY)?.apply()
    }

    /**
     * Helper to verify if the decoded role matches a Park Ranger.
     */
    fun isRangerRole(role: String?): Boolean {
        if (role.isNullOrEmpty()) return false
        val normalized = role.trim().uppercase()
        return normalized.contains("RANGER")
    }

    /**
     * Decodes a JWT token and extracts the user role from its payload claims.
     */
    fun decodeJwtRole(token: String): String? {
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return null
            val payloadEncoded = parts[1]
            val payloadDecodedBytes = Base64.decode(
                payloadEncoded, 
                Base64.URL_SAFE or Base64.NO_WRAP or Base64.DEFAULT
            )
            val payloadString = String(payloadDecodedBytes, Charsets.UTF_8)
            val jsonObject = JSONObject(payloadString)
            
            when {
                jsonObject.has("role") -> jsonObject.getString("role")
                jsonObject.has("roles") -> jsonObject.getString("roles")
                jsonObject.has("roleName") -> jsonObject.getString("roleName")
                jsonObject.has("role_name") -> jsonObject.getString("role_name")
                jsonObject.has("http://schemas.microsoft.com/ws/2008/06/identity/claims/role") -> {
                    jsonObject.getString("http://schemas.microsoft.com/ws/2008/06/identity/claims/role")
                }
                else -> null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
