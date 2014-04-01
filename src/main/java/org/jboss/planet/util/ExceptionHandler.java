/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.util;

import com.ocpsoft.pretty.PrettyContext;
import com.ocpsoft.pretty.PrettyException;
import org.jboss.planet.filter.CasLoginFilter;
import org.jboss.planet.security.PermissionException;
import org.jboss.planet.security.UserNotLoggedInException;

import javax.ejb.EJBException;
import javax.el.ELException;
import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Exception handler to handle business logic exceptions like {@link UserNotLoggedInException} etc.
 * 
 * @author Libor Krzyzanek
 */
public class ExceptionHandler extends ExceptionHandlerWrapper {

	private static final Logger log = Logger.getLogger(ExceptionHandler.class.getName());

	private final javax.faces.context.ExceptionHandler wrapped;

	public ExceptionHandler(final javax.faces.context.ExceptionHandler wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	public javax.faces.context.ExceptionHandler getWrapped() {
		return this.wrapped;
	}

	@Override
	public void handle() throws FacesException {
		for (final Iterator<ExceptionQueuedEvent> it = getUnhandledExceptionQueuedEvents().iterator(); it.hasNext();) {
			Throwable t = it.next().getContext().getException();
			while ((t instanceof ELException || t instanceof PrettyException || t instanceof FacesException || t instanceof EJBException)
					&& t.getCause() != null) {
				t = t.getCause();
			}
			final FacesContext facesContext = FacesContext.getCurrentInstance();
			final ExternalContext externalContext = facesContext.getExternalContext();
			final String contextPath = facesContext.getExternalContext().getRequestContextPath();

			String pageToRedirect = null;
			if (t instanceof UserNotLoggedInException) {
				// Could be redirected to /error/notloggedin but Login page it's obvious enough to say that user needs
				// to login
				final HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
				String originalUrl = PrettyContext.getCurrentInstance(request).getRequestURL().encode().toString();

				pageToRedirect = "/login?" + CasLoginFilter.BACK_URL_PARAM_NAME + "=" + contextPath + originalUrl;
			} else if (t instanceof PermissionException) {
				pageToRedirect = "/error/notauthorized";
			} else {
				// do not write whole stacktrace because it's probably already logged
				log.log(Level.SEVERE, "Unknown error: " + t.getMessage());
				facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Unknown error occurred. "
						+ t.getMessage(), null));
			}
			try {
				if (pageToRedirect != null) {
					externalContext.setResponseStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
					externalContext.redirect(contextPath + pageToRedirect);
				} else {
					externalContext.responseSendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unknown Error");
				}

			} catch (final IOException e) {
				log.log(Level.SEVERE, "Error view '" + pageToRedirect + "' page", e);
			} finally {
				it.remove();
			}
			facesContext.responseComplete();
		}
		getWrapped().handle();
	}
}
