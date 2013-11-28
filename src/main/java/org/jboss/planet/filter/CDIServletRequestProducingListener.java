/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.filter;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpServletRequest;

/**
 * Producer of HTTP request. When in place we can inject HTTP request via CDI.
 * Note: direct injecting of HTTP request should work from CDI 1.1 so once we upgrade we can consider getting rid
 * of this class.
 *
 * @author Libor Krzyzanek
 */

@WebListener
public class CDIServletRequestProducingListener implements ServletRequestListener {

	private static ThreadLocal<HttpServletRequest> SERVLET_REQUESTS = new ThreadLocal<>();

	@Override
	public void requestInitialized(ServletRequestEvent sre) {
		ServletRequest servletRequest = sre.getServletRequest();

		if (servletRequest instanceof HttpServletRequest) {
			SERVLET_REQUESTS.set((HttpServletRequest) servletRequest);
		}
	}

	@Override
	public void requestDestroyed(ServletRequestEvent sre) {
		SERVLET_REQUESTS.remove();
	}

	@Produces
	@RequestScoped
	@Named("httpServletRequest")
	private HttpServletRequest produceRequest() {
		return SERVLET_REQUESTS.get();
	}

}