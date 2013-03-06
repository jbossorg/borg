/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.controller;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.jboss.planet.exception.DuplicateEntryException;
import org.jboss.planet.model.FeedsSecurityRole;
import org.jboss.planet.model.SecurityUser;
import org.jboss.planet.service.PermissionService;
import org.jboss.planet.service.UserService;
import org.jboss.planet.util.ApplicationMessages;

/**
 * Template for controllers who needs to control permissions
 * 
 * @author Libor Krzyzanek
 */
public abstract class AdminController {

	@Inject
	private PermissionService permissionService;

	@Inject
	private FacesContext facesContext;

	@Inject
	private UserService userService;

	@Inject
	private ApplicationMessages messages;

	/**
	 * Placeholder for adding/removing administrator of group.
	 * 
	 * @see SecurityUser#getExternalId()
	 */
	private String adminToUpdate;

	/**
	 * Add Admin
	 * 
	 * @return JSF navigation
	 */
	public abstract String addAdmin();

	/**
	 * Remove admin
	 * 
	 * @return JSF navigation
	 */
	public abstract String removeAdmin();

	public void addAdmin(FeedsSecurityRole role, Integer idForRole) {
		SecurityUser admin = userService.getUser(adminToUpdate);
		if (admin == null) {
			facesContext.addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, messages.getString(
							"manage.systemAdmins.error.userNotFound", adminToUpdate), null));
		} else {
			try {
				if (role.equals(FeedsSecurityRole.ADMIN)) {
					permissionService.addSystemAdministrator(admin);
				} else {
					permissionService.addAdministrator(role, idForRole, admin);
				}
				facesContext.addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_INFO, messages.getString(
								"manage.systemAdmins.text.added", adminToUpdate), null));
			} catch (DuplicateEntryException e) {
				facesContext.addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, messages.getString(
								"manage.systemAdmins.error.userHasPermissions", adminToUpdate), null));
			}
		}
	}

	public void removeAdmin(FeedsSecurityRole role, Integer idForRole) {
		SecurityUser admin = userService.getUser(adminToUpdate);
		if (admin == null) {
			facesContext.addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, messages.getString(
							"manage.systemAdmins.error.userNotFound", adminToUpdate), null));
		} else {
			if (role.equals(FeedsSecurityRole.ADMIN)) {
				permissionService.removeSystemAdministrator(admin);
			} else {
				permissionService.removeAdministrator(role, idForRole, admin);
			}
			facesContext.addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, messages.getString("manage.systemAdmins.text.deleted",
							adminToUpdate), null));
		}
	}

	public String getAdminToUpdate() {
		return adminToUpdate;
	}

	public void setAdminToUpdate(String adminToUpdate) {
		this.adminToUpdate = adminToUpdate;
	}
}
