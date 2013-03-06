/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.model;

import java.io.Serializable;

/**
 * 
 * @author Adam Warski (adam at warski dot org)
 * 
 */
public interface PostFilter extends Serializable {
	public boolean filter(Post post);
}
