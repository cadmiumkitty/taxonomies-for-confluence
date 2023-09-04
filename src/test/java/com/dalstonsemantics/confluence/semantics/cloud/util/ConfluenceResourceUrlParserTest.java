package com.dalstonsemantics.confluence.semantics.cloud.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ConfluenceResourceUrlParserTest {
    
    @Test
    public void shouldParseValidBlogUrl() throws Exception {

        ConfluenceResource resource = ConfluenceResourceUrlParser.parseUrl("https://emorozov.atlassian.net/wiki/spaces/TFC/blog/2022/12/27/13991937/Sample+Blog#001");

        assertEquals(true, resource.isValid());
        assertEquals("TFC", resource.getSpaceKey());
        assertEquals("blogpost", resource.getContentType());
        assertEquals("13991937", resource.getContentId());
        assertEquals("001", resource.getAnchor());
    }

    @Test
    public void shouldParseValidPageUrl() throws Exception {

        ConfluenceResource resource = ConfluenceResourceUrlParser.parseUrl("https://emorozov.atlassian.net/wiki/spaces/TFC/pages/23101443/Data+Steward#001");

        assertEquals(true, resource.isValid());
        assertEquals("TFC", resource.getSpaceKey());
        assertEquals("page", resource.getContentType());
        assertEquals("23101443", resource.getContentId());
        assertEquals("001", resource.getAnchor());
    }

    @Test
    public void shouldParseValidPageUrlWithoutAnchor() throws Exception {

        ConfluenceResource resource = ConfluenceResourceUrlParser.parseUrl("https://emorozov.atlassian.net/wiki/spaces/TFC/pages/23101443/Data+Steward");

        assertEquals(true, resource.isValid());
        assertEquals("TFC", resource.getSpaceKey());
        assertEquals("page", resource.getContentType());
        assertEquals("23101443", resource.getContentId());
        assertEquals(null, resource.getAnchor());
    }

    @Test
    public void shouldReportIvalidUrlWithoutContentId() throws Exception {

        ConfluenceResource resource = ConfluenceResourceUrlParser.parseUrl("https://emorozov.atlassian.net/wiki/spaces/TFC/pages");

        assertEquals(false, resource.isValid());
    }

    @Test
    public void shouldReportIvalidUrlWithoutContentType() throws Exception {

        ConfluenceResource resource = ConfluenceResourceUrlParser.parseUrl("https://emorozov.atlassian.net/wiki/spaces/TFC");

        assertEquals(false, resource.isValid());
    }

    @Test
    public void shouldReportIvalidUrlWithoutSpaceKey() throws Exception {

        ConfluenceResource resource = ConfluenceResourceUrlParser.parseUrl("https://emorozov.atlassian.net/wiki/spaces");

        assertEquals(false, resource.isValid());
    }

    @Test
    public void shouldReportIvalidUrl() throws Exception {

        ConfluenceResource resource = ConfluenceResourceUrlParser.parseUrl("https://google.coom");

        assertEquals(false, resource.isValid());
    }
}