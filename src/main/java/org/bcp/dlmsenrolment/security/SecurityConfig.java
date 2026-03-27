//package org.bcp.dlmsenrolment.security;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.HttpMethod;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
//import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.web.SecurityFilterChain;
//
//import static org.springframework.security.config.Customizer.withDefaults;
//
//@Configuration
//@EnableWebSecurity
//class SecurityConfig {
//
//    private static final String[] AUTH_WHITELIST = {
//          //   -- Swagger UI v2"/v2/api-docs",
//            "/swagger-resources",
//            "/swagger-resources/**",
//            "/configuration/ui",
//            "/configuration/security",
//            "/swagger-ui.html",
//            "/webjars/**",
//            "/v3/api-docs/**",
//            "/transfert/auth/**",
//            "/public/v1/**",
//          //   -- Swagger UI v3 (OpenAPI)"/v3/api-docs/**",
//            "/swagger-ui/**" // other public endpoints of your API may be appended to this array
//    };
//
//
//    @Bean
//    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//
//
//        http
//                .authorizeHttpRequests(c ->
//                        c
//                                .requestMatchers(AUTH_WHITELIST).permitAll()
//                                .requestMatchers(HttpMethod.GET, "/distribution/**")
//                                .authenticated()
//                                .anyRequest()
//                                .authenticated()
//                )
//                .sessionManagement(c ->
//                        c.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                )
//                .cors(CorsConfigurer::disable)
//                .csrf(CsrfConfigurer::disable)
//                .oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults()));
//        return http.build();
//    }
//}
