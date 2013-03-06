/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.controller;

import java.text.DateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.inject.Model;
import javax.inject.Inject;

import org.jboss.planet.model.Post;
import org.jboss.planet.service.PostService;

/**
 * Model for {@link Post}
 * 
 * @author Libor Krzyzanek
 */
@Model
public class PostController {

	@Inject
	private Logger log;

	@Inject
	private PostService postService;

	private Post post;

	private String titleAsId;

	public void loadPost() {
		log.log(Level.FINE, "Load post with id: {0}", titleAsId);

		post = postService.find(titleAsId);
		if (post == null) {
			// TODO: Return NOT_FOUND
		} else {
			DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
			post.setPublishedDate(df.format(post.getPublished()));
		}
	}

	public Post getPost() {
		return post;
	}

	public void setPost(Post post) {
		this.post = post;
	}

	public String getTitleAsId() {
		return titleAsId;
	}

	public void setTitleAsId(String titleAsId) {
		this.titleAsId = titleAsId;
	}

}
