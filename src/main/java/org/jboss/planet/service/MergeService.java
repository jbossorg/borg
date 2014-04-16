/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.service;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.jboss.planet.event.MergePostsEvent;
import org.jboss.planet.model.Post;
import org.jboss.planet.model.PostStatus;
import org.jboss.planet.model.RemoteFeed;
import org.jboss.planet.util.StringTools;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service responsible for merging new posts
 *
 * @author Libor Krzyzanek
 */
@Named
@Stateless
@TransactionAttribute(TransactionAttributeType.NEVER)
public class MergeService {

	@Inject
	private Logger log;

	@Inject
	private FeedsService feedsService;

	@Inject
	private LinkService linkService;

	@Inject
	private PostService postService;

	/**
	 * Date threshold. Older posts than threshold are ignored.
	 */
	public static final int DATE_THRESHOLD_MONTHS = -6;

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public MergePostsEvent mergePosts(RemoteFeed feed, List<Post> postsToMerge) {
		int newPosts = 0;
		int mergedPosts = 0;
		int ignoredPosts = 0;
		Date threshold = DateUtils.addMonths(new Date(), DATE_THRESHOLD_MONTHS);

		log.log(Level.FINE, "Merging posts from feed ''{0}''. Date Threshold: {1}", new Object[]{feed.getName(), threshold});

		for (Post p : postsToMerge) {
			if (StringUtils.isEmpty(p.getTitle())) {
				log.log(Level.WARNING,
						"Post does not contain title - cannot be retrieved to aggregator. Blog post link: {0}",
						p.getTitle());
				ignoredPosts++;
				continue;
			}
			if (!needToCheck(p.getPublished(), threshold)) {
				ignoredPosts++;
				continue;
			}
			try {
				List<Post> postDbs = postService.find(feed, p.getTitle(), p.getPublished());
				if (postDbs.size() == 0) {
					savePost(feed, p);
					newPosts++;
				} else {
					for (Post postDb : postDbs) {
						if (!postDb.getContent().equals(p.getContent())) {
							log.log(Level.FINE, "Saving merged post ''{0}''", postDb.getTitleAsId());

							postDb.setContent(p.getContent());
							postDb.setStatus(PostStatus.FORCE_SYNC);

							postService.update(postDb, false);
							mergedPosts++;
							log.log(Level.INFO, "Post ''{0}'' merged", postDb.getTitleAsId());
						}
					}
				}
			} catch (Exception e) {
				// single post cannot stop merging all posts
				log.log(Level.SEVERE, "Error occurred during merging post, post url: " + p.getLink(), e);
			}
		}
		return new MergePostsEvent(newPosts, mergedPosts, postsToMerge.size(), ignoredPosts);
	}

	public boolean needToCheck(Date published, Date threshold) {
		if (published.getTime() <= threshold.getTime()) {
			return false;
		}
		return true;
	}

	public void savePost(RemoteFeed feed, Post post) {
		// Preparing the post
		post.setTitleAsId(feedsService.generateTitleAsId(post.getTitle()));

		if (StringTools.isEmpty(post.getLink())) {
			post.setLink(linkService.generatePostLink(post.getTitleAsId()));
		}

		post.setContent(post.getContent());

		post.setFeed(feed);

		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, "Saving post, feed: {0}, post title: {1}, post titleAsId: {2}, published: {3}.",
					new Object[]{feed.getName(), post.getTitle(), post.getTitleAsId(), post.getPublished()});
		}

		log.log(Level.INFO, "Saving new post ''{0}''", post.getTitleAsId());

		postService.create(post, false);
		log.log(Level.INFO, "New post ''{0}'' saved", post.getTitleAsId());
	}

}
