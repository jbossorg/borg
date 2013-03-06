/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.controller;

import javax.enterprise.inject.Model;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.jboss.planet.model.SecurityUser;
import org.jboss.planet.security.AdminAllowed;
import org.jboss.planet.security.LoggedIn;
import org.jboss.planet.security.PermissionException;
import org.jboss.planet.service.SecurityService;

/**
 * Model for {@link SecurityUser}
 * 
 * @author Libor Krzyzanek
 * 
 */
@Model
public class UserController {

	@Inject
	private SecurityService securityService;

	@Inject
	private FacesContext facesContext;

	@Inject
	private HttpServletRequest request;

	public String getUsername() {
		if (isLoggedIn()) {
			return securityService.getCurrentUser().getName();
		} else {
			return null;
		}
	}

	/**
	 * Check if user is logged in. Implemented by annotation {@link LoggedIn}
	 */
	@LoggedIn
	public void checkIsLoggedIn() {
	}

	public boolean isLoggedIn() {
		// very important to check if session is created
		// otherwise session would be always created - also for guest users!
		if (request.getSession(false) != null) {
			SecurityUser user = securityService.getCurrentUser();
			return !securityService.isAnonymous(user);
		}
		return false;
	}

	/**
	 * Check if user is Administrator. Implemented by annotation {@link AdminAllowed}
	 */
	@AdminAllowed
	public void checkIsAdmin() {
	}

	public boolean isAdmin() {
		if (isLoggedIn()) {
			return securityService.isAdmin(securityService.getCurrentUser());
		} else {
			return false;
		}
	}

	public void checkIsGroupAdmin() {
		if (!isGroupAdmin()) {
			throw new PermissionException(securityService.getCurrentUser(),
					" Only Group Administrator is permitted for this action.");
		}
	}

	public boolean isGroupAdmin() {
		if (isLoggedIn()) {
			return securityService.isGroupAdmin(securityService.getCurrentUser());
		} else {
			return false;
		}
	}

	public String checkUserOnErrorPage() {
		if (isLoggedIn()) {
			return "pretty:home";
		} else {
			return null;
		}
	}

	public void logout() {
		securityService.logout();
		facesContext.getExternalContext().invalidateSession();
	}

}
