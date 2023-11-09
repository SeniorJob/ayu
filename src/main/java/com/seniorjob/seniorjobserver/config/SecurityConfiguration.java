package com.seniorjob.seniorjobserver.config;

import com.seniorjob.seniorjobserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;
import java.util.List;


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
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .formLogin().disable()
                .httpBasic().disable()

                // exception handling시 클래스 추가
                .exceptionHandling()

                // 세션설정을 Stateless로 설정
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                .and()
                // CORS 설정 커스터마이즈
                .cors().configurationSource(request -> {
                    CorsConfiguration cors = new CorsConfiguration();
                    cors.setAllowedOrigins(List.of("*"));
                    cors.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    cors.setAllowedHeaders(List.of("*")); // 허용할 HTTP 헤더
                    cors.setAllowCredentials(true);
                    cors.setExposedHeaders(Arrays.asList("Authorization", "Authorization-refresh"));
                    return cors;
                })

                // 로그인, 회원가입 등 토큰이 없는 상태에서 요청이 들어오는 api는 permitAll설정
                .and()
                .authorizeRequests()
                .antMatchers("/api/users/**", "/api/auth/logout", "/api/lecturesStepTwo/*/review/**",
                "/api/lectures/filter", "/api/lectures/detail/**", "/api/lectures/popular").permitAll() // 로그인과 로그아웃은 모두에게 허용

                // 권한테스트
                .antMatchers("/api/users/detail", "api/users/update", "/api/users/delete",
                        "/api/lectures/**", "/api/lectures/**", "/api/lectures/delete/**",
                        "/api/lectureapply/apply/**",
                        "/api/mypageCreateLecture/myCreateLectureDetail/**",
                        "/api/mypageCreateLecture/filter",
                        "/api/mypageApplyLecture/filter",
                        "/api/mypageApplyLecture/myAppliedLectureDetail/**",
                        "/api/mypageApplyLecture/updateLectureApplyReason",
                        "/api/mypageApplyLecture/deleteLectureApply/**", "api/lectures/recommendLecture"
                ).hasRole("USER")
                .anyRequest().authenticated()

                // JwtFilter를 addFilterBefore로 등록
                .and()
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }

    private static final String[] AUTH_WHITELIST = {
            "/v2/api-docs",
            "/v3/api-docs/**",
            "/configuration/ui",
            "/swagger-resources/**",
            "/configuration/security",
            "/swagger-ui.html",
            "/webjars/**",
            "/file/**",
            "/image/**",
            "/swagger/**",
            "/swagger-ui/**",
            "/h2/**"
    };

    // 정적인 파일 요청에 대해 무시
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers(AUTH_WHITELIST);
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