/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.filter;

import org.jboss.planet.controller.UserController;

import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Application filter for securing content. It does:<br/>
 * <ol>
 * <li>Filter forbid all .jsf URLs except resources. All pages are served via pretty faces and security is handled by
 * annotations in action methods.</li>
 * <li>Forces using https scheme if user is logged in or trying to access /login page - this is disabled when forceSSL
 * parameter is "false" otherwise is always true.</li>
 * </ol>
 * 
 * @author Libor Krzyzanek
 */
public class SecurityFilter implements Filter {

	@Inject
	private UserController userController;

	@Inject
	private Logger log;

	private boolean forceSSL = true;

	@Override
	public void init(FilterConfig config) throws ServletException {
		String ssl = config.getInitParameter("forceSSL");
		if ("false".equalsIgnoreCase(ssl)) {
			forceSSL = false;
		}
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		final String path = httpRequest.getRequestURI();

		// Secure https for logged in users
		if (forceSSL && userController.isLoggedIn()) {
			if (!request.isSecure()) {
				String httpsUrl = "https://" + httpRequest.getServerName() + path;
				log.log(Level.INFO, "Redirecting user to https URL: {0}", httpsUrl);

				((HttpServletResponse) response).sendRedirect(httpsUrl);
				return;
			}
		}

		final String jsfResourcesPrefix = httpRequest.getContextPath() + "/javax.faces.resource";

		if (path.endsWith(".jsf") && !path.startsWith(jsfResourcesPrefix)) {
			HttpServletResponse httpServletResponse = (HttpServletResponse) response;
			httpServletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		chain.doFilter(request, response);
		return;
	}

	@Override
	public void destroy() {
	}

}
