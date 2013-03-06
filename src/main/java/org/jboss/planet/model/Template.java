/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
public class Template implements Serializable {

	private static final long serialVersionUID = 5237509555544954138L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(updatable = false)
	private Integer id;

	@Size(max = 32)
	@NotNull
	@Column(unique = true)
	private String name;

	@Lob
	@NotNull
	private String text;

	@Enumerated
	private XmlType type;

	@Temporal(value = TemporalType.TIMESTAMP)
	private Date lastModified;

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

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public XmlType getType() {
		return type;
	}

	public void setType(XmlType type) {
		this.type = type;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Template))
			return false;

		Template template = (Template) o;

		if (type != template.type)
			return false;
		if (id != null ? !id.equals(template.id) : template.id != null)
			return false;
		if (name != null ? !name.equals(template.name) : template.name != null)
			return false;

		return true;
	}

	public int hashCode() {
		int result;
		result = (id != null ? id.hashCode() : 0);
		result = 31 * result + (name != null ? name.hashCode() : 0);
		result = 31 * result + (type != null ? type.hashCode() : 0);
		return result;
	}

	@PrePersist
	@PreUpdate
	public void updateLastModified() {
		setLastModified(new Date());
	}
}
