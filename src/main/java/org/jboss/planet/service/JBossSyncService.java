/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.service;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.planet.model.Post;
import org.jboss.planet.model.PostStatus;

/**
 * Syncing service with JBoss backend. Wrapper for {@link JBossSyncAdapter} and {@link PostService}
 *
 * @author Libor Krzyzanek
 */
@Named
@Singleton
@Lock(LockType.READ)
public class JBossSyncService {

    @Inject
    private Logger log;

    @Inject
    private PostService postService;

    @Inject
    protected JBossSyncAdapter jBossSyncAdapter;

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public boolean syncPost(int postId, PostStatus newStatus) {
        log.log(Level.FINE, "Sync Post to jboss.org. Post id: {0}", postId);

        try {
            Post p = postService.find(postId);
            if (p == null) {
                // sometime occur - very weird why
                return false;
            }
            jBossSyncAdapter.pushPostToDcp(p);

            p.setStatus(newStatus);
            postService.update(p, false);

            return true;
        } catch (Exception e) {
            log.log(Level.SEVERE, "Cannot push post with id: " + postId, e);
        }
        return false;
    }

    /**
     * Delete post from JBoss back-end
     *
     * @param postTitleAsId
     * @throws IOException
     */
    public void deletePost(String postTitleAsId) throws IOException {
        jBossSyncAdapter.deletePostInDcp(postTitleAsId);
    }

}
