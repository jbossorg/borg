/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.filter;

import org.apache.commons.lang.StringUtils;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.util.AbstractCasFilter;
import org.jasig.cas.client.validation.Assertion;
import org.jboss.planet.model.SecurityUser;
import org.jboss.planet.service.SecurityService;

import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Authentication logic for CAS server (sso.jboss.org)
 *
 * @author Libor Krzyzanek
 */
public class CasLoginFilter implements Filter {

	@Inject
	private Logger log;

	@Inject
	private SecurityService securityService;

	private boolean forceSSL = true;

	public static final String BACK_URL_PARAM_NAME = "backurl";

	@Override
	public void init(FilterConfig config) throws ServletException {
		String ssl = config.getInitParameter("forceSSL");
		if ("false".equalsIgnoreCase(ssl)) {
			forceSSL = false;
		}
	}

	protected AttributePrincipal retrievePrincipalFromSession(HttpServletRequest httpRequest) {
		final Assertion assertion = (Assertion) httpRequest.getSession(false).getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION);
		return assertion == null ? null : assertion.getPrincipal();
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;

		if (httpRequest.getSession(false) == null) {
			chain.doFilter(request, response);
			return;
		}

		AttributePrincipal assertion = retrievePrincipalFromSession(httpRequest);

		if (assertion == null) {
			chain.doFilter(request, response);
			return;
		}
		String originalURL = getOriginalUrl(httpRequest);

		SecurityUser currentUser = securityService.getCurrentUser();
		// Check if user is already logged in and usernames matches. Otherwise it's needed to set right user
		if (currentUser == null || !currentUser.getExternalId().equals(assertion.getName())) {
			log.log(Level.INFO, "Login User, remote user: {0}", assertion);
			securityService.setCurrentUser(assertion);
		}

		if (originalURL == null || originalURL.contains("/logout") || originalURL.contains("/login")) {
			// redirect to main page
			originalURL = httpRequest.getContextPath();
			if (originalURL.equals("")) {
				originalURL = "/";
			}
		}

		log.log(Level.INFO, "Redirecting to url: {0}", originalURL);
		((HttpServletResponse) response).sendRedirect(originalURL);
		return;
	}

	/**
	 * Get original URL based on URL parameter named by {@link #BACK_URL_PARAM_NAME} or referer HTTP header
	 *
	 * @param httpRequest
	 * @return original URL or null if not defined in described methods above
	 */
	protected String getOriginalUrl(HttpServletRequest httpRequest) {
		String backUrl = httpRequest.getParameter(BACK_URL_PARAM_NAME);
		log.log(Level.FINEST, "Backurl from parameter: {0}", backUrl);
		if (backUrl == null) {
			backUrl = httpRequest.getHeader("referer");
			log.log(Level.FINEST, "Backurl from referer: {0}", backUrl);
		}

		if (forceSSL) {
			return StringUtils.replace(backUrl, "http", "https", 1);
		}

		return backUrl;
	}

	@Override
	public void destroy() {
	}

	public boolean isForceSSL() {
		return forceSSL;
	}

}
