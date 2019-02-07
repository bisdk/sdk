package com.hormann.app.account

import android.accounts.*
import android.accounts.AccountManager.KEY_BOOLEAN_RESULT
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils


class HormannAccountAuthenticator(private val context: Context) : AbstractAccountAuthenticator(context) {

    override fun editProperties(accountAuthenticatorResponse: AccountAuthenticatorResponse, s: String): Bundle? {
        return null
    }


    @Throws(NetworkErrorException::class)
    override fun addAccount(
        response: AccountAuthenticatorResponse, accountType: String,
        authTokenType: String?, requiredFeatures: Array<String>?, options: Bundle?
    ): Bundle? {
        val intent = Intent(context, HormannAccountActivity::class.java)
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, accountType)
        intent.putExtra(ADD_ACCOUNT, true)
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)

        val bundle = Bundle()
        bundle.putParcelable(AccountManager.KEY_INTENT, intent)

        return bundle
    }

    @Throws(NetworkErrorException::class)
    override fun confirmCredentials(
        accountAuthenticatorResponse: AccountAuthenticatorResponse,
        account: Account,
        options: Bundle?
    ): Bundle? {
        return null
    }

    @Throws(NetworkErrorException::class)
    override fun getAuthToken(
        response: AccountAuthenticatorResponse, account: Account,
        authTokenType: String, options: Bundle?
    ): Bundle? {
        val accountManager = AccountManager.get(context)

        var authToken = accountManager.peekAuthToken(account, authTokenType)

        if (TextUtils.isEmpty(authToken)) {
            val password = accountManager.getPassword(account)
            if (password != null) {
                authToken = HormannAccountRegLoginHelper.authenticate(account.name, password)
            }
        }

        if (!TextUtils.isEmpty(authToken)) {
            val result = Bundle()
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name)
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type)
            result.putString(AccountManager.KEY_AUTHTOKEN, authToken)
            return result
        }

        val intent = Intent(context, HormannAccountActivity::class.java)
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, account.type)
        intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, account.name)
        intent.putExtra(TOKEN_TYPE, authTokenType)
        val bundle = Bundle()
        bundle.putParcelable(AccountManager.KEY_INTENT, intent)
        return bundle
    }


    override fun getAuthTokenLabel(authTokenType: String): String? {
        return "full"
    }


    @Throws(NetworkErrorException::class)
    override fun updateCredentials(
        response: AccountAuthenticatorResponse, account: Account,
        authTokenType: String?, options: Bundle?
    ): Bundle? {
        return null
    }


    @Throws(NetworkErrorException::class)
    override fun hasFeatures(
        response: AccountAuthenticatorResponse,
        account: Account,
        features: Array<String>
    ): Bundle? {
        val result = Bundle()
        result.putBoolean(KEY_BOOLEAN_RESULT, false)
        return result
    }

    companion object {
        const val PASSWORD = "password"
        const val ADD_ACCOUNT = "addAccount"
        const val TOKEN_TYPE = "tokenType"
    }
}