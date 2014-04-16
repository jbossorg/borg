/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.model;

/**
 * Enumeration for Post Status field
 * 
 * @author Libor Krzyzanek
 */
public enum PostStatus {
	/**
	 * Post created during updating form remote feed
	 */
	CREATED,

	/**
	 * Synchronized to jboss.org
	 */
	SYNCED,

	/**
	 * Force synchronized to jboss.org again
	 */
	FORCE_SYNC,

	/**
	 * Synchronized again to jboss.org
	 */
	RESYNCED,

	/**
	 * Posted to twitter
	 */
	POSTED_TWITTER

}
