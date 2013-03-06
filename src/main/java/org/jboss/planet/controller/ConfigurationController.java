/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.controller;

import java.util.logging.Logger;

import javax.enterprise.inject.Model;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.jboss.planet.model.Configuration;
import org.jboss.planet.security.AdminAllowed;
import org.jboss.planet.service.ConfigurationService;
import org.jboss.planet.util.ApplicationMessages;

/**
 * @author Libor Krzyzanek
 */
@Model
public class ConfigurationController {

	@Inject
	private ConfigurationService configurationService;

	@Inject
	private FacesContext facesContext;

	@Inject
	private ApplicationMessages messages;

	private Configuration config;

	@Inject
	private Logger log;

	@AdminAllowed
	public void load() {
		config = configurationService.getConfiguration();
	}

	@AdminAllowed
	public String update() {
		log.info("Update Configuration");
		if (StringUtils.isBlank(config.getSyncPassword())) {
			Configuration dbConfig = configurationService.getConfiguration();
			config.setSyncPassword(dbConfig.getSyncPassword());
		}
		configurationService.update(config);
		facesContext
				.addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_INFO, messages
								.getString("management.config.text.updated"), null));
		return "pretty:manage-index";
	}

	public Configuration getConfig() {
		return config;
	}

	public void setConfig(Configuration config) {
		this.config = config;
	}
}
