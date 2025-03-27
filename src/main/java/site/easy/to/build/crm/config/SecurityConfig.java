package site.easy.to.build.crm.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import site.easy.to.build.crm.config.oauth2.CustomOAuth2UserService;
import site.easy.to.build.crm.config.oauth2.OAuthLoginSuccessHandler;
import site.easy.to.build.crm.service.user.OAuthUserService;
import site.easy.to.build.crm.util.StringUtils;

import java.util.Optional;


@Configuration
public class SecurityConfig {


    private final OAuthLoginSuccessHandler oAuth2LoginSuccessHandler;

    private final CustomOAuth2UserService oauthUserService;

    private final CrmUserDetails crmUserDetails;

    private final CustomerUserDetails customerUserDetails;

    private final Environment environment;

    @Autowired
    public SecurityConfig(OAuthLoginSuccessHandler oAuth2LoginSuccessHandler, CustomOAuth2UserService oauthUserService, CrmUserDetails crmUserDetails,
                          CustomerUserDetails customerUserDetails, Environment environment) {
        this.oAuth2LoginSuccessHandler = oAuth2LoginSuccessHandler;
        this.oauthUserService = oauthUserService;
        this.crmUserDetails = crmUserDetails;
        this.customerUserDetails = customerUserDetails;
        this.environment = environment;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

//    @Bean
//    @Order(1)  // Premier ordre pour priorité
//    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .securityMatcher("/api/**")  // Isoler uniquement les endpoints API
//                .csrf(AbstractHttpConfigurer::disable)  // Désactiver CSRF pour les APIs
//                .authorizeHttpRequests(authorize -> authorize
//                        .requestMatchers("/api/login").permitAll()
//                        .anyRequest().authenticated()
//                )
//                .httpBasic(AbstractHttpConfigurer::disable)  // Désactiver Basic Auth si non utilisé
//                .formLogin(AbstractHttpConfigurer::disable)  // Désactiver le form login
//                .logout(AbstractHttpConfigurer::disable)  // Désactiver le logout par défaut
//                .sessionManagement(session -> session
//                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))  // API sans état
//                .exceptionHandling(exception -> exception
//                        .authenticationEntryPoint((request, response, authException) -> {
//                            // Retourner une réponse JSON pour les erreurs d'authentification
//                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
//                            response.getWriter().write("{\"error\":\"Unauthorized\", \"message\":\"Full authentication is required to access this resource\"}");
//                        })
//                        .accessDeniedHandler((request, response, accessDeniedException) -> {
//                            // Retourner une réponse JSON pour les accès refusés
//                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//                            response.setStatus(HttpStatus.FORBIDDEN.value());
//                            response.getWriter().write("{\"error\":\"Forbidden\", \"message\":\"You don't have permission to access this resource\"}");
//                        })
//                )
//                .userDetailsService(crmUserDetails);
//
//        return http.build();
//    }

    @Bean
    @Order(3)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        HttpSessionCsrfTokenRepository httpSessionCsrfTokenRepository = new HttpSessionCsrfTokenRepository();
        httpSessionCsrfTokenRepository.setParameterName("csrf");

        http.csrf((csrf) -> csrf
                .csrfTokenRepository(httpSessionCsrfTokenRepository)
        );

        http.
                authorizeHttpRequests((authorize) -> authorize

                        .requestMatchers("/api/**").permitAll()
                        .requestMatchers("/register/**").permitAll()
                        .requestMatchers("/set-employee-password/**").permitAll()
                        .requestMatchers("/change-password/**").permitAll()
                        .requestMatchers("/font-awesome/**").permitAll()
                        .requestMatchers("/fonts/**").permitAll()
                        .requestMatchers("/images/**").permitAll()
                        .requestMatchers("/save").permitAll()
                        .requestMatchers("/js/**").permitAll()
                        .requestMatchers("/css/**").permitAll()
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/**/manager/**")).hasRole("MANAGER")
                        .requestMatchers("/employee/**").hasAnyRole("MANAGER", "EMPLOYEE")
                        .requestMatchers("/customer/**").hasRole("CUSTOMER")
                        .anyRequest().authenticated()
                ).csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))

                .formLogin((form) -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login")
                        .permitAll()
                ).userDetailsService(crmUserDetails)
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(oauthUserService))
                        .successHandler(oAuth2LoginSuccessHandler)
                ).logout((logout) -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                        .permitAll())
                .exceptionHandling(exception -> {
                    exception.accessDeniedHandler(accessDeniedHandler());
                });


        return http.build();
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new CustomAccessDeniedHandler();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain customerSecurityFilterChain(HttpSecurity http) throws Exception {


        HttpSessionCsrfTokenRepository httpSessionCsrfTokenRepository = new HttpSessionCsrfTokenRepository();
        httpSessionCsrfTokenRepository.setParameterName("csrf");

        http.csrf((csrf) -> csrf
                .csrfTokenRepository(httpSessionCsrfTokenRepository)
        );

        http.securityMatcher("/customer-login/**").
                authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers("/api/**").permitAll()
                        .requestMatchers("/set-password/**").permitAll()
                        .requestMatchers("/font-awesome/**").permitAll()
                        .requestMatchers("/fonts/**").permitAll()
                        .requestMatchers("/images/**").permitAll()
                        .requestMatchers("/js/**").permitAll()
                        .requestMatchers("/css/**").permitAll()
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/**/manager/**")).hasRole("MANAGER")
                        .anyRequest().authenticated()
                ).csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))

                .formLogin((form) -> form
                        .loginPage("/customer-login")
                        .loginProcessingUrl("/customer-login")
                        .failureUrl("/customer-login")
                        .defaultSuccessUrl("/", true)
                        .permitAll()).userDetailsService(customerUserDetails)
                .logout((logout) -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/customer-login")
                        .permitAll());

        return http.build();
    }
    @Bean
    @Order(1)  // API en premier
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**") // Applique cette config uniquement aux routes API
                .csrf(AbstractHttpConfigurer::disable) // Désactive CSRF pour l'API
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/**").permitAll() // Autorise GET sans auth
                        .requestMatchers(HttpMethod.POST, "/api/**").permitAll() // POST nécessite auth
                        .anyRequest().denyAll() // Bloquer tout le reste
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Mode sans état
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            // Gestion personnalisée des erreurs d'authentification
                            response.setContentType("application/json");
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Authentication required\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            // Gestion personnalisée des accès refusés
                            response.setContentType("application/json");
                            response.setStatus(HttpStatus.FORBIDDEN.value());
                            response.getWriter().write("{\"error\":\"Forbidden\",\"message\":\"You don't have permission\"}");
                        })
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        return authenticationManagerBuilder.build();
    }




}
