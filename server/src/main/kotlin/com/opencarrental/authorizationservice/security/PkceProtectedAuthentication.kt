package com.opencarrental.authorizationservice.security

import org.springframework.security.oauth2.common.exceptions.InvalidGrantException
import org.springframework.security.oauth2.provider.OAuth2Authentication


class PkceProtectedAuthentication {
    private val codeChallenge: String?
    private val codeChallengeMethod: CodeChallengeMethod
    private val authentication: OAuth2Authentication

    constructor(authentication: OAuth2Authentication) {
        codeChallenge = null
        codeChallengeMethod = CodeChallengeMethod.NONE
        this.authentication = authentication
    }

    constructor(codeChallenge: String?, codeChallengeMethod: CodeChallengeMethod, authentication: OAuth2Authentication) {
        this.codeChallenge = codeChallenge
        this.codeChallengeMethod = codeChallengeMethod
        this.authentication = authentication
    }

    fun getAuthentication(codeVerifier: String?): OAuth2Authentication? {
        return when {
            codeChallengeMethod === CodeChallengeMethod.NONE -> if (codeVerifier === codeChallenge) authentication else null
            codeChallengeMethod.transform(codeVerifier!!) == codeChallenge -> authentication
            else -> throw InvalidGrantException("Invalid code verifier.")
        }
    }
}