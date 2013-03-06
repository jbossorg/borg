/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Model;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.http.client.ClientProtocolException;
import org.jboss.planet.model.FeedsSecurityRole;
import org.jboss.planet.model.RemoteFeed;
import org.jboss.planet.model.RemoteFeed.FeedStatus;
import org.jboss.planet.model.SecurityUser;
import org.jboss.planet.security.CRUDOperationType;
import org.jboss.planet.security.LoggedIn;
import org.jboss.planet.service.FeedsService;
import org.jboss.planet.service.ParserService;
import org.jboss.planet.service.SecurityService;
import org.jboss.planet.util.ApplicationMessages;

import com.sun.syndication.feed.synd.SyndCategory;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;

/**
 * Model for {@link RemoteFeed}
 * 
 * @author Libor Krzyzanek
 */
@Model
public class FeedController extends AdminController {

	@Inject
	private FeedsService feedsService;

	@Inject
	private ParserService parserService;

	@Inject
	private SecurityService securityService;

	@Inject
	private FacesContext facesContext;

	@Inject
	private ApplicationMessages messages;

	private RemoteFeed feedToUpdate;

	private String includeCategory;

	@Inject
	private FeedCategoriesHolder includeCategoriesHolder;

	private String feedName = null;

	@Inject
	private Logger log;

	private final static String ALL_CATEGORIES = "ALL";

	@LoggedIn
	public void loadFeed() {
		feedToUpdate = feedsService.getFeed(feedName);
		feedsService.checkEditPermissions(feedToUpdate);
	}

	@PostConstruct
	public void init() {
		feedToUpdate = new RemoteFeed();
	}

	public boolean canEditGroup() {
		return securityService.hasPermission(securityService.getCurrentUser(), feedToUpdate.getGroup(),
				CRUDOperationType.UPDATE);
	}

	private SyndFeed parseAndValidateRemoteFeed(String url) {
		try {
			return parserService.getRemoteFeed(feedToUpdate.getRemoteLink());
		} catch (Exception e) {
			log.log(Level.WARNING, "Cannot retrieve feed", e);
			UIInput urlInput = (UIInput) facesContext.getViewRoot().findComponent("feed:url");
			urlInput.setValid(false);
			facesContext.addMessage(
					urlInput.getClientId(),
					new FacesMessage(FacesMessage.SEVERITY_ERROR, messages.getString(
							"management.feed.text.retrieveFeedError", e.getMessage()), null));
			return null;
		}
	}

	@LoggedIn
	public String retrieveFeed() {
		feedToUpdate.setAuthorJbossUsername(securityService.getCurrentUser().getName());

		SyndFeed syndFeed = parseAndValidateRemoteFeed(feedToUpdate.getRemoteLink());

		if (syndFeed != null) {
			feedToUpdate.setAuthor(syndFeed.getAuthor());
			feedToUpdate.setDescription(syndFeed.getDescription());
			feedToUpdate.setLink(syndFeed.getLink());
			feedToUpdate.setTitle(syndFeed.getTitle());

			Map<String, String> includeCategories = new HashMap<String, String>();
			includeCategories.put(messages.getString("management.feed.includeCategory.all"), ALL_CATEGORIES);

			if (syndFeed.getEntries() != null) {
				for (Object entryObj : syndFeed.getEntries()) {
					SyndEntry entry = (SyndEntry) entryObj;
					for (Object categoryObj : entry.getCategories()) {
						SyndCategory category = (SyndCategory) categoryObj;
						includeCategories.put(category.getName(), category.getName());
					}
				}
			}
			includeCategoriesHolder.setCategories(includeCategories);
			includeCategory = ALL_CATEGORIES;
		}
		return null;
	}

	private boolean validate() {
		boolean valid = true;

		boolean validateName = true;
		if (feedToUpdate.getId() != null) {
			RemoteFeed originalFeed = feedsService.find(feedToUpdate.getId());
			if (originalFeed.getName().equals(feedToUpdate.getName())) {
				// when updating same feed name is correct
				validateName = false;
			}
		}

		if (validateName && feedsService.exists(feedToUpdate.getName())) {
			UIInput nameInput = (UIInput) facesContext.getViewRoot().findComponent("feed:name");
			nameInput.setValid(false);
			facesContext.addMessage(
					nameInput.getClientId(),
					new FacesMessage(FacesMessage.SEVERITY_ERROR, messages
							.getString("management.feed.text.feedAlreadyExists"), null));
			valid = false;
		}

		if (parseAndValidateRemoteFeed(feedToUpdate.getRemoteLink()) == null) {
			valid = false;
		}
		return valid;
	}

	@LoggedIn
	public String update() {
		if (!validate()) {
			return null;
		}
		if (feedToUpdate.getId() == null) {
			// ensure that acceptance was not forced
			feedToUpdate.setStatus(FeedStatus.PROPOSED);

			if (ALL_CATEGORIES.equals(includeCategory)) {
				feedToUpdate.setIncludeCategoryRegexp(null);
			} else if (includeCategory != null) {
				String regexp = Pattern.quote(includeCategory);
				feedToUpdate.setIncludeCategoryRegexp(regexp);
			}

			feedsService.create(feedToUpdate, securityService.getCurrentUser());
			facesContext.addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, messages.getString("management.feed.text.added",
							feedToUpdate.getTitle()), null));
		} else {
			if (feedToUpdate.isAccepted()) {
				feedToUpdate.setUpdateFailCount(0);
			}
			feedsService.update(feedToUpdate);
			facesContext.addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, messages.getString("management.feed.text.updated",
							feedToUpdate.getTitle()), null));
		}
		return "pretty:manage-feed";
	}

	@LoggedIn
	@Override
	public String addAdmin() {
		addAdmin(FeedsSecurityRole.FEED_ADMIN, feedToUpdate.getId());
		return "pretty:manage-feed-admins";
	}

	@LoggedIn
	@Override
	public String removeAdmin() {
		removeAdmin(FeedsSecurityRole.FEED_ADMIN, feedToUpdate.getId());
		return "pretty:manage-feed-admins";
	}

	@LoggedIn
	public List<RemoteFeed> getUserFeeds() {
		SecurityUser user = securityService.getCurrentUser();
		return feedsService.getUserFeeds(user);
	}

	@LoggedIn
	public List<RemoteFeed> getProposedFeeds() {
		return feedsService.getProposedFeeds();
	}

	@LoggedIn
	public String deleteFeed() throws ClientProtocolException, IOException {
		log.log(Level.INFO, "Delete Feed {0}", feedName);
		feedsService.deleteFeed(feedName);
		facesContext.addMessage(
				null,
				new FacesMessage(FacesMessage.SEVERITY_INFO, messages.getString("management.feed.text.deleted",
						feedName), null));
		return "pretty:manage-feed";
	}

	@LoggedIn
	public String acceptFeed() {
		log.log(Level.INFO, "Accept Feed {0}", feedToUpdate.getId());
		feedsService.acceptFeed(feedToUpdate);
		facesContext.addMessage(
				null,
				new FacesMessage(FacesMessage.SEVERITY_INFO, messages.getString("management.feed.text.accepted",
						feedToUpdate.getTitle()), null));
		return null;
	}

	@LoggedIn
	public String declineFeed() {
		log.log(Level.INFO, "Decline Feed {0}", feedToUpdate.getId());
		feedsService.delete(feedToUpdate.getId());
		facesContext.addMessage(
				null,
				new FacesMessage(FacesMessage.SEVERITY_INFO, messages.getString("management.feed.text.declined",
						feedToUpdate.getTitle()), null));
		return null;
	}

	public void setFeedToUpdate(RemoteFeed feedToUpdate) {
		this.feedToUpdate = feedToUpdate;
	}

	public RemoteFeed getFeedToUpdate() {
		return feedToUpdate;
	}

	public String getFeedName() {
		return feedName;
	}

	public void setFeedName(String feedName) {
		this.feedName = feedName;
	}

	public String getIncludeCategory() {
		return includeCategory;
	}

	public void setIncludeCategory(String includeCategory) {
		this.includeCategory = includeCategory;
	}

}
