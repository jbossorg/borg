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

import org.apache.commons.lang.time.DateUtils;
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
	 * Find posts based on its status and published no older than threshold in hours.
	 *
	 * @param status
	 * @param publishDateThresholdInHours threshold how old posts (in hours)
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Integer> find(PostStatus status, int publishDateThresholdInHours) {
		Date date = DateUtils.addHours(new Date(), -publishDateThresholdInHours);

		return getEntityManager().createQuery("select post.id from Post post WHERE post.status = ?1 and post.published >= ?2"
				+ " order by post.published asc")
				.setParameter(1, status).setParameter(2, date).getResultList();
	}

	/**
	 * Find posts based on input parameters
	 *
	 * @param feed
	 * @param title
	 * @param published posts within 1 day range
	 * @return list of posts
	 */
	@SuppressWarnings("unchecked")
	public List<Post> find(RemoteFeed feed, String title, Date published) {
		return getEntityManager()
				.createQuery("select p from Post p WHERE p.feed = :feed and ADDDATE(p.published, -1) <= :published and ADDDATE(p.published, 1) >= :published and p.title = :title")
				.setParameter("feed", feed)
				.setParameter("published", published, TemporalType.DATE)
				.setParameter("title", title)
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
