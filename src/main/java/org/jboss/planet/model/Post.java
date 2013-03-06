/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.jboss.planet.util.GeneralTools;
import org.jboss.planet.util.StringTools;

/**
 * Blog Post entity
 * 
 * @author Libor Krzyzanek
 */
@Entity
@XmlRootElement
public class Post implements Serializable {

	private static final long serialVersionUID = 5310806000974155875L;

	/**
	 * Internal ID
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(updatable = false)
	private Integer id;

	/** Blog post Title */
	@NotNull
	@Size(max = 512)
	private String title;

	/** Unique title as ID */
	@NotNull
	@Pattern(regexp = "^[a-z0-9_]*$")
	@Column(unique = true)
	private String titleAsId;

	/** Blog post content */
	@Lob
	@Column(length = 16777215)
	private String content;

	/** Blog post preview - used for REST API */
	@Transient
	private String contentPreview;

	/** Original Blog post URL */
	@Size(max = 512)
	@NotNull
	private String link;

	/** Author */
	@Size(max = 250)
	@JsonIgnore
	private String author;

	/** Tags (in RSS world called categories */
	@ManyToMany
	@XmlTransient
	@JsonIgnore
	private List<Category> categories;

	/** Date of publish */
	@Temporal(value = TemporalType.TIMESTAMP)
	@NotNull
	private Date published;

	/** Formatted date as string - used in REST API */
	@Transient
	private String publishedDate;

	/** modified */
	@Temporal(value = TemporalType.TIMESTAMP)
	@NotNull
	private Date modified;

	/** Formatted date as string - used in REST API */
	@Transient
	private String modifiedDate;

	/** Feed which blog post belongs to */
	@ManyToOne
	@NotNull
	@JsonIgnore
	private RemoteFeed feed;

	/**
	 * Status of post
	 */
	@NotNull
	@JsonIgnore
	private PostStatus status;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		setTitleAsId(StringTools.convertTitleToLink(title));
		this.title = title;
	}

	public String getTitleAsId() {
		return titleAsId;
	}

	public void setTitleAsId(String titleAsId) {
		this.titleAsId = titleAsId;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public List<Category> getCategories() {
		return categories;
	}

	public void setCategories(List<Category> categories) {
		this.categories = categories;
	}

	public Date getPublished() {
		return published;
	}

	public void setPublished(Date published) {
		this.published = published;
	}

	public Date getModified() {
		return modified;
	}

	public void setModified(Date modified) {
		this.modified = modified;
	}

	public RemoteFeed getFeed() {
		return feed;
	}

	public void setFeed(RemoteFeed feed) {
		this.feed = feed;
	}

	@Transient
	@JsonProperty("author")
	public String getEffectiveAuthor() {
		String postAuthor = getAuthor();
		switch (getFeed().getPostAuthorType()) {
		case POST_AUTHOR:
			return postAuthor == null ? "" : postAuthor;

		case BLOG_AUTHOR:
			return getFeed().getAuthor();

		case BLOG_AUTHOR_IF_MISSING:
			return StringTools.isEmpty(postAuthor) ? getFeed().getAuthor() : postAuthor;
		}

		return null;
	}

	@JsonProperty("avatarLink")
	public String getEffectiveAvatarLink() {
		return getFeed().getEffectiveAvatarLink();
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Post))
			return false;

		Post post = (Post) o;

		if (id != null ? !id.equals(post.id) : post.id != null)
			return false;
		if (title != null ? !title.equals(post.title) : post.title != null)
			return false;
		if (titleAsId != null ? !titleAsId.equals(post.titleAsId) : post.titleAsId != null)
			return false;

		return true;
	}

	public int hashCode() {
		int result;
		result = (id != null ? id.hashCode() : 0);
		result = 31 * result + (title != null ? title.hashCode() : 0);
		result = 31 * result + (titleAsId != null ? titleAsId.hashCode() : 0);
		return result;
	}

	public int compareTo(Post post2) {
		int result = -GeneralTools.compareDates(getPublished(), post2.getPublished());
		if (result == 0) {
			return GeneralTools.compareStrings(getTitle(), post2.getTitle());
		} else {
			return result;
		}
	}

	public String toString() {
		return "Post(title = " + title + ", published = " + published + ", titleAsId = " + titleAsId + ")";
	}

	public String getContentPreview() {
		return contentPreview;
	}

	public void setContentPreview(String contentPreview) {
		this.contentPreview = contentPreview;
	}

	public String getPublishedDate() {
		return publishedDate;
	}

	public void setPublishedDate(String publishedDate) {
		this.publishedDate = publishedDate;
	}

	public String getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(String modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public PostStatus getStatus() {
		return status;
	}

	public void setStatus(PostStatus status) {
		this.status = status;
	}
}
