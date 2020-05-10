package com.opencarrental.authorizationservice.configuration
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.core.io.ClassPathResource
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerEndpointsConfiguration
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer
import org.springframework.security.oauth2.provider.token.TokenStore
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory
import java.security.KeyPair
import java.security.interfaces.RSAPublicKey

@Import(AuthorizationServerEndpointsConfiguration::class)
@Configuration
class AuthorizationServerConfiguration(val passwordEncoder: PasswordEncoder,
                                       val authenticationManager: AuthenticationManager,
                                       @Qualifier("customUserDetailService") val userDetailsService: UserDetailsService,
                                       @Value("\${admin.client_id}") val adminClientId: String,
                                       @Value("\${admin.client_secret}") val adminClientSecret: String,
                                       @Value("\${service_client.client_id}") val serviceClientId: String,
                                       @Value("\${service_client.acces_token_validity_period}") val accessTokenValidity: Int,
                                       @Value("\${service_client.refresh_token_validity_period}") val refreshTokenValidity: Int) : AuthorizationServerConfigurerAdapter() {

    override fun configure(security: AuthorizationServerSecurityConfigurer?) {
        security!!.allowFormAuthenticationForClients()
    }


    override fun configure(clients: ClientDetailsServiceConfigurer?) {
        clients!!.inMemory()
                .withClient(serviceClientId)
                .authorizedGrantTypes("password", "refresh_token")
                .scopes("read")
                .accessTokenValiditySeconds(accessTokenValidity)
                .refreshTokenValiditySeconds(refreshTokenValidity)
                .and()
                .withClient(adminClientId)
                .secret(passwordEncoder.encode(adminClientSecret))
                .authorizedGrantTypes("client_credentials")
                .scopes("all")
    }

    override fun configure(endpoints: AuthorizationServerEndpointsConfigurer?) {
        endpoints!!.tokenStore(tokenStore())
                .accessTokenConverter(jwtAccessTokenConverter())
                .authenticationManager(authenticationManager)
                .userDetailsService(userDetailsService);
    }

    @Bean
    fun keyPair(): KeyPair {
        val ksFile = ClassPathResource("auth-jwt.jks")
        val ksFactory = KeyStoreKeyFactory(ksFile, "auth-pass".toCharArray())
        return ksFactory.getKeyPair("auth-jwt")
    }

    @Bean
    fun jwtAccessTokenConverter(): JwtAccessTokenConverter {
        val converter = JwtAccessTokenConverter()
        converter.setKeyPair(keyPair())
        return converter
    }

    @Bean
    fun tokenStore(): TokenStore = JwtTokenStore(jwtAccessTokenConverter())

    @Bean
    fun jwkSet(): JWKSet? {
        val publicKey = keyPair().public as RSAPublicKey
        val key: RSAKey = RSAKey.Builder(publicKey).build()
        return JWKSet(key)
    }
}