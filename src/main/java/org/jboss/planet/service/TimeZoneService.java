/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.service;

import java.util.TimeZone;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

/**
 * {@link TimeZone} related service. On initialization it sets timezone to UTC!
 * 
 * @author Libor Krzyzanek
 * 
 * @see TimeZone
 */
@Startup
@Singleton
public class TimeZoneService {

	@Inject
	private Logger log;

	@PostConstruct
	public void init() {
		// Disabled it should be EST
		// log.info("Setting up timezone to UTC");
		// TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}

}
