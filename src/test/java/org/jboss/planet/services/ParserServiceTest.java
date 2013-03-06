/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.services;

import static org.junit.Assert.assertEquals;

import org.jboss.planet.exception.ParserException;
import org.jboss.planet.model.Post;
import org.jboss.planet.model.RemoteFeed;
import org.jboss.planet.service.ParserService;
import org.junit.Test;

public class ParserServiceTest {

	@Test
	public void testParse() throws ParserException {
		ParserService parserService = new ParserService();

		RemoteFeed feed = parserService.parse(ParserServiceTest.class.getResourceAsStream("/posts.txt"));
		assertEquals("Weekly Editorial", feed.getTitle());
		assertEquals(null, feed.getAuthor());

		Post p = feed.getPosts().get(0);
		assertEquals("Kevin Conner", p.getAuthor());
		assertEquals("Summary of post no. 1", p.getContent());

		RemoteFeed feed2 = parserService.parse(ParserServiceTest.class.getResourceAsStream("/posts-special-chars.txt"));
		Post p2 = feed2.getPosts().get(0);
		assertEquals(
				"<p class=\"wikiPara\">本文是<a href=\"http://in.relation.to/Bloggers/HibernateOGMNOSQL\">上文</a>的延续.</p>",
				p2.getContent());

	}

}
