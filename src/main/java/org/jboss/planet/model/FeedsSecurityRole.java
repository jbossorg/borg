/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.model;

/**
 * Security Roles in application
 * 
 * @author Libor Krzyzanek
 */
public enum FeedsSecurityRole {
	/**
	 * Global admin
	 */
	ADMIN,
	/**
	 * Group admin.
	 * 
	 * @see FeedGroup#getId()
	 */
	GROUP_ADMIN,
	/**
	 * Remote Feed admin
	 * 
	 * @see RemoteFeed#getId()
	 */
	FEED_ADMIN,

	/**
	 * Can view feed
	 */
	VIEW
}
