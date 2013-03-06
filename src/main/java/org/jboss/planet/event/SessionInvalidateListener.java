/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.event;

import javax.inject.Inject;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.jboss.planet.service.SecurityService;

/**
 * Listener listening session invalidation
 * 
 * @author Libor Krzyzanek
 */
@WebListener("sessionInvalidateListener")
public class SessionInvalidateListener implements HttpSessionListener {

	@Inject
	private SecurityService securityService;

	@Override
	public void sessionCreated(HttpSessionEvent se) {
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent se) {
		// Once session is invalidated then session coped beans are invalidated as well.
		// securityService.logout();
	}

}
