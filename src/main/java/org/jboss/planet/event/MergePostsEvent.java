/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.event;

/**
 * Event fired when update service is finished
 *
 * @author Libor Krzyzanek
 */
public class MergePostsEvent {

	/**
	 * Count of new posts
	 */
	private int newPosts;

	/**
	 * Count of merged posts
	 */
	private int mergedPosts;

	/**
	 * Count of total posts processed
	 */
	private int totalPosts;

	/**
	 * Count of ignored posts
	 */
	private int ignoredPosts;

	/**
	 * Count of posts with duplicate title
	 */
	private int duplicateTitles;

	public MergePostsEvent(int newPosts, int mergedPosts, int totalPosts, int ignoredPosts, int duplicateTitles) {
		this.newPosts = newPosts;
		this.mergedPosts = mergedPosts;
		this.totalPosts = totalPosts;
		this.ignoredPosts = ignoredPosts;
		this.duplicateTitles = duplicateTitles;
	}

	public int getMergedPosts() {
		return mergedPosts;
	}

	public int getNewPosts() {
		return newPosts;
	}

	public int getTotalPosts() {
		return totalPosts;
	}

	public int getIgnoredPosts() {
		return ignoredPosts;
	}

	public int getDuplicateTitles() {
		return duplicateTitles;
	}
}