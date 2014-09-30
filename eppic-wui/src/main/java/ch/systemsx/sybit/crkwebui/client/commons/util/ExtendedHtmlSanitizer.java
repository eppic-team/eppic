package ch.systemsx.sybit.crkwebui.client.commons.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.google.gwt.safehtml.shared.HtmlSanitizer;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/*
 * Based on SimpleHtmlSanitizer:
 * 
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
  * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
public class ExtendedHtmlSanitizer implements HtmlSanitizer
{
	private final Set<String> ALLOWED_TAGS = new HashSet<String>(
		       Arrays.asList("b", "em", "i", "h1", "h2", "h3", "h4", "h5", "h6", "hr",
		           "ul", "ol", "li", "br", "sup"));

	/**
	 * Creates empty instance of html sanitizer.
	 */
	public ExtendedHtmlSanitizer()
	{
		
	}
	
	/**
	 * Creates instance of html sanitizer with set of additional tags not to escape.
	 * @param additionalTags additional white listed tags
	 */
	public ExtendedHtmlSanitizer(Set<String> additionalTags)
	{
		if(additionalTags != null)
		{
			for(String tag : additionalTags)
			{
				ALLOWED_TAGS.add(tag);
			}
		}
	}
	
	/**
	 * Escapes html string.
	 * @param html string to escape
	 */
	public SafeHtml sanitize(String html) 
	{
		if (html == null) 
		{
			throw new NullPointerException("html is null");
		}
		
		return SafeHtmlUtils.fromTrustedString(simpleSanitize(html));
	}
	 
	/**
	 * Sanitazes string by escaping non-white listed tags.
	 * @param text string to sanitize
	 * @return sanitized string
	 */
	private String simpleSanitize(String text) 
	{
		StringBuilder sanitized = new StringBuilder();
	 
		boolean firstSegment = true;
		
	    for (String segment : text.split("<", -1)) 
	    {
	    	if (firstSegment) 
	    	{
	    		firstSegment = false;
	    		sanitized.append(SafeHtmlUtils.htmlEscapeAllowEntities(segment));
	    	}
	    	else
	    	{
		    	int tagStart = 0; 
		    	int tagEnd = segment.indexOf('>');

		    	String tag = null;
		    	boolean isValidTag = false;
		    	boolean isEnclosedTag = false;
		    	
		    	if (tagEnd > 0) 
		    	{
		    		if (segment.charAt(0) == '/') 
		    		{
		    			tagStart = 1;
		    		} 
		    		else if (segment.charAt(tagEnd - 1) == '/')
		    		{
		    			isEnclosedTag = true;
		    		}

		    		if(isEnclosedTag)
		    		{
		    			tag = segment.substring(tagStart, tagEnd - 1);
		    		}
		    		else
		    		{
		    			tag = segment.substring(tagStart, tagEnd);
		    		}
		    		
		    		if (ALLOWED_TAGS.contains(tag)) 
		    		{
		    			isValidTag = true;
		    		}
		    	}
	
		    	if (isValidTag) 
		    	{
		    		if (tagStart == 0) 
		    		{
		    			sanitized.append('<');
		    		} 
		    		else 
		    		{
		    			sanitized.append("</");
		    		}
		    		
		    		sanitized.append(tag);
		    		
		    		if(isEnclosedTag)
		    		{
		    			sanitized.append('/');
		    		}
		    		
		    		sanitized.append('>');
	
		    		sanitized.append(SafeHtmlUtils.htmlEscapeAllowEntities(segment.substring(tagEnd + 1)));
		    	} 
		    	else 
		    	{
		    		sanitized.append("&lt;").append(SafeHtmlUtils.htmlEscapeAllowEntities(segment));
		    	}
	    	}
	    }
	    
	    return sanitized.toString();
	}
}
