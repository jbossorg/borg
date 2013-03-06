/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

/**
 * Utils related to Request scope
 * 
 * @author Libor Krzyzanek
 * 
 */
@Named
@RequestScoped
public class RequestUtils {

	@Inject
	private HttpServletRequest request;

	@Inject
	private FacesContext facesContext;

	private String scheme;

	@PostConstruct
	public void init() {
		String originalScheme = request.getHeader("x-forwarded-proto");
		if (originalScheme != null) {
			scheme = originalScheme;
		} else {

			scheme = request.getScheme();
		}
	}

	/**
	 * Get scheme. It uses http header "x-forwarded-proto" if it's set. Otherwise standard
	 * {@link HttpServletRequest#getScheme()} is used
	 * 
	 * @return value of http header "x-forwarded-proto" or {@link HttpServletRequest#getScheme()}
	 */
	public String getScheme() {
		return scheme;
	}

	public List<FacesMessage> getGlobalMessages() {
		Iterator<FacesMessage> messages = facesContext.getMessages(null);
		ArrayList<FacesMessage> list = new ArrayList<FacesMessage>();
		while (messages.hasNext()) {
			list.add(messages.next());
		}
		return list;
	}

}
