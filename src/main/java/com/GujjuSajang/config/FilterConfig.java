package com.GujjuSajang.config;

import com.GujjuSajang.Jwt.util.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class FilterConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public FilterRegistrationBean<JwtFilter> jwtFilterRegistration() {
        FilterRegistrationBean<JwtFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(jwtFilter);
        registrationBean.addUrlPatterns("/member/logout");
        registrationBean.addUrlPatterns("/member/detail/*");
        registrationBean.addUrlPatterns("/product", "/products","/product/*");
        registrationBean.addUrlPatterns("/cart", "/cart/*");
        return registrationBean;
    }

}
