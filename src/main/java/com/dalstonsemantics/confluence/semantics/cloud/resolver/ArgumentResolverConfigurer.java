package com.dalstonsemantics.confluence.semantics.cloud.resolver;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ArgumentResolverConfigurer implements WebMvcConfigurer {

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
    argumentResolvers.add(new ContextParamArgumentResolver());
    argumentResolvers.add(new IssParamArgumentResolver());
    argumentResolvers.add(new SubParamArgumentResolver());
  }
}