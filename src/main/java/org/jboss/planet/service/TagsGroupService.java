/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.service;

import org.jboss.planet.model.TagsGroup;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.logging.Logger;

/**
 * Service responsibly for TagsGroup
 *
 * @author Libor Krzyzanek
 */
@Named
@Stateless
public class TagsGroupService extends EntityServiceJpa<TagsGroup> {

	@Inject
	private Logger log;

	public TagsGroupService() {
		super(TagsGroup.class);
	}

	public TagsGroup find(String groupName) {
		return (TagsGroup) getEntityManager().createQuery("select entity from TagsGroup entity WHERE entity.name = ?1")
				.setParameter(1, groupName).getSingleResult();
	}

	/**
	 * Returns all tags groups with showInMenu = true and ordered by menuOrder
	 *
	 * @return
	 */
	public List<TagsGroup> findAllForMenu() {
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<TagsGroup> criteria = cb.createQuery(TagsGroup.class);
		Root<TagsGroup> group = criteria.from(TagsGroup.class);

		criteria.select(group).where(cb.equal(group.get("showInMenu"), true));
		criteria.orderBy(cb.asc(group.get("menuOrder")));

		return getEntityManager().createQuery(criteria).getResultList();
	}

	public boolean exists(String name) {
		return getEntityManager().createQuery("select entity from TagsGroup entity WHERE entity.name = ?1")
				.setParameter(1, name).getResultList().size() == 1;
	}

}
