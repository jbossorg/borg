/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.model;

import java.io.Serializable;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.URL;
import org.jboss.planet.model.RemoteFeed.FeedStatus;

@Entity
@Cacheable(true)
public class Configuration implements Serializable {

	private static final long serialVersionUID = 392975634737686960L;

	@Id
	@Column(updatable = false)
	private Integer id;

	@Column
	@NotNull
	private Integer readTimeout;

	@Column
	@NotNull
	private Integer connectionTimeout;

	@Column
	@NotNull
	private Integer updateInterval;

	/**
	 * Threshold for updating feeds. If update feeds has any problem then it's counted and if this threshold is reached
	 * then feed is disabled
	 *
	 * @see FeedStatus
	 */
	private Integer updateFeedFailsThreshold;

	@Column
	@Email
	private String adminEmail;

	@Column
	private String serverAddress;

	@Column
	private String contextName;

	/**
	 * URL of synchronization server e.g. https://dcp.jboss.org/v2
	 */
	@URL
	private String syncServer;

	/**
	 * Flag telling view layer to use http
	 */
	private Boolean syncServerHttpInViewLayer = true;

	private String syncUsername;

	private String syncPassword;

	private String syncContentType;

	/**
	 * JBoss Developer / Zurb Theme
	 */
	@Column
	@NotNull
	private String themeUrl;

	/**
	 * Search Query
	 */
	@Column
	private String searchQuery;


	private boolean twitterEnabled;
	private String twitterOAuthConsumerKey;
	private String twitterOAuthConsumerSecret;
	private String twitterOAuthAccessToken;
	private String twitterOAuthAccessTokenSecret;
	private String twitterText;
	private int twitterPublishDateThresholdInHours;

	@Override
	public String toString() {
		return "Configuration{" +
				"id=" + id +
				", readTimeout=" + readTimeout +
				", connectionTimeout=" + connectionTimeout +
				", updateInterval=" + updateInterval +
				", updateFeedFailsThreshold=" + updateFeedFailsThreshold +
				", adminEmail='" + adminEmail + '\'' +
				", serverAddress='" + serverAddress + '\'' +
				", contextName='" + contextName + '\'' +
				", syncServer='" + syncServer + '\'' +
				", syncServerHttpInViewLayer=" + syncServerHttpInViewLayer +
				", syncUsername='" + syncUsername + '\'' +
				", syncPassword=******" +
				", syncContentType='" + syncContentType + '\'' +
				", themeUrl='" + themeUrl + '\'' +
				", twitterEnabled=" + twitterEnabled +
				", twitterOAuthConsumerKey='" + twitterOAuthConsumerKey + '\'' +
				", twitterOAuthConsumerSecret='" + twitterOAuthConsumerSecret + '\'' +
				", twitterOAuthAccessToken=" +
				", twitterOAuthAccessTokenSecret=****" +
				", twitterPublishDateThresholdInHours=" + twitterPublishDateThresholdInHours + '\'' +
				'}';
	}


	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getReadTimeout() {
		return readTimeout;
	}

	public void setReadTimeout(Integer readTimeout) {
		this.readTimeout = readTimeout;
	}

	public Integer getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(Integer connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public Integer getUpdateInterval() {
		return updateInterval;
	}

	public void setUpdateInterval(Integer updateInterval) {
		this.updateInterval = updateInterval;
	}

	public String getAdminEmail() {
		return adminEmail;
	}

	public void setAdminEmail(String adminEmail) {
		this.adminEmail = adminEmail;
	}

	public String getServerAddress() {
		return serverAddress;
	}

	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}

	public String getContextName() {
		return contextName;
	}

	public void setContextName(String contextName) {
		this.contextName = contextName;
	}

	public String getThemeUrl() {
		if (StringUtils.isBlank(themeUrl)) {
			// Default value for initial configuration
			return "static.jboss.org/www";
		}
		return themeUrl;
	}

	public void setThemeUrl(String themeUrl) {
		this.themeUrl = themeUrl;
	}

	public String getSearchQuery() {
		if (StringUtils.isBlank(searchQuery)) {
			// Default value for initial configuration
			return "search?sys_type=blogpost&sortBy=new-create&field=_source";
		}
		return searchQuery;
	}

	public void setSearchQuery(String searchQuery) {
		this.searchQuery = searchQuery;
	}

	public String getSyncServer() {
		return syncServer;
	}

	public void setSyncServer(String syncServer) {
		this.syncServer = syncServer;
	}

	public String getSyncUsername() {
		return syncUsername;
	}

	public void setSyncUsername(String syncUsername) {
		this.syncUsername = syncUsername;
	}

	public String getSyncPassword() {
		return syncPassword;
	}

	public void setSyncPassword(String syncPassword) {
		this.syncPassword = syncPassword;
	}

	public String getSyncContentType() {
		return syncContentType;
	}

	public void setSyncContentType(String syncContentType) {
		this.syncContentType = syncContentType;
	}

	public Boolean getSyncServerHttpInViewLayer() {
		return syncServerHttpInViewLayer;
	}

	public void setSyncServerHttpInViewLayer(Boolean syncServerHttpInViewLayer) {
		this.syncServerHttpInViewLayer = syncServerHttpInViewLayer;
	}

	public Integer getUpdateFeedFailsThreshold() {
		return updateFeedFailsThreshold;
	}

	public void setUpdateFeedFailsThreshold(Integer updateFeedFailsThreshold) {
		this.updateFeedFailsThreshold = updateFeedFailsThreshold;
	}

	public boolean isTwitterEnabled() {
		return twitterEnabled;
	}

	public void setTwitterEnabled(boolean twitterEnabled) {
		this.twitterEnabled = twitterEnabled;
	}

	public String getTwitterOAuthConsumerKey() {
		return twitterOAuthConsumerKey;
	}

	public void setTwitterOAuthConsumerKey(String twitterOAuthConsumerKey) {
		this.twitterOAuthConsumerKey = twitterOAuthConsumerKey;
	}

	public String getTwitterOAuthConsumerSecret() {
		return twitterOAuthConsumerSecret;
	}

	public void setTwitterOAuthConsumerSecret(String twitterOAuthConsumerSecret) {
		this.twitterOAuthConsumerSecret = twitterOAuthConsumerSecret;
	}

	public String getTwitterOAuthAccessToken() {
		return twitterOAuthAccessToken;
	}

	public void setTwitterOAuthAccessToken(String twitterOAuthAccessToken) {
		this.twitterOAuthAccessToken = twitterOAuthAccessToken;
	}

	public String getTwitterOAuthAccessTokenSecret() {
		return twitterOAuthAccessTokenSecret;
	}

	public void setTwitterOAuthAccessTokenSecret(String twitterOAuthAccessTokenSecret) {
		this.twitterOAuthAccessTokenSecret = twitterOAuthAccessTokenSecret;
	}

	public String getTwitterText() {
		return twitterText;
	}

	public void setTwitterText(String twitterText) {
		this.twitterText = twitterText;
	}

	public int getTwitterPublishDateThresholdInHours() {
		return twitterPublishDateThresholdInHours;
	}

	public void setTwitterPublishDateThresholdInHours(int twitterPublishDateThresholdInHours) {
		this.twitterPublishDateThresholdInHours = twitterPublishDateThresholdInHours;
	}
}
