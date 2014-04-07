/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.service;

import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Named;
import javax.persistence.NoResultException;
import javax.persistence.TemporalType;

import org.jboss.planet.model.Post;
import org.jboss.planet.model.PostStatus;
import org.jboss.planet.model.RemoteFeed;

/**
 * Service related to {@link Post} entity
 *
 * @author Libor Krzyzanek
 */
@Named
@Stateless
public class PostService extends EntityServiceJpa<Post> {

	public PostService() {
		super(Post.class);
	}

	/**
	 * Find post based on its titleAsId
	 *
	 * @param titleAsId
	 * @return post or null
	 */
	public Post find(String titleAsId) {
		try {
			return (Post) getEntityManager().createQuery("select post from Post post WHERE post.titleAsId = ?1")
					.setParameter(1, titleAsId).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	/**
	 * Find posts based on its status
	 *
	 * @param status
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Integer> find(PostStatus status) {
		return getEntityManager().createQuery("select post.id from Post post WHERE post.status = ?1")
				.setParameter(1, status).getResultList();
	}

	/**
	 * Find posts based on input parameters
	 *
	 * @param feed
	 * @param title
	 * @param published only date is taken. time is ignored.
	 * @return list of posts
	 */
	@SuppressWarnings("unchecked")
	public List<Post> find(RemoteFeed feed, String title, Date published) {
		return getEntityManager()
				.createQuery("select p from Post p WHERE p.feed = ?1 and date(p.published) = ?2 and p.title = ?3")
				.setParameter(1, feed).setParameter(2, published, TemporalType.DATE).setParameter(3, title)
				.getResultList();
	}

	/**
	 * Update status of all posts belonging to specified feed.
	 *
	 * @param status
	 * @param feed
	 * @return number of affected posts
	 */
	public int updateStatus(PostStatus status, RemoteFeed feed) {
		return getEntityManager()
				.createQuery("update Post set status = :status where feed = :feed")
				.setParameter("status", status)
				.setParameter("feed", feed)
				.executeUpdate();
	}

}
