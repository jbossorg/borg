/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.util;

import java.io.*;
import java.util.List;
import java.util.Collections;
import java.util.Date;

/**
 * @author Adam Warski (adam at warski dot org)
 */
public class GeneralTools {
	public static boolean objectsEqual(Object o1, Object o2) {
		if (o1 == null) {
			return o2 == null;
		} else {
			return o1.equals(o2);
		}
	}

	private static final int TRANSFER_BUFFER_SIZE = 2048;

	/**
	 * Transferes all bytes from the given input stream to the given output stream.
	 * 
	 * @param is
	 *            Input stream to read from.
	 * @param w
	 *            Printwriter to write to.
	 * @throws java.io.IOException
	 *             In case of an IO exception.
	 */
	public static void transfer(InputStream is, Writer w) throws IOException {
		char[] buffer = new char[TRANSFER_BUFFER_SIZE];
		int read;
		InputStreamReader isr = new InputStreamReader(is);
		while ((read = isr.read(buffer)) != -1) {
			w.write(buffer, 0, read);
		}
	}

	/**
	 * Transferes all bytes from the given reader to the writer
	 * 
	 * @param r
	 *            Reader to read from.
	 * @param w
	 *            Writer to write to.
	 * @throws java.io.IOException
	 *             In case of an IO exception.
	 */
	public static void transfer(Reader r, Writer w) throws IOException {
		char[] buffer = new char[TRANSFER_BUFFER_SIZE];
		int read;
		while ((read = r.read(buffer)) != -1) {
			w.write(buffer, 0, read);
		}
	}

	/**
	 * Transferes all bytes from the given input stream to the given output stream.
	 * 
	 * @param is
	 *            Input stream to read from.
	 * @param os
	 *            Output stream to write to.
	 * @throws IOException
	 *             In case of an IO exception.
	 */
	public static void transfer(InputStream is, OutputStream os) throws IOException {
		byte[] buffer = new byte[TRANSFER_BUFFER_SIZE];
		int read;
		while ((read = is.read(buffer)) != -1) {
			os.write(buffer, 0, read);
		}
	}

	public static <T> int safeCompare(Comparable<T> o1, T o2) {
		if (o1 == null) {
			if (o2 == null) {
				return 0;
			}

			return 1;
		} else if (o2 == null) {
			return -1;
		} else {
			return o1.compareTo(o2);
		}
	}

	public static <T> List<T> subList(List<T> list, int from, int to) {
		if (from > to) {
			return Collections.emptyList();
		}

		if (from > list.size()) {
			return Collections.emptyList();
		}

		return list.subList(from, Math.min(to, list.size()));
	}

	public static <T> void moveElement(List<T> list, int from, int to) {
		if (from < 0 || from > list.size() || to < 0 || to > list.size()) {
			return;
		}

		if (from == to) {
			return;
		}

		T toMove = list.get(from);

		if (from < to) {
			int currentIndex = from;
			while (currentIndex != to) {
				list.set(currentIndex, list.get(currentIndex + 1));
				currentIndex++;
			}

			list.set(to, toMove);
		} else {
			int currentIndex = from;
			while (currentIndex != to) {
				list.set(currentIndex, list.get(currentIndex - 1));
				currentIndex--;
			}

			list.set(to, toMove);
		}
	}

	public static int compareDates(Date date1, Date date2) {
		long seconds1 = (date1 == null) ? 0 : (date1.getTime() / 1000);
		long seconds2 = (date2 == null) ? 0 : (date2.getTime() / 1000);

		if (seconds1 == seconds2) {
			return 0;
		} else if (seconds1 > seconds2) {
			return 1;
		} else {
			return -1;
		}
	}

	public static int compareStrings(String str1, String str2) {
		if (str1 == null) {
			if (str2 == null) {
				return 0;
			} else {
				return -1;
			}
		} else if (str2 == null) {
			return 1;
		} else {
			return str1.compareTo(str2);
		}
	}

	public static String readInputStream(InputStream is) throws IOException {
		StringBuffer contents = new StringBuffer();

		BufferedReader input = new BufferedReader(new InputStreamReader(is));
		try {
			String line;
			while ((line = input.readLine()) != null) {
				contents.append(line);
				contents.append(System.getProperty("line.separator"));
			}
		} finally {
			input.close();
		}

		return contents.toString();
	}

	public static String getExceptionStackTrace(Exception e) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintWriter pw = new PrintWriter(baos);

		e.printStackTrace(pw);

		pw.flush();
		return baos.toString();
	}
}
