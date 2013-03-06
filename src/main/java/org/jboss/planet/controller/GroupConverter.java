/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.controller;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.planet.model.FeedGroup;
import org.jboss.planet.service.GroupService;

/**
 * Converter from {@link FeedGroup} based on its id
 * 
 * @author Libor Krzyzanek
 * @see FeedGroup#getId()
 */
@Named
public class GroupConverter implements Converter {

	@Inject
	private GroupService groupService;

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		return groupService.find(Integer.parseInt(value));
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		return ((FeedGroup) value).getId().toString();
	}

}
