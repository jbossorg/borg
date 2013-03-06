/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.util;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.codehaus.jackson.JsonEncoding;
import org.jboss.planet.model.Category;
import org.jboss.planet.model.FeedGroup;
import org.jboss.planet.model.Post;
import org.jboss.planet.model.PostAuthorType;
import org.jboss.planet.model.RemoteFeed;
import org.junit.Test;

public class PostToDCPContentProducerTest {

	@Test
	public void testWriteTo() throws IOException, ParseException {
		FeedGroup group = new FeedGroup();
		group.setName("GroupName");

		RemoteFeed feed = new RemoteFeed();
		feed.setGroup(group);
		feed.setPostAuthorType(PostAuthorType.POST_AUTHOR);
		feed.setAuthorAvatarLink("https://community.jboss.org/people/test/avatar/46.png");
		feed.setName("FeedName");

		SimpleDateFormat df = new SimpleDateFormat(PostToDCPContentProducer.DATE_FORMAT);
		Date d = new Date();
		String dateStr = df.format(d);

		Post p = new Post();
		p.setAuthor("Post Author");
		p.setPublished(d);
		p.setModified(d);
		p.setTitle("Post Title");
		p.setContent("Post Content");
		p.setContentPreview("Post C...");
		p.setLink("http://jboss.org/post");

		p.setFeed(feed);

		List<Category> categories = new ArrayList<Category>();
		categories.add(new Category("tag1"));
		p.setCategories(categories);

		ByteArrayOutputStream outstream = new ByteArrayOutputStream();

		PostToDCPContentProducer producer = new PostToDCPContentProducer(p, JsonEncoding.UTF8);
		producer.writeTo(outstream);

		assertEquals("{" + "\"author\":\"Post Author\""
				+ ",\"avatar_link\":\"https://community.jboss.org/people/test/avatar/46.png\"" + ",\"dcp_created\":\""
				+ dateStr + "\",\"modified\":\"" + dateStr + "\",\"feed\":\"FeedName\""
				+ ",\"dcp_description\":\"Post C...\"" + ",\"dcp_content\":\"Post Content\""
				+ ",\"dcp_title\":\"Post Title\"" + ",\"dcp_url_view\":\"http://jboss.org/post\""
				+ ",\"dcp_activity_dates\":[\"" + dateStr + "\",\"" + dateStr
				+ "\"],\"tags\":[\"tag1\",\"feed_name_FeedName\",\"feed_group_name_GroupName\"]" + "}",
				outstream.toString(producer.getEncoding().getJavaName()));

	}
}
