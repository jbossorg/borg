/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class StringToolsTest {

	@Test
	public void testIsEmpty() {
	}

	@Test
	public void testSafeToString() {
	}

	@Test
	public void testConvertTitleToLink() {
	}

	@Test
	public void testStripHtml() {
		assertEquals("Some text.", StringTools.stripHtml("<p>Some text.</p>"));
		assertEquals("This: < shouldn't get removed.", StringTools.stripHtml("This: < shouldn't get removed."));
		assertEquals("Non-terminated tags. And now this!. >",
				StringTools.stripHtml("<p>Non-terminated tags. <a> And now this!. >"));
		assertEquals("Tags with spaces, and some not terminated .",
				StringTools.stripHtml("<p > Tags with spaces, and some <a > not terminated </a>."));
		assertEquals(
				"Escaped characters within PRE",
				StringTools
						.stripHtml("<pre>&lt;pool&gt; Escaped characters within PRE &lt;allow-multiple-users/&gt;&lt;/pool&gt;</pre>"));
	}

	@Test
	public void testIsValidXml() {
	}

	@Test
	public void testCheckAndFixHtml() {
		assertEquals("<div><p></p></div>", StringTools.checkAndFixHtml("<div><p></div></p>", true));
		assertEquals("<p>Some text.</p>\n", StringTools.checkAndFixHtml("<p>Some text.</p>\n", true));
		assertEquals("<p><i>Some text.</i>\n</p>\n", StringTools.checkAndFixHtml("<p><i>Some text.</i>\n</p>\n", true));
		assertEquals("Some text.", StringTools.checkAndFixHtml("Some text.", true));
		final String scriptTagTest = "<script src=\"https://gist.github.com/2343383.js?file=nQueensScoreRules\"></script>";
		assertEquals(scriptTagTest, StringTools.checkAndFixHtml(scriptTagTest, true));
		assertEquals("Special characters: \u03C0", StringTools.checkAndFixHtml("Special characters: \u03C0", true));
	}

	@Test
	public void testCreateSummary() {
	}

	@Test
	public void testEscape() {
	}

	@Test
	public void testUnescapeDoubleEscapedString() {
	}

}
