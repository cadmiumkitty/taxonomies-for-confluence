package com.dalstonsemantics.confluence.semantics.cloud.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.NoSuchElementException;

public class ConfluenceResourceUrlParser {
    
    /**
     * Attempts to parse Confluence URL.
     * 
     * Example blog URL: https://emorozov.atlassian.net/wiki/spaces/TFC/blog/2022/12/27/13991937/Sample+Blog
     * Example page URL: https://emorozov.atlassian.net/wiki/spaces/TFC/pages/23101443/Data+Steward
     * 
     * @param url URL to be parsed
     * @return ConfluenceResource parsed from the URL
     */
    public static final ConfluenceResource parseUrl(String s) {

        try {

            URL url = new URL(s);
            String path = url.getPath();
            String anchor = url.getRef();
            
            String[] elements = path.split("/");
            Deque<String> stack = new ArrayDeque<String>(elements.length);
            for (int i = 0; i < elements.length; i++) {
                stack.addLast(elements[i]);
            }
            
            stack.removeFirst();   // Skip the leading slashf in the path (empty element)
            
            // TODO: Initial version will only support resources on pages and blogs. We will need to add support for spaces.
            if ("wiki".equals(stack.removeFirst()) && "spaces".equals(stack.removeFirst())) {
                String spaceKey = stack.removeFirst();
                if ("blog".equals(stack.peekFirst())) {
                    stack.removeFirst();   // blog
                    stack.removeFirst();   // YYYY
                    stack.removeFirst();   // MM
                    stack.removeFirst();   // DD
                    if (stack.peekFirst() != null) {
                        return ConfluenceResource.builder().valid(true).spaceKey(spaceKey).contentType("blogpost").contentId(stack.removeFirst()).anchor(anchor).build();
                    }
                } else if ("pages".equals(stack.peekFirst())) {
                    stack.removeFirst();   // pages
                    if (stack.peekFirst() != null) {
                        return ConfluenceResource.builder().valid(true).spaceKey(spaceKey).contentType("page").contentId(stack.removeFirst()).anchor(anchor).build();
                    }
                }
            }

            return ConfluenceResource.builder().valid(false).build();
        } catch (NoSuchElementException nsee) {

            return ConfluenceResource.builder().valid(false).build();
        } catch (MalformedURLException mue) {

            return ConfluenceResource.builder().valid(false).build();
        }
    }
}
