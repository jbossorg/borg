/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.filter;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.util.AbstractCasFilter;
import org.jasig.cas.client.validation.Assertion;
import org.jboss.planet.model.SecurityUser;
import org.jboss.planet.service.SecurityService;

/**
 * Authentication logic for CAS server (sso.jboss.org)
 * 
 * @author Libor Krzyzanek
 */
@WebFilter(filterName = "CAS App Login Filter")
public class CasLoginFilter implements Filter {

	@Inject
	private Logger log;

	@Inject
	private SecurityService securityService;

	@Inject
	private HttpSession session;

	public static final String BACK_URL_PARAM_NAME = "backurl";

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	protected AttributePrincipal retrievePrincipalFromSession() {
		final Assertion assertion = (Assertion) session.getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION);
		return assertion == null ? null : assertion.getPrincipal();
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		String originalURL = getOriginalUrl(httpRequest);

		AttributePrincipal assertion = retrievePrincipalFromSession();

		if (assertion == null) {
			chain.doFilter(request, response);
			return;
		}

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
	private String getOriginalUrl(HttpServletRequest req) {
		String backUrl = req.getParameter(BACK_URL_PARAM_NAME);
		if (backUrl != null) {
			return backUrl;
		} else {
			return req.getHeader("referer");
		}
	}

	@Override
	public void destroy() {
	}

}
