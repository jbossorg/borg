/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.service;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.apache.http.client.ClientProtocolException;
import org.jboss.planet.model.FeedGroup;
import org.jboss.planet.model.FeedsSecurityRole;
import org.jboss.planet.model.Post;
import org.jboss.planet.model.RemoteFeed;
import org.jboss.planet.model.RemoteFeed.FeedStatus;
import org.jboss.planet.model.SecurityMapping;
import org.jboss.planet.model.SecurityUser;
import org.jboss.planet.security.AdminAllowed;
import org.jboss.planet.security.CRUDAllowed;
import org.jboss.planet.security.CRUDOperationType;
import org.jboss.planet.util.StringTools;

/**
 * Business logic around feeds and their posts
 * 
 * @author Libor Krzyzanek
 * 
 */
@Named
@Stateless
public class FeedsService extends EntityServiceJpa<RemoteFeed> {

	@Inject
	private EntityManager em;

	@Inject
	private Logger log;

	@Inject
	private ConfigurationService configurationService;

	@Inject
	private SecurityService securityService;

	@Inject
	private JBossSyncService jbossSyncService;

	public FeedsService() {
		super(RemoteFeed.class);
	}

	public RemoteFeed create(RemoteFeed t, SecurityUser admin) {
		t = super.create(t);

		SecurityMapping mapping = new SecurityMapping();
		mapping.setRole(FeedsSecurityRole.FEED_ADMIN);
		mapping.setIdForRole(t.getId());

		ArrayList<SecurityUser> admins = new ArrayList<SecurityUser>();
		admins.add(admin);

		mapping.setUsers(admins);

		em.persist(mapping);
		em.flush();

		return t;
	}

	public RemoteFeed getFeed(Integer id) {
		return em.find(RemoteFeed.class, id);
	}


	public RemoteFeed getFeed(String feedName) {
		return (RemoteFeed) em.createQuery("select feed from RemoteFeed feed WHERE feed.name = ?1")
				.setParameter(1, feedName).getSingleResult();
	}

	public boolean exists(String feedName) {
		return em.createQuery("select feed from RemoteFeed feed WHERE feed.name = ?1").setParameter(1, feedName)
				.getResultList().size() == 1;
	}

	/**
	 * Check if feed exists for remote url
	 * @param remoteFeedUrl
	 * @return
	 */
	public boolean existsByRemoteLink(String remoteFeedUrl) {
		return em.createQuery("select feed from RemoteFeed feed WHERE feed.remoteLink = ?1").setParameter(1, remoteFeedUrl)
				.getResultList().size() >= 1;
	}

	@SuppressWarnings("unchecked")
	public List<RemoteFeed> getFeeds(FeedGroup group) {
		return em.createQuery("select feed from RemoteFeed feed WHERE feed.group = ?1").setParameter(1, group)
				.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<RemoteFeed> getFeeds(Integer groupId) {
		return em.createQuery("select feed from RemoteFeed feed WHERE feed.group.id = ?1").setParameter(1, groupId)
				.getResultList();
	}

	/**
	 * Helper method to check permissions of specified entity via annotation
	 * 
	 * @param entity
	 */
	@CRUDAllowed(operation = CRUDOperationType.UPDATE)
	public void checkEditPermissions(Object entity) {
	}

	@AdminAllowed
	public void acceptFeed(RemoteFeed feed) {
		feed.setStatus(FeedStatus.ACCEPTED);
		update(feed);
	}

	public void deleteFeed(String feedName) throws ClientProtocolException, IOException {
		final RemoteFeed f = getFeed(feedName);

		// Check permissions before deleting anything on Sync server.
		securityService.checkPermission(f, CRUDOperationType.DELETE);

		List<Post> posts = f.getPosts();
		for (Post post : posts) {
			jbossSyncService.deletePost(post.getTitleAsId());
		}

		delete(f.getId());
	}

	/**
	 * Check if update failed reached threshold and update status if it happened
	 * 
	 * @param feed
	 * @return true if update fails didn't reached the threshold otherwise false
	 */
	public boolean checkUpdateFails(RemoteFeed feed) {
		if (feed.getUpdateFailCount() == null) {
			return true;
		}
		Integer updateFeedFailsThreshold = configurationService.getConfiguration().getUpdateFeedFailsThreshold();
		if (updateFeedFailsThreshold != null && updateFeedFailsThreshold != -1) {
			if (feed.getUpdateFailCount() >= updateFeedFailsThreshold) {
				feed.setStatus(FeedStatus.DISABLED_CONNECTION_PROBLEM);
				update(feed, false);
				return false;
			}
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	public List<Post> getRecentPosts(int from, int to, String feedName) {
		if (log.isLoggable(Level.FINE)) {
			log.fine("Get posts, form " + from + " to " + to);
		}

		List<Integer> postIds;
		if (feedName != null) {
			postIds = em
					.createQuery(
							"select post.id from Post post Where post.feed.name = ?1 "
									+ "order by post.published desc, post.link").setParameter(1, feedName)
					.setMaxResults(to - from).setFirstResult(from).getResultList();
		} else {
			postIds = em.createQuery("select post.id from Post post order by post.published desc, post.link")
					.setMaxResults(to - from).setFirstResult(from).getResultList();
		}

		// Construct recent posts from IDs.
		List<Post> posts = new LinkedList<Post>();
		for (Integer id : postIds) {
			posts.add(getPost(id));
		}

		return posts;
	}

	public Post getPost(int id) {
		DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);

		Post post = em.find(Post.class, id);

		post.setPublishedDate(df.format(post.getPublished()));
		post.setModifiedDate(df.format(post.getModified()));
		post.setContentPreview(StringTools.createSummary(post.getContent(), 400));

		return post;
	}

	@SuppressWarnings("unchecked")
	public List<RemoteFeed> getAcceptedFeeds() {
		return em.createQuery("select feed from RemoteFeed feed WHERE feed.status = ?1")
				.setParameter(1, FeedStatus.ACCEPTED).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Integer> getAcceptedFeedIds() {
		return em.createQuery("select feed.id from RemoteFeed feed WHERE feed.status = ?1")
				.setParameter(1, FeedStatus.ACCEPTED).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<RemoteFeed> getFeedsNoProposition() {
		return em.createQuery("select feed from RemoteFeed feed WHERE feed.status != ?1")
				.setParameter(1, FeedStatus.PROPOSED).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<RemoteFeed> getAcceptedFeedsSortedByTitle() {
		return em.createQuery("select feed from RemoteFeed feed WHERE feed.status = ?1 ORDER BY feed.title")
				.setParameter(1, FeedStatus.ACCEPTED).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<RemoteFeed> getAcceptedFeeds(Integer groupId) {
		return em.createQuery("select feed from RemoteFeed feed WHERE feed.group.id = ?1 and feed.status = ?2")
				.setParameter(1, groupId).setParameter(2, FeedStatus.ACCEPTED).getResultList();
	}

	public Long getFeedPostsCount(RemoteFeed feed) {
		log.log(Level.FINE, "Get feed ''{0}'' posts Count from the DB.", feed.getName());
		try {
			return (Long) em.createQuery("select count(*) from Post post where post.feed = ?1").setParameter(1, feed)
					.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public List<RemoteFeed> getUserFeeds(SecurityUser user) {
		ArrayList<RemoteFeed> result = new ArrayList<RemoteFeed>();
		for (SecurityMapping mapping : user.getMappings()) {
			if (FeedsSecurityRole.ADMIN.equals(mapping.getRole())) {
				return getFeedsNoProposition();
			}
			if (FeedsSecurityRole.FEED_ADMIN.equals(mapping.getRole())) {
				Integer feedId = mapping.getIdForRole();
				result.add(find(feedId));
			}
			if (FeedsSecurityRole.GROUP_ADMIN.equals(mapping.getRole())) {
				Integer groupId = mapping.getIdForRole();
				result.addAll(getFeeds(groupId));
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@AdminAllowed
	public List<RemoteFeed> getProposedFeeds() {
		return em.createQuery("select feed from RemoteFeed feed WHERE feed.status = ?1 ORDER BY feed.title")
				.setParameter(1, FeedStatus.PROPOSED).getResultList();
	}

	public String generateTitleAsId(String title) {
		String candidate = StringTools.convertTitleToLink(title);
		int nextCandidateNumber = 0;

		while (true) {
			int candidateCount = em.createQuery("select post from Post post where post.titleAsId = ?1")
					.setParameter(1, candidate).getResultList().size();

			if (candidateCount > 0) {
				candidate = StringTools.convertTitleToLink(title) + nextCandidateNumber;
				nextCandidateNumber++;
			} else {
				return candidate;
			}
		}
	}
}
