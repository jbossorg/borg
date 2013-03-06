/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

/**
 * @author Adam Warski (adam at warski dot org)
 * @author Libor Krzyzanek
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "role", "idForRole" }))
@Cacheable
public class SecurityMapping implements Serializable {

	private static final long serialVersionUID = -2950803972603221239L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(updatable = false)
	private Integer id;

	@Column
	@NotNull
	private FeedsSecurityRole role;

	/**
	 * Resource ID for defined role. It vary based on role.
	 */
	@Column
	private Integer idForRole;

	@ManyToMany
	private List<SecurityUser> users;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public FeedsSecurityRole getRole() {
		return role;
	}

	public void setRole(FeedsSecurityRole role) {
		this.role = role;
	}

	public Integer getIdForRole() {
		return idForRole;
	}

	public void setIdForRole(Integer idForRole) {
		this.idForRole = idForRole;
	}

	public List<SecurityUser> getUsers() {
		return users;
	}

	public void setUsers(List<SecurityUser> users) {
		this.users = users;
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof SecurityMapping))
			return false;

		SecurityMapping that = (SecurityMapping) o;

		if (id != null ? !id.equals(that.id) : that.id != null)
			return false;
		if (idForRole != null ? !idForRole.equals(that.idForRole) : that.idForRole != null)
			return false;
		if (role != that.role)
			return false;

		return true;
	}

	public int hashCode() {
		int result;
		result = (id != null ? id.hashCode() : 0);
		result = 31 * result + (role != null ? role.hashCode() : 0);
		result = 31 * result + (idForRole != null ? idForRole.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "SecurityMapping, id: " + id + ", idForRole: " + idForRole + ", role: " + role;
	}
}
