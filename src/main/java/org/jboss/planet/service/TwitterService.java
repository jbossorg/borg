package org.jboss.planet.service;

import org.jboss.planet.model.Configuration;
import org.jboss.planet.model.Post;
import org.jboss.planet.model.PostStatus;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

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
	private GlobalConfigurationService globalConfigurationService;

	public void resetTwitterClient() {
		Configuration conf = configurationService.getConfiguration();
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(false)
				.setOAuthConsumerKey(conf.getTwitterOAuthConsumerKey())
				.setOAuthConsumerSecret(conf.getTwitterOAuthConsumerSecret())
				.setOAuthAccessToken(conf.getTwitterOAuthAccessToken())
				.setOAuthAccessTokenSecret(conf.getTwitterOAuthAccessTokenSecret());

		new TwitterFactory(cb.build());
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public boolean syncPost(int postId) {
		log.log(Level.FINE, "Sync Post to Twitter. Post id: {0}", postId);

		try {
			Post p = postService.find(postId);
			if (p == null) {
				// sometime occur - very weird why
				return false;
			}
			postToTwitter(p);
			p.setStatus(PostStatus.POSTED_TWITTER);
			postService.update(p, false);

			return true;
		} catch (Exception e) {
			log.log(Level.SEVERE, "Cannot push post with id: " + postId, e);
		}
		return false;
	}

	public void postToTwitter(Post p) throws TwitterException {
		Twitter twitter = TwitterFactory.getSingleton();

		String text = getStatusText(p);
		log.log(Level.FINEST, "Twitter status text: {0}", text);

		twitter.updateStatus(text);
	}

	public String getStatusText(Post p) {
		String url = globalConfigurationService.getAppUrl() + "/post/" + p.getTitleAsId();
		return MessageFormat.format(configurationService.getConfiguration().getTwitterText(), p.getTitle(), p.getAuthor(), url);
	}
}
