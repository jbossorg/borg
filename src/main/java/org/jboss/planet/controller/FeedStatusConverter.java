/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.controller;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.planet.model.RemoteFeed.FeedStatus;
import org.jboss.planet.util.ApplicationMessages;

/**
 * Converter for {@link FeedStatus}
 * 
 * @author Libor Krzyzanek
 */
@Named
@RequestScoped
public class FeedStatusConverter implements Converter {

	@Inject
	private ApplicationMessages messages;

	public static final String STATUS_LABEL_PREFIX = "management.feed.status.";

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		if (value.equals(FeedStatus.PROPOSED.toString())) {
			return FeedStatus.PROPOSED;
		} else if (value.equals(FeedStatus.ACCEPTED.toString())) {
			return FeedStatus.ACCEPTED;
		} else if (value.equals(FeedStatus.DISABLED.toString())) {
			return FeedStatus.DISABLED;
		} else if (value.equals(FeedStatus.DISABLED_CONNECTION_PROBLEM.toString())) {
			return FeedStatus.DISABLED_CONNECTION_PROBLEM;
		}
		return null;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		return value.toString();
	}

	public List<SelectItem> getOptions() {
		List<SelectItem> options = new ArrayList<SelectItem>();
		options.add(new SelectItem(FeedStatus.PROPOSED.toString(), messages.getString(STATUS_LABEL_PREFIX
				+ FeedStatus.PROPOSED.ordinal())));

		options.add(new SelectItem(FeedStatus.ACCEPTED.toString(), messages.getString(STATUS_LABEL_PREFIX
				+ FeedStatus.ACCEPTED.ordinal())));

		options.add(new SelectItem(FeedStatus.DISABLED.toString(), messages.getString(STATUS_LABEL_PREFIX
				+ FeedStatus.DISABLED.ordinal())));

		options.add(new SelectItem(FeedStatus.DISABLED_CONNECTION_PROBLEM.toString(), messages
				.getString(STATUS_LABEL_PREFIX + FeedStatus.DISABLED_CONNECTION_PROBLEM.ordinal())));

		return options;
	}
}
