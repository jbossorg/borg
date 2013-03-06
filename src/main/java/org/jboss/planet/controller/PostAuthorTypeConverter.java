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

import org.jboss.planet.model.PostAuthorType;
import org.jboss.planet.util.ApplicationMessages;

/**
 * Converter for {@link PostAuthorType}
 * 
 * @author Libor Krzyzanek
 */
@Named
@RequestScoped
public class PostAuthorTypeConverter implements Converter {

	@Inject
	private ApplicationMessages messages;

	public static final String POST_AUTHOR_LABEL_PREFIX = "management.feed.postAuthorType.";

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		if (value.equals(PostAuthorType.BLOG_AUTHOR.toString())) {
			return PostAuthorType.BLOG_AUTHOR;
		} else if (value.equals(PostAuthorType.BLOG_AUTHOR_IF_MISSING.toString())) {
			return PostAuthorType.BLOG_AUTHOR_IF_MISSING;
		} else if (value.equals(PostAuthorType.POST_AUTHOR.toString())) {
			return PostAuthorType.POST_AUTHOR;
		}
		return null;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		return value.toString();
	}

	public List<SelectItem> getOptions() {
		List<SelectItem> options = new ArrayList<SelectItem>();
		options.add(new SelectItem(PostAuthorType.BLOG_AUTHOR_IF_MISSING.toString(), messages
				.getString(POST_AUTHOR_LABEL_PREFIX + PostAuthorType.BLOG_AUTHOR_IF_MISSING.toString())));
		options.add(new SelectItem(PostAuthorType.POST_AUTHOR.toString(), messages.getString(POST_AUTHOR_LABEL_PREFIX
				+ PostAuthorType.POST_AUTHOR.toString())));
		options.add(new SelectItem(PostAuthorType.BLOG_AUTHOR.toString(), messages.getString(POST_AUTHOR_LABEL_PREFIX
				+ PostAuthorType.BLOG_AUTHOR.toString())));
		return options;
	}
}
