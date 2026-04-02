package com.buixuantruong.shopapp.Configuration;

import com.buixuantruong.shopapp.model.Role;
import com.buixuantruong.shopapp.security.jwt.JWTTokenFilter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
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
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
//                .authorizeHttpRequests(requests -> {
//                    requests.requestMatchers("/api/v1/users/register").permitAll()
//                            .requestMatchers("/api/v1/users/login").permitAll()
//                            .requestMatchers("/api/v1/auth/google/**").permitAll()
//                            .requestMatchers("/api/v1/auth/facebook/**").permitAll()
//                            .requestMatchers("/api/v1/auth/oauth2/callback/**").permitAll()
//                            //category
//                            .requestMatchers(GET, "/api/v1/categories?**").hasAnyRole(Role.USER.name(), Role.ADMIN.name())
//                            .requestMatchers(POST, "/api/v1/categories/**").hasRole(Role.ADMIN.name())
//                            .requestMatchers(PUT, "/api/v1/categories/**").hasRole(Role.ADMIN.name())
//                            .requestMatchers(DELETE, "/api/v1/categories/**").hasRole(Role.ADMIN.name())
//                            //product
//                            .requestMatchers(GET, "/api/v1/products/**").permitAll()
//                            .requestMatchers(POST, "/api/v1/products/**").hasRole(Role.ADMIN.name())
//                            .requestMatchers(PUT, "/api/v1/products/**").hasRole(Role.ADMIN.name())
//                            .requestMatchers(DELETE, "/api/v1/products/**").hasRole(Role.ADMIN.name())
//                            .requestMatchers(GET, "/api/v1/products/images/**").permitAll()
//                            //order
//                            .requestMatchers(PUT, "/api/v1/orders/**").hasRole(Role.ADMIN.name())
//                            .requestMatchers(GET, "/api/v1/orders/**").permitAll()
//                            .requestMatchers(DELETE, "/api/v1/orders/**").hasRole(Role.ADMIN.name())
//                            .requestMatchers(POST, "/api/v1/orders").hasRole(Role.USER.name())
//                            .requestMatchers(GET, "/api/v1/orders/get-user-orders").hasAnyRole(Role.ADMIN.name())
//                            //orderDetail
//                            .requestMatchers(PUT, "/api/v1/order_details").hasRole(Role.ADMIN.name())
//                            .requestMatchers(GET, "/api/v1/order_details/**").hasAnyRole(Role.USER.name(), Role.ADMIN.name())
//                            .requestMatchers(DELETE, "/api/v1/order_details/**").hasRole(Role.ADMIN.name())
//                            .requestMatchers(POST, "/api/v1/order_details").hasRole(Role.USER.name())
//                            //payment
//                            .requestMatchers(POST, "/api/v1/payments/create").permitAll()
//                            .requestMatchers(GET, "/api/v1/payments/vnpay-payment-callback").permitAll()
//                            .requestMatchers(GET, "/api/v1/payments/orders/**").permitAll()
//                            //review
//                            .requestMatchers(GET, "/api/v1/reviews/**").permitAll()
//                            .requestMatchers(POST, "/api/v1/reviews").hasAnyRole(Role.USER.name(), Role.ADMIN.name())
//                            .requestMatchers(PUT, "/api/v1/reviews/**").hasAnyRole(Role.USER.name(), Role.ADMIN.name())
//                            .requestMatchers(DELETE, "/api/v1/reviews/**").hasAnyRole(Role.USER.name(), Role.ADMIN.name())
//                            .requestMatchers(POST, "/api/v1/reviews/*/respond").hasRole(Role.ADMIN.name())
//                            //coupon
//                            .requestMatchers(GET, "/api/v1/coupons/apply").permitAll()
//                            .requestMatchers("/api/v1/coupons/**").hasRole(Role.ADMIN.name())
//                            //analytics + AI
//                            .requestMatchers("/api/v1/admin/analytics/**").hasRole(Role.ADMIN.name())
//                            //flash sale
//                            .requestMatchers("/api/v1/flash-sales/**").hasRole(Role.ADMIN.name())
//                            //shipping
//                            .requestMatchers("/api/v1/shipping/**").permitAll()
//                            //cart
//                            .requestMatchers("/api/v1/carts/**").hasRole(Role.USER.name())
//                            //customer suppoter
//                            .requestMatchers(POST,"/api/v1/support/**" ).permitAll()
//                            .anyRequest().authenticated();
//                })
                .authorizeHttpRequests(requests -> requests.anyRequest().permitAll())
                .csrf((AbstractHttpConfigurer::disable));
        security.cors(new Customizer<CorsConfigurer<HttpSecurity>>() {
            @Override
            public void customize(CorsConfigurer<HttpSecurity> httpSecurityCorsConfigurer) {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOrigins(List.of("*"));
                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
                configuration.setAllowedHeaders(Arrays.asList("x-auth-token", "content-type", "authorization"));
                configuration.setExposedHeaders(List.of("x-auth-token"));
                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                httpSecurityCorsConfigurer.configurationSource(source);
            }
        });

        return security.build();
    }
}
