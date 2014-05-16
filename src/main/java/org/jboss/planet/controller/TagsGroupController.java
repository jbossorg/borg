package org.jboss.planet.controller;

import org.jboss.planet.model.TagsGroup;

import javax.enterprise.inject.Model;
import javax.inject.Inject;
import java.util.ArrayList;
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

	protected TagsGroup tagsGroup;

	public void loadTagsGroup() {
		//TODO: Load TagsGroup from Service
		log.log(Level.FINE, "Load tags group: {0}", tagsGroupName);

		tagsGroup = new TagsGroup();
		tagsGroup.setName(tagsGroupName);
		tagsGroup.setTitle("Accelerate");
		tagsGroup.setTags("arquillian, openshift");
	}

	public List<TagsGroup> getAllTagsGroupInMenu() {
		// TODO: Get all tags for menu ordered by menuOrder
		ArrayList<TagsGroup> tags = new ArrayList<>();

		tagsGroup = new TagsGroup();
		tagsGroup.setName("accelerate");
		tagsGroup.setShowInMenu(true);
		tagsGroup.setTitle("Accelerate");
		tagsGroup.setTags("arquillian, openshift");

		tags.add(tagsGroup);

		return tags;
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
