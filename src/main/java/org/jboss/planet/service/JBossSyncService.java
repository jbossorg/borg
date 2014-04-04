/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.service;

import org.apache.http.Consts;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.jboss.planet.model.Configuration;
import org.jboss.planet.model.Post;
import org.jboss.planet.model.PostStatus;
import org.jboss.planet.util.PostToDCPContentProducer;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JBoss back-end service.<br/>
 * Documentation: http://docs.jbossorg.apiary.io/
 *
 * @author Libor Krzyzanek
 */
@Named
@Stateless
public class JBossSyncService {

	@Inject
	private Logger log;

	@Inject
	private PostService postService;

	@Inject
	private EntityManager em;

	@Inject
	private ConfigurationService configurationService;

	public static final String SYNC_REST_API = "/v1/rest/content/";

	protected DefaultHttpClient createHttpClient() {
		DefaultHttpClient httpClient = new DefaultHttpClient();

		httpClient.getCredentialsProvider().setCredentials(
				new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
				new UsernamePasswordCredentials(configurationService.getConfiguration().getSyncUsername(),
						configurationService.getConfiguration().getSyncPassword())
		);
		return httpClient;
	}
	protected void shutdownHttpClient(DefaultHttpClient httpClient) {
		httpClient.getConnectionManager().shutdown();
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public boolean syncPost(int postId, HttpClient httpClient) {
		log.log(Level.FINE, "Sync Post to jboss.org. Post id: {0}", postId);

		try {
			Post p = postService.find(postId);
			if (p == null) {
				// sometime occur - very weird why
				return false;
			}
			pushToJBoss(p, httpClient);
			p.setStatus(PostStatus.SYNCED);
			postService.update(p, false);

			return true;
		} catch (Exception e) {
			log.log(Level.SEVERE, "Cannot push post with id: " + postId, e);
		}
		return false;
	}

	public void syncAllPosts() {
		DefaultHttpClient httpClient = new DefaultHttpClient();

		List<Post> allPosts = postService.findAll();
		for (Post post : allPosts) {
			syncPost(post.getId(), httpClient);
		}
		shutdownHttpClient(httpClient);
	}

	/**
	 * Push content to JBoss back-end
	 *
	 * @param p
	 */
	private void pushToJBoss(final Post p, HttpClient httpClient) throws JsonGenerationException, JsonMappingException,
			IOException, HttpResponseException {
		Configuration c = configurationService.getConfiguration();
		String syncApiURL = c.getSyncServer() + SYNC_REST_API + c.getSyncContentType() + "/" + p.getTitleAsId();

		HttpPost httpPost = new HttpPost(syncApiURL);

		PostToDCPContentProducer producer = new PostToDCPContentProducer(p, JsonEncoding.UTF8);

		EntityTemplate entityTemplate = new EntityTemplate(producer);

		ContentType httpContentType = ContentType.create(producer.getMimeType(), Consts.UTF_8);
		entityTemplate.setContentType(httpContentType.toString());
		httpPost.setEntity(entityTemplate);

		httpClient.execute(httpPost, new BasicResponseHandler());
	}

	/**
	 * Delete post from JBoss back-end
	 *
	 * @param postTitleAsId
	 * @param httpClient
	 * @throws IOException
	 */
	public void deletePost(String postTitleAsId, HttpClient httpClient) throws IOException {
		log.log(Level.INFO, "Delete post {0} from DCP", postTitleAsId);

		Configuration c = configurationService.getConfiguration();
		// Documentation: http://docs.jbossorg.apiary.io/
		String syncApiURL = c.getSyncServer() + SYNC_REST_API + c.getSyncContentType() + "/" + postTitleAsId
				+ "?ignore_missing=true";

		HttpDelete httpDelete = new HttpDelete(syncApiURL);
		httpClient.execute(httpDelete, new BasicResponseHandler());
	}

	/**
	 * Delete post with auto create/destroy http client
	 *
	 * @param postTitleAsId
	 * @see #createHttpClient()
	 */
	public void deletePost(String postTitleAsId) throws IOException {
		DefaultHttpClient httpClient = createHttpClient();

		deletePost(postTitleAsId, httpClient);

		shutdownHttpClient(httpClient);
	}

}
