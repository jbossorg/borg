/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.controller;

import java.util.List;

import javax.enterprise.inject.Model;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.jboss.planet.model.FeedGroup;
import org.jboss.planet.model.FeedsSecurityRole;
import org.jboss.planet.model.SecurityUser;
import org.jboss.planet.security.AdminAllowed;
import org.jboss.planet.security.LoggedIn;
import org.jboss.planet.service.GroupService;
import org.jboss.planet.service.SecurityService;
import org.jboss.planet.util.ApplicationMessages;

/**
 * Controller for {@link FeedGroup}
 * 
 * @author Libor Krzyzanek
 */
@Model
public class GroupController extends AdminController {

	@Inject
	private GroupService groupService;

	@Inject
	private SecurityService securityService;

	@Inject
	private FacesContext facesContext;

	@Inject
	private ApplicationMessages messages;

	private String groupName;

	private FeedGroup groupToUpdate;

	@LoggedIn
	public void loadGroup() {
		groupToUpdate = groupService.getGroup(groupName);
		groupService.checkEditPermissions(groupToUpdate);
	}

	@LoggedIn
	public List<FeedGroup> getUserGroups() {
		SecurityUser user = securityService.getCurrentUser();
		return groupService.getUserGroups(user);
	}

	@AdminAllowed
	public void newGroup() {
		groupToUpdate = new FeedGroup();
	}

	@SuppressWarnings("unused")
	private boolean validate() {
		boolean valid = true;

		boolean validateName = true;
		if (groupToUpdate.getId() != null) {
			FeedGroup originalGroup = groupService.find(groupToUpdate.getId());
			if (originalGroup.getName().equals(groupToUpdate.getName())) {
				// when updating same feed name is correct
				validateName = false;
			}
		}

		if (validateName && groupService.exists(groupToUpdate.getName())) {
			UIInput nameInput = (UIInput) facesContext.getViewRoot().findComponent("group:name");
			nameInput.setValid(false);
			facesContext.addMessage(
					nameInput.getClientId(),
					new FacesMessage(FacesMessage.SEVERITY_ERROR, messages
							.getString("management.group.text.groupAlreadyExists"), null));
			valid = false;
		}
		return valid;
	}

	@LoggedIn
	public String update() {
		// if (!validate()) {
		// return null;
		// }

		groupService.update(groupToUpdate);
		if (groupToUpdate.getId() == null) {
			facesContext.addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, messages.getString("management.group.text.added",
							groupToUpdate.getDisplayName()), null));
		} else {
			facesContext.addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, messages.getString("management.group.text.updated",
							groupToUpdate.getDisplayName()), null));
		}
		return "pretty:manage-group";
	}

	@LoggedIn
	@Override
	public String addAdmin() {
		addAdmin(FeedsSecurityRole.GROUP_ADMIN, groupToUpdate.getId());
		return "pretty:manage-group-admins";
	}

	@LoggedIn
	@Override
	public String removeAdmin() {
		removeAdmin(FeedsSecurityRole.GROUP_ADMIN, groupToUpdate.getId());
		return "pretty:manage-group-admins";
	}

	@AdminAllowed
	public String deleteGroup() {
		groupService.deleteGroup(groupName);
		facesContext.addMessage(
				null,
				new FacesMessage(FacesMessage.SEVERITY_INFO, messages.getString("management.group.text.deleted",
						groupName), null));
		return "pretty:manage-group";
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public FeedGroup getGroupToUpdate() {
		return groupToUpdate;
	}

	public void setGroupToUpdate(FeedGroup groupToUpdate) {
		this.groupToUpdate = groupToUpdate;
	}

}
