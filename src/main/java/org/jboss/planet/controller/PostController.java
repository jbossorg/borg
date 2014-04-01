/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.controller;

import org.jboss.planet.model.Post;
import org.jboss.planet.service.PostService;

import javax.enterprise.inject.Model;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

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

	@Inject
	private FacesContext facesContext;

	private Post post;

	private String titleAsId;

	public void loadPost() throws IOException {
		log.log(Level.FINE, "Load post with id: {0}", titleAsId);

		post = postService.find(titleAsId);
		if (post == null) {
			facesContext.getExternalContext().responseSendError(HttpServletResponse.SC_NOT_FOUND, "Blog Post Not Found");
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
