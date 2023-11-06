package com.seniorjob.seniorjobserver.config;

import com.seniorjob.seniorjobserver.controller.CustomAccessDeniedHandler;
import com.seniorjob.seniorjobserver.domain.entity.UserEntity;
import com.seniorjob.seniorjobserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Arrays;

@SpringBootApplication
@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private UserRepository userRepository;
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/api/users/join", "/api/users/all", "/api/users/login",
                        "/api/lectures/all", "/api/lectures/filter", "/api/lectures/detail/**", "/api/lectures/search",
                        "/api/lectures/sort/**", "/api/lectures/paging", "/api/lectureapply/list", "/api/lectureproposal/filter",
                        "/api/lectureproposal/all", "/api/lectureproposal/detail/**", "/api/lectures/popular").permitAll()
                .antMatchers("/", "/login", "/lecture/lectureList", "/lecture/detail/**",
                        "/register", "/lecture/cancel-lecture-apply/**").permitAll()
                .antMatchers( "/api/users/update", "/api/lectures", "/api/lectures/**",
                        "/api/lectureapply/apply/**", "/api/lectureapply/close", "/api/lectures/myLectureAll",
                        "api/lectures/myLectureDetail/**", "aip/lectureapply/cancel/**",
                        "/api/lectureapply/close", "/api/lectureapply/approve", "/api/lectureproposal/apply",
                        "/api/lectureproposal/update", "/api/lectureproposal/delete", "/api/lectureproposalapply/apply",
                        "/api/lectureproposalapply/cancel/**", "/api/lectureproposalapply/approve",
                        "/api/lectureproposalapply/close", "/api/users/detail",
                        "/api/mypageApplyLecture/myApplyLectureAll", "/api/mypageApplyLecture/updateLectureApplyReason",
                        "/api/mypageCreateLecture/myCreateLectureAll", "/api/mypageCreateLecture/myCreateLectureDetail/**",
                        "/api/myProposalLecture/myProposalAll", "/api/users/delete", "/api/mypageApplyLecture/deleteLectureApply/**",
                        "/api/lecturesStepTwo/week-title/**", "/api/lectures/completeCreation", "/api/lecturesStepTwo/**/weeks",
                        "/api/lecturesStepTwo/lectures/**/weeks/**/plans", "/api/lectureStepTwo/**/attendance", "/api/lectureStepTwo/**/review",
                        "/api/lecturesStepTwo/**/weeks/**/week-update", "/api/lecturesStepTwo/**/weeks/**/week-delete",
                        "/api/lecturesStepTwo/**/weeks/**/plans/**/plan-update", "/api/lecturesStepTwo/**/weeks/**/plans/**/plan-delete",
                        "/api/lectures/delete/**", "/api/lectures/recommendLecture", "/api/myProposalLecture/filter", "/api/myProposalLecture/myProposalDetail/",
                        "/api/mypageApplyLecture/filter", "/api/mypageApplyLecture/myApplyLectureDetail/**"
                ).authenticated()
                .antMatchers("/lecture/lectureCreate", "/mypage/applied-lectures",
                        "/mypage/edit-apply-reason/**", "/mypage/lecture/apply/**",
                        "/lecture/lectureCreate3").authenticated()
                .anyRequest().authenticated()
                .and()
                .cors().and()
                .formLogin()
                .loginPage("/login")
                .defaultSuccessUrl("/main")
                .failureHandler(new CustomAuthFailureHandler())
                .successHandler(new SavedRequestAwareAuthenticationSuccessHandler())
                .permitAll()
                .and()
                .httpBasic()
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .and()
                .exceptionHandling()
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setCharacterEncoding("UTF-8");
                    response.setContentType("application/json");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    PrintWriter out = response.getWriter();
                    out.println("{\"message\":\"로그인이 필요합니다.\"}");
                })

                .accessDeniedHandler(new CustomAccessDeniedHandler())
                .and()
                .csrf().disable()
                .logout()
                .logoutUrl("/api/users/logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .logoutSuccessHandler((request, response, authentication) -> {
                    try {
                        // 로그인 중인 회원이 없는 경우
                        if (authentication == null || authentication.getPrincipal() == null || "anonymousUser".equals(authentication.getPrincipal().toString())) {
                            response.setCharacterEncoding("UTF-8");
                            response.setContentType("application/json");
                            response.setStatus(HttpServletResponse.SC_OK);
                            PrintWriter out = response.getWriter();
                            out.println("{\"message\":\"로그인 중인 회원이 없습니다.\"}");
                            return;
                        }

                        request.getSession().invalidate();
                        String phoneNumber = ((UserDetails) authentication.getPrincipal()).getUsername();

                        UserEntity user = userRepository.findByPhoneNumber(phoneNumber)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found with phone number: " + phoneNumber));

                        String message = user.getName() + " 회원님 로그아웃에 성공하였습니다.";

                        response.setCharacterEncoding("UTF-8");
                        response.setContentType("application/json");
                        response.setStatus(HttpServletResponse.SC_OK);
                        PrintWriter out = response.getWriter();
                        out.println("{\"message\":\"" + message + "\"}");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    CookieUtil.clearCookie(response, "JSESSIONID");
                });
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET","POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}