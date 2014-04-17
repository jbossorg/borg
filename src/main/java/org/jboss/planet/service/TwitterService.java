package org.jboss.planet.service;

import org.jboss.planet.model.Configuration;
import org.jboss.planet.model.Post;
import org.jboss.planet.model.PostStatus;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.util.CharacterUtil;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.inject.Named;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Twitter Client Service
 *
 * @author Libor Krzyzanek
 */
@Named
@Stateless
public class TwitterService {

	@Inject
	private Logger log;

	@Inject
	private PostService postService;

	@Inject
	private ConfigurationService configurationService;

	@Inject
	private LinkService linkService;

	/**
	 * Default value for short URL
	 */
	public static final int TWITTER_SHORT_URL_LENGTH_DEFAULT = 22;

	public static final int TWITTER_STATUS_LENGTH = 140;

	public Twitter createTwitterClient() {
		Configuration conf = configurationService.getConfiguration();
		log.log(Level.FINEST, "Configuration during Twitter initialization: {0}", conf);

		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(false)
				.setOAuthConsumerKey(conf.getTwitterOAuthConsumerKey())
				.setOAuthConsumerSecret(conf.getTwitterOAuthConsumerSecret())
				.setOAuthAccessToken(conf.getTwitterOAuthAccessToken())
				.setOAuthAccessTokenSecret(conf.getTwitterOAuthAccessTokenSecret());

		TwitterFactory twitterFactory = new TwitterFactory(cb.build());
		return twitterFactory.getInstance();
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public boolean syncPost(int postId, Twitter twitter, int shortURLLength) {
		log.log(Level.FINE, "Sync Post to Twitter. Post id: {0}", postId);

		try {
			Post p = postService.find(postId);
			if (p == null) {
				// sometime occur - very weird why
				return false;
			}
			long tweetId = postToTwitter(p, twitter, shortURLLength);
			p.setTwitterStatusId(tweetId);
			p.setStatus(PostStatus.POSTED_TWITTER);

			postService.update(p, false);

			return true;
		} catch (Exception e) {
			log.log(Level.SEVERE, "Cannot push post with id: " + postId, e);
		}
		return false;
	}

	/**
	 * Post to twitter particular Blog post
	 *
	 * @param p              Blog post
	 * @param twitter        twitter client
	 * @param shortURLLength length of shorted URL (provided by Twitter)
	 * @return Tweet ID
	 * @throws TwitterException
	 */
	public long postToTwitter(Post p, Twitter twitter, int shortURLLength) throws TwitterException {
		String url = linkService.generatePostLink(p.getTitleAsId());
		String template = configurationService.getConfiguration().getTwitterText();

		String text = getStatusText(template, p.getTitle(), url, shortURLLength);
		log.log(Level.FINEST, "Twitter status text: {0}", text);

		Status status = twitter.updateStatus(text);
		log.log(Level.FINEST, "Twitter status response: {0}", status);

		return status.getId();
	}

	public String getStatusText(String template, String title, String url, int shortURLLength) {
		String msg = MessageFormat.format(template, title);
		if (CharacterUtil.count(msg) + shortURLLength > TWITTER_STATUS_LENGTH) {
			return msg.substring(0, TWITTER_STATUS_LENGTH - shortURLLength) + url;
		} else {
			return msg + url;
		}
	}
}
