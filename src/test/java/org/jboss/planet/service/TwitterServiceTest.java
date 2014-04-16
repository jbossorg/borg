package org.jboss.planet.service;

import junit.framework.Assert;
import org.junit.Test;

/**
 * @author Libor Krzyzanek
 */
public class TwitterServiceTest {

	@Test
	public void testGetStatusText() throws Exception {
		TwitterService service = new TwitterService();
 		// OK title
		Assert.assertEquals(
				"Get Post title http://planet.jboss.org/post/title",
				service.getStatusText("Get Post {0} ", "title", "http://planet.jboss.org/post/title", TwitterService.TWITTER_SHORT_URL_LENGTH_DEFAULT));
		// very long title
		Assert.assertEquals(
				"1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678http://planet.jboss.org/post/title",
				service.getStatusText("{0}", "12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890",
						"http://planet.jboss.org/post/title", TwitterService.TWITTER_SHORT_URL_LENGTH_DEFAULT)
		);
		// maximal length 140 characters = 118 long title + 22
		Assert.assertEquals(
				"1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678http://planet.jboss.org/post/title",
				service.getStatusText("{0}", "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678",
						"http://planet.jboss.org/post/title", TwitterService.TWITTER_SHORT_URL_LENGTH_DEFAULT)
		);
	}
}
