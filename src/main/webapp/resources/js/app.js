/** **************** Application JS ****************** */

/* Common variables for third party plugins */

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
	this.getHashParam = function(key) {
		var value = $.deparam.fragment()[key];
		if (value) {
			return value;
		} else {
			return null;
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
	this.stringStartWith = function(str, startWithStr) {
		return str.lastIndexOf(startWithStr) == 0;
	};
	this.stringToArray = function(str) {
		if (str == null) {
			return null;
		}
		if ($.isArray(str)) {
			return str;
		} else {
			return str.split(",");
		}
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

	/* Get post based on ID */
	this.getPost = function(id, callback) {
		$.ajax({
			url : planet.dcpRestApi + "content/" + planet.dcpContentType + "/" + id + "?field=_source",
			type : "get",
			dataType : 'json',
			success : function(data) {
				callback(data);
			}
		});
	},

	this.previewElm = null;

	this.getPreviewElm = function() {
		if (this.previewElm != null) {
			return this.previewElm;
		}
		var preview = '<article id="home-post-li-' + this.data._id + '" class="blog-post-article">';
		var addThisTempl = planet.addThisTemplate(this.data._source.dcp_url_view, this.data._source.dcp_title);
		var projectName = planet.getProjectName(this.data._source.dcp_project);
		var projectInfo = "";
		if (projectName != "") {
			projectInfo = ' in <a href="#project=' + this.data._source.dcp_project + '">' + projectName + '</a>';
		}

		var tags = this.getTagsRow();
		preview += '<header><h2 class="ui-li-heading"><a href="' + this.data._source.dcp_url_view + '" data-id="'
				+ this.data._id + '">' + this.data._source.dcp_title
				+ '</a></h2><div class="blog-post-header-info"><img src="' + this.getAuthorAvatarUrl()
				+ '" class="ui-li-thumb img-polaroid" height="46px" width="46px"/>'
				+ '<span class="blog-post-list-date">' + util.dateToString(this.getPublished()) + '<br/>by '
				+ this.getAuthor().name + projectInfo + '</span>' + addThisTempl + '</div></header>';
		if (this.displayFormat == 1) {
			preview += '<div class="blog-post-content">'
					+ this.data._source.dcp_description
					+ '</div>'
					+ '<footer><div class="blog-post-tags">'
					+ tags
					+ '</div>'
					+ '<div class="blog-post-show-more"><a href="" class="show-more btn btn-small">Read more</a></div></footer>';
		} else {
			preview += '<div class="blog-post-content">' + this.data._source.dcp_content + '</div>'
					+ '<footer><div class="blog-post-tags">' + tags + '</div></footer>';
		}
		preview += '</article>';
		this.previewElm = $(preview);

		return this.previewElm;
	};

	this.getTagsRow = function() {
		var tags = "";
		$.each(this.data._source.tags, function(i, val) {
			if (!util.stringStartWith(val, "feed_name_") && !util.stringStartWith(val, "feed_group_name_")) {
				tags += '<a href="#tags=' + val + '" ><span class="label">' + val + '</span></a> ';
			}
		});
		return tags;
	};

	var author = null;
	this.getAuthor = function() {
		if (author == null) {
			if (this.data._source.dcp_contributors != null) {
				author = util.parseEmail(this.data._source.dcp_contributors);
			} else {
				author = util.parseEmail(this.data._source.author);
			}
		}
		return author;

	};

	this.getAuthorAvatarUrl = function() {
		if (this.getAuthor().email != null) {
			var emailMd5 = md5(this.getAuthor().email);
			return "http://www.gravatar.com/avatar/" + emailMd5 + "?s=46&d=https%3A%2F%2Fcommunity.jboss.org/gravatar/"
					+ emailMd5 + "/46.png";
		} else {
			return this.data._source.avatar_link;
		}
	};

	this.getPublished = function() {
		return util.parseISODateString(this.data._source.dcp_created);
	};

	this.showFullPost = function() {
		var contentElm = $(".blog-post-content", this.previewElm);
		contentElm.empty();
		contentElm.hide();
		contentElm.append(this.data._source.dcp_content);
		$(".blog-post-show-more", this.previewElm).empty();

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
	dcpRestApi : syncServer + "/v1/rest/",
	dcpContentType : syncContentType,

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

	addThisTemplate : function(url, title, style) {
		switch (style) {
		case 1:
			return '<div class="addthis_toolbox addthis_default_style" addthis:url="' + url + '" addthis:title="'
					+ title + '">' + '<a class="addthis_button_facebook_like" fb:like:layout="button_count"></a>'
					+ '<a class="addthis_button_tweet"></a>'
					+ '<a class="addthis_button_google_plusone" g:plusone:size="medium"></a>'
					+ '<a class="addthis_counter addthis_pill_style"></a>' + '</div>';
		default:
			return '<div class="addthis_toolbox addthis_default_style" addthis:url="' + url + '" addthis:title="'
					+ title + '">' + '<a class="addthis_button_preferred_1"></a>'
					+ '<a class="addthis_button_preferred_2"></a>' + '<a class="addthis_button_preferred_3"></a>'
					+ '<a class="addthis_button_compact"></a>' + '</div>';
		}
		if (style = 1) {
		}
	},

	/* Refresh entire page */
	refreshPage : function() {
		window.location.href = window.location.href;
	},

	canRetrieveNewPosts : true,

	/* Global method for retrieving new posts */
	retrieveNewPosts : function(currentFrom, count, callback, projectCode, tags) {
		planet.canRetrieveNewPosts = false;
		var url = planet.dcpRestApi + "search?dcp_type=blogpost&from=" + currentFrom + "&size=" + count
				+ "&sortBy=new&field=_source";
		if (typeof tags != "undefined" && tags != "" && tags != null) {
			if ($.isArray(tags)) {
				$.each(tags, function(index, value) {
					url += "&tag=" + $.trim(value);
				});
			} else {
				url += "&tag=" + tags;
			}
		}

		if (typeof projectCode != "undefined" && projectCode != null && projectCode != "") {
			url += "&project=" + projectCode;
		}

		url = encodeURI(url);

		$.ajax({
			url : url,
			type : "get",
			dataType : 'json',
			success : function(data) {
				// must be before callback because it can turn off retrieving new posts in case of "no more posts"
				planet.canRetrieveNewPosts = true;
				callback(data);
			}
		});

	},

	projects : null,

	projectNames : null,

	getProjectNames : function(callback) {
		if (planet.projectNames != null) {
			return callback(planet.projectNames);
		}
		var projectsRestUrl = planet.dcpRestApi + "project?size=200";
		$.ajax({
			url : projectsRestUrl,
			type : "get",
			dataType : 'json',
			success : function(data) {
				planet.projects = [];
				planet.projectNames = [];
				$.each(data.hits, function(index, item) {
					planet.projects.push([ item.data.code, item.data.name ]);
					planet.projectNames.push(item.data.name);
				});

				callback(planet.projectNames);
			}
		});
	},

	getProjectCode : function(name) {
		if (name == null || name == "") {
			return "";
		}
		for ( var i = 0; i < planet.projects.length; i++) {
			var p = planet.projects[i];
			if (p[1] == name) {
				return p[0];
			}
		}

		return "";
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
			if (p[0] == code) {
				return p[1];
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
		feedId : "",
		tags : ""
	},
	currentPage : null,
	previewDiv : null,
	previewDivInitialPosition : null,
	init : function(page) {
		home.currentPage = page;
		planet.canRetrieveNewPosts = true;
		home.previewDiv = $("#home-right-preview", page);

		home.decorateLayoutLinks(planet.layout);

		home.data.feedId = home.getFeedIdFromUrl();
		feedFilter = $("#home-feed-filter", page);

		feedFilter.change(function() {
			var projectCode = planet.getProjectCode($(this).val());
			home.changeFeed(projectCode);
		});

		var feedFilterOptions = {
			source : function(query, process) {
				planet.getProjectNames(process);
			}
		};
		feedFilter.typeahead(feedFilterOptions);

		$("#home-feed-filter-remove", page).bind('click', function() {
			var filter = $("#home-feed-filter");
			filter.val("");
			filter.change();
			return false;
		});

		home.data.tags = home.getTagsFromUrl();
		tagsFilter = $("#home-tags-filter", page);

		tagsFilter.val(util.arrayToString(home.data.tags));

		tagsFilter.change(function() {
			home.changeTags($(this).val());
		});

		$("#home-tags-filter-remove", page).bind('click', function() {
			var filter = $("#home-tags-filter");
			filter.val("");
			filter.change();
			return false;
		});

		if (home.data.feedId != null || home.data.tags != null) {
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
			home.data.feedId = state.project;
			home.data.tags = util.stringToArray(state.tags);

			$("#home-feed-filter", page).val(planet.getProjectName(home.data.feedId));
			$("#home-tags-filter", page).val(util.arrayToString(home.data.tags));

			if (home.data.feedId != null || home.data.tags != null) {
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
						if (($(window).height() + $(window).scrollTop()) >= ($("#page-id-home", home.currentPage)
								.offset().top + $("#page-id-home", home.currentPage).height())) {
							$("#loading-div-home", home.currentPage).show();
							planet.retrieveNewPosts(home.data.currentFrom, home.data.count, home.addPosts,
									home.data.feedId, home.data.tags);
						}
					}
				});

		planet.getProjectNames(function() {
			if (home.data.feedId != null) {
				feedFilter.val(planet.getProjectName(home.data.feedId));
			}
			home.updateFeedLink();
			home.refresh();
		});
	},
	changeFeed : function(feedId) {
		var state = $.bbq.getState();

		if (feedId == "all" || feedId == "") {
			state.project = "";
		} else {
			state.project = feedId;
		}
		$.bbq.pushState(state);
	},
	changeTags : function(tags) {
		var state = $.bbq.getState();
		state.tags = tags;
		$.bbq.pushState(state);
	},
	updateFeedLink : function() {
		if (home.data.feedId == null && home.data.tags == null) {
			return;
		}
		var feedLink = $("#home-feed-link", home.currentPage);
		var feedUrl = feedLink.attr('href');
		var params = $.deparam.querystring(feedUrl);
		var title = feedLink.attr('data-title-base');
		if (home.data.feedId != null) {
			params['project'] = home.data.feedId;
			title += " for project " + planet.getProjectName(home.data.feedId);
		}
		if (home.data.tags != null) {
			params['tag'] = home.data.tags;
			if (home.data.feedId == null) {
				title += " for";
			} else {
				title += " and";
			}
			title += " tag " + home.data.tags;
		}
		params['feed_title'] = title;
		feedUrl = $.param.querystring(feedUrl, params);

		feedLink.attr('href', feedUrl);
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
	addPosts : function(data) {
		size = 0;
		var postsList = $("#home-posts", home.currentPage);

		$("#loading-div-home", home.currentPage).hide();

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

			navigation.appendSearchUrl(val.link);
			size++;
		});

		addthis.toolbox(".addthis_toolbox");

		if (size == 0 || size != home.data.count) {
			var message = "No more posts";
			if (home.data.currentFrom == 0) {
				message = "No posts";
			}
			postsList.append('<h2 class="ui-li-heading"><center>' + message + '</center></h2>');
			planet.canRetrieveNewPosts = false;
		}
		home.data.currentFrom = home.data.currentFrom + size;
	},
	toogleButton : function(selector) {
		var elm = $(selector);
		$("i", elm).toggleClass("icon-white");
		elm.toggleClass("btn-info");
	},
	refresh : function() {
		home.toogleButton("#home-refresh");
		home.data.currentFrom = 0;
		$("#home-posts").empty();
		$("#loading-div-home").show();
		navigation.clearSearchUrls();
		planet.retrieveNewPosts(home.data.currentFrom, home.data.count, function(data) {
			home.addPosts(data);
			home.toogleButton("#home-refresh");
		}, home.data.feedId, home.data.tags);
	},
	toggleFilter : function() {
		var elm = $("#home-filter");
		if (elm.css('display') == 'none') {
			elm.css('height', 0);
			elm.show();
			home.toogleButton("#filter-link");
			elm.animate({
				height : '35px',
				useTranslate3d : true,
				leaveTransforms : false
			}, 200);
		} else {
			elm.animate({
				height : '0px',
				useTranslate3d : true,
				leaveTransforms : false
			}, 200, function() {
				home.toogleButton("#filter-link");
				elm.hide();
			});
		}
	},
	showFilter : function() {
		if ($("#home-filter").css("display") == "none") {
			home.toogleButton("#filter-link");
			$("#home-filter").css({
				'height' : '35px',
				'display' : 'block'
			});
		}
	},
	getFeedIdFromUrl : function() {
		return util.getHashParam("project");
	},
	getTagsFromUrl : function() {
		return util.stringToArray(util.getHashParam("tags"));
	},
	destroy : function() {
		$(window).unbind("scroll");
	}
};

/** *********** PLUGINS ************* */
/**
 * DataTable for bootstrap Source: http://datatables.net/media/blog/bootstrap_2/DT_bootstrap.js
 */
function initDataTable(table) {
	dataTable.init();
	dataTable.initDataTable(table);
}

DataTable = function() {
	this.initialized = false;

	this.init = function() {
		if (this.initialized) {
			return;
		}
		/* Default class modification */
		$.extend($.fn.dataTableExt.oStdClasses, {
			"sWrapper" : "dataTables_wrapper form-inline"
		});

		/* API method to get paging information */
		$.fn.dataTableExt.oApi.fnPagingInfo = function(oSettings) {
			return {
				"iStart" : oSettings._iDisplayStart,
				"iEnd" : oSettings.fnDisplayEnd(),
				"iLength" : oSettings._iDisplayLength,
				"iTotal" : oSettings.fnRecordsTotal(),
				"iFilteredTotal" : oSettings.fnRecordsDisplay(),
				"iPage" : Math.ceil(oSettings._iDisplayStart / oSettings._iDisplayLength),
				"iTotalPages" : Math.ceil(oSettings.fnRecordsDisplay() / oSettings._iDisplayLength)
			};
		};

		/* Bootstrap style pagination control */
		$.extend($.fn.dataTableExt.oPagination, {
			"bootstrap" : {
				"fnInit" : function(oSettings, nPaging, fnDraw) {
					var oLang = oSettings.oLanguage.oPaginate;
					var fnClickHandler = function(e) {
						e.preventDefault();
						if (oSettings.oApi._fnPageChange(oSettings, e.data.action)) {
							fnDraw(oSettings);
						}
					};

					$(nPaging).addClass('pagination').append(
							'<ul>' + '<li class="prev disabled"><a href="#">&larr; ' + oLang.sPrevious + '</a></li>'
									+ '<li class="next disabled"><a href="#">' + oLang.sNext + ' &rarr; </a></li>'
									+ '</ul>');
					var els = $('a', nPaging);
					$(els[0]).bind('click.DT', {
						action : "previous"
					}, fnClickHandler);
					$(els[1]).bind('click.DT', {
						action : "next"
					}, fnClickHandler);
				},

				"fnUpdate" : function(oSettings, fnDraw) {
					var iListLength = 5;
					var oPaging = oSettings.oInstance.fnPagingInfo();
					var an = oSettings.aanFeatures.p;
					var i, j, sClass, iStart, iEnd, iHalf = Math.floor(iListLength / 2);

					if (oPaging.iTotalPages < iListLength) {
						iStart = 1;
						iEnd = oPaging.iTotalPages;
					} else if (oPaging.iPage <= iHalf) {
						iStart = 1;
						iEnd = iListLength;
					} else if (oPaging.iPage >= (oPaging.iTotalPages - iHalf)) {
						iStart = oPaging.iTotalPages - iListLength + 1;
						iEnd = oPaging.iTotalPages;
					} else {
						iStart = oPaging.iPage - iHalf + 1;
						iEnd = iStart + iListLength - 1;
					}

					for (i = 0, iLen = an.length; i < iLen; i++) {
						// Remove the middle elements
						$('li:gt(0)', an[i]).filter(':not(:last)').remove();

						// Add the new list items and their event handlers
						for (j = iStart; j <= iEnd; j++) {
							sClass = (j == oPaging.iPage + 1) ? 'class="active"' : '';
							$('<li ' + sClass + '><a href="#">' + j + '</a></li>').insertBefore($('li:last', an[i])[0])
									.bind(
											'click',
											function(e) {
												e.preventDefault();
												oSettings._iDisplayStart = (parseInt($('a', this).text(), 10) - 1)
														* oPaging.iLength;
												fnDraw(oSettings);
											});
						}

						// Add / remove disabled classes from the static elements
						if (oPaging.iPage === 0) {
							$('li:first', an[i]).addClass('disabled');
						} else {
							$('li:first', an[i]).removeClass('disabled');
						}

						if (oPaging.iPage === oPaging.iTotalPages - 1 || oPaging.iTotalPages === 0) {
							$('li:last', an[i]).addClass('disabled');
						} else {
							$('li:last', an[i]).removeClass('disabled');
						}
					}
				}
			}
		});

	};

	this.initDataTable = function(table) {
		table.dataTable({
			"bStateSave" : true,
			"sPaginationType" : "bootstrap",
			"oLanguage" : {
				"sLengthMenu" : "_MENU_ records per page"
			}
		});
	};
};

var dataTable = new DataTable();
