/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.SimpleXmlSerializer;
import org.htmlcleaner.TagNode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @author Adam Warski (adam at warski dot org)
 * @author Libor Krzyzanek
 */
@Named
@ApplicationScoped
public class StringTools {

	public static String ensureHttp(String url) {
		if (url.startsWith("https")) {
			return "http" + url.substring(5, url.length());
		}
		return url;
	}

	public static String convertTitleToLink(String title) {
		if (title == null) {
			return null;
		}

		char[] titleWithUnderscores = title.toLowerCase().replaceAll("[^a-z0-9_]", "_").toCharArray();

		StringBuffer newTitle = new StringBuffer();

		// Removing _ from the beginning.
		int titleIndex = 0;
		while ((titleIndex < titleWithUnderscores.length) && (titleWithUnderscores[titleIndex] == '_')) {
			titleIndex++;
		}

		// Removing multiple _ in the text.
		boolean previousLetter = true;
		while (titleIndex < titleWithUnderscores.length) {
			if (titleWithUnderscores[titleIndex] == '_') {
				if (previousLetter) {
					newTitle.append(titleWithUnderscores[titleIndex]);
				}

				previousLetter = false;
			} else {
				newTitle.append(titleWithUnderscores[titleIndex]);
				previousLetter = true;
			}

			titleIndex++;
		}

		// Removing _ from the end, if there was one.
		if ((newTitle.length() > 0) && (newTitle.charAt(newTitle.length() - 1) == '_')) {
			newTitle.deleteCharAt(newTitle.length() - 1);
		}

		return newTitle.toString();
	}

	public static String stripHtml(String html) {
		// it's needed to convert escaped characters to unicode values and remove such characters within PRE tag
		html = StringEscapeUtils.unescapeHtml(html);
		Document doc = Jsoup.parse(html);

		return doc.text();
	}

	public static boolean isValidXml(String html) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);

		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// Logger.getLogger(StringTools.class).error(e);
			return false;
		}

		builder.setErrorHandler(new ErrorHandler() {
			public void warning(SAXParseException exception) throws SAXException {
				throw exception;
			}

			public void error(SAXParseException exception) throws SAXException {
				throw exception;
			}

			public void fatalError(SAXParseException exception) throws SAXException {
				throw exception;
			}
		});

		try {
			builder.parse(new InputSource(new StringReader(html)));
			return true;
		} catch (SAXException e) {
			return false;
		} catch (IOException e) {
			// Logger.getLogger(StringTools.class).error(e);
			return false;
		}
	}

	public static String checkAndFixHtml(String html) {
		return checkAndFixHtml(html, false);
	}

	public static String checkAndFixHtml(String html, boolean forceCleaning) {
		if (StringUtils.isBlank(html)) {
			return html;
		}

		String htmlToCheck = "<div>" + html + "</div>";

		if (!forceCleaning && isValidXml(htmlToCheck)) {
			return html;
		}

		CleanerProperties props = new CleanerProperties();
		props.setOmitHtmlEnvelope(true);
		props.setOmitXmlDeclaration(true);
		props.setRecognizeUnicodeChars(false);
		props.setTranslateSpecialEntities(false);
		props.setAdvancedXmlEscape(true);

		// Very important to use empty element tags especially for <script></script> case.
		// More info: ORG-1367
		props.setUseEmptyElementTags(false);
		props.setUseCdataForScriptAndStyle(false);

		HtmlCleaner cleaner = new HtmlCleaner(props);
		try {
			TagNode rootTag = cleaner.clean(htmlToCheck);

			SimpleXmlSerializer serializer = new SimpleXmlSerializer(props);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			// Remove earliry added enclosing <div>
			serializer.writeToStream(rootTag, baos);
			String ret = baos.toString();

			// Removing the <div> and </div>
			// We have to use this substring instead of rootTag.getChildren().get(0); because it can return ContentTag
			ret = ret.substring(5, ret.length() - 6);

			return ret;
		} catch (IOException e) {
			return html;
		}
	}

	public static String createSummary(String s, int length) {
		if (s == null) {
			return null;
		}

		s = stripHtml(s);

		if (s.length() > length) {
			s = s.substring(0, length);
			return s + "...";
		} else {
			return s;
		}
	}

}
