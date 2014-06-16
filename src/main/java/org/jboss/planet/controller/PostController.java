/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.controller;

import javax.enterprise.inject.Model;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.planet.model.Post;
import org.jboss.planet.model.PostStatus;
import org.jboss.planet.security.CRUDAllowed;
import org.jboss.planet.security.CRUDOperationType;
import org.jboss.planet.service.JBossSyncService;
import org.jboss.planet.service.PostService;
import org.jboss.planet.util.ApplicationMessages;

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
	private JBossSyncService jbossSyncService;

	@Inject
	private FacesContext facesContext;

	@Inject
	private ApplicationMessages messages;

	private Post post;

	private String titleAsId;

	public void loadPost() throws IOException {
		log.log(Level.FINE, "Load post with id: {0}", titleAsId);

		post = postService.find(titleAsId);
		if (post == null) {
			facesContext.getExternalContext().responseSendError(HttpServletResponse.SC_NOT_FOUND, "Blog Post Not Found");
			facesContext.responseComplete();
			return;
		} else {
			if (post.isOnModeration()) {
				postService.checkEditPermissions(post);
			}

			DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
			post.setPublishedDate(df.format(post.getPublished()));
		}
	}

	public String deletePost() throws IOException {
		log.log(Level.INFO, "Delete Post {0}", titleAsId);

		Post p = postService.find(titleAsId);
		if (p == null) {
			facesContext.getExternalContext().responseSendError(HttpServletResponse.SC_NOT_FOUND, "Blog Post Not Found");
			facesContext.responseComplete();
			return "pretty:home";
		}

		deletePost(p);

		facesContext.addMessage(
				null,
				new FacesMessage(FacesMessage.SEVERITY_INFO, messages.getString("post.deleted",
						titleAsId), null));
		return "pretty:home";
	}

	@CRUDAllowed(operation = CRUDOperationType.DELETE)
	public void deletePost(Post p) throws IOException {
		jbossSyncService.deletePost(titleAsId);
		postService.delete(p.getId());
	}

	public String activatePost() throws IOException {
		log.log(Level.INFO, "Set status of post to created. Post {0}", titleAsId);

		Post p = postService.find(titleAsId);
		if (p == null) {
			facesContext.getExternalContext().responseSendError(HttpServletResponse.SC_NOT_FOUND, "Blog Post Not Found");
			facesContext.responseComplete();
		}

		updatePostStatus(p, PostStatus.CREATED);
		facesContext.addMessage(
				null,
				new FacesMessage(FacesMessage.SEVERITY_INFO, messages.getString("post.activated",
						titleAsId), null));
		return "pretty:post";
	}

	public void updatePostStatus(Post p, PostStatus status) throws IOException {
		p.setStatus(status);
		post = postService.update(p);
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
