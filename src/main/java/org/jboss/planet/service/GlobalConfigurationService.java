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
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Global configuration
 *
 * @author Libor Krzyzanek
 */
@Named("globalConfig")
@ApplicationScoped
public class GlobalConfigurationService {

	@Inject
	private Logger log;

	private Properties prop;

	@PostConstruct
	public void loadConfig() throws IOException {
		prop = new Properties();
		InputStream inStream = GlobalConfigurationService.class.getResourceAsStream("/app.properties");
		try {
			prop.load(inStream);
			inStream.close();
		} catch (IOException e) {
			log.log(Level.SEVERE, "Cannot load app.properties", e);
			throw e;
		}
	}

	/**
	 * Get SSO Server URL
	 *
	 * @return
	 */
	public String getSSOServerUrl() {
		return prop.getProperty("cas.ssoServerUrl");
	}

	/**
	 * Get Application URL
	 *
	 * @return
	 */
	public String getAppUrl() {
		return prop.getProperty("app.url");
	}
}
