/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.util;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Utility to produce easy way for application messages from resource bundle
 * 
 * @author Libor Krzyzanek
 */
@Named
@ApplicationScoped
public class ApplicationMessages {

	@Inject
	private FacesContext facesContext;

	ResourceBundle bundle;

	@PostConstruct
	public void init() {
		bundle = facesContext.getApplication().getResourceBundle(facesContext, "msg");
	}

	private ResourceBundle getBundle() {
		return bundle;
	}

	public String getString(String key) {
		try {
			return getBundle().getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}

	public String getString(String key, Object... params) {
		try {
			return MessageFormat.format(getBundle().getString(key), params);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}

}
