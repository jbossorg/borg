Util = function() {
	this.dateToString = function(date) {
		return date.toDateString() + ", " + date.getHours() + ":" + date.getMinutes();
	};
	this.parseISODateString = function(t) {
		nativeParsedDate = new Date(t);
		if (typeof nativeParsedDate != "undefined") {
			return nativeParsedDate;
		}

		// Taken from http://stackoverflow.com/a/10181937
		// 2013-02-19T10:44:00.000+0100
		var dateRE = /(\d+)-(\d+)-(\d+)T(\d+):(\d+):(\d+).(\d+)([+\-]\d+)/;
		var match = t.match(dateRE);
		if (match) {
			var nums = [];
			for ( var i = 1; i < match.length; i++) {
				nums.push(parseInt(match[i], 10));
			}
			// [2013, 2, 19, 10, 44, 0, 0, 100]
			var d = new Date();
			var offset = (nums[7] / 100) + (d.getTimezoneOffset() / 60);
			var d = new Date(nums[0], nums[1] - 1, nums[2], nums[3] + offset, nums[4], nums[5], nums[6]);
			return d;
		}
	};
	this.parseEmail = function(fullEmail) {
		var emailRE = /([^<]+)\s<(.*)>/;
		match = fullEmail.match(emailRE);
		if (match) {
			return {
				name : match[1],
				email : match[2]
			};
		}
		return {
			name : fullEmail,
			email : null
		};
	};
};
var util = new Util();

Effects = function() {
	this.fadeInDuration = 500;

	this.fadeInByOpacity = function(elm, callback) {
		elm.css({
			"opacity" : "0",
			"display" : "block"
		});

		elm.animate({
			opacity : "+=" + "1",
			useTranslate3d : true,
			leaveTransforms : false
		}, this.fadeInDuration, callback);
	};
};
var effects = new Effects();

Post = function(val) {
	this.data = val;

	var previewElm = null;

	this.currentScrollTop = 0;

	this.getPreviewElm = function() {
		if (previewElm != null) {
			return previewElm;
		}
		var preview = '<li id="home-post-li-' + this.getId() + '" class="post-list-preview">';
		preview += '<a href="#content" class="post-link">'
		// + '<img src="' + this.data._source.avatar_link
		// + '" class="ui-li-thumb" height="46px" width="46px"/>'
		+ '<h2 class="ui-li-heading">' + this.data._source.sys_title + '</h2>'
				+ '<p class="ui-li-desc"><span class="blog-post-list-date">' + this.getPublishedAndAuthor()
				+ '</span><br/>' + this.data._source.sys_description + '</p></a></li>';
		previewElm = $(preview);

		return previewElm;
	};

	this.getId = function() {
		return this.data._id;
	};

	this.getContent = function() {
		return this.data._source.sys_content;
	};

	this.getTitle = function() {
		return this.data._source.sys_title;
	};

	this.getPublished = function() {
		return util.parseISODateString(this.data._source.sys_created);
	};

	this.getPublishedAndAuthor = function() {
		return util.dateToString(this.getPublished()) + ', by ' + this.data._source.author;
	};

};

Navigator = function() {

	var homeUpdated = "planet.reader.home.updated";
	var currentPostsKey = "planet.reader.currentPosts";
	var previewPostKey = "planet.reader.previewpost.id";
	var postKeyPrefix = "planet.reader.post.";

	var postObjectPool = {};
	var previousPreviewPostId = null;

	this.reset = function() {
		while (localStorage.length) {
			localStorage.removeItem(localStorage.key(0));
		}
	};

	this.getHomeUpdated = function() {
		return getValue(homeUpdated);
	};

	this.setHomeUpdated = function(updated) {
		setValue(homeUpdated, updated);
	};

	var currPosts = null;

	this.getCurrentPosts = function() {
		if (currPosts == null) {
			currPosts = getValueArray(currentPostsKey);
		}
		return currPosts;
	};

	this.setCurrentPosts = function(postIds) {
		currPosts = postIds;
		setValue(currentPostsKey, postIds);
	};

	var getPreviewPostId = function() {
		return getValue(previewPostKey);
	};

	this.getPreviewPostId = getPreviewPostId;

	this.setPreviewPostId = function(postId) {
		previousPreviewPostId = getPreviewPostId();
		setValue(previewPostKey, postId);
	};

	this.getPreviousPreviewPostId = function() {
		return previousPreviewPostId;
	};

	this.getPost = function(id) {
		return getValueJSON(postKeyPrefix + id);
	};

	this.getPostObject = function(id) {
		if (postObjectPool[id] == null) {
			postObjectPool[id] = new Post(getPost(id));
		}
		return postObjectPool[id];
	};

	this.setPostObject = function(id, post) {
		postObjectPool[id] = post;
		setValueJSON(postKeyPrefix + id, post.data);
	};

	this.getNextPostId = function() {
		var index = this.getCurrentPosts().indexOf(getPreviewPostId());
		if (index == this.getCurrentPosts().length) {
			return null;
		}
		return this.getCurrentPosts()[index + 1];
	};

	this.getPreviousPostId = function() {
		var index = this.getCurrentPosts().indexOf(getPreviewPostId());
		if (index == 0) {
			return null;
		}
		return this.getCurrentPosts()[index - 1];
	};

	// Helper methods for setting value to key-value store like localStorage or can be memory based storage

	var setValue = function(id, value) {
		localStorage[id] = value;
	};

	var setValueJSON = function(id, value) {
		localStorage[id] = JSON.stringify(value);
	};

	var getValue = function(id) {
		if (localStorage[id] == null || localStorage[id] == "null") {
			return null;
		}
		return localStorage[id];
	};

	var getValueJSON = function(id) {
		if (getValue(id) != null) {
			return JSON.parse(getValue(id));
		} else {
			return null;
		}
	};

	var getValueArray = function(id) {
		if (getValue(id) != null) {
			return getValue(id).split(",");
		} else {
			return null;
		}
	};

};

Home = function() {
	var header = null;
	var page = null;
	var pageAnimationElm = null;
	var currentFrom = 0;
	var count = 20;
	// var feedId = null;
	var retrievingNewPosts = false;
	var visible = true;

	this.init = function() {
		header = $("#header-home");
		page = $("#home");
		pageAnimationElm = $("#elm-to-animate", page);

		$("#refresh", header).bind('click', function() {
			home.refresh();
			return false;
		});

		$(window).bind('scroll', function() {
			home.scrollFired();
		});
	};

	this.show = function(scrollAfterFadeIn) {
		visible = true;

		header.show();
		pageAnimationElm.css({
			"opacity" : "0",
			"display" : "block"
		});
		page.show();

		if (scrollAfterFadeIn) {
			effects.fadeInByOpacity(pageAnimationElm, function() {
				scrollToActivePost();
			});
		} else {
			effects.fadeInByOpacity(pageAnimationElm);
			scrollToActivePost();
		}
	};

	var getPostElm = function(id) {
		return $("#home-post-li-" + id, page);
	};

	/** Find which post was the last shown and scroll to have it in the middle */
	var scrollToActivePost = function() {
		var id = nav.getPreviewPostId();
		if (id != null) {
			var postElm = getPostElm(id);
			$(window).scrollTop(postElm.offset().top - ($(window).height() / 2) + (postElm.height() / 2));
		}
	};

	this.hide = function(callback) {
		visible = false;

		pageAnimationElm.animate({
			left : "-=" + $(document).width() + "px",
			useTranslate3d : true,
			leaveTransforms : false
		}, reader.effect.slideDuration, function() {
			page.hide();
			header.hide();
			callback();
		});
	};

	this.refresh = function() {
		nav.reset();
		currentFrom = 0;
		$("#posts_list", page).empty();
		this.retrievePosts(false);
	};

	this.restoreState = function() {
		if (nav.getCurrentPosts() == null) {
			return false;
		}
		var ids = nav.getCurrentPosts();
		var posts = new Array();
		for ( var i = 0; i < ids.length; i++) {
			var post = nav.getPost(ids[i]);
			if (post == null) {
				return false;
			}
			posts[i] = post;
		}

		currentFrom = ids.length;

		data = {
			"hits" : {
				"hits" : posts
			}
		};

		home.updatePostsList(data, false);

		if (nav.getPreviewPostId() != null) {
			activatePost(nav.getPreviewPostId());
		}

		return true;
	};

	this.retrievePosts = function(history, callback) {
		$("#loading", page).text("Loading ...");
		retrievingNewPosts = true;
		var url = reader.dcpRestApi + "search?sys_type=blogpost&from=" + currentFrom + "&size=" + count
				+ "&sortBy=new&field=_source";

		$.ajax({
			url : url,
			type : "get",
			dataType : 'json',
			success : function(data) {
				if (!history) {
					nav.setHomeUpdated(util.dateToString(new Date()));
				}
				home.updatePostsList(data, history);
				currentFrom += count;
				retrievingNewPosts = false;
				$("#loading", page).empty();
				if (callback) {
					callback();
				}
			}
		});

	};

	this.updatePostsList = function(data, history) {
		var postsList = $("#posts_list", page);
		if (!history) {
			postsList.empty();
			nav.setCurrentPosts(null);
		}
		var currentPostIds = nav.getCurrentPosts();
		if (currentPostIds == null) {
			currentPostIds = new Array();
		}

		$.each(data.hits.hits, function(key, val) {
			var p = new Post(val);
			nav.setPostObject(p.getId(), p);
			var entryElm = p.getPreviewElm();

			var links = $("a.post-link", entryElm);
			links.unbind('click');
			links.bind("click", function() {
				reader.showContentPage(p);
				return false;
			});

			postsList.append(entryElm);
			currentPostIds.push(p.getId());
		});
		$("#updated").text(nav.getHomeUpdated());
		nav.setCurrentPosts(currentPostIds);

	};

	var activatePost = function(postId) {
		prevPostId = nav.getPreviousPreviewPostId();
		if (prevPostId != null) {
			getPostElm(prevPostId).removeClass("active");
		}
		getPostElm(postId).addClass("active");
	};

	this.activatePost = activatePost;

	this.scrollFired = function() {
		if (!visible) {
			return;
		}
		if (!retrievingNewPosts) {
			var homeWrapper = page;
			if (($(window).height() + $(window).scrollTop() + 100) >= (homeWrapper.offset().top + homeWrapper.height())) {
				this.retrievePosts(true);
			}
		}
	};

};

Preview = function() {
	var header = null;
	var page = null;
	var pageAnimationElm = null;
	var previewElm = null;
	var titleElm = null;
	var publishedElm = null;
	var currentPostObject = null;

	this.init = function() {
		header = $("#header-preview");
		page = $("#preview");
		pageAnimationElm = $("#elm-to-animate", page);
		previewElm = $("#postpreview", page);
		titleElm = $("#title", page);
		publishedElm = $("#published", page);

		$("#back", header).bind('click', function() {
			back();
			return false;
		});
		$("#next", header).bind('click', function() {
			showNextPost();
			return false;
		});
		$("#previous", header).bind('click', function() {
			showPrevPost();
			return false;
		});

		pageAnimationElm.hammer({
			"drag" : false,
			"transform" : false,
			"tap" : false,
			"tap_double" : false,
			"hold" : false,
			"swipe_min_distance" : 50
		}).bind("swipe", function(ev) {
			if (ev.direction == "left") {
				showNextPost();
			} else {
				showPrevPost();
			}
		});
	};

	var updateCurrentPostScrollTop = function() {
		currentPostObject.currentScrollTop = $(window).scrollTop();
	};

	var showNextPost = function() {
		updateCurrentPostScrollTop();
		pageAnimationElm.animate({
			left : "-=" + $(document).width() + "px",
			useTranslate3d : true,
			leaveTransforms : false
		}, reader.effect.slideDuration, function() {
			var postId = nav.getNextPostId();
			if (postId != null) {
				var post = nav.getPostObject(postId);
				setPost(post);
			} else {
				home.retrievePosts(true, function() {
					var postId = nav.getNextPostId();
					var post = nav.getPostObject(postId);
					setPost(post);
				});
			}
			pageAnimationElm.hide();
			pageAnimationElm.fadeIn(reader.effect.fadeInDuration);
			$(window).scrollTop(currentPostObject.currentScrollTop);
		});
	};

	var showPrevPost = function() {
		updateCurrentPostScrollTop();
		var postId = nav.getPreviousPostId();
		if (postId == null) {
			pageAnimationElm.animate({
				left : "+=" + $(document).width() / 6 + "px",
				useTranslate3d : true,
				leaveTransforms : false
			}, reader.effect.slideDuration, function() {
				$(this).animate({
					left : "-=" + $(document).width() / 6 + "px",
					useTranslate3d : true,
					leaveTransforms : false
				}, reader.effect.slideDuration, function() {
				});
			});
			return;
		} else {
			pageAnimationElm.animate({
				left : "+=" + $(document).width() + "px",
				useTranslate3d : true,
				leaveTransforms : false
			}, reader.effect.slideDuration, function() {
				var postId = nav.getPreviousPostId();
				var post = nav.getPostObject(postId);
				setPost(post);

				pageAnimationElm.hide();
				pageAnimationElm.fadeIn(reader.effect.fadeInDuration);
				$(window).scrollTop(currentPostObject.currentScrollTop);
			});
		}
	};

	this.show = function() {
		header.show();
		$(window).scrollTop(0);
		pageAnimationElm.hide();
		page.show();
		pageAnimationElm.fadeIn(reader.effect.fadeInDuration);
	};
	var hide = function(immediate, callback) {
		if (immediate) {
			header.hide();
			page.hide();
		} else {
			pageAnimationElm.animate({
				left : "+=" + $(document).width() + "px",
				useTranslate3d : true,
				leaveTransforms : false
			}, reader.effect.slideDuration, function() {
				page.hide();
				header.hide();
				callback();
			});
		}
	};
	this.hide = hide;

	var back = function() {
		updateCurrentPostScrollTop();
		hide(false, home.show);
	};
	this.back = back;

	var setPost = function(p) {
		nav.setPreviewPostId(p.getId());
		// Must be called after setPreviewPostId because it needs previous postId
		home.activatePost(p.getId());

		currentPostObject = p;
		previewElm.empty();
		previewElm.append(p.getContent());
		titleElm.empty();
		titleElm.append(p.getTitle());
		publishedElm.empty();
		publishedElm.append(p.getPublishedAndAuthor());

		$(window).scrollTop(p.currentScrollTop);
	};
	this.setPost = setPost;
};

/**
 * Global variables for pages
 */
var nav = new Navigator();
var home = new Home();
var preview = new Preview();

/**
 * 
 */
var reader = {
	dcpRestApi : syncServer + "/v1/rest/",
	effect : {
		fadeInDuration : 400,
		slideDuration : 400
	},

	init : function() {
		home.init();
		preview.init();
		preview.hide(true);

		var stateRestored = home.restoreState();

		if (!stateRestored) {
			home.refresh();
		}
		home.show(true);
	},

	showContentPage : function(p) {
		home.hide(function() {
			preview.setPost(p);
			preview.show();
		});
	},

};

$(document).ready(reader.init);
