import java.util.*;
import java.io.*;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.DOMException;

/**
 * A <code>Book</code> object represents a book. It has a title,
 * at least one author and a covertype. Optionally it could have
 * a publish date and ISBN.
 * 
 * @author amac
 * @version 1.0
 *
 */
//public class Book implements Comparable, Comparator {
public class Book {

	/*
	 * A "hardcover" book
	 */
	static final int HARDCOVER = 0;
	/*
	 * A "softcover" book
	 */
	static final int SOFTCOVER = 1;

	/*
	 * Any cover of book
	 */
	static final int ANYCOVER = 2;

	/*
	 * Cover limits
	 */
	static final int MAXREALCOVERS = SOFTCOVER + 1;
	static final int MAXALLCOVERS = ANYCOVER + 1;

	/*
	 * Cover names
	 */
	static final String[] COVERNAME =
		{ "Hard Cover", "Soft Cover", "Any Cover" };

	/*
	 * An invalid publish date indicator
	 */
	static final int BADDATE = -1;

	/*
	 * An invalid book cover indicator
	 
	 */
	static final int BADCOVER = -1;

	/*
	 * Maximum number of authors for a book
	 */
	static final int MAXAUTHORS = 10;

	/*
	 * XML tagnames used by the book object
	 */
	public static final String TAGNAME_BOOK = "book";
	private static final String TAGNAME_COVERTYPE = "covertype";
	private static final String TAGNAME_TITLE = "title";
	private static final String TAGNAME_SERIES = "series";
	private static final String TAGNAME_YEAR = "year";
	private static final String TAGNAME_ISBN = "isbn";

	// Book properties
	protected String title;
	protected LinkedList<Author> authors;
	protected String ISBN;
	protected String series;
	protected int publishYear;
	protected int coverType;

	// Constuctors

	// Private constructor helper function to populate data
	// in book object
	private void initBookData(
		String booktitle,
		String series,
		String isbn,
		int publishyear,
		int cover) {
		title = booktitle;
		if ((series == null) || (series.equals("")))
			this.series = null;
		else
			this.series = series;
		if ((isbn == null) || isbn.equals(""))
			ISBN = null;
		else
			ISBN = isbn;
		publishYear = publishyear;
		coverType = cover;
		if ((coverType < HARDCOVER) || (coverType > SOFTCOVER))
			coverType = SOFTCOVER;
	}

	/**
	* Creates a new <code>Book</code> object
	* 
	* @param title Title of the book
	* @param author Author of the book
	* @param ISBN The ISBN of the book
	* @param publishyear The year the book was published
	*/
	public Book(
		String booktitle,
		String series,
		String author,
		String isbn,
		int publishyear,
		int cover)
		throws IllegalArgumentException {

		if (booktitle == null)
			throw new IllegalArgumentException("A Book must have a title");
		if (author == null)
			throw new IllegalArgumentException("A Book must have an author");

		initBookData(booktitle, series, isbn, publishyear, cover);

		authors = new LinkedList<Author>();
		Author bookauthor = new Author(author);
		authors.add(bookauthor);

	};

	/**
	 * Creates a new <code>Book</code> object
	 * 
	 * @param title Title of the book
	 * @param author Authors of the book
	 * @param ISBN The ISBN of the book
	 * @param publishyear The year the book was published
	 */
	public Book(
		String booktitle,
		String series,
		String[] bookauthors,
		String isbn,
		int publishyear,
		int cover)
		throws IllegalArgumentException, IndexOutOfBoundsException {

		if (booktitle == null)
			throw new IllegalArgumentException("A Book must have a title");
		if (bookauthors == null)
			throw new IllegalArgumentException("A Book must have an author");
		if (bookauthors.length > MAXAUTHORS)
			throw new IndexOutOfBoundsException(
				"A Book cannot have more than "
					+ String.valueOf(MAXAUTHORS)
					+ " authors");

		initBookData(booktitle, series, isbn, publishyear, cover);

		authors = new LinkedList<Author>();
		Author bookauthor;
		for (int i = 0; i < bookauthors.length; i++) {
			bookauthor = new Author(bookauthors[i]);
			authors.add(bookauthor);
		}
	};

	/**
	 * Creates a new <code>Book</code> object
	 * 
	 * @param title Title of the book
	 * @param author Author of the book
	 * @param ISBN The ISBN of the book
	 * @param publishyear The year the book was published
	 */
	public Book(
		String booktitle,
		String series,
		LinkedList<Author> bookauthors,
		String isbn,
		int publishyear,
		int cover) {

		if (booktitle == null)
			throw new IllegalArgumentException("A Book must have a title");
		if (bookauthors == null)
			throw new IllegalArgumentException("A Book must have an author");
		if (bookauthors.size() > MAXAUTHORS)
			throw new IndexOutOfBoundsException(
				"A Book cannot have more than "
					+ String.valueOf(MAXAUTHORS)
					+ " authors");

		initBookData(booktitle, series, isbn, publishyear, cover);

		authors = new LinkedList<Author>(bookauthors);

	};

	/**
	 * Creates a new book object by parsing the passed XML node which is assumed
	 * to contain the XML definition of a book object.
	 * 
	 * @param n The XML node containing the XML definition of a book
	 * @throws DOMException If XML structure contains an invalid node
	 */
	public Book(Node bookNode) throws DOMException {

		authors = new LinkedList<Author>();
		NodeList childNodes = bookNode.getChildNodes();
		for (int i = 0; i < childNodes.getLength();) {
			Node n = childNodes.item(i);
			int type = n.getNodeType();
			switch (type) {
				case Node.ELEMENT_NODE :
					{
						boolean invalidtag = true;
						String n_name = n.getNodeName();
						Node child = n.getFirstChild();
						if (n_name.equals(TAGNAME_TITLE)) {
							invalidtag = false;
							title = child.getNodeValue();
						}
						if (n_name.equals(TAGNAME_SERIES)) {
							invalidtag = false;
							series = child.getNodeValue();
						}
						if (n_name.equals(TAGNAME_ISBN)) {
							invalidtag = false;
							ISBN = child.getNodeValue();
						}
						if (n_name.equals(TAGNAME_YEAR)) {
							invalidtag = false;
							publishYear =
								Integer.parseInt(child.getNodeValue());
						}
						if (n_name.equals(TAGNAME_COVERTYPE)) {
							invalidtag = false;
							for (int k = 0; k < COVERNAME.length; k++)
								if ((child.getNodeValue())
									.equals(COVERNAME[k]))
									coverType = k;
						}
						if (n_name.equals(Author.TAGNAME_AUTHOR)) {
							invalidtag = false;
							Author author;
							try {
								author = new Author(n);
							} catch (DOMException e) {
								throw e;
							}
							authors.add(author);
						};
						i = i + 2; // Need to skip the closing tag

						if (invalidtag) // Unknown node name for book definition
							throw new DOMException(
								DOMException.NOT_SUPPORTED_ERR,
								"Invalid XML node name for defining a book: "
									+ n.getNodeName());
						break;
					}
				case Node.TEXT_NODE :
					{
						// Skip these as they should only be blank space
						i++;
						break;
					}
				default :
					{
						throw new DOMException(
							DOMException.NOT_SUPPORTED_ERR,
							"Invalid XML node type for defining a book: "
								+ n.getNodeType());

					}
			};
		};
	};

	// Accessor Methods

	/** 
	 * Get the title of a book
	 * @return The complete title of this book
	 */
	public String getTitle() {
		return title;
	};
	/** 
	 * Get the series of a book
	 * @return The complete series of this book
	 */
	public String getSeries() {
		return series;
	};
	/**
	 * Get the list of authors of a book
	 * @return The complete list of authors of this book
	 */
	public LinkedList<Author> getAuthors() {
		return authors;
	};
	/**
	 * Get the ISBN of a book
	 * @return The ISBN of this book
	 */
	public String getISBN() {
		return ISBN;
	};
	/**
	 * Get the publish date of a book
	 * @return The publish date of this book
	 */
	public int getPublishYear() {
		return publishYear;
	};

	/** 
	 * Get the cover type of the book
	 * @return The cover type of the book
	 */
	public int getCoverType() {
		return coverType;
	}

	// Modification Methods

	/**
	 * Determines if the passed book object is different from the current
	 * book object. Any differences are incorporated into the current book
	 * object, ie. the current book object values are changed to that of the
	 * passed book object. True is returned if they were different and the 
	 * current book object is modified. False otherwise
	 * 
	 * @param modbook The book object to compare against and use its values
	 *                for updating if different
	 * @return True if the current book object was modified. False otherwise.
	 */
	public boolean isModified(Book modbook) {
		boolean changed = false;

		if (!title.equals(modbook.getTitle())) {
			title = modbook.getTitle();
			changed = true;
		}
		if ((series != null) && (modbook.getSeries() != null)) {
			if (!series.equals(modbook.getSeries())) {
				series = modbook.getSeries();
				changed = true;
			}
		} else if ((series != null) || (modbook.getSeries() != null)) {
			// Case where one or other was null and we can't safely use equals()
			series = modbook.getSeries();
			changed = true;
		}
		if ((ISBN != null) && (modbook.getISBN() != null)) {
			if (!ISBN.equals(modbook.getISBN())) {
				ISBN = modbook.getISBN();
				changed = true;
			}
		} else if ((ISBN != null) || (modbook.getISBN() != null)) {
			ISBN = modbook.getISBN();
			changed = true;
		}
		if (publishYear != modbook.getPublishYear()) {
			publishYear = modbook.getPublishYear();
			changed = true;
		}
		if (coverType != modbook.getCoverType()) {
			coverType = modbook.getCoverType();
			changed = true;
		}
		// Now need to check the author lists against each other. 
		// true if found in original author list
		LinkedList<Author> auth_list = modbook.getAuthors();
		boolean[] new_list = new boolean[auth_list.size()];
		for (int i = 0; i < new_list.length; i++)
			new_list[i] = false;
		// true if found in new author list
		boolean[] orig_list = new boolean[authors.size()];
		for (int i = 0; i < orig_list.length; i++)
			orig_list[i] = false;
		// Loop over the new list
		for (int i = 0; i < auth_list.size(); i++) {
			Author new_auth = (Author) auth_list.get(i);
			// Find this author in the old list and update that author's
			// information if necessary
			for (int k = 0; k < authors.size(); k++) {
				Author orig_auth = (Author) authors.get(k);
				if (new_auth.equals(orig_auth)) {
					new_list[i] = true;
					orig_list[k] = true;
					if (orig_auth.isModified(new_auth))
						changed = true;
				}
			}
		};
		// Any "false" values in the original list indicate authors to delete
		// for this book. This must be done before additions for the mapping to
		// be accurate
		for (int i = 0; i < orig_list.length; i++)
			if (!orig_list[i]) {
				changed = true;
				authors.remove(i);
			}

		// Any "false" values in the new list indicate an author to add
		// for this book
		for (int i = 0; i < new_list.length; i++)
			if (!new_list[i]) {
				changed = true;
				authors.add(auth_list.get(i));
			}

		return changed;
	}

	//	Comparison Methods

	/**
	 * Determines if the passed object is equal to the calling
	 * Book object. They are equal if the ISBNs are equal or
	 * if they do not have an ISBN, their titles are the same.
	 * Used for comparison in list operations.
	 * 
	 * @param y An object.
	 * @return True is equal. False otherwise.
	 */
	public boolean equals(Object o) {
		if (!(o instanceof Book))
			return false;
		Book y = (Book) o;
		if ((ISBN != null) && (y.getISBN() != null))
			return ISBN.equalsIgnoreCase(y.getISBN());
		else
			return title.equalsIgnoreCase(y.getTitle());
	};

	/**
	 * Determines if the passed Book object is equal to the calling
	 * Book object. They are equal if the ISBNs are equal.
	 * Used for comparison in list operations.
	 * 
	 * @param y A Book object.
	 * @return True is equal. False otherwise.
	*/
	public boolean equals(Book y) {
		if ((ISBN != null) && (y.getISBN() != null))
			return ISBN.equalsIgnoreCase(y.getISBN());
		else
			return title.equalsIgnoreCase(y.getTitle());
	};

	/**
	* Determines if the passed Book object is equal to the calling
	* Book object. They are equal if the ISBNs are equal of if they
	* do not have and ISBN, their titles are the same.
	* Used for comparison in list operations.
	* 
	* @param y A Book object.
	* @return True is equal. False otherwise.
	*/
	public boolean equals(Book x, Book y) {
		String x_isbn = x.getISBN();
		String y_isbn = y.getISBN();
		if ((x_isbn != null) && (y_isbn != null))
			return x_isbn.equalsIgnoreCase(y_isbn);
		else
			return (x.getTitle()).equalsIgnoreCase(y.getTitle());
	};

	/**
	 * Compares the passed book object to the current book. Calls the equals
	 * method for equality. Otherwise, the title strings are compared and result
	 * returned
	 * 
	 * @param obj The passed book object to compare against
	 * @return 0 if equal. -1 if current book is less than passed book. 1 if current
	 *           book is greater than the passed book.
	*/
  	public int compareTo(Book obj) {
		if (this.equals(obj))
			return 0;
		else
			return title.compareTo(obj.getTitle());
	}

	// Output methods

	/**
	 * Create a formatted string from the list of <code>Author</code> separated by the passed separator.
	 * 
	 * @param sep The separator to use to separate the author names.
	 * @return The formatted string of Authors.
	 */
	public String getAuthorsString(String sep) {
		String authorsString;
		ListIterator<Author> authorListIterator = authors.listIterator();
		if (authors.size() > 0) {
			Author authorNext;
			authorNext = authorListIterator.next();
			authorsString = authorNext.toString();
			while (authorListIterator.hasNext()) {
				authorNext = authorListIterator.next();
				authorsString = authorsString + sep + authorNext.toString();
			};
		} else {
			authorsString = "";	
		};
		return authorsString;
	}

	/**
	 * Create a valid XML definition for representing the book object.
	 * 
	 * @param indent The initial space indentation for formatting the xml
	 * @return A string with the formated xml book information
	 */
	public String toXML(String indent) {
		class convertXML {
			private String convert(String text) {
				StringBuffer resbuf = new StringBuffer(1024);
				for (int i = 0, k = 0; i < text.length(); i++) {
					char c = text.charAt(i);
					switch (c) {
						case '&' :
							resbuf.insert(k++, '&');
							resbuf.insert(k++, 'a');
							resbuf.insert(k++, 'm');
							resbuf.insert(k++, 'p');
							resbuf.insert(k++, ';');
							break;
						case '<' :
							resbuf.insert(k++, '&');
							resbuf.insert(k++, 'l');
							resbuf.insert(k++, 't');
							resbuf.insert(k++, ';');
							break;
						case '>' :
							resbuf.insert(k++, '&');
							resbuf.insert(k++, 'g');
							resbuf.insert(k++, 't');
							resbuf.insert(k++, ';');
							break;
						default :
							resbuf.insert(k++, c);
					}
				}
				return resbuf.toString();
			}
		}
		String indent1 = indent + " ";
		String xmlString =
			indent
				+ "<"
				+ TAGNAME_BOOK
				+ ">\n"
				+ indent1
				+ "<"
				+ TAGNAME_TITLE
				+ ">"
				+ (new convertXML().convert(title))
				+ "</"
				+ TAGNAME_TITLE
				+ ">\n";
		xmlString =
			xmlString
				+ indent1
				+ "<"
				+ TAGNAME_COVERTYPE
				+ ">"
				+ COVERNAME[coverType]
				+ "</"
				+ TAGNAME_COVERTYPE
				+ ">\n";
		if (series != null)
			xmlString =
				xmlString
					+ indent1
					+ "<"
					+ TAGNAME_SERIES
					+ ">"
					+ series
					+ "</"
					+ TAGNAME_SERIES
					+ ">\n";
		for (int i = 0; i < authors.size(); i++) {
			Author bookauthor = (Author) authors.get(i);
			xmlString = xmlString + bookauthor.toXML(indent1);
		};
		xmlString =
			xmlString
				+ indent1
				+ "<"
				+ TAGNAME_YEAR
				+ ">"
				+ publishYear
				+ "</"
				+ TAGNAME_YEAR
				+ ">\n";
		if (ISBN != null) {
			xmlString =
				xmlString
					+ indent1
					+ "<"
					+ TAGNAME_ISBN
					+ ">"
					+ ISBN
					+ "</"
					+ TAGNAME_ISBN
					+ ">\n";
		}
		xmlString = xmlString + indent + "</" + TAGNAME_BOOK + ">\n";
		return xmlString;
	}

	/**
	 * Outputs the XML definition of the book to the passed Writer object (usually
	 * a file).
	 * 
	 * @param out The writer object where the XML definition will be written
	 * @param indent The initial space indentation for formatting the xml
	 * @throws IOException
	*/
	public void writeXML(Writer out, String indent) throws IOException {
		out.write((toXML(indent)));
	}

	/**
	 * Dump a book's information to the console
	 * 
	*/
	public void consoleOutput() {
		System.out.println("\"" + title + "\"");
		System.out.println("\"" + series + "\"");
		for (int i = 0; i < authors.size(); i++)
			System.out.println("  " + authors.get(i).toString());
		System.out.println(publishYear);
		System.out.println(ISBN);
	};
}
