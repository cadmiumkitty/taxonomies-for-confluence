package com.dalstonsemantics.confluence.semantics.cloud.resolver;

import java.security.Principal;

import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.nimbusds.jwt.JWTClaimsSet;

/**
 * Want to be able to resolve iss claim from JWT set (host identifier) to use in processing the requests.
 */
public class IssParamArgumentResolver implements HandlerMethodArgumentResolver {

  @Override
  public boolean supportsParameter(MethodParameter methodParameter) {
    return methodParameter.getParameterAnnotation(IssParam.class) != null;
  }

  @Override
  public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer,
      NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {

    Principal principal = nativeWebRequest.getUserPrincipal();
    JWTClaimsSet jwtClaimSet = (JWTClaimsSet) ((Authentication) principal).getCredentials();

    Object iss = jwtClaimSet.getClaim("iss");

    return iss.toString();
  }
}