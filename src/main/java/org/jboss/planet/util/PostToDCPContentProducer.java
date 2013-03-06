/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.util;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.entity.ContentProducer;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.jboss.planet.model.Category;
import org.jboss.planet.model.Post;

/**
 * Class produces JSON from {@link Post} which is consumed by DCP
 * 
 * @author Libor Krzyzanek
 * 
 */
public class PostToDCPContentProducer implements ContentProducer {

	private Post p;

	private JsonEncoding encoding;

	public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

	public PostToDCPContentProducer(Post p, JsonEncoding encoding) {
		this.p = p;
		this.encoding = encoding;
	}

	public JsonEncoding getEncoding() {
		return encoding;
	}

	public String getMimeType() {
		return "application/json";
	}

	public String dateToString(Date d) {
		if (d == null) {
			return "";
		}
		SimpleDateFormat dt = new SimpleDateFormat(DATE_FORMAT);
		return dt.format(d);
	}

	@Override
	public void writeTo(OutputStream outstream) throws IOException {
		JsonFactory f = new JsonFactory();
		JsonGenerator g = f.createJsonGenerator(outstream, encoding);

		g.writeStartObject();
		g.writeStringField("author", p.getEffectiveAuthor());
		g.writeStringField("avatar_link", p.getEffectiveAvatarLink());
		String published = dateToString(p.getPublished());
		g.writeStringField("dcp_created", published);
		String modified = dateToString(p.getModified());
		g.writeStringField("modified", modified);

		g.writeStringField("feed", p.getFeed().getName());

		if (p.getContentPreview() == null) {
			p.setContentPreview(StringTools.createSummary(p.getContent(), 400));
		}
		g.writeStringField("dcp_description", p.getContentPreview());

		g.writeStringField("dcp_content", p.getContent());

		g.writeStringField("dcp_title", p.getTitle());
		g.writeStringField("dcp_url_view", p.getLink());

		g.writeFieldName("dcp_activity_dates");
		g.writeStartArray();
		if (p.getModified() != null) {
			g.writeString(modified);
		}
		g.writeString(published);
		g.writeEndArray();

		g.writeFieldName("tags");
		g.writeStartArray();

		for (Category cat : p.getCategories()) {
			g.writeString(cat.getName());
		}

		// Add special tag telling name of feed and name of group
		g.writeString("feed_name_" + p.getFeed().getName());
		g.writeString("feed_group_name_" + p.getFeed().getGroup().getName());

		g.writeEndArray();

		g.writeEndObject();
		g.close();
	}
}
