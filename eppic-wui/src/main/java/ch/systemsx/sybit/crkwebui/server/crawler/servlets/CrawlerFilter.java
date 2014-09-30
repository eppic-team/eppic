package ch.systemsx.sybit.crkwebui.server.crawler.servlets;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.systemsx.sybit.crkwebui.server.commons.util.io.FileContentReader;

public class CrawlerFilter implements Filter
{
	private final static String ESCAPED_FRAGMENT = "_escaped_fragment_=";
	protected FilterConfig filterConfig;

	public void init(FilterConfig filterConfig) throws ServletException 
	{
		this.filterConfig = filterConfig;
	}
    
	public void doFilter(ServletRequest request,
						 ServletResponse response,
						 FilterChain chain) throws IOException, ServletException
	{
		HttpServletRequest httpRequest = (HttpServletRequest) request;
	    HttpServletResponse httpResponse = (HttpServletResponse) response;
	    String queryString = httpRequest.getQueryString();
	    
	    if((queryString != null) && (queryString.contains(ESCAPED_FRAGMENT)))
	    {
	    	response.setContentType("text/html;charset=UTF-8");
	    	PrintWriter output = httpResponse.getWriter();
	 
	    	String pageName = preparePageName(queryString);
	    	
	    	InputStream pageStream = filterConfig.getServletContext().
	    										  getResourceAsStream(pageName);
	    	
	    	if(pageStream != null)
	    	{
	    		String pageContent = FileContentReader.readContentOfFile(pageStream, true);
	    		
	    		String page = preparePage(pageContent);
	    		output.println(page);
	    	}
	    	
	    	output.close();
	    }
	    else
	    {
	    	chain.doFilter(request, response);
	    }
	}
	
	private String preparePageName(String queryString)
	{
		String pageName = "/" + queryString.substring(queryString.indexOf(ESCAPED_FRAGMENT) +
													  ESCAPED_FRAGMENT.length());
		pageName += ".html";
		return pageName;
	}
	
	private String preparePage(String content)
	{
		StringBuffer page = new StringBuffer();
		page.append("<html>");
		page.append("<head>");
		page.append("<title>");
		page.append("</title>");
		page.append("</head>");
		page.append("<body>");
		page.append(content);
		page.append("</body>");
		page.append("</html>");
		return page.toString();
	}

	public void destroy() 
	{
		this.filterConfig = null;
	}
}