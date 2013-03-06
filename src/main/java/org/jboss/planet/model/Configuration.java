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
	private int readTimeout;

	@Column
	@NotNull
	private int connectionTimeout;

	@Column
	@NotNull
	private int updateInterval;

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
	 * URL of synchronization server e.g. search.jboss.org
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

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public int getReadTimeout() {
		return readTimeout;
	}

	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public int getUpdateInterval() {
		return updateInterval;
	}

	public void setUpdateInterval(int updateInterval) {
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

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Configuration))
			return false;

		Configuration that = (Configuration) o;

		if (connectionTimeout != that.connectionTimeout)
			return false;
		if (readTimeout != that.readTimeout)
			return false;
		if (updateInterval != that.updateInterval)
			return false;
		if (adminEmail != null ? !adminEmail.equals(that.adminEmail) : that.adminEmail != null)
			return false;
		if (id != null ? !id.equals(that.id) : that.id != null)
			return false;

		return true;
	}

	public int hashCode() {
		int result;
		result = (id != null ? id.hashCode() : 0);
		result = 31 * result + readTimeout;
		result = 31 * result + connectionTimeout;
		result = 31 * result + updateInterval;
		result = 31 * result + (adminEmail != null ? adminEmail.hashCode() : 0);
		return result;
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
}
