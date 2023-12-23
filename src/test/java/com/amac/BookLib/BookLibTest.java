package com.amac.BookLib;

import java.util.*;

public class BookLibTest {

	public static void main(String agrs[]) {
		Book a, b, c, d;
		LinkedList authors;
		Author auth1, auth2;
		BookLibrary mylibrary = new BookLibrary();
		String[] authlist = { "Greg MacLeod", "Andrew R. MacLeod" };

		a =
			new Book(
				"A new book",
				"Series 1",
				"Andrew R. MacLeod",
				"123-456",
				2002,
				Book.HARDCOVER);
		a.consoleOutput();
		b =
			new Book(
				"A second book",
				"Series 1",
				authlist,
				"667-667",
				2003,
				Book.SOFTCOVER);
		b.consoleOutput();
		System.out.print(a.toXML(""));
		System.out.print(b.toXML(""));
		c =
			new Book(
				"A new book",
				"Series 1",
				"Andrew R. MacLeod",
				"123-456",
				2002,
				Book.HARDCOVER);
		d =
			new Book(
				"A new book",
				"Series 1",
				"Andrew R. MacLeod",
				"123-456",
				2002,
				Book.HARDCOVER);
		authors = a.getAuthors();
		auth1 = (Author) authors.get(0);
		auth1.consoleOutput();
		((Author) authors.get(0)).consoleOutput();
		if (authors.contains(auth1))
			System.out.print("Found " + auth1.toString() + "\n");
		else
			System.out.print(
				"### ERROR ### Did not find " + auth1.toString() + "\n");
		authors = b.getAuthors();
		if (authors.contains(auth1))
			System.out.print("Found " + auth1.toString() + "\n");
		else
			System.out.print(
				"### ERROR ### Did not find " + auth1.toString() + "\n");
		if (c.equals(d))
			System.out.println("Books the same");
		else
			System.out.println("### ERROR ### Books NOT the same");
		if (c.equals(a))
			System.out.println("Books the same");
		else
			System.out.println("### ERROR ### Books NOT the same");

		GenTestBookLib testdata = new GenTestBookLib();
		mylibrary = testdata.getBookLib();
		mylibrary.addBook(c);
		System.out.println("---Added book--");
		mylibrary.consoleOutput();
		if (mylibrary.containsBook(c))
			System.out.println("Found book c in library");
		else
			System.out.println("### ERROR ### Library does not contain book c");
		mylibrary.removeBook(c);
		System.out.println("--- Removed book ---");
		mylibrary.consoleOutput();

		
	};
}
