/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.data;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.planet.model.FeedGroup;
import org.jboss.planet.service.GroupService;

/**
 * Producer for {@link FeedGroup} entity
 * 
 * @author Libor Krzyzanek
 * 
 */
@RequestScoped
public class GroupListProducer {

	@Inject
	private GroupService groupService;

	private List<FeedGroup> groups;

	/**
	 * List of all groups ordered by name
	 * 
	 * @return
	 */
	@Produces
	@Named
	public List<FeedGroup> getGroups() {
		return groups;
	}

	@PostConstruct
	public void retrieveAllGroupsOrderedByName() {
		groups = groupService.getAllGroupsOrderedByName();
	}
}
