package org.jboss.planet.service;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.jboss.planet.model.PostStatus;

/**
 * Service for Sitemap generation
 *
 * @author Libor Krzyzanek
 */
@Named
@RequestScoped
public class SitemapService {

	@Inject
	private Logger log;

	@Inject
	private EntityManager em;

	@Inject
	private HttpServletRequest httpServletRequest;

	/**
	 * Produces data for sitemap index
	 *
	 * @return List of arrays where 0 = year of published, 1 = latest date where blog posts was published within year
	 */
	@Produces
	@Named("sitemapIndex")
	@RequestScoped
	@SuppressWarnings("unchecked")
	public List<Object[]> produceSitemapIndex() {
		return em.createQuery(
				"select YEAR(published), MAX(published) from Post where status != :status group by YEAR(published)")
				.setParameter("status", PostStatus.MODERATION_REQUIRED)
				.getResultList();
	}

	/**
	 * Produces sitemap data. If request has attribute year then only posts within defined year is returned.
	 * Posts with status = PostStatus#MODERATION_REQUIRED are omitted.
	 *
	 * @return list of Post#titleAsId
	 */
	@Produces
	@Named("sitemap")
	@RequestScoped
	@SuppressWarnings("unchecked")
	public List<String> produceSitemap() {
		String yearStr = httpServletRequest.getParameter("year");
		log.log(Level.FINE, "Produce Sitemap for year {0}", yearStr);

		try {
			int year = Integer.parseInt(yearStr);

			return em.createQuery(
					"select titleAsId from Post where YEAR(published) = :year and status != :status order by published desc")
					.setParameter("year", year)
					.setParameter("status", PostStatus.MODERATION_REQUIRED)
					.getResultList();
		} catch (NumberFormatException e) {
			log.log(Level.FINE, "Invalid input parameter year. Returning all URLs");

			return em.createQuery(
					"select titleAsId from Post order by published desc")
					.getResultList();
		}


	}

}
