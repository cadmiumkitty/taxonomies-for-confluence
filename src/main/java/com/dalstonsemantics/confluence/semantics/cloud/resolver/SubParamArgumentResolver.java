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
 * Want to be able to resolve sub claim from JWT set (identifier of the user who triggered an action) to use in processing the requests.
 */
public class SubParamArgumentResolver implements HandlerMethodArgumentResolver {

  @Override
  public boolean supportsParameter(MethodParameter methodParameter) {
    return methodParameter.getParameterAnnotation(SubParam.class) != null;
  }

  @Override
  public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer,
      NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {

    Principal principal = nativeWebRequest.getUserPrincipal();
    JWTClaimsSet jwtClaimSet = (JWTClaimsSet) ((Authentication) principal).getCredentials();

    Object sub = jwtClaimSet.getClaim("sub");

    return sub.toString();
  }
}