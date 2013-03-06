/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.controller;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Model;
import javax.inject.Inject;

import org.hibernate.stat.Statistics;
import org.jboss.planet.security.AdminAllowed;
import org.jboss.planet.service.StatisticService;

/**
 * Controller for statistics
 * 
 * @author Libor Krzyzanek
 * 
 */
@Model
public class StatisticController {

	@Inject
	private StatisticService statisticService;

	private Statistics hibernateStatistics;

	@PostConstruct
	public void init() {
		hibernateStatistics = statisticService.getHibernateStatistics();
	}

	@AdminAllowed
	public String startHibernateStatistics() {
		statisticService.getHibernateStatistics().setStatisticsEnabled(true);
		return "pretty:manage-statistics";
	}

	@AdminAllowed
	public String stopHibernateStatistics() {
		statisticService.getHibernateStatistics().clear();
		statisticService.getHibernateStatistics().setStatisticsEnabled(false);
		return "pretty:manage-statistics";
	}

	@AdminAllowed
	public String clearHibernateStatistics() {
		statisticService.getHibernateStatistics().clear();
		return "pretty:manage-statistics";
	}

	public Statistics getHibernateStatistics() {
		return hibernateStatistics;
	}
}
