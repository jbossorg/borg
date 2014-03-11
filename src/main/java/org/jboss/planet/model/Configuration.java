/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.model;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.URL;
import org.jboss.planet.model.RemoteFeed.FeedStatus;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

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
				'}';
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
