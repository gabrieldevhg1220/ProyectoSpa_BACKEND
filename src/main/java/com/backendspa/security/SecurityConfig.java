package com.backendspa.security;

import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtRequestFilter jwtResquestFilter;
    private final SpaUserDetailsService spaUserDetailsService;

    public SecurityConfig(@Lazy JwtRequestFilter jwtResquestFilter, SpaUserDetailsService spaUserDetailsService) {
        this.jwtResquestFilter = jwtResquestFilter;
        this.spaUserDetailsService = spaUserDetailsService;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200", "http://localhost:8080", "https://mdssentirsebien.netlify.app/")); // Añadido localhost:8080 para desarrollo
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // Tiempo de caché para preflight requests
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtRequestFilter jwtRequestFilter) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Habilitar CORS explícitamente
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Permitir solicitudes OPTIONS sin autenticación
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**", "/api/auth/register").permitAll()
                        .requestMatchers("/api/admin/**").hasAuthority("ROLE_GERENTE_GENERAL") // Solo GERENTE_GENERAL
                        .requestMatchers("/api/roles/**").hasAuthority("ROLE_GERENTE_GENERAL") // Solo GERENTE_GENERAL
                        .requestMatchers("/api/recepcionista/**").hasAuthority("ROLE_RECEPCIONISTA") // Nueva regla para rol RECEPCIONISTA.
                        .requestMatchers("/api/factura/**").hasAnyAuthority("ROLE_CLIENTE", "ROLE_RECEPCIONISTA", "ROLE_GERENTE_GENERAL")
                        .requestMatchers("/api/clientes/**").authenticated()
                        .requestMatchers("/api/servicios/**").authenticated()
                        .requestMatchers("/api/empleados/**").authenticated() // Asegurar que empleados sea accesible para autenticados
                        .requestMatchers("/api/reservas/**").authenticated() // Permitir a clientes y empleados autenticados
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder
                .userDetailsService(spaUserDetailsService)
                .passwordEncoder(passwordEncoder());
        return authenticationManagerBuilder.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}