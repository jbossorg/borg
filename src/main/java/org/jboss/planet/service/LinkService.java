/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.service;

import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * Helper Service for generating links
 *
 * @author Libor Krzyzanek
 */
@Named
@ApplicationScoped
@Singleton
public class LinkService {

	@Inject
	private GlobalConfigurationService globalConfigurationService;

	/**
	 * Get Permanent Link for Post
	 * @param titleAsId
	 * @return
	 */
	public String generatePostLink(String titleAsId) {
		return globalConfigurationService.getAppUrl() + "/post/" + titleAsId;
	}

}
