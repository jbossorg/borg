/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.service;

import org.jboss.planet.exception.DuplicateEntryException;
import org.jboss.planet.model.FeedsSecurityRole;
import org.jboss.planet.model.SecurityMapping;
import org.jboss.planet.model.SecurityUser;
import org.jboss.planet.security.AdminAllowed;
import org.jboss.planet.security.CRUDOperationType;
import org.jboss.planet.service.qualifier.Updated;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.List;

/**
 * Business logic for permissions
 * 
 * @author Libor Krzyzanek
 */
@Named
@Stateless
public class PermissionService extends EntityServiceJpa<SecurityMapping> {

	@Inject
	private EntityManager em;

	@Inject
	@Updated
	private Event<SecurityUser> userUpdated;

	public PermissionService() {
		super(SecurityMapping.class);
	}

	public List<SecurityUser> getSystemAdministartors() {
		return getUsersFromMapping(FeedsSecurityRole.ADMIN);
	}

	public List<SecurityUser> getFeedAdministrators(Integer feedId) {
		return getUsersFromMapping(FeedsSecurityRole.FEED_ADMIN, feedId);
	}

	public List<SecurityUser> getGroupAdministrators(Integer groupId) {
		return getUsersFromMapping(FeedsSecurityRole.GROUP_ADMIN, groupId);
	}

	@AdminAllowed
	public void addSystemAdministrator(SecurityUser user) throws DuplicateEntryException {
		SecurityMapping mapping = (SecurityMapping) em
				.createQuery("select mapping from SecurityMapping mapping where mapping.role = ?1")
				.setParameter(1, FeedsSecurityRole.ADMIN).getSingleResult();

		if (mapping.getUsers().contains(user)) {
			throw new DuplicateEntryException("User " + user + " already has required permissions");
		}

		mapping.getUsers().add(user);

		em.merge(mapping);
		em.flush();

		userUpdated.fire(user);
	}

	public void addAdministrator(FeedsSecurityRole role, Integer idForRole, SecurityUser user)
			throws DuplicateEntryException {
		SecurityMapping mapping;
		try {
			mapping = (SecurityMapping) em
					.createQuery(
							"select mapping from SecurityMapping mapping where mapping.role = ?1 and mapping.idForRole = ?2")
					.setParameter(1, role).setParameter(2, idForRole).getSingleResult();
			checkPermission(mapping, CRUDOperationType.CREATE);
		} catch (NoResultException e) {
			mapping = new SecurityMapping();
			mapping.setRole(role);
			mapping.setIdForRole(idForRole);
			mapping.setUsers(new ArrayList<SecurityUser>());
			// Security is checked in parent
			mapping = create(mapping);
		}

		if (mapping.getUsers().contains(user)) {
			throw new DuplicateEntryException("User " + user + " already has required permissions");
		}

		mapping.getUsers().add(user);

		em.merge(mapping);
		em.flush();

		userUpdated.fire(user);
	}

	@AdminAllowed
	public void removeSystemAdministrator(SecurityUser user) {
		SecurityMapping mapping = (SecurityMapping) em
				.createQuery("select mapping from SecurityMapping mapping where mapping.role = ?1")
				.setParameter(1, FeedsSecurityRole.ADMIN).getSingleResult();

		mapping.getUsers().remove(user);

		em.merge(mapping);
		em.flush();

		userUpdated.fire(user);
	}

	public void removeAdministrator(FeedsSecurityRole role, Integer idForRole, SecurityUser user) {
		SecurityMapping mapping = (SecurityMapping) em
				.createQuery(
						"select mapping from SecurityMapping mapping where mapping.role = ?1 and mapping.idForRole = ?2")
				.setParameter(1, role).setParameter(2, idForRole).getSingleResult();

		checkPermission(mapping, CRUDOperationType.DELETE);

		mapping.getUsers().remove(user);

		em.merge(mapping);
		em.flush();

		userUpdated.fire(user);
	}

	/**
	 * Get List of {@link SecurityUser} for particular role and entity ID
	 * 
	 * @param role
	 * @param idForRole
	 *            id of entity
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<SecurityUser> getUsersFromMapping(FeedsSecurityRole role, Integer idForRole) {
		try {
			return (List<SecurityUser>) em
					.createQuery(
							"select mapping.users from SecurityMapping mapping where mapping.role = ?1 and mapping.idForRole = ?2")
					.setParameter(1, role).setParameter(2, idForRole).getResultList();
		} catch (NoResultException e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public List<SecurityUser> getUsersFromMapping(FeedsSecurityRole role) {
		try {
			return (List<SecurityUser>) em
					.createQuery("select mapping.users from SecurityMapping mapping where mapping.role = ?1")
					.setParameter(1, role).getResultList();
		} catch (NoResultException e) {
			return null;
		}
	}
}
