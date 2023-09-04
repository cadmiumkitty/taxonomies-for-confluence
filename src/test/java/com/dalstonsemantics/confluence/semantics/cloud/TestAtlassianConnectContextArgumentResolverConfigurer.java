package com.dalstonsemantics.confluence.semantics.cloud;

import java.util.List;

import com.dalstonsemantics.confluence.semantics.cloud.domain.context.Confluence;
import com.dalstonsemantics.confluence.semantics.cloud.domain.context.Content;
import com.dalstonsemantics.confluence.semantics.cloud.domain.context.Context;
import com.dalstonsemantics.confluence.semantics.cloud.domain.context.Space;
import com.dalstonsemantics.confluence.semantics.cloud.resolver.ContextParam;
import com.dalstonsemantics.confluence.semantics.cloud.resolver.IssParam;
import com.dalstonsemantics.confluence.semantics.cloud.resolver.SubParam;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@TestConfiguration
public class TestAtlassianConnectContextArgumentResolverConfigurer implements WebMvcConfigurer {
    
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {

        argumentResolvers.add(new HandlerMethodArgumentResolver() {

            @Override
            public boolean supportsParameter(MethodParameter methodParameter) {
                return methodParameter.getParameterAnnotation(SubParam.class) != null;
            }

            @Override
            public Object resolveArgument(MethodParameter methodParameter,
                    ModelAndViewContainer modelAndViewContainer, NativeWebRequest nativeWebRequest,
                    WebDataBinderFactory webDataBinderFactory) throws Exception {

                return "557058:d092c978-5ab7-4c8a-8d87-32ffac22c584";
            }
        });

        argumentResolvers.add(new HandlerMethodArgumentResolver() {

            @Override
            public boolean supportsParameter(MethodParameter methodParameter) {
                return methodParameter.getParameterAnnotation(IssParam.class) != null;
            }

            @Override
            public Object resolveArgument(MethodParameter methodParameter,
                    ModelAndViewContainer modelAndViewContainer, NativeWebRequest nativeWebRequest,
                    WebDataBinderFactory webDataBinderFactory) throws Exception {

                return "927294f7-0a9f-3d01-8120-b3ca3a45df38";
            }
        });

        argumentResolvers.add(new HandlerMethodArgumentResolver() {

            @Override
            public boolean supportsParameter(MethodParameter methodParameter) {
                return methodParameter.getParameterAnnotation(ContextParam.class) != null;
            }

            @Override
            public Object resolveArgument(MethodParameter methodParameter,
                    ModelAndViewContainer modelAndViewContainer, NativeWebRequest nativeWebRequest,
                    WebDataBinderFactory webDataBinderFactory) throws Exception {

                Context context = Context.builder()
                        .confluence(Confluence.builder()
                                .content(Content.builder().id("294914").type("page").version("1").build())
                                .space(Space.builder().id("262145").key("VM").build()).build())
                        .build();
                return context;
            }
        });
    }
}

