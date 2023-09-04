package com.dalstonsemantics.confluence.semantics.cloud.resolver;

import java.security.Principal;

import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.dalstonsemantics.confluence.semantics.cloud.domain.context.Context;
import com.google.gson.Gson;
import com.nimbusds.jwt.JWTClaimsSet;

/**
 * Want to be able to resolve context claim from JWT set (information about Confluence content associated with the event) to use in processing the requests.
 */
public class ContextParamArgumentResolver implements HandlerMethodArgumentResolver {

  @Override
  public boolean supportsParameter(MethodParameter methodParameter) {
    return methodParameter.getParameterAnnotation(ContextParam.class) != null;
  }

  @Override
  public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer,
      NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {

    Principal principal = nativeWebRequest.getUserPrincipal();
    JWTClaimsSet jwtClaimSet = (JWTClaimsSet) ((Authentication) principal).getCredentials();

    Object contextJson = jwtClaimSet.getClaim("context");

    Gson g = new Gson();
    Context context = g.fromJson(contextJson.toString(), Context.class);

    return context;
  }
}
