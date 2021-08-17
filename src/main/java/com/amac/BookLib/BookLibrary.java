import java.util.*;
import java.io.*;
import java.nio.CharBuffer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.DOMException;
import java.util.regex.Pattern;

/**
 * A <code>BookLibrary</code> holds a linked list of <code>Book</code>
 * objects and constructs a linked list of <code>Author</code> objects
 * from the authors of the books. It allows for the insertion,
 * deletion and modification of books in the list. The book and
 * author lists can be searched using a <code>BookSearchObject</code>.
 * Most commonly, it is created from a file containing the xml
 * definition of the book library. Any changes should also be written 
 * back to that file. Books are sorted in the library based on the book
 * title.
 * 
 * @author amac
 * @version 1.0
 *
 */
public class BookLibrary {

	// Book Library constants
	public static final String TAGNAME_XML = "<?xml version='1.0'?>\n";
	public static final String TAGNAME_STYLESHEET =
		"<?xml-stylesheet type='text/xsl' href='booklibrary.xsl'?>\n";
	public static final String TAGNAME_NAMESPACE =
		" xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:noNamspaceSchemaLocation='booklibrary.xsd'";
	public static final String TAGNAME_BOOKLIBRARY = "booklibrary";
	public static final String EXT_BOOKLIBRARY = ".bdb";
	public static final String EXT_BOOKLIBRARY_BACKUP = "bak";

	// Book Library Members
	private LinkedList<Book> bookList;
	private LinkedList<Author> authorList;

	//	Static Methods

	/*
	 * File filter to only show book library data files with the
	 * appropriate extention.
	 */
	static public class fileNameFilter implements FilenameFilter {
		public boolean accept(File dir, String name) {
			return name.endsWith(EXT_BOOKLIBRARY);
		}

	}

	// Constructors

	/** 
	 * Create a blank book library object.
	 */
	public BookLibrary() {
		bookList = new LinkedList<Book>();
		authorList = new LinkedList<Author>();
	};

	/**
	 * Create a book library by reading from the passed file descriptor. It is
	 * assumed that the file is in xml and defines a book library. If this is not
	 * the case, and exception will be thrown and maybe a stack trace dumped
	 * depending on where the error occurred.
	 * 
	 * @param f File descriptor of the file containing the
	 */
	public BookLibrary(File f) throws DOMException {
		bookList = new LinkedList<Book>();
		authorList = new LinkedList<Author>();

		Document doc = null;
		try {
			DocumentBuilderFactory factory =
				DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			doc = builder.parse(f.getPath());
		} catch (Exception e) {
			e.printStackTrace(System.err);
			throw new DOMException(
				DOMException.INVALID_STATE_ERR,
				"Unable to parse file");
		}

		NodeList bookNodes = doc.getElementsByTagName(Book.TAGNAME_BOOK);
		for (int i = 0; i < bookNodes.getLength(); i++) {
			Book book;
			Node n = bookNodes.item(i);
			try {
				book = new Book(n);
			} catch (DOMException e) {
				throw e;
			}
			addBook(book);
		}

	};

	/** 
	 * Create a book library from the passed list of books and authors. The
	 * list of books is then sorted by title.
	 * 
	 * @param booklist A linked list of book objects
	 * @param authorlist A linked list of author objects
	 */
	public BookLibrary(LinkedList<Book> booklist, LinkedList<Author> authorlist) {
		bookList = booklist;
		authorList = authorlist;
		Collections.sort(bookList);
	};

	// Accessor Methods

	/**
	 * Returns the all Authors from the managed author list which 
	 * contains the passed last name.
	 * 
	 * @param lastname The last name of the author to find.
	 */
	public Author[] findAuthorByLastName(String lastname) {
		LinkedList<Author> auths = new LinkedList<>();
		Author a;
		String lname;
		for (int i = 0; i < authorList.size(); i++) {
			a = (Author) authorList.get(i);
			lname = a.getLastName();
			if (lastname.equalsIgnoreCase(lname)) {
				auths.add(a);
			}
		};
		return (Author[]) auths.toArray();

	};

	/**
	 * Returns the Author from the managed author list which 
	 * contains the passed name. There should only be a single match.
	 * 
	 * @param fullname The name of the author to find.
	 */
	public Author findAuthorByName(String fullname) {
		Author a = new Author(fullname);
		int index = authorList.indexOf(a);
		if (index < 0) {
			return null;
		} else {
			return (Author) authorList.get(index);
		}
	};

	/**
	 * Returns the Author from the managed author list which 
	 * contains the passed author. There should only be a single match.
	 * 
	 * @param Author The author to find.
	 * @return The matched Author in the author list. Otherwise, null.
	 */
	public Author findAuthor(Author author) {
		int index = authorList.indexOf(author);
		if (index < 0) {
			return null;
		} else {
			return (Author) authorList.get(index);
		}
	};

	/**
	 * Returns the book at index i in the book list.
	 * 
	 * @param i Index into book list
	 * @return The Book object at that index
	 */
	public Book getBook(int i) {
		return (Book) bookList.get(i);
	}

	/**
	 * Return the linked list of books in the book library.
	 * 
	 * @return Linked list of book objects in the library
	 */
	public LinkedList<Book> getBookList() {
		return this.bookList;
	};

	/**
	 * Returns the number of books in the library
	 * 
	 * @return Number of books in the library
	 */
	public int length() {
		return bookList.size();
	}

	/**
	 * Returns an iterator over the list of books held in the library
	 * 
	 * @return Iterator over list of books in the library
	 */
	public ListIterator<Book> booklistIterator() {
		return bookList.listIterator();
	}

	// Modification Methods

	/**
	 * First checks if the passed Book object is already in the book list.
	 * If it is already in the book list, an exception is thrown. 
	 * Otherwise, the book is added to the managed list of books and
	 * the author is added/updated to the managed author list. The addition is
	 * done in order based on the comparators of the book and author lists
	 * 
	 * @param newbook The Book object to add to the managed book list
	 * @throws IllegalStateException If book already in library
	 */
	public void addBook(Book newbook) throws IllegalStateException {
		if ((bookList != null) && (bookList.contains(newbook))) {
			consoleOutput();
			System.out.println(newbook.getISBN());
			throw new IllegalStateException(
				"Book already exists in library: " + newbook.getTitle());
		}

		// Loop through the book list finding the correct spot to insert the book
		int i = 0;
		boolean found = false;
		while ((i < bookList.size()) && (!found)) {
			Book b = (Book) bookList.get(i);
			if (b.compareTo(newbook) == 1)
				found = true;
			else
				i++;
		}
		bookList.add(i, newbook);
		LinkedList<Author> auths = newbook.getAuthors();
		for (i = 0; i < auths.size(); i++) {
			Author a = (Author) auths.get(i);
			a.addBook(newbook);
			if (!authorList.contains(a))
				authorList.add(a);
		}
	};

	/**
	 * Remove the passed book from the library of books. Also update the author list
	 * to remove that book from being associated with that author.
	 * 
	 * @param delbook Book to remove from the library
	 * @throws IllegalStateException If book is removed but the author does have
	 *                               the book in their list of authored books
	 */
	public void removeBook(Book delbook) throws IllegalStateException {
		if (bookList != null) {
			if (bookList.contains(delbook)) {
				LinkedList<Author> auths = delbook.getAuthors();
				//dumpAuthors();
				for (int i = 0; i < auths.size(); i++) {
					Author auth = (Author) auths.get(i);
					if (!auth.removeBook(delbook))
						throw new IllegalStateException(
							"Book not found for author " + auth.toString());
					if (auth.getBooks().size() == 0)
						authorList.remove(auth);
				};
				bookList.remove(delbook);

			} else
				throw new IllegalStateException("Book not found");
		}
	}

	/**
	 * Checks if the passed author is already in the managed author
	 * list. If the author is already in the list, the entry is updated
	 * to add the passed Book. Otherwise, the author is added to the
	 * managed author list with the passed Book.
	 * 
	 * @param newauthor The author to update/add to the managed author list
	 * @param newbook The book written by the newauthor
	 */
	private void addAuthor(Author newauthor, Book newbook) {
		int index = authorList.indexOf(newauthor);
		if (index < 0) {
			newauthor.addBook(newbook);
			authorList.add(newauthor);
		} else {
			Author a = (Author) authorList.get(index);
			a.addBook(newbook);
		}

	};

	// Search Methods

	/**
	 * Returns true if the passed book is in the book library. 
	 * 
	 * @param book The book to find.
	 * @return True if book is in list, false otherwise.
	 */
	public boolean containsBook(Book book) {
		return bookList.contains(book);
	};

	/**
	 * Returns a linked list of book objects from the complete list of
	 * books objects in the book library that matched the search criteria
	 * specified in the search object.
	 * 
	 * @param searchObject
	 * @param caseInsensitive If we should use a case insensitive search
	 * @return Linked list of books matching the search criteria
	 */
	public LinkedList<Book> searchResults(BookSearchObject searchObject, boolean caseInsensitive) {

		// Farm the search off to the appropriate private method based
		// on the type of search in the search object
		if (searchObject.getSearchType() == BookSearchObject.BOOKSEARCH)
			return bookSearchResults(searchObject, caseInsensitive);
		else if (searchObject.getSearchType() == BookSearchObject.AUTHORSEARCH)
			return authorSearchResults(searchObject, caseInsensitive);
		else if (searchObject.getSearchType() == BookSearchObject.SERIESSEARCH)
			return seriesSearchResults(searchObject, caseInsensitive);
		else
			return null;
	}

	/**
	 * Search through the book list of the library for books which match the book
	 * information search criteria specified in the search object. If multiple
	 * pieces of search information are specified, all must match for a result
	 * to be returned.
	 * 
	 * @param searchObject
	 * @param caseInsensitive If we should use a case insensitive search
	 * @return Linked list of books matching the book information search criteria
	 */
	private LinkedList<Book> bookSearchResults(BookSearchObject searchObject, boolean caseInsensitive) {
		LinkedList<Book> resultList = new LinkedList<>();
		for (int i = 0; i < bookList.size(); i++) {

			// If anything in the book search object does not
			// match with the book, the book is *not* added to the
			// returned result list
			boolean addToList = true;

			Book b = (Book) bookList.get(i);

			if ((searchObject.getCoverType() != Book.BADCOVER)
				&& (searchObject.getCoverType() != Book.ANYCOVER))
				if (searchObject.getCoverType() != b.getCoverType())
					addToList = false;

			if (searchObject.getDate() != Book.BADDATE)
				if (searchObject.getDate() != b.getPublishYear())
					addToList = false;

			if (!searchObject.getISBN().equals(""))
				if (!BookSearchObject
					.WildCardMatch(searchObject.getISBN(), b.getISBN(), caseInsensitive))
					addToList = false;

			if (!searchObject.getTitle().equals(""))
				if (!BookSearchObject
					.WildCardMatch(searchObject.getTitle(), b.getTitle(), caseInsensitive))
					addToList = false;

			if (addToList)
				resultList.add(b);
		}
		return resultList;
	};

	/**
	 * Search through the book list of the library for books which match the book
	 * series search criteria specified in the search object
	 * 
	 * @param searchObject
	 * @param caseInsensitive If we should use a case insensitive search
	 * @return Linked list of books matching the book series search criteria
	 */
	private LinkedList<Book> seriesSearchResults(BookSearchObject searchObject, boolean caseInsensitive) {
		LinkedList<Book> resultList = new LinkedList<>();
		for (int i = 0; i < bookList.size(); i++) {

			// If anything in the book search object does not
			// match with the book, the book is *not* added to the
			// returned result list
			boolean addToList = true;

			Book b = (Book) bookList.get(i);
			if (!searchObject.getSeries().equals(""))
				if (!BookSearchObject
					.WildCardMatch(searchObject.getSeries(), b.getSeries(), caseInsensitive))
					addToList = false;

			if (addToList)
				resultList.add(b);
		}
		return resultList;
	};

	/**
	 * Search through the book list of the library for books which match the book
	 * author search criteria specified in the search object. If multiple pieces
	 * of search information are specified, all must match for a result to be 
	 * returned.
	 * 
	 * @param searchObject
	 * @param caseInsensitive If we should use a case insensitive search
	 * @return Linked list of books matching the book author search criteria
	 */
	private LinkedList<Book> authorSearchResults(BookSearchObject searchObject, boolean caseInsensitive) {
		LinkedList<Book> resultList = new LinkedList<>();
		for (int i = 0; i < bookList.size(); i++) {

			// If any Author in the author list of the book matches,
			// the book is added to the list
			Book b = (Book) bookList.get(i);
			LinkedList<Author> auth_list = b.getAuthors();
			boolean authorFound = false;
			for (int j = 0;(!authorFound) && (j < auth_list.size()); j++) {
				boolean foundFirst = true;
				boolean foundLast = true;
				Author auth = (Author) auth_list.get(j);
				if (!searchObject.getFirstName().equals(""))
					if (!BookSearchObject
						.WildCardMatch(
							searchObject.getFirstName(),
							auth.getFirstName(),
							caseInsensitive))
						foundFirst = false;
				if (!searchObject.getLastName().equals(""))
					if (!BookSearchObject
						.WildCardMatch(
							searchObject.getLastName(),
							auth.getLastName(),
							caseInsensitive))
						foundLast = false;
				authorFound = foundFirst && foundLast;
			}

			if (authorFound)
				resultList.add(b);
		}
		return resultList;
	};

	// Output methods

	/**
	 * Writes out the XML definition of the book library to the passed file.
	 * A backup file is first created, over-writing any existing backup file.
	 * None of the I/O exceptions which could be generated by these I/O operations
	 * are caught...they are passed upwards.
	 * 
	 * @param f File to write the book library xml definition
	 */
	public void writeXML(File f) throws FileNotFoundException, IOException {

		// Create backup file
		if (f.exists()) {
			File f_bak = new File(f.getPath() + "." + EXT_BOOKLIBRARY_BACKUP);
			BufferedReader in = new BufferedReader(new InputStreamReader( new FileInputStream(f), "UTF-8"));
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter( new FileOutputStream(f_bak), "UTF-8"));

			// Transfer bytes from in to out
			CharBuffer buf = CharBuffer.allocate(1024);
			while (in.read(buf) > 0) {
				out.append(buf.flip());
				buf.clear();
			}
			in.close();
			out.close();
		}

		FileOutputStream fout = new FileOutputStream(f);
		BufferedWriter bufwriter =
			new BufferedWriter(new OutputStreamWriter(fout, "UTF-8"));

		// Write out the required xml file header information and then
		// the start of the book library xml definition
		bufwriter.write(TAGNAME_XML + TAGNAME_STYLESHEET);
		bufwriter.write("<" + TAGNAME_BOOKLIBRARY + TAGNAME_NAMESPACE + ">\n");

		// Loop through each book and have it write out it's xml definition
		// to the file
		for (int i = 0; i < bookList.size(); i++) {
			Book b = (Book) bookList.get(i);
			b.writeXML(bufwriter, "  ");
			bufwriter.flush();
		}
		bufwriter.write("</booklibrary>\n");
		bufwriter.close();
		fout.close();

	}

	/**
	 * Dump the book library to the console
	 *
	 */
	public void consoleOutput() {
		Author a;
		Book b;
		System.out.println("AUTHORS");
		System.out.println("+++++++");
		for (int i = 0; i < authorList.size(); i++) {
			a = (Author) authorList.get(i);
			a.consoleOutput();
		}
		System.out.println("BOOKS");
		System.out.println("+++++");
		for (int i = 0; i < bookList.size(); i++) {
			b = (Book) bookList.get(i);
			b.consoleOutput();

		}

	};

	/**
	 * Dump extensive information on the authors in the book library
	 * author list to the output console
	 *
	 */
	public void dumpAuthors() {
		System.out.println("Dumping Library Author List");
		System.out.println("+++++++++++++++++++++++++++");
		for (int i = 0; i < authorList.size(); i++) {
			Author a = (Author) authorList.get(i);
			System.out.println(a.toString());
			LinkedList<Book> bookl = a.getBooks();
			System.out.println("--HAS " + bookl.size() + " BOOKS");
			System.out.println("--++++++++++++");
			for (int j = 0; j < bookl.size(); j++) {
				Book b = (Book) bookl.get(j);
				System.out.println("--" + b.getTitle());
			}
		}
	}
};

/**
 * An auxilliary class to allow the search of library book and
 * author lists. There are three types of search criteria which
 * can be used:
 *  1. Book Information Search:
 *       Can optionally define the following information to search 
 *       for matching books:
 *            - "Title" (will do wild card matching)
 *            - "ISBN" (will do wild card matching)
 *            - "Publish Date" (Must be an exact match if specified)
 *            - "Cover type" (Must be an exact match if specified)
 *  2. Author Information Search:
 *       Can optionally define the following information to search
 *       for matching books:
 *            - "First Name" (will do wild card matching)
 *            - "Last Name" (will do wild card matching)
 *  3. Book Series Search:
 *       Can only define the book series to search for matching books
 *       but you can do wild card matching
 * 
 * Wildcard matching will only be done if the correct wildcard is found
 * in the string, <code>BookSearchObject.WILDCARD</code>. There is no
 * way to override the definition of the wildcard character if it is
 * found in the search string (ie. it cannot be "escaped")
 *
 */
class BookSearchObject {

	// An unknown search
	static final int BADSEARCH = -1;

	// Search based on series information
	static final int SERIESSEARCH = 1;

	// Search based on book information
	static final int BOOKSEARCH = 2;

	// Search based on author information
	static final int AUTHORSEARCH = 3;

	// Wildcard character in search string
	static final char WILDCARD = '*';

	private int searchtype;
	private String series;
	private String title;
	private String isbn;
	private int date;
	private int bookcover;
	private String lastname;
	private String firstname;

	/**
	 * The wildcard match method. It takes the search string and looks for
	 * positive match in the passed string. It uses the java regular expression
	 * parser. The wildcard character, if found in the search string, is converted
	 * to a mactch 0 or more characters before being passed to the the regular
	 * expression parser.
	 * 
	 * @param searchStr Search string
	 * @param Str String to look in for a match of the search string
	 * @param caseInsensitive If we should use a case insensitive search
	 * @return True if a match is found. False otherwise (ie. no match)
	 */
	static public boolean WildCardMatch(String searchStr, String Str, boolean caseInsensitive) {

		if ((searchStr == null) || (Str == null))
			return false;

		// Convert the wildcard character to the excepted version
		// for one or more character matches in regular expressions
		int i, k;
		char[] patStrArray = searchStr.trim().toCharArray();
		StringBuffer patStr = new StringBuffer(patStrArray.length * 2);
		for (i = 0, k = 0; i < patStrArray.length; i++, k++) {
			if (patStrArray[i] == WILDCARD) {
				patStr.insert(k, '.');
				k++;
				patStr.insert(k, WILDCARD);
			} else
				patStr.insert(k, patStrArray[i]);

		};

		// Pad, if required, the regular expression to allow a match
		// anywhere in the string
		if (patStr.charAt(0) != '.')
			patStr.insert(0, ".*");
		if (patStr.charAt(i - 1) != WILDCARD)
			patStr.append(".*");

		// Make case insensitive if required
		if (caseInsensitive)
			patStr.insert(0, "(?i)");

		boolean matched = Pattern.matches(patStr.toString(), Str);
		return matched;
	}

	// Constructors

	/**
	 * Create a blank search object which will later be filled
	 * in with the information to use in the search.
	 *
	 */
	public BookSearchObject() {
		searchtype = BADSEARCH;
		series = "";
		title = isbn = "";
		date = Book.BADDATE;
		bookcover = Book.BADCOVER;
		lastname = firstname = "";
	}

	// Setter methods

	/**
	 * Set the type of search to be performed. It will be based on the
	 * information populated in the search object.
	 * 
	 * @param SearchType Type of search information to use in the search.
	 */
	public void setSearchType(int SearchType) {
		switch (SearchType) {
			case SERIESSEARCH :
				searchtype = SERIESSEARCH;
				break;
			case AUTHORSEARCH :
				searchtype = AUTHORSEARCH;
				break;
			case BOOKSEARCH :
				searchtype = BOOKSEARCH;
				break;
			default :
				searchtype = BADSEARCH;
		};
	};

	/**
	 * Set the information for searching based on the passed series 
	 * search string
	 * 
	 * @param Series Book series search string.
	 */
	public void setSeries(String Series) {
		if (Series == null)
			series = "";
		else
			series = Series;
	}

	/**
	 * Set the information for searching based on the passed title
	 * search string.
	 * 
	 * @param Title Book title search string.
	 */
	public void setTitle(String Title) {
		if (Title == null)
			title = "";
		else
			title = Title;
	}

	/**
	 * Set the information for searching based on the passed ISBN
	 * search string.
	 * 
	 * @param ISBN Book ISBN search string.
	 */
	public void setISBN(String ISBN) {
		if (ISBN == null)
			isbn = "";
		else
			isbn = ISBN;
	}

	/**
	 * Set publish date of the book to search on.
	 * 
	 * @param Date Book publish date to search on.
	 */
	public void setDate(int Date) {
		date = Date;
	}

	/**
	 * Set the cover type of the book to search on.
	 * 
	 * @param BookType Cover type of the book to search on.
	 */
	public void setCoverType(int BookType) {
		bookcover = BookType;
	}

	/**
	 * Set information for searching based on the passed
	 * author last name search string.
	 * 
	 * @param LastName Author last name search string.
	 */
	public void setLastName(String LastName) {
		if (LastName == null)
			lastname = "";
		else
			lastname = LastName;
	}

	/** 
	 * Set information for searching based on the passed author
	 * first name search string.
	 * 
	 * @param FirstName Author first name search string.
	 */
	public void setFirstName(String FirstName) {
		if (FirstName == null)
			firstname = "";
		firstname = FirstName;
	}

	// Accessor Methods

	/**
	 * Get the type of search to perform.
	 * @return Type of search to perform. 
	 */
	public int getSearchType() {
		return searchtype;
	}

	/**
	 * Get the series search string.
	 * @return Series search string.
	 */
	public String getSeries() {
		return series;
	}

	/**
	 * Get the book title search string.
	 * @return Book title search string.
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Get the book ISBN search string.
	 * @return Book ISBN search string.
	 */
	public String getISBN() {
		return isbn;
	}

	/**
	 * Get the book publish date to search on.
	 * @return The book publish date to search on.
	 */
	public int getDate() {
		return date;
	}

	/**
	 * Get the book cover type to search on.
	 * @return The book cover type to search on.
	 */
	public int getCoverType() {
		return bookcover;
	}

	/**
	 * Get the author last name search string.
	 * @return The author last name search string.
	 */
	public String getLastName() {
		return lastname;
	}

	/**
	 * Get the author first name search string.
	 * @return The author first name search string.
	 */
	public String getFirstName() {
		return firstname;
	}

	// Output methods

	/**
	 * Dump search information to the console.
	 */
	public void consoleOutput() {
		System.out.println("Search Type:" + searchtype);
		System.out.println("Series:" + series);
		System.out.println("Title:" + title);
		System.out.println("ISBN:" + isbn);
		System.out.println("Date:" + date);
		System.out.println("Book Type:" + bookcover);
		System.out.println("Last Name:" + lastname);
		System.out.println("First Name:" + firstname);

	}
}

/**
 * An auxilliary class to implement the comparison sorting of books
 * based on their authors. If the first author of each book is equal
 * and one (or both) of the books has more than one author, the book
 * with the fewest authors is considered "less" (-1 in Comparator terms). 
 * The authors are compared first by last names. If they are equal, then
 * they are compared by first names with no first name being "less".
 * 
 * The primary purpose of this class is to allow the sorting of a list
 * of books by author.
 */
class AuthorListCompare implements Comparator<Object> {
	public int compare(Object a, Object b) {
		int less = -1;
		int equal = 0;
		int greater = 1;

		Book book_a = (Book) a;
		Book book_b = (Book) b;
		LinkedList<Author> auths_a = book_a.getAuthors();
		LinkedList<Author> auths_b = book_b.getAuthors();

		// Only check the first author in the list even if the book
		// has multiple authors. But the book with the least authors
		// has precedence when the first author matches.
		Author auth_a = (Author) auths_a.get(0);
		Author auth_b = (Author) auths_b.get(0);
		String lastname_a = auth_a.getLastName();
		String lastname_b = auth_b.getLastName();
		if (lastname_a.equals(lastname_b)) {
			String firstname_a = auth_a.getFirstName();
			String firstname_b = auth_b.getFirstName();
			if (firstname_a.equals(firstname_b)) {
				if (auths_a.size() < auths_b.size())
					return less;
				if (auths_a.size() == auths_b.size())
					return equal;
				if (auths_a.size() > auths_b.size())
					return greater;
				return less;
			} else
				return firstname_a.compareTo(firstname_b);
		} else
			return lastname_a.compareTo(lastname_b);
	}
}

/**
 * An auxilliary class to implement the comparison sorting of books
 * based on their publish date. If the publish date of each book is equal
 * the secondary sort is based on book title.
 * 
 * The primary purpose of this class is to allow the sorting of a list
 * of books by publish date.
 */
class PublishDateListCompare implements Comparator<Object> {
	public int compare(Object a, Object b) {
		int less = -1;
		int greater = 1;

		Book book_a = (Book) a;
		Book book_b = (Book) b;
		int publishDate_a = book_a.getPublishYear();
		int publishDate_b = book_b.getPublishYear();

		if (publishDate_a == publishDate_b) {
			// Handle same publish date scenario
			String title_a = book_a.getTitle();
			String title_b = book_b.getTitle();
			return title_a.compareTo(title_b);
		} else {
			 return publishDate_a < publishDate_b ? less : greater;
		}

	}
}

