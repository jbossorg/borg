/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.model;

import java.security.Principal;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.validation.constraints.NotNull;

/**
 * @author Libor Krzyzanek
 */
@Entity
@Cacheable
public class SecurityUser implements Principal {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(updatable = false)
	private Integer id;

	/**
	 * ID of user in external system. In jboss.org it's username
	 */
	@NotNull
	private String externalId;

	/**
	 * Roles for user
	 */
	@ManyToMany(mappedBy = "users", fetch = FetchType.EAGER)
	private List<SecurityMapping> mappings;

	@Override
	public String getName() {
		return externalId;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public List<SecurityMapping> getMappings() {
		return mappings;
	}

	public void setMappings(List<SecurityMapping> mappings) {
		this.mappings = mappings;
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof SecurityUser))
			return false;

		SecurityUser user = (SecurityUser) o;

		if (externalId != null ? !externalId.equals(user.externalId) : user.externalId != null)
			return false;
		if (id != null ? !id.equals(user.id) : user.id != null)
			return false;

		return true;
	}

	public int hashCode() {
		int result;
		result = (id != null ? id.hashCode() : 0);
		result = 31 * result + (externalId != null ? externalId.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return getName();
	}

}
