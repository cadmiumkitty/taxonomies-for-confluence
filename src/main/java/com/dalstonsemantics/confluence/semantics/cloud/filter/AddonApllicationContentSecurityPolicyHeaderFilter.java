package com.dalstonsemantics.confluence.semantics.cloud.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;

import lombok.extern.slf4j.Slf4j;

/**
 * It is Atlassian requirement to supply CSP headers for pages surfaced to the end user of Confluence https://developer.atlassian.com/platform/marketplace/security-requirements/
 * The easiest way to achieve it and avoid Spring/Atlassian Connect compatibility issues is to just create a filter for specific set of patterns; we only have a small number of pages.
 */
@Slf4j
@WebFilter(urlPatterns = {"/subject-byline", "/type-byline", "/class-byline", "/taxonomy-admin-page", "/taxonomy-page", "/relation-editor", "/toc-editor", "/sparql-macro-editor", "/resource-editor"})
public class AddonApllicationContentSecurityPolicyHeaderFilter implements Filter {

    private String contentSecurityPolicyHeaderValue;

    public AddonApllicationContentSecurityPolicyHeaderFilter(@Value("${addon.headers.content-security-policy}") String contentSecurityPolicyHeaderValue) {
        this. contentSecurityPolicyHeaderValue = contentSecurityPolicyHeaderValue;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        httpServletResponse.setHeader("Content-Security-Policy", contentSecurityPolicyHeaderValue);
        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("Initialized Content-Security-Policy filter.");
    }

    @Override
    public void destroy() {
        log.info("Destroyed Content-Security-Policy filter.");
    }
}