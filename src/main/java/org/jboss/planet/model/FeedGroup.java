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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Email;

/**
 * Group of feeds
 * 
 * @author Libor Krzyzanek
 * 
 */
@Entity
@Cacheable
public class FeedGroup implements Serializable {

	private static final long serialVersionUID = -4033223670597552952L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(updatable = false)
	private Integer id;

	@NotNull
	@Column(unique = true)
	@Size(max = 32)
	@Pattern(regexp = "^[a-z0-9_]*$", message = "{management.group.text.invalidName}")
	private String name;

	@NotNull
	@Size(max = 64)
	private String displayName;

	@Email
	private String adminEmail;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getAdminEmail() {
		return adminEmail;
	}

	public void setAdminEmail(String adminEmail) {
		this.adminEmail = adminEmail;
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof FeedGroup))
			return false;

		FeedGroup group = (FeedGroup) o;

		if (id != null ? !id.equals(group.id) : group.id != null)
			return false;
		if (name != null ? !name.equals(group.name) : group.name != null)
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
		return "FeedGroup, id: " + id + ", name: " + name;
	}

}
