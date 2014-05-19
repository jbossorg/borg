package org.jboss.planet.controller;

import org.jboss.planet.model.TagsGroup;
import org.jboss.planet.security.AdminAllowed;
import org.jboss.planet.service.TagsGroupService;
import org.jboss.planet.util.ApplicationMessages;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Model;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for {@link org.jboss.planet.model.TagsGroup}
 *
 * @author Libor Krzyzanek
 */
@Model
public class TagsGroupController {

	@Inject
	private Logger log;

	protected String tagsGroupName;

	protected TagsGroup tagsGroup = new TagsGroup();

	protected List<TagsGroup> tagsGroupsInMenu;

	protected List<TagsGroup> allTagsGroups;

	@Inject
	protected TagsGroupService tagsGroupService;

	@Inject
	private FacesContext facesContext;

	@Inject
	private ApplicationMessages messages;

	@PostConstruct
	public void init() {
		// Used in Main menu so needs to be always available.
		tagsGroupsInMenu = tagsGroupService.findAllForMenu();
	}

	@AdminAllowed
	public void loadAllTagsGroup() {
		allTagsGroups = tagsGroupService.findAll();
	}


	@AdminAllowed
	public void newGroup() {
		tagsGroup = new TagsGroup();
	}

	@AdminAllowed
	public void loadTagsGroup() {
		log.log(Level.FINE, "Load tags group: {0}", tagsGroupName);

		tagsGroup = tagsGroupService.find(tagsGroupName);
	}

	private boolean validate() {
		boolean valid = true;

		boolean validateName = true;
		if (tagsGroup.getId() != null && tagsGroup.getId() != 0) {
			TagsGroup originalGroup = tagsGroupService.find(tagsGroup.getId());
			if (originalGroup.getName().equals(tagsGroup.getName())) {
				// when updating same name is correct
				validateName = false;
			}
		}

		if (validateName && tagsGroupService.exists(tagsGroup.getName())) {
			UIInput nameInput = (UIInput) facesContext.getViewRoot().findComponent("tagsgroup:name");
			nameInput.setValid(false);
			facesContext.addMessage(
					nameInput.getClientId(),
					new FacesMessage(FacesMessage.SEVERITY_ERROR, messages
							.getString("management.tagsgroup.text.groupAlreadyExists"), null)
			);
			valid = false;
		}
		return valid;
	}

	@AdminAllowed
	public String update() {
		if (!validate()) {
			return null;
		}

		tagsGroupService.update(tagsGroup);
		if (tagsGroup.getId() == null) {
			facesContext.addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, messages.getString("management.tagsgroup.text.added",
							tagsGroup.getTitle()), null)
			);
		} else {
			facesContext.addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, messages.getString("management.tagsgroup.text.updated",
							tagsGroup.getTitle()), null)
			);
		}
		return "pretty:manage-tagsgroup";
	}


	public List<TagsGroup> getAllTagsGroupInMenu() {
		return tagsGroupsInMenu;
	}

	public List<TagsGroup> getAllTagsGroups() {
		return allTagsGroups;

	}

	public String getTagsGroupName() {
		return tagsGroupName;
	}

	public void setTagsGroupName(String tagsGroupName) {
		this.tagsGroupName = tagsGroupName;
	}

	public TagsGroup getTagsGroup() {
		return tagsGroup;
	}

	public void setTagsGroup(TagsGroup tagsGroup) {
		this.tagsGroup = tagsGroup;
	}
}
