/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.util;

import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
	private FacesContext facesContext;


	public List<FacesMessage> getGlobalMessages() {
		Iterator<FacesMessage> messages = facesContext.getMessages(null);
		ArrayList<FacesMessage> list = new ArrayList<FacesMessage>();
		while (messages.hasNext()) {
			list.add(messages.next());
		}
		return list;
	}

}
