/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.service;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.jboss.planet.event.MergePostsEvent;
import org.jboss.planet.model.Post;
import org.jboss.planet.model.PostStatus;
import org.jboss.planet.model.RemoteFeed;

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
		int duplicateTitles = 0;
		Date threshold = DateUtils.addMonths(new Date(), DATE_THRESHOLD_MONTHS);

		log.log(Level.FINE, "Merging posts from feed ''{0}''. Date Threshold: {1}", new Object[]{feed.getName(), threshold});

		for (Post p : postsToMerge) {
			if (StringUtils.isEmpty(p.getTitle())) {
				log.log(Level.FINE,
						"Post does not contain a title - cannot be retrieved to aggregator. Blog post link: {0}",
						p.getTitle());
				ignoredPosts++;
				continue;
			}
			if (!needToCheck(p.getPublished(), threshold)) {
				ignoredPosts++;
				continue;
			}
			try {
				// Decide new or update post
				List<Post> postDbs = postService.find(feed, p.getTitle(), p.getPublished());
				if (postDbs.size() == 0) {
					boolean duplicatePostHandled = handleDuplicatePosts(p, feed);
					if (duplicatePostHandled) {
						duplicateTitles++;
						continue;
					}
					savePost(feed, p);
					newPosts++;
				} else {
					for (Post postDb : postDbs) {
						// ORG-2015 - Do not compare content. Only dates (title is same)
						if (postDb.compareTo(p) != 0) {
							if (log.isLoggable(Level.FINEST)) {
								log.log(Level.FINEST, "Merging post: ''{0}''", postDb.getTitleAsId());
								log.log(Level.FINEST, "Content difference: ''{0}''", StringUtils.difference(postDb.getContent(), p.getContent()));
								log.log(Level.FINEST, "Published: current: {0}, new: {1}", new Object[]{postDb.getPublished(), p.getPublished()});
								log.log(Level.FINEST, "Modified : current: {0}, new: {1}", new Object[]{postDb.getModified(), p.getModified()});
							}
							// Title is not changed because it has unique titleAsId and is already created. See above.
							postDb.setPublished(p.getPublished());
							postDb.setModified(p.getModified());
							postDb.setContent(removeNonBmpCharacters(p.getContent()));

							if (!postDb.isOnModeration()) {
								postDb.setStatus(PostStatus.FORCE_SYNC);
							}

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
		return new MergePostsEvent(newPosts, mergedPosts, postsToMerge.size(), ignoredPosts, duplicateTitles);
	}

	/**
	 * Handle duplicate posts. Tries to find posts with same author and title. Save the post with moderation flag if such post exists
	 *
	 * @param p
	 * @param feed
	 * @return true if duplicate post exists (either newly created or already exists from previous update run)
	 */
	public boolean handleDuplicatePosts(Post p, RemoteFeed feed) {
		List<Post> duplicateTitlePosts = postService.find(p.getAuthor(), p.getTitle());

		for (Post duplicateTitlePost : duplicateTitlePosts) {
			// Check if duplicate post is same
			if (duplicateTitlePost.compareTo(p) == 0) {
				if (log.isLoggable(Level.FINE)) {
					log.log(Level.FINE, "Blog post with duplicate title and same author: ''{0}'', title: ''{1}'', duplicate post: {2}",
							new Object[]{p.getAuthor(), p.getTitle(), duplicateTitlePost});
				}
				log.log(Level.FINEST, "Save blog post with moderation flag, post: {0}", p);
				p.setStatus(PostStatus.MODERATION_REQUIRED);
				savePost(feed, p);
				// duplicate post exists - just saved or already existed from previous update run
				return true;
			}
		}

		return false;
	}

	public boolean needToCheck(Date published, Date threshold) {
		if (published.getTime() <= threshold.getTime()) {
			return false;
		}
		return true;
	}

	/**
	 * Makes string to be Basic Multilingual Plane unicode.
	 * The reason of using it is MySql doesn't support nonn BMP characters like emoticons
	 *
	 * @param input
	 * @return String without BMP chracters
	 * @see
	 */
	public static String removeNonBmpCharacters(String input) {
		if (input == null) {
			return null;
		}
		return input.replaceAll("[^\\u0000-\\uFFFF]", "");
	}

	public void savePost(RemoteFeed feed, Post post) {
		// Preparing the post
		post.setTitleAsId(feedsService.generateTitleAsId(post.getTitle()));

		if (StringUtils.isBlank(post.getLink())) {
			post.setLink(linkService.generatePostLink(post.getTitleAsId()));
		}

		post.setContent(removeNonBmpCharacters(post.getContent()));

		post.setFeed(feed);

		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, "Saving post, feed: {0}, post title: {1}, post titleAsId: {2}, published: {3}, status: {4}",
					new Object[]{feed.getName(), post.getTitle(), post.getTitleAsId(), post.getPublished(), post.getStatus()});
		}

		log.log(Level.INFO, "Saving new post ''{0}''", post.getTitleAsId());

		postService.create(post, false);
		log.log(Level.INFO, "New post ''{0}'' saved", post.getTitleAsId());
	}

}
