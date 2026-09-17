package com.pedidos360.product.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Value("${app.security.audience}")
    private String expectedAudience;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            // No usamos sesiones ni CSRF porque la API utiliza JWT
            .csrf(AbstractHttpConfigurer::disable)

            // Habilitar CORS integrado con Spring Security
            .cors(cors -> {})

            .sessionManagement(sess ->
                sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            .authorizeHttpRequests(auth -> auth

                // Permitir las peticiones OPTIONS utilizadas por CORS
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // Imágenes de productos públicas
                .requestMatchers("/images/**").permitAll()

                // GET de productos es público
                .requestMatchers(
                    HttpMethod.GET,
                    "/products/**"
                ).permitAll()

                // POST, PUT y DELETE de productos requieren ADMIN
                .requestMatchers(
                    "/products/**"
                ).hasRole("ADMIN")

                // Cualquier otra ruta requiere autenticación
                .anyRequest().authenticated()
            )

            // Validación del JWT de Microsoft Entra ID
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder())
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            );

        return http.build();
    }

    /**
     * Configuración CORS para permitir que Angular
     * (http://localhost:4200) consuma esta API.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        // Frontend Angular
        configuration.setAllowedOrigins(
            List.of("http://localhost:4200")
        );

        // Métodos utilizados por la API
        configuration.setAllowedMethods(
            List.of(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "OPTIONS"
            )
        );

        // Headers permitidos, incluyendo el JWT
        configuration.setAllowedHeaders(
            List.of(
                "Authorization",
                "Content-Type"
            )
        );

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
            "/**",
            configuration
        );

        return source;
    }

    /**
     * Configuración del decoder para validar
     * los tokens JWT emitidos por Microsoft Entra ID.
     */
    @Bean
    public JwtDecoder jwtDecoder() {

        NimbusJwtDecoder decoder =
            NimbusJwtDecoder.withIssuerLocation(issuerUri).build();

        // Valida que el token provenga del issuer configurado
        OAuth2TokenValidator<Jwt> defaultValidator =
            JwtValidators.createDefaultWithIssuer(issuerUri);

        // Valida que el token esté destinado a nuestra API
        OAuth2TokenValidator<Jwt> audienceValidator =
            new AudienceValidator(expectedAudience);

        decoder.setJwtValidator(
            new org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator<>(
                defaultValidator,
                audienceValidator
            )
        );

        return decoder;
    }

    /**
     * Convierte los App Roles de Microsoft Entra ID
     * en authorities de Spring Security.
     *
     * Azure:
     * roles: ["ADMIN"]
     *
     * Spring:
     * ROLE_ADMIN
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtGrantedAuthoritiesConverter authoritiesConverter =
            new JwtGrantedAuthoritiesConverter();

        // Claim donde Entra ID entrega los App Roles
        authoritiesConverter.setAuthoritiesClaimName("roles");

        // Spring espera ROLE_ADMIN / ROLE_USER
        authoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter =
            new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(
            authoritiesConverter
        );

        return converter;
    }
}