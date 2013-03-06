/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.service;

import java.util.List;

/**
 * Base interface for data manipulation of specified entity
 * 
 * @author Libor Krzyzanek
 * @param <T>
 */
public interface EntityService<T> {

	/**
	 * Create an entity
	 * 
	 * @param t
	 * @return actual state of new entity
	 */
	public T create(T t);

	/**
	 * Find entity based on its ID
	 * 
	 * @param id
	 * @return
	 */
	public T find(Object id);

	/**
	 * Find all entities
	 * 
	 * @return
	 */
	public List<T> findAll();

	/**
	 * Update entity
	 * 
	 * @param t
	 * @return updated entity
	 */
	public T update(T t);

	/**
	 * Delete entity
	 * 
	 * @param id
	 */
	public void delete(Object id);

}
