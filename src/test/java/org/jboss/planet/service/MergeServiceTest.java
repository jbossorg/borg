package org.jboss.planet.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Logger;

import org.jboss.planet.model.Post;
import org.jboss.planet.model.PostStatus;
import org.jboss.planet.model.RemoteFeed;
import org.junit.Test;
import org.mockito.Mockito;

import junit.framework.Assert;

/**
 * @author Libor Krzyzanek
 */
public class MergeServiceTest {

    @Test
    public void handleDuplicatePosts() throws Exception {
        String duplicateAuthor = "Author";
        String duplicateTitle = "Title";

        Post p1 = new Post();
        p1.setAuthor(duplicateAuthor);
        p1.setTitle(duplicateTitle);
        p1.setPublished(new Date());
        p1.setModified(new Date());

        Post p2 = new Post();
        p2.setAuthor(duplicateAuthor);
        p2.setTitle(duplicateTitle);
        // intentionally different dates
        p2.setPublished(new Date());
        p2.setModified(new Date());

        MergeService tested = getTested();
        Mockito.when(tested.handleDuplicatePosts(Mockito.any(Post.class), Mockito.any(RemoteFeed.class))).thenCallRealMethod();
        Mockito.doNothing().when(tested).savePost(Mockito.any(RemoteFeed.class), Mockito.any(Post.class));

        // CASE: no duplicates
        {
            Mockito.when(tested.postService.find(duplicateAuthor, duplicateTitle)).thenReturn(new ArrayList<Post>(0));
            boolean result = tested.handleDuplicatePosts(p2, null);
            Assert.assertEquals(result, false);
            Assert.assertNotSame(PostStatus.MODERATION_REQUIRED, p2.getStatus());
        }

        // CASE: find duplicates
        {
            ArrayList<Post> posts = new ArrayList<>(2);
            posts.add(p1);
            posts.add(p2);

            Mockito.when(tested.postService.find(duplicateAuthor, duplicateTitle)).thenReturn(posts);
            boolean result = tested.handleDuplicatePosts(p2, null);
            Assert.assertEquals(result, true);
            Assert.assertEquals(PostStatus.MODERATION_REQUIRED, p2.getStatus());
        }


    }

    private MergeService getTested() {
        MergeService tested = Mockito.mock(MergeService.class);
        tested.log = Logger.getLogger("testlogger");
        tested.postService = Mockito.mock(PostService.class);
        return tested;
    }

}