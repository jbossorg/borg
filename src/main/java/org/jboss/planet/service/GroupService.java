/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.jboss.planet.model.FeedGroup;
import org.jboss.planet.model.FeedsSecurityRole;
import org.jboss.planet.model.RemoteFeed;
import org.jboss.planet.model.SecurityMapping;
import org.jboss.planet.model.SecurityUser;
import org.jboss.planet.security.AdminAllowed;
import org.jboss.planet.security.CRUDAllowed;
import org.jboss.planet.security.CRUDOperationType;

/**
 * Service responsibly for groups
 * 
 * @author Libor Krzyzanek
 */
@Named
@Stateless
public class GroupService extends EntityServiceJpa<FeedGroup> {

	@Inject
	private FeedsService feedsService;

	@Inject
	private Logger log;

	public GroupService() {
		super(FeedGroup.class);
	}

	/**
	 * Helper method to check permissions of specified entity via annotation
	 * 
	 * @param entity
	 */
	@CRUDAllowed(operation = CRUDOperationType.UPDATE)
	public void checkEditPermissions(Object entity) {
	}

	public boolean exists(String name) {
		return getEntityManager().createQuery("select group from FeedGroup group WHERE group.name = ?1")
				.setParameter(1, name).getResultList().size() == 1;
	}

	public FeedGroup getGroup(String groupName) {
		return (FeedGroup) getEntityManager().createQuery("select group from FeedGroup group WHERE group.name = ?1")
				.setParameter(1, groupName).getSingleResult();
	}

	public List<FeedGroup> getAllGroupsOrderedByName() {
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<FeedGroup> criteria = cb.createQuery(FeedGroup.class);
		Root<FeedGroup> group = criteria.from(FeedGroup.class);
		criteria.select(group).orderBy(cb.asc(group.get("name")));
		return getEntityManager().createQuery(criteria).getResultList();
	}

	public List<FeedGroup> getUserGroups(SecurityUser user) {
		ArrayList<FeedGroup> result = new ArrayList<FeedGroup>();
		for (SecurityMapping mapping : user.getMappings()) {
			if (FeedsSecurityRole.ADMIN.equals(mapping.getRole())) {
				return getAllGroupsOrderedByName();
			}
			if (FeedsSecurityRole.GROUP_ADMIN.equals(mapping.getRole())) {
				Integer groupId = mapping.getIdForRole();
				result.add(find(groupId));
			}
		}
		return result;
	}

	@AdminAllowed
	public void deleteGroup(String groupName) {
		log.log(Level.INFO, "Delete Group {0}", groupName);
		FeedGroup group = getGroup(groupName);
		List<RemoteFeed> feeds = feedsService.getFeeds(group);
		// Iterate over all feeds and take advantage of implementation in feedsService including events/cache handling
		for (RemoteFeed remoteFeed : feeds) {
			feedsService.delete(remoteFeed.getId());
		}
		delete(group.getId());
	}
}
