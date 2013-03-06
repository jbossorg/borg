/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.service;

import java.lang.reflect.ParameterizedType;
import java.util.List;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.jboss.planet.security.CRUDOperationType;
import org.jboss.planet.security.PermissionCRUDException;
import org.jboss.planet.security.UserNotLoggedInException;
import org.jboss.planet.service.qualifier.Created;
import org.jboss.planet.service.qualifier.Deleted;
import org.jboss.planet.service.qualifier.Updated;

/**
 * Implementation for JPA. Specified has to use constructor {@link #EntityServiceJpa(Class)} and specify for which
 * entity class it's used for.
 * 
 * @author Libor Krzyzanek
 * @param <T>
 */
@Stateless
public class EntityServiceJpa<T> implements EntityService<T> {

	private Class<T> persistentClass;

	@Inject
	private EntityManager em;

	@Inject
	private SecurityService securityService;

	@Inject
	@Created
	protected Event<T> created;

	@Inject
	@Updated
	protected Event<T> updated;

	@Inject
	@Deleted
	protected Event<T> deleted;

	@SuppressWarnings("unchecked")
	public EntityServiceJpa() {
		this.persistentClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass())
				.getActualTypeArguments()[0];
	}

	public EntityServiceJpa(Class<T> persistentClass) {
		this.persistentClass = persistentClass;
	}

	public EntityManager getEntityManager() {
		return em;
	}

	/**
	 * Check permission for current user to do operation {@link CRUDOperationType} with particular entity
	 * 
	 * @param t
	 * @param operation
	 * @throws UserNotLoggedInException
	 * @throws PermissionCRUDException
	 */
	protected void checkPermission(T t, CRUDOperationType operation) throws UserNotLoggedInException,
			PermissionCRUDException {
		securityService.checkPermission(t, operation);
	}

	public T create(T t, boolean checkSecurity) {
		if (checkSecurity) {
			checkPermission(t, CRUDOperationType.CREATE);
		}
		this.getEntityManager().persist(t);

		created.fire(t);

		return t;
	}

	@Override
	public T create(T t) {
		return create(t, true);
	}

	@Override
	public T find(Object id) {
		return getEntityManager().find(persistentClass, id);
	}

	@Override
	public List<T> findAll() {
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<T> criteria = cb.createQuery(persistentClass);
		Root<T> root = criteria.from(persistentClass);
		criteria.select(root);
		return getEntityManager().createQuery(criteria).getResultList();
	}

	public T update(T t, boolean checkSecurity) {
		if (checkSecurity) {
			checkPermission(t, CRUDOperationType.UPDATE);
		}

		t = (T) this.getEntityManager().merge(t);

		updated.fire(t);

		return t;
	}

	@Override
	public T update(T t) {
		return update(t, true);
	}

	public void delete(Object id, boolean checkSecurity) {
		T t = this.getEntityManager().getReference(persistentClass, id);

		if (checkSecurity) {
			checkPermission(t, CRUDOperationType.DELETE);
		}

		this.getEntityManager().remove(t);

		deleted.fire(t);

	}

	@Override
	public void delete(Object id) {
		delete(id, true);
	}

}
