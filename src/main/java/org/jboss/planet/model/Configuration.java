/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.model;

import org.apache.commons.lang.StringUtils;
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

	/**
	 * JBoss Developer / Zurb Theme
	 */
	@Column
	@NotNull
	private String themeUrl;

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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Configuration that = (Configuration) o;

		if (!id.equals(that.id)) return false;
		if (adminEmail != null ? !adminEmail.equals(that.adminEmail) : that.adminEmail != null) return false;
		if (connectionTimeout != null ? !connectionTimeout.equals(that.connectionTimeout) : that.connectionTimeout != null)
			return false;
		if (contextName != null ? !contextName.equals(that.contextName) : that.contextName != null) return false;
		if (readTimeout != null ? !readTimeout.equals(that.readTimeout) : that.readTimeout != null) return false;
		if (serverAddress != null ? !serverAddress.equals(that.serverAddress) : that.serverAddress != null)
			return false;
		if (syncContentType != null ? !syncContentType.equals(that.syncContentType) : that.syncContentType != null)
			return false;
		if (syncPassword != null ? !syncPassword.equals(that.syncPassword) : that.syncPassword != null) return false;
		if (syncServer != null ? !syncServer.equals(that.syncServer) : that.syncServer != null) return false;
		if (syncServerHttpInViewLayer != null ? !syncServerHttpInViewLayer.equals(that.syncServerHttpInViewLayer) : that.syncServerHttpInViewLayer != null)
			return false;
		if (syncUsername != null ? !syncUsername.equals(that.syncUsername) : that.syncUsername != null) return false;
		if (themeUrl != null ? !themeUrl.equals(that.themeUrl) : that.themeUrl != null) return false;
		if (updateFeedFailsThreshold != null ? !updateFeedFailsThreshold.equals(that.updateFeedFailsThreshold) : that.updateFeedFailsThreshold != null)
			return false;
		if (updateInterval != null ? !updateInterval.equals(that.updateInterval) : that.updateInterval != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = id.hashCode();
		result = 31 * result + (readTimeout != null ? readTimeout.hashCode() : 0);
		result = 31 * result + (connectionTimeout != null ? connectionTimeout.hashCode() : 0);
		result = 31 * result + (updateInterval != null ? updateInterval.hashCode() : 0);
		result = 31 * result + (updateFeedFailsThreshold != null ? updateFeedFailsThreshold.hashCode() : 0);
		result = 31 * result + (adminEmail != null ? adminEmail.hashCode() : 0);
		result = 31 * result + (serverAddress != null ? serverAddress.hashCode() : 0);
		result = 31 * result + (contextName != null ? contextName.hashCode() : 0);
		result = 31 * result + (syncServer != null ? syncServer.hashCode() : 0);
		result = 31 * result + (syncServerHttpInViewLayer != null ? syncServerHttpInViewLayer.hashCode() : 0);
		result = 31 * result + (syncUsername != null ? syncUsername.hashCode() : 0);
		result = 31 * result + (syncPassword != null ? syncPassword.hashCode() : 0);
		result = 31 * result + (syncContentType != null ? syncContentType.hashCode() : 0);
		result = 31 * result + (themeUrl != null ? themeUrl.hashCode() : 0);
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
				", themeUrl='" + themeUrl + '\'' +
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
