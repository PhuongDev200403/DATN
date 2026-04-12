package com.buixuantruong.shopapp.Configuration;

import com.buixuantruong.shopapp.model.Role;
import com.buixuantruong.shopapp.security.jwt.JWTTokenFilter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.Arrays;
import java.util.List;

import static org.springframework.http.HttpMethod.*;

@Configuration
@EnableMethodSecurity
@EnableWebMvc
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class WebSecurityConfig {

    JWTTokenFilter jwtTokenFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity security) throws Exception {
        security.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers(OPTIONS, "/**").permitAll()
                        .requestMatchers("/error").permitAll()

                        .requestMatchers("/api/v1/users/register", "/api/v1/users/login").permitAll()
                        .requestMatchers("/api/v1/auth/**").permitAll()

                        .requestMatchers(GET, "/api/v1/categories/**").permitAll()
                        .requestMatchers(GET, "/api/v1/products/**").permitAll()
                        .requestMatchers(GET, "/api/v1/variants/**").permitAll()
                        .requestMatchers(GET, "/api/v1/shipping/**").permitAll()
                        .requestMatchers(GET, "/api/v1/flash-sales/**").permitAll()
                        .requestMatchers(GET, "/api/v1/payments/vnpay-payment-callback").permitAll()
                        .requestMatchers(POST, "/api/v1/payments/create").permitAll()
                        .requestMatchers(POST, "/api/v1/support/**").permitAll()
                        .requestMatchers(POST, "/api/v1/coupons/apply").permitAll()

                        .requestMatchers("/api/v1/admin/**").hasRole(Role.ADMIN.name())
                        .requestMatchers("/api/v1/coupons/**").hasRole(Role.ADMIN.name())

                        .requestMatchers(POST, "/api/v1/categories/**").hasRole(Role.ADMIN.name())
                        .requestMatchers(PUT, "/api/v1/categories/**").hasRole(Role.ADMIN.name())
                        .requestMatchers(DELETE, "/api/v1/categories/**").hasRole(Role.ADMIN.name())

                        .requestMatchers(POST, "/api/v1/products/**").hasRole(Role.ADMIN.name())
                        .requestMatchers(PUT, "/api/v1/products/**").hasRole(Role.ADMIN.name())
                        .requestMatchers(DELETE, "/api/v1/products/**").hasRole(Role.ADMIN.name())

                        .requestMatchers(POST, "/api/v1/variants/**").hasRole(Role.ADMIN.name())
                        .requestMatchers(PUT, "/api/v1/variants/**").hasRole(Role.ADMIN.name())
                        .requestMatchers(PATCH, "/api/v1/variants/**").hasRole(Role.ADMIN.name())
                        .requestMatchers(DELETE, "/api/v1/variants/**").hasRole(Role.ADMIN.name())

                        .requestMatchers(POST, "/api/v1/chatbot/**").permitAll()

                        .requestMatchers("/api/v1/carts/**").hasAnyRole(Role.USER.name(), Role.ADMIN.name())
                        .requestMatchers("/api/v1/product-units/**").hasAnyRole(Role.USER.name(), Role.ADMIN.name())
                        .requestMatchers("/api/v1/order_details/**").hasAnyRole(Role.USER.name(), Role.ADMIN.name())
                        .requestMatchers(GET, "/api/v1/payments/orders/**").hasAnyRole(Role.USER.name(), Role.ADMIN.name())
                        .requestMatchers("/api/v1/orders/**").hasAnyRole(Role.USER.name(), Role.ADMIN.name())

                        .anyRequest().authenticated())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler()));
        security.cors(new Customizer<CorsConfigurer<HttpSecurity>>() {
            @Override
            public void customize(CorsConfigurer<HttpSecurity> httpSecurityCorsConfigurer) {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOrigins(List.of("*"));
                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
                configuration.setAllowedHeaders(List.of("*"));
                configuration.setExposedHeaders(List.of("x-auth-token"));
                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                httpSecurityCorsConfigurer.configurationSource(source);
            }
        });

        return security.build();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, exception) -> {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"code\":401,\"message\":\"Unauthorized\"}");
        };
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, exception) -> {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"code\":403,\"message\":\"Forbidden\"}");
        };
    }
}
