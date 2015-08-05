/** **************** Application JS ****************** */

/* Common variables for third party plugins */

Util = function() {
	this.dateToString = function(date) {
		return date.toDateString();
	};
	this.parseISODateString = function(t) {
		var nativeParsedDate = new Date(t);
		if (typeof nativeParsedDate != "undefined" && !isNaN(nativeParsedDate.getTime())) {
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
	this.getHashParam = function(key) {
		var value = $.deparam.fragment()[key];
		if (value) {
			return value;
		} else {
			return null;
		}
	};
	this.parseEmail = function(fullEmail) {
		var val = util.arrayToString(fullEmail);

		var emailRE = /([^<]+)\s<(.*)>/;
		var match = val.match(emailRE);
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
	this.stringStartWith = function(str, startWithStr) {
		return str.lastIndexOf(startWithStr) == 0;
	};
	this.stringToArray = function(str) {
		if (util.isEmpty(str)) {
			return [];
		}
		if ($.isArray(str)) {
			return str;
		} else {
			return $.map(str.split(","), $.trim);
		}
	};
	this.isEmpty = function(str) {
		return (!str || 0 === str.length);
	};
	this.arrayToString = function(array) {
		if (array == null) {
			return "";
		}
		if ($.isArray(array)) {
			var ret = "";
			for ( var i = 0; i < array.length; i++) {
				ret += $.trim(array[i]);
				if (i < (array.length - 1)) {
					ret += ", ";
				}
			}
			return ret;
		} else {
			return array;
		}
	};
};
var util = new Util();

/* Configuration of localStorage usage */
localStorageInfo = {
	displayLayout : "planet.displayLayout",
	navigationSearchUrls : "planet.navigation.search.urls",

	supportCheck : null,

	/* Checks if local storage is supported */
	isSupported : function() {
		if (localStorageInfo.supportCheck == null) {
			try {
				var uid = new Date();
				localStorage.setItem(uid, uid);
				var result = localStorage.getItem(uid) == uid;
				localStorage.removeItem(uid);
				localStorageInfo.supportCheck = result;
			} catch (e) {
				localStorageInfo.supportCheck = false;
			}

		}
		return localStorageInfo.supportCheck;
	}
};

/*
 * Blog post object. Can be initialized based on val or by getPost method
 */
Post = function(val, format) {

	this.data = val;

	this.displayFormat = format;

	var fields = ["sys_content_id", "sys_url_view", "sys_created", "sys_title", "sys_project", "sys_description", "sys_contributors", "sys_content", "sys_tags", "avatar_link"];

	/* Get post based on ID */
	this.getPost = function(id, callback) {
		var url = planet.dcpRestApi + "content/" + planet.dcpContentType + "/" + id;
		for (var i = 0; i < fields.length; i++) {
			if (i == 0) {
				url += "?field=" + fields[i];
			} else {
				url += "&field=" + fields[i];
			}
		}
		$.ajax(url)
			.done(function(data) {
				callback(data, true);
			})
			.fail(function() {
				callback({}, false);
			});
	};

	this.previewElm = null;

	this.getPreviewElm = function() {
		if (this.previewElm != null) {
			return this.previewElm;
		}
		var preview = '<article id="home-post-li-' + this.data._id + '" class="blog-post-article">';
		var addThisTempl = planet.addThisTemplate(this.data.fields.sys_url_view, this.data.fields.sys_title);
		var projectName = planet.getProjectName(this.data.fields.sys_project);
		var projectInfo = "";
		if (projectName != "") {
			projectInfo = 'in <a href="#projects=' + this.data.fields.sys_project + '">' + projectName + '</a>';
		}
		var tags = this.getTagsRow();
		var originalLink = this.data.fields.sys_url_view;
		var originalLinkText = '<i class="fa fa-external-link-square"></i> Original Post';
		var permanentLink = planet.resourcesPrefix + 'post/' + this.data.fields.sys_content_id;
		var permanentLinkText = '<i class="fa fa-bookmark-o"></i> Permanent Link';

		preview += '<header><h3><a class="post-title-link" href="' + permanentLink + '" data-id="'
				+ this.data._id + '">' + this.data.fields.sys_title + '</a></h3>'
				+ '<div class="blog-post-header-info row collapse"><div class="small-4 large-3 columns"><img src="' + this.getAuthorAvatarUrl()
				+ '" height="80px" width="80px"/></div>'
				+ '<div class="small-17 columns">' + util.dateToString(this.getPublished()) + '<br/>by '
				+ this.getAuthor().name
				+ '<br/>' + projectInfo + '</div>'
				+ '<div class="small-3 large-4 columns">' + addThisTempl + '</div>' + '</div></header>';

		if (this.displayFormat == 1) {
			preview += '<div class="blog-post-content">'
				+ this.data.fields.sys_description
				+ '</div>'
				+ '<footer><div class="blog-post-tags"><h5>'
				+ tags
				+ '</h5></div>'
				+ '<div class="blog-post-show-more"><a href="" class="show-more button blue">Read more</a></div>'
				+ '<div class="home-post-perm-link" style="display: none">'
				+ '<a href="' + permanentLink + '">' + permanentLinkText + '</a>'
				+ '<a href="' + originalLink + '">' + originalLinkText + '</a></div></footer>';
		} else {
			preview += '<div class="blog-post-content">' + this.data.fields.sys_content + '</div>'
				+ '<footer><div class="blog-post-tags"><h5>' + tags + '</h5></div>'
				+ '<div class="home-post-perm-link">'
				+ '<a href="' + permanentLink + '">' + permanentLinkText + '</a>'
				+ '<a href="' + originalLink + '">' + originalLinkText + '</a></div></footer>';
		}

		preview += '</article>';
		this.previewElm = $(preview);

		return this.previewElm;
	};

	this.getTagsRow = function() {
		var tags = "";
		if (this.data.fields.sys_tags == null) {
			return tags;
		}
		$.each(this.data.fields.sys_tags, function(i, val) {
			if (!util.stringStartWith(val, "feed_name_") && !util.stringStartWith(val, "feed_group_name_")) {
				tags += '<a href="' + planet.resourcesPrefix + '#tags=' + val + '" ><span class="label">' + val + '</span></a> ';
			}
		});
		return tags;
	};

	var author = null;
	this.getAuthor = function() {
		if (author == null) {
			if (this.data.fields.sys_contributors != null) {
				author = util.parseEmail(this.data.fields.sys_contributors);
			} else {
				author = util.parseEmail(this.data.fields.author);
			}
		}
		return author;

	};

	this.getAuthorAvatarUrl = function() {
		if (this.data.fields.avatar_link != null && this.data.fields.avatar_link != "") {
			return this.data.fields.avatar_link;
		} else if (this.getAuthor().email != null) {
			var emailMd5 = md5(this.getAuthor().email);
			return "//www.gravatar.com/avatar/" + emailMd5 + "?s=80&d=https%3A%2F%2Fstatic.jboss.org/developer/gravatar/"
					+ emailMd5 + "/80.png";
		} else {
			return "https://developer.jboss.org/people/sbs-default-avatar/avatar/80.png";
		}
	};

	this.getPublished = function() {
		var str = util.arrayToString(this.data.fields.sys_created);
		return util.parseISODateString(str);
	};

	this.showFullPost = function() {
		var contentElm = $(".blog-post-content", this.previewElm);
		contentElm.empty();
		contentElm.hide();
		contentElm.append(this.data.fields.sys_content);
		$(".blog-post-show-more", this.previewElm).empty();
		$(".home-post-perm-link", this.previewElm).show();


		contentElm.animate({
			height : 'auto'
		}, 1, function() {
			var h = contentElm.height();
			contentElm.css({
				'height' : 0,
				'overflow' : 'hidden'
			});
			contentElm.show();
			contentElm.animate({
				height : h,
				useTranslate3d : true,
				leaveTransforms : false
			}, 800, function() {
				contentElm.css({
					'height' : 'auto',
					'overflow' : 'visible'
				});
			});
		});

		// Change display format to 'full';
		this.displayFormat = 2;
	};
};

/**
 * Main application
 */
var planet = {
	resourcesPrefix : contextRoot,
	dcpRestApi : syncServer + "/rest/",
	dcpContentType : syncContentType,
	dcpSearchQuery : searchQuery,

	layout : 1,

	/* Function configure planet. Called before any resources are loaded */
	configurePlanet : function() {
		if (localStorageInfo.isSupported()) {
			if (localStorage[localStorageInfo.displayLayout] != null) {
				planet.layout = parseInt(localStorage[localStorageInfo.displayLayout]);
			}
		}
	},
	/* Function to load all resources based on user is on mobile or not */
	loadAllResources : function() {
		$(document).ready(function() {
			if (typeof appLoadedOnDesktop == "function") {
				appLoadedOnDesktop();
			}
		});
	},

	onMobile : function() {
		return ($(window).width() > 768) ? false : true;
	},

	addThisTemplate : function(url, title) {
		if (planet.onMobile()) {
			return '<div class="addthis_toolbox addthis_default_style addthis_32x32_style" addthis:url="'
				+ url + '" addthis:title="' + title + '">'
				+ '<a class="addthis_button_compact"></a>' + '</div>';
		} else {
			return '<div class="addthis_toolbox addthis_default_style" addthis:url="' + url + '" addthis:title="'
					+ title + '">' + '<a class="addthis_button_preferred_1"></a>'
					+ '<a class="addthis_button_preferred_2"></a>' + '<a class="addthis_button_preferred_3"></a>'
					+ '<a class="addthis_button_compact"></a>' + '</div>';
		}
	},

	/* Refresh entire page */
	refreshPage : function() {
		window.location.href = window.location.href;
	},

	canRetrieveNewPosts : true,

	/* Global method for retrieving new posts */
	retrieveNewPosts : function(currentFrom, count, callback, projects, tags) {
		planet.canRetrieveNewPosts = false;

		var url = planet.dcpRestApi + planet.dcpSearchQuery;
		if(url.indexOf("?") > -1) {
			url += "&";
		} else {
			url += "?";
		}
		url += "from=" + currentFrom + "&size=" + count;

		if (typeof tags != "undefined" && tags != "" && tags != null) {
			if ($.isArray(tags)) {
				$.each(tags, function(index, value) {
					url += "&tag=" + $.trim(value).toLowerCase();
				});
			} else {
				url += "&tag=" + tags.toLowerCase();
			}
		}

		if (typeof projects != "undefined" && projects != "" && projects != null) {
			if ($.isArray(projects)) {
				$.each(projects, function(i, value) {
					url += "&project=" + $.trim(value);
				});
			} else {
				url += "&project=" + projects;
			}
		}

		url = encodeURI(url);

		$.ajax(url)
		.done(function(data) {
			// must be before callback because it can turn off retrieving new posts in case of "no more posts"
			planet.canRetrieveNewPosts = true;
			callback(data, true); })
		.fail(function() {
			callback({}, false);
		});

	},

	projects : null,

	projectNames : null,

	getProjectItems : function(callback) {
		if (planet.projectNames != null) {
			return callback(planet.projectNames);
		}
		var projectsRestUrl = planet.dcpRestApi + "search?sys_type=project_info&field=sys_project&field=sys_title&from=0&size=200&sortBy=new-create";
		$.ajax({
			url : encodeURI(projectsRestUrl),
			type : "get",
			dataType : 'json',
			success : function(data) {
				planet.projects = [];
				planet.projectNames = [];
				$.each(data.hits.hits, function(index, item) {
					planet.projects.push({ "id" : item.fields.sys_project[0], "name" : item.fields.sys_title[0] });
					planet.projectNames.push(item.fields.sys_title[0]);
				});

				callback(planet.projects);
			}
		});
	},

	getProjectItem : function(code) {
		if (code == null || code == "") {
			return null;
		}
		for (var i = 0; i < planet.projects.length; i++) {
			var p = planet.projects[i];
			if (p.id == code) {
				return p;
			}
		}

		return null;
	},

	getProjectName : function(code) {
		if (code == null || code == "undefined") {
			return "";
		}
		if (planet.projects == null) {
			return "";
		}
		for ( var i = 0; i < planet.projects.length; i++) {
			var p = planet.projects[i];
			if (p.id == code) {
				return p.name;
			}
		}
		return "";
	}
};

planet.configurePlanet();
planet.loadAllResources();

/**
 * Logic for navigation in browser
 */
var navigation = {
	changePage : function(url, options) {
		$(location).attr('href', url);
	},

	getSearchUrls : function() {
		if (localStorageInfo.isSupported()) {
			if (localStorage[localStorageInfo.navigationSearchUrls] == null) {
				return null;
			} else {
				return JSON.parse(localStorage[localStorageInfo.navigationSearchUrls]).urls;
			}
		} else {
			return null;
		}
	},

	setSearchUrls : function(u) {
		if (localStorageInfo.isSupported()) {
			var urls = {
				"urls" : u
			};
			localStorage[localStorageInfo.navigationSearchUrls] = JSON.stringify(urls);
		}
	},
	appendSearchUrl : function(url) {
		var fromCache = navigation.getSearchUrls();
		if (fromCache != null) {
			fromCache.push(url);
			navigation.setSearchUrls(fromCache);
		}
	},
	clearSearchUrls : function() {
		navigation.setSearchUrls([]);
	},

	isLast : function(url) {

	},

	setSearchUrlsFromLinks : function(links) {
		array = [];
		$.each(links, function(i) {
			array.push($(links[i]).attr('href'));
		});
		navigation.setSearchUrls(array);
	},

	currentUrl : null,

	getNextUrl : function() {
		var currentPath = window.location.pathname;
		if (navigation.currentUrl != null) {
			currentPath = navigation.currentUrl;
		}
		var searchUrls = navigation.getSearchUrls();
		var index = $.inArray(currentPath, searchUrls);
		if (index >= 0 && index < searchUrls.length) {
			return searchUrls[index + 1];
		} else {
			return null;
		}
	},

	next : function() {
		var url = navigation.getNextUrl();
		if (url == null) {
			return;
		}
		navigation.changePage(url, {
			showLoadMsg : false
		});
	},

	getPreviousUrl : function() {
		var currentPath = window.location.pathname;
		if (navigation.currentUrl != null) {
			currentPath = navigation.currentUrl;
		}
		var searchUrls = navigation.getSearchUrls();
		var index = $.inArray(currentPath, searchUrls);
		if (index >= 1) {
			return searchUrls[index - 1];
		} else {
			return null;
		}
	},

	previous : function() {
		var url = navigation.getPreviousUrl();
		if (url == null) {
			return;
		}
		navigation.changePage(url, {
			reverse : true,
			showLoadMsg : false
		});
	}

};

/**
 * Logic for page 'home'
 */
var home = {
	data : {
		currentFrom : 0,
		count : 10,
		// Array of project ids
		projects : [],
		// Array of tags
		tags : [],
		hashChangeByUser: false,
		filterProjectNoAction: false
	},
	currentPage : null,
	defaultTags : [],
	previewDiv : null,
	previewDivInitialPosition : null,

	/**
	 * Initialize page
	 * @param page
	 * @param defaultTags Array of default tags
	 */
	init : function(page, defaultTags) {
		home.currentPage = page;
		home.defaultTags = defaultTags;
		planet.canRetrieveNewPosts = true;
		home.previewDiv = $("#home-right-preview", page);

		home.decorateLayoutLinks(planet.layout);

		home.data.projects = home.getProjectsFromUrl();

		$("#home-filter-clearall", page).bind('click', function() {
			var filterFeed = $("#home-feed-filter");
			filterFeed.tokenInput("clear");

			var filterTags = $("#home-tags-filter");
			filterTags.val("");
			filterTags.change();
			return false;
		});

		home.data.tags = home.getTagsFromUrl();
		tagsFilter = $("#home-tags-filter", page);

		tagsFilter.val(util.arrayToString(home.data.tags));

		tagsFilter.change(function() {
			home.changeTags($(this).val());
		});

		if (home.data.projects.length > 0 || home.data.tags.length > 0) {
			home.showFilter();
		}
		;
		$("#home-refresh", page).bind('click', function() {
			home.refresh();
			return false;
		});
		$("#filter-link", page).bind('click', function() {
			home.toggleFilter();
			return false;
		});

		$(window).bind('hashchange', function(e) {
			var state = $.bbq.getState();
			home.data.projects = util.stringToArray(state.projects);
			home.data.tags = util.stringToArray(state.tags);

			if (!home.data.hashChangeByUser) {
				home.data.filterProjectNoAction = true;
				var projectFilter = $("#home-feed-filter", page);
				projectFilter.tokenInput("clear");
				for (var i=0; i < home.data.projects.length; i++) {
					var item = planet.getProjectItem(home.data.projects[i]);
					projectFilter.tokenInput("add", item);
				}
				home.data.filterProjectNoAction = false;
			}
			home.data.hashChangeByUser = false;

			$("#home-tags-filter", page).val(util.arrayToString(home.data.tags));

			if (home.data.projects.length > 0 || home.data.tags.length > 0) {
				home.showFilter();
			}

			home.updateFeedLink();
			home.refresh();
		});

		$(window).scroll(
				function() {
					if (home.previewDivInitialPosition == null && $(this).scrollTop() > 0) {
						home.previewDivInitialPosition = home.previewDiv.offset();
					}
					if (planet.canRetrieveNewPosts) {
						var contentContainer = $(".content-container");
						if (($(window).height() + $(window).scrollTop()) >= (contentContainer.offset().top + contentContainer.height())) {
							$("#loading-div-home", home.currentPage).show();
							planet.retrieveNewPosts(home.data.currentFrom, home.data.count, home.addPosts,
									home.data.projects, home.getActualTags());
						}
					}
				});

		planet.getProjectItems(function(data) {
			var initialProjects = [];
			$.each(home.data.projects, function(i, val) {
				initialProjects.push(planet.getProjectItem(val));
			});
			$("#home-feed-filter", page).tokenInput(data, {
				propertyToSearch: "name",
				theme: "jbdev",
				preventDuplicates: true,
				animateDropdown: false,
				searchDelay: 0,
				searchingText: "",
				onAdd: home.changeProjectEvent,
				onDelete: home.changeProjectEvent,
				prePopulate: initialProjects
			});

			home.updateFeedLink();
			home.refresh();
		});
	},
	changeProjectEvent : function() {
		if (home.data.filterProjectNoAction) {
			return;
		}
		home.data.hashChangeByUser = true;
		var state = $.bbq.getState();

		var projectItems = $("#home-feed-filter", home.page).tokenInput("get");
		if (projectItems.length == 0) {
			state.projects = "";
		} else {
			var vals = "";
			$.each(projectItems, function(i, val) {
				if (i != 0) {
					vals += ",";
				}
				vals += val.id;
			} );
			state.projects = vals;
		}
		$.bbq.pushState(state);
	},
	changeTags : function(tags) {
		var state = $.bbq.getState();
		state.tags = tags;
		$.bbq.pushState(state);
	},
	updateFeedLink : function() {
		var tags = home.getActualTags();
		if (home.data.projects.length == 0 && tags.length == 0) {
			return;
		}
		var feedLink = $("#home-feed-link", home.currentPage);
		var feedUrl = feedLink.attr('data-url-base');
		var params = {
			title: "",
			paramString: ""
		};
		if (home.data.projects.length > 0) {
			if (home.data.projects.length > 1) {
				params.title += " for projects ";
			} else {
				params.title += " for project ";
			}
			params.project = [];
			for (var i = 0; i < home.data.projects.length; i++) {
				params.paramString += "&project=" + home.data.projects[i];
				params.title += planet.getProjectName(home.data.projects[i]);
				if (i + 1 < home.data.projects.length) {
					params.title += ", ";
				}
			}
		}
		if (tags.length > 0) {
			if (home.data.projects.length == 0) {
				params.title += " for ";
			} else {
				params.title += " and ";
			}
			if (tags.length > 1) {
				params.title += "tags ";
			} else {
				params.title += "tag ";
			}

			for (var i = 0; i < tags.length; i++) {
				params.paramString += "&tag=" + tags[i];
				params.title += tags[i];
				if (i + 1 < tags.length) {
					params.title += ", ";
				}
			}
		}
		feedUrl += params.title;
		if (params.paramString.length > 0) {
			feedUrl += params.paramString;
		}

		feedLink.attr('href', encodeURI(feedUrl));
	},
	switchDisplay : function(layout) {
		planet.layout = layout;
		home.decorateLayoutLinks(layout);
		if (localStorageInfo.isSupported()) {
			localStorage[localStorageInfo.displayLayout] = planet.layout;
		}
		home.refresh();
	},
	decorateLayoutLinks : function(layout) {
		var compactLink = $("#layout-compact", home.currentPage);
		var fullLink = $("#layout-full", home.currentPage);
		var btnClass = "btn-info";
		switch (layout) {
		case 1:
			compactLink.addClass(btnClass);
			fullLink.removeClass(btnClass);
			break;
		case 2:
			compactLink.removeClass(btnClass);
			fullLink.addClass(btnClass);
			break;
		}
	},
	addPosts : function(data, success) {
		size = 0;
		var postsList = $("#home-posts", home.currentPage);

		$("#loading-div-home", home.currentPage).hide();
		if (!success) {
			$("#dcp-error-home", home.currentPage).show();
			return;
		}

		$.each(data.hits.hits, function(key, val) {
			var postEntry = new Post(val, planet.layout);
			postsList.append(postEntry.getPreviewElm());

			if (postEntry.displayFormat == 1) {
				var links = $("a.show-more", postEntry.previewElm);
				links.unbind('click');
				links.bind("click", function() {
					postEntry.showFullPost();
					return false;
				});
			}

			var links = $("a.post-title-link", postEntry.previewElm);
			links.unbind('click');
			links.bind("click", function() {
				if (postEntry.displayFormat == 1) {
					postEntry.showFullPost();
					return false;
				}
			});


			navigation.appendSearchUrl(val.link);
			size++;
		});

		if (size == 0 || size != home.data.count) {
			var message = "No more posts";
			if (home.data.currentFrom == 0) {
				message = "No posts";
			}
			postsList.append('<h2 class="ui-li-heading"><center>' + message + '</center></h2>');
			planet.canRetrieveNewPosts = false;
		}
		home.data.currentFrom = home.data.currentFrom + size;

		// AddThis component can be blocked by adv plugins.
		if (typeof addthis != 'undefined') {
			addthis.toolbox(".addthis_toolbox");
		}
	},
	toogleButton : function(selector) {
		// Zurb doesn't use classes so there is no way how to toogle button.
	},
	refresh : function() {
		home.toogleButton("#home-refresh");
		home.data.currentFrom = 0;
		$("#home-refresh i").addClass("fa-spin");
		$("#home-posts").empty();
		$("#loading-div-home").show();
		$("#dcp-error-home").hide();
		navigation.clearSearchUrls();
		planet.retrieveNewPosts(home.data.currentFrom, home.data.count, function(data, success) {
			home.addPosts(data, success);
			home.toogleButton("#home-refresh");
			$("#home-refresh i").removeClass("fa-spin");
		}, home.data.projects, home.getActualTags());
	},
	toggleFilter : function() {
		var elm = $("#home-filter");
		if (elm.css('display') == 'none') {
			elm.height(0);
			elm.show();
			home.toogleButton("#filter-link");
			var filterHeight = home.getFilterHeight();
			elm.animate({
				"min-height" : filterHeight,
				useTranslate3d : true,
				leaveTransforms : false
			}, 200, function() {
				$(this).css('height', '');
			});
		} else {
			elm.height(elm.css("min-height"));
			elm.animate({
				"height" : '0px',
				useTranslate3d : true,
				leaveTransforms : false
			}, 200, function() {
				home.toogleButton("#filter-link");
				elm.hide();
				elm.css('min-height', '');
			});
		}
	},
	showFilter : function() {
		if ($("#home-filter").css("display") == "none") {
			home.toogleButton("#filter-link");
			var filterHeight = home.getFilterHeight();
			$("#home-filter").css({
				'min-height' : filterHeight,
				'display' : 'block'
			});
		}
	},
	getFilterHeight : function() {
		if ($("#home-filter-tags-group").is(':visible')) {
			return '135px';
		} else {
			return '96px';
		}
	},
	getProjectsFromUrl : function() {
		return util.stringToArray(util.getHashParam("projects"));
	},
	getTagsFromUrl : function() {
		return util.stringToArray(util.getHashParam("tags"));
	},
	getActualTags : function() {
		var tags = home.data.tags;
		if (typeof tags != "undefined" && tags != "" && tags != null && $.isArray(tags)) {
			tags = tags.concat(home.defaultTags);
		} else {
			tags = home.defaultTags;
		}
		return tags;
	},
	destroy : function() {
		$(window).unbind("scroll");
	}
};

/** *********** PLUGINS ************* */
/**
 * DataTable for bootstrap Source: http://datatables.github.io/Plugins/integration/foundation/dataTables.foundation.js
 * See: https://www.datatables.net/forums/discussion/14408/zurb-foundation-integration-for-datatables/p1
 *
 * Tuned to be 24 columns and using collapse rows
 */
function initDataTable(table, options) {
	dataTable.init();
	dataTable.initDataTable(table, options);
}

DataTable = function() {
	this.initialized = false;

	this.init = function() {
		if (this.initialized) {
			return;
		}
		/* Set the defaults for DataTables initialisation */
		$.extend( true, $.fn.dataTable.defaults, {
			"sDom":
				"<'row collapse'<'small-12 columns'l><'small-12 columns'f>r>"+
					"t"+
					"<'row collapse'<'small-12 columns'i><'small-12 columns'p>>",
			"sPaginationType": "foundation",
			"oLanguage": {
				"sLengthMenu": "_MENU_ records per page"
			}
		} );


		/* API method to get paging information */
		$.fn.dataTableExt.oApi.fnPagingInfo = function ( oSettings )
		{
			return {
				"iStart":         oSettings._iDisplayStart,
				"iEnd":           oSettings.fnDisplayEnd(),
				"iLength":        oSettings._iDisplayLength,
				"iTotal":         oSettings.fnRecordsTotal(),
				"iFilteredTotal": oSettings.fnRecordsDisplay(),
				"iPage":          oSettings._iDisplayLength === -1 ?
					0 : Math.ceil( oSettings._iDisplayStart / oSettings._iDisplayLength ),
				"iTotalPages":    oSettings._iDisplayLength === -1 ?
					0 : Math.ceil( oSettings.fnRecordsDisplay() / oSettings._iDisplayLength )
			};
		};


		/* Bootstrap style pagination control */
		$.extend( $.fn.dataTableExt.oPagination, {
			"foundation": {
				"fnInit": function( oSettings, nPaging, fnDraw ) {
					var oLang = oSettings.oLanguage.oPaginate;
					var fnClickHandler = function ( e ) {
						e.preventDefault();
						if ( oSettings.oApi._fnPageChange(oSettings, e.data.action) ) {
							fnDraw( oSettings );
						}
					};

					$(nPaging).append(
						'<ul class="pagination">'+
							'<li class="prev arrow unavailable"><a href="">&laquo;</a></li>'+
							'<li class="next arrow unavailable"><a href="">&raquo;</a></li>'+
							'</ul>'
					);
					var els = $('a', nPaging);
					$(els[0]).bind( 'click.DT', { action: "previous" }, fnClickHandler );
					$(els[1]).bind( 'click.DT', { action: "next" }, fnClickHandler );
				},

				"fnUpdate": function ( oSettings, fnDraw ) {
					var iListLength = 5;
					var oPaging = oSettings.oInstance.fnPagingInfo();
					var an = oSettings.aanFeatures.p;
					var pages = [];
					var i, ien, klass, host;

					// This could use some improving - however, see
					// https://github.com/DataTables/DataTables/issues/163 - this will
					// be changing in the near future, so not much point in doing too
					// much just now
					if ( oPaging.iTotalPages <= 6 ) {
						for ( i=0 ; i<oPaging.iTotalPages ; i++ ) {
							pages.push( i );
						}
					}
					else {
						// Current page
						pages.push( oPaging.iPage );

						// After current page
						var pagesAfter = oPaging.iPage + 2 >= oPaging.iTotalPages ?
							oPaging.iTotalPages :
							oPaging.iPage + 2;
						for ( i=oPaging.iPage+1 ; i<pagesAfter ; i++ ) {
							pages.push( i );
						}

						// After gap
						if ( pagesAfter < oPaging.iTotalPages-2 ) {
							pages.push( null );
						}

						// End
						if ( $.inArray( oPaging.iTotalPages-2, pages ) === -1 && oPaging.iPage < oPaging.iTotalPages-2 ) {
							pages.push( oPaging.iTotalPages-2 );
						}
						if ( $.inArray( oPaging.iTotalPages-1, pages ) === -1 ) {
							pages.push( oPaging.iTotalPages-1 );
						}

						// Pages before
						var pagesBefore = oPaging.iPage - 2 > 0 ?
							oPaging.iPage - 2 :
							0;
						for ( i=oPaging.iPage-1 ; i>pagesBefore ; i-- ) {
							pages.unshift( i );
						}

						// Before gap
						if ( pagesBefore > 1 ) {
							pages.unshift( null );
						}

						// Start
						if ( $.inArray( 1, pages ) === -1 && oPaging.iTotalPages > 1 ) {
							pages.unshift( 1 );
						}
						if ( $.inArray( 0, pages ) === -1 ) {
							pages.unshift( 0 );
						}
					}

					for ( i=0, ien=an.length ; i<ien ; i++ ) {
						// Remove the middle elements
						host = an[i];
						$('li:gt(0)', host).filter(':not(:last)').remove();

						// Add the new list items and their event handlers
						$.each( pages, function( i, page ) {
							klass = page === null ? 'unavailable' :
								page === oPaging.iPage ? 'current' : '';
							$('<li class="'+klass+'"><a href="">'+(page===null? '&hellip;' : page+1)+'</a></li>')
								.insertBefore( $('li:last', host) )
								.bind('click', function (e) {
									e.preventDefault();
									var pageNum = parseInt($('a', this).text(),10);
									if ( ! isNaN(pageNum)) {
										oSettings._iDisplayStart = (pageNum-1) * oPaging.iLength;
										fnDraw( oSettings );
									}
								} );
						} );

						// Add / remove disabled classes from the static elements
						if ( oPaging.iPage === 0 ) {
							$('li:first', host).addClass('unavailable');
						} else {
							$('li:first', host).removeClass('unavailable');
						}

						if ( oPaging.iPage === oPaging.iTotalPages-1 || oPaging.iTotalPages === 0 ) {
							$('li:last', host).addClass('unavailable');
						} else {
							$('li:last', host).removeClass('unavailable');
						}
					}
				}
			}
		} );


		/*
		 * TableTools Foundation compatibility
		 * Required TableTools 2.1+
		 */
		if ( $.fn.DataTable.TableTools ) {
			// Set the classes that TableTools uses to something suitable for Foundation
			$.extend( true, $.fn.DataTable.TableTools.classes, {
				"container": "DTTT button-group",
				"buttons": {
					"normal": "button",
					"disabled": "disabled"
				},
				"collection": {
					"container": "DTTT_dropdown dropdown-menu",
					"buttons": {
						"normal": "",
						"disabled": "disabled"
					}
				},
				"select": {
					"row": "active"
				}
			} );

			// Have the collection use a bootstrap compatible dropdown
			$.extend( true, $.fn.DataTable.TableTools.DEFAULTS.oTags, {
				"collection": {
					"container": "ul",
					"button": "li",
					"liner": "a"
				}
			} );
		}


	};

	this.initDataTable = function(table, options) {
		var defaults = {
			"bStateSave" : true,
			"sPaginationType" : "foundation"
		};
		if (options != null) {
			defaults = $.extend({}, defaults, options);
		}
		table.dataTable(defaults);
	};
};

var dataTable = new DataTable();
