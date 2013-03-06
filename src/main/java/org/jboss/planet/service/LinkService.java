/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.service;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.planet.model.Post;
import org.jboss.planet.model.RemoteFeed;
import org.jboss.planet.model.XmlType;

/**
 * @author Libor Krzyzanek
 */
@Named
@ApplicationScoped
public class LinkService {

	@Inject
	private ConfigurationService configurationService;

	private String serverAddress = null;
	private String contextName = null;

	@PostConstruct
	public void init() {
		serverAddress = configurationService.getConfiguration().getServerAddress();
		if (serverAddress == null) {
			serverAddress = "http://planet.jboss.org";
		}
		contextName = configurationService.getConfiguration().getContextName();
		if (contextName == null) {
			contextName = "";
		}
	}

	public String getServerAddress() {
		return serverAddress;
	}

	public String getContextName() {
		return contextName;
	}

	public String generateFeedLink(RemoteFeed feed, XmlType type) {
		return getServerAddress() + getContextName() + "/xml/" + feed.getName() + "?type="
				+ type.toString().toLowerCase();
	}

	public String generateFeedPageLink(RemoteFeed feed) {
		return getServerAddress() + getContextName() + "/view/" + feed.getName();
	}

	public String generatePostLink(Post post) {
		return getServerAddress() + getContextName() + "/post/" + post.getTitleAsId();
	}

}
