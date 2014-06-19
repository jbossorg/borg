/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.validator.constraints.URL;
import org.jboss.planet.util.StringTools;

@Entity
@Cacheable
public class RemoteFeed implements Serializable {

	private static final long serialVersionUID = -6052112657043167763L;

	public enum FeedStatus {
		/**
		 * Proposed
		 */
		PROPOSED,

		/**
		 * Accepted feed
		 */
		ACCEPTED,

		/**
		 * Generally disabled
		 */
		DISABLED,

		/**
		 * Disabled because of connection problem
		 */
		DISABLED_CONNECTION_PROBLEM

	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(updatable = false)
	private Integer id;

	@NotNull
	@ManyToOne
	private FeedGroup group;

	@NotNull
	@Size(min = 1, max = 32)
	@Column(unique = true)
	@Pattern(regexp = "^[a-z0-9_]*$", message = "allowed characters are a-z, 0-9 and underscore _")
	private String name;

	@NotNull
	@Size(max = 512)
	private String title;

	@NotNull
	@Size(max = 250)
	private String author;

	@Size(max = 250)
	private String authorAvatarLink;

	@Size(max = 512)
	private String link;

	@OneToMany(cascade = { CascadeType.REMOVE }, mappedBy = "feed")
	private List<Post> posts;

	@Lob
	private String description;

	@ManyToMany
	private Map<XmlType, Template> templates;

	private int maxPostsInFeed;

	private int maxPostsOnPage;

	@NotNull
	private PostAuthorType postAuthorType;

	private Boolean showDigg;

	private Boolean showDzone;

	private Boolean showDelicious;

	private Boolean showStumble;

	@NotNull
	@Size(max = 512)
	@URL
	private String remoteLink;

	// TODO: Check regexp validation
	// @Pattern(regexp = "*")
	private String includeCategoryRegexp;

	@Transient
	private PostFilter includeCategoryFilter;

	@NotNull
	@JsonIgnore
	private FeedStatus status;

	@JsonIgnore
	private Integer updateFailCount;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public FeedGroup getGroup() {
		return group;
	}

	public void setGroup(FeedGroup group) {
		this.group = group;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getAuthorAvatarLink() {
		return authorAvatarLink;
	}

	public void setAuthorAvatarLink(String authorAvatarLink) {
		this.authorAvatarLink = authorAvatarLink;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public List<Post> getPosts() {
		return posts;
	}

	public void setPosts(List<Post> posts) {
		this.posts = posts;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Map<XmlType, Template> getTemplates() {
		return templates;
	}

	public void setTemplates(Map<XmlType, Template> templates) {
		this.templates = templates;
	}

	public int getMaxPostsInFeed() {
		return maxPostsInFeed;
	}

	public void setMaxPostsInFeed(int maxPostsInFeed) {
		this.maxPostsInFeed = maxPostsInFeed;
	}

	public int getMaxPostsOnPage() {
		return maxPostsOnPage;
	}

	public void setMaxPostsOnPage(int maxPostsOnPage) {
		this.maxPostsOnPage = maxPostsOnPage;
	}

	public PostAuthorType getPostAuthorType() {
		return postAuthorType;
	}

	public void setPostAuthorType(PostAuthorType postAuthorType) {
		this.postAuthorType = postAuthorType;
	}

	public Boolean getShowDigg() {
		return showDigg;
	}

	public void setShowDigg(Boolean showDigg) {
		this.showDigg = showDigg;
	}

	public Boolean getShowDzone() {
		return showDzone;
	}

	public void setShowDzone(Boolean showDzone) {
		this.showDzone = showDzone;
	}

	public Boolean getShowDelicious() {
		return showDelicious;
	}

	public void setShowDelicious(Boolean showDelicious) {
		this.showDelicious = showDelicious;
	}

	public Boolean getShowStumble() {
		return showStumble;
	}

	public void setShowStumble(Boolean showStumble) {
		this.showStumble = showStumble;
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof RemoteFeed))
			return false;

		RemoteFeed feed = (RemoteFeed) o;

		if (id != null ? !id.equals(feed.id) : feed.id != null)
			return false;
		if (name != null ? !name.equals(feed.name) : feed.name != null)
			return false;

		return true;
	}

	public int hashCode() {
		int result;
		result = (id != null ? id.hashCode() : 0);
		result = 31 * result + (name != null ? name.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "RemoteFeed, id: " + id + ", name: " + name;
	}

	public String getRemoteLink() {
		return remoteLink;
	}

	public void setRemoteLink(String remoteLink) {
		this.remoteLink = remoteLink;
	}

	public String getIncludeCategoryRegexp() {
		return includeCategoryRegexp;
	}

	public void setIncludeCategoryRegexp(String includeCategoryRegexp) {
		this.includeCategoryRegexp = includeCategoryRegexp;

		synchronized (this) {
			includeCategoryFilter = null;
		}
	}

	public synchronized PostFilter getIncludeCategoryFilter() {
		if (includeCategoryFilter == null) {
			if (StringTools.isEmpty(includeCategoryRegexp)) {
				includeCategoryFilter = new TotalFilter();
			} else {
				includeCategoryFilter = new CategoryRegexpFilter(includeCategoryRegexp);
			}
		}

		return includeCategoryFilter;
	}

	public FeedStatus getStatus() {
		return status;
	}

	public boolean isAccepted() {
		return FeedStatus.ACCEPTED.compareTo(status) == 0;
	}

	public void setStatus(FeedStatus status) {
		this.status = status;
	}

	public Integer getUpdateFailCount() {
		return updateFailCount;
	}

	public void setUpdateFailCount(Integer updateFailCount) {
		this.updateFailCount = updateFailCount;
	}

	public void incrementUpdateFailCount() {
		if (updateFailCount == null) {
			updateFailCount = 1;
		} else {
			updateFailCount++;
		}
	}
}
