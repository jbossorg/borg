/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.rest;

import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.jboss.planet.model.Post;
import org.jboss.planet.service.FeedsService;

/**
 * JAX-RS for Posts
 * 
 * This class produces a RESTful service to read the contents of the posts table.
 */
@Path("/posts")
@RequestScoped
@Produces({ "application/json" })
public class PostResourceRESTService {

	@Inject
	private FeedsService feedsService;

	@GET
	@Path("/recent")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Post> recentPosts(@QueryParam("from") int from, @QueryParam("count") int count,
			@QueryParam("feedId") String feedId) {
		return feedsService.getRecentPosts(from, from + count, feedId);
	}

	@GET
	@Path("/{id:[0-9][0-9]*}")
	@Produces(MediaType.APPLICATION_JSON)
	public Post lookupPostById(@PathParam("id") int id) {
		return feedsService.getPost(id);
	}

}
