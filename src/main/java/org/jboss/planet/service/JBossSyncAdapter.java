/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.http.Consts;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.jboss.planet.model.Configuration;
import org.jboss.planet.model.Post;
import org.jboss.planet.util.PostToDCPContentProducer;

/**
 * Adapter for JBoss back-end service.<br/>
 * Documentation: http://docs.jbossorg.apiary.io/
 *
 * @author Libor Krzyzanek
 */
@Singleton
@Named
@Lock(LockType.READ)
public class JBossSyncAdapter {

    @Inject
    private Logger log;

    @Inject
    private ConfigurationService configurationService;

    public static final String SYNC_REST_API = "/rest/content/";

    private CloseableHttpClient httpClient = null;

    private HttpContext localContext = null;

    @PostConstruct
    public void init() {
        //https://hc.apache.org/httpcomponents-client-4.3.x/httpclient/examples/org/apache/http/examples/client/ClientMultiThreadedExecution.java
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(50);
        cm.setDefaultMaxPerRoute(10);

        Configuration cfg = configurationService.getConfiguration();
        HttpHost syncHost;
        try {
            syncHost = URIUtils.extractHost(new URI(cfg.getSyncServer()));
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid rest api url" + configurationService.getConfiguration(), e);
        }

        // Don't use cookies
        RequestConfig globalConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
                .build();

        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
                new AuthScope(syncHost.getHostName(), AuthScope.ANY_PORT),
                new UsernamePasswordCredentials(cfg.getSyncUsername(), cfg.getSyncPassword()));

        this.localContext = createPreemptiveAuthContext(syncHost);

        this.httpClient = HttpClients.custom()
                .useSystemProperties()
                .setDefaultRequestConfig(globalConfig)
                .setConnectionManager(cm)
                .setDefaultCredentialsProvider(credentialsProvider)
                .build();
        log.log(Level.INFO, "New http client for DCP sync created.");
    }

    protected HttpContext createPreemptiveAuthContext(HttpHost targetHost) {
        // Create AuthCache instance
        AuthCache authCache = new BasicAuthCache();
        // Generate BASIC scheme object and add it to the local auth cache
        BasicScheme basicAuth = new BasicScheme();
        authCache.put(targetHost, basicAuth);

        // Add AuthCache to the execution context
        BasicHttpContext localcontext = new BasicHttpContext();
        localcontext.setAttribute(HttpClientContext.AUTH_CACHE, authCache);

        return localcontext;
    }

    @PreDestroy
    public void close() {
        if (httpClient != null) {
            try {
                httpClient.close();
            } catch (IOException e) {
                log.log(Level.SEVERE, "Cannot close httpclient", e);
            }
        }
    }

    /**
     * Push content to JBoss back-end
     *
     * @param p
     */
    public void pushPostToDcp(final Post p) throws JsonGenerationException, JsonMappingException,
            IOException, HttpResponseException {
        Configuration c = configurationService.getConfiguration();
        String syncApiURL = c.getSyncServer() + SYNC_REST_API + c.getSyncContentType() + "/" + p.getTitleAsId();

        HttpPost httpPost = new HttpPost(syncApiURL);

        PostToDCPContentProducer producer = new PostToDCPContentProducer(p, JsonEncoding.UTF8);

        EntityTemplate entityTemplate = new EntityTemplate(producer);

        ContentType httpContentType = ContentType.create(producer.getMimeType(), Consts.UTF_8);
        entityTemplate.setContentType(httpContentType.toString());
        httpPost.setEntity(entityTemplate);

        httpClient.execute(httpPost, new BasicResponseHandler(), localContext);
    }

    /**
     * Delete post from JBoss back-end
     *
     * @param postTitleAsId
     * @throws IOException
     */
    public void deletePostInDcp(String postTitleAsId) throws IOException {
        log.log(Level.INFO, "Delete post {0} from DCP", postTitleAsId);

        Configuration c = configurationService.getConfiguration();
        // Documentation: http://docs.jbossorg.apiary.io/
        String syncApiURL = c.getSyncServer() + SYNC_REST_API + c.getSyncContentType() + "/" + postTitleAsId
                + "?ignore_missing=true";

        HttpDelete httpDelete = new HttpDelete(syncApiURL);
        httpClient.execute(httpDelete, new BasicResponseHandler(), localContext);
    }

}
