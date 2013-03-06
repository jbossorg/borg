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

import org.jboss.planet.model.RemoteFeed;
import org.jboss.planet.service.FeedsService;

/**
 * Converter from {@link RemoteFeed} based on its name
 * 
 * @author Libor Krzyzanek
 * @see RemoteFeed#getName()
 */
@Named
public class FeedConverter implements Converter {

	@Inject
	private FeedsService feedsService;

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		return feedsService.getFeed(value);
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		return ((RemoteFeed) value).getName();
	}

}
