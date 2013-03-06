/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.controller;

import javax.enterprise.inject.Model;

import org.jboss.planet.model.FeedsSecurityRole;
import org.jboss.planet.security.AdminAllowed;

/**
 * Controller for system administrators
 * 
 * @author Libor Krzyzanek
 */
@Model
@AdminAllowed
public class SystemAdminController extends AdminController {

	@Override
	public String addAdmin() {
		addAdmin(FeedsSecurityRole.ADMIN, null);
		return "pretty:manage-system-admins";
	}

	@Override
	public String removeAdmin() {
		removeAdmin(FeedsSecurityRole.ADMIN, null);
		return "pretty:manage-system-admins";
	}
}
