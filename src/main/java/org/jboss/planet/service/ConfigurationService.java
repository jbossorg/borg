/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.service;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.jboss.planet.model.Configuration;
import org.jboss.planet.security.AdminAllowed;

/**
 * Application Configuration service
 * 
 * @author Libor Krzyzanek
 */
@Named
@Stateless
public class ConfigurationService extends EntityServiceJpa<Configuration> {

	@Inject
	private HttpServletRequest request;

	public ConfigurationService() {
		super(Configuration.class);
	}

	private Configuration initNewConfiguration() {
		Configuration conf = new Configuration();
		conf.setId(1);
		conf.setConnectionTimeout(10000);
		conf.setReadTimeout(6000);
		conf.setUpdateInterval(900);
		conf.setSyncServer("https://dcp2.jboss.org/v2");
		conf.setSyncContentType("jbossorg_blog");

		return conf;
	}

	public Configuration getConfiguration() {
		Configuration c = find(1);
		if (c == null) {
			c = initNewConfiguration();
			c = create(c, false);
		}
		return c;
	}

	public String getSyncServerOnView() {
		Configuration config = getConfiguration();
		if (config.getSyncServerHttpInViewLayer() != null && config.getSyncServerHttpInViewLayer()) {
			// Allows http only if request is not https
			if (config.getSyncServer().startsWith("https") && !request.isSecure()) {
				return config.getSyncServer().replace("https", "http");
			}
		}
		return config.getSyncServer();
	}

	@AdminAllowed
	public Configuration update(Configuration configuration) {
		return super.update(configuration);
	}
}
