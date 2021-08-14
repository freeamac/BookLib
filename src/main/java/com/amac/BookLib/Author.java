import java.util.*;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.DOMException;

/**
 * An <code>Author</code> object encapsulates an author's name
 * which is broken down into:
 *   1. an optional title (eg. "Dr.")
 *   2. an optional first name
 *   3. optional middle names
 *   4. a required last name
 *   5. an optional sur-title (eg. "Junior", "IV", "Ed.")
 * and the list of books written by this author.
 * 
 * @author amac
 * @version 1.0
 * 
 */
public class Author {

	// Author constants
	/*
	 * XML tag names used by the Author object
	 */
	public static final String TAGNAME_AUTHOR = "author";
	private static final String TAGNAME_TITLE = "title";
	private static final String TAGNAME_FIRSTNAME = "first";
	private static final String TAGNAME_MIDDLENAME = "middle";
	private static final String TAGNAME_LASTNAME = "last";
	private static final String TAGNAME_SURTITLE = "surtitle";

	// Data members
	protected String title;
	protected String firstname;
	protected String middlename;
	protected String lastname;
	protected String surtitle;
	protected LinkedList books;

	// Constructors
	/**
	 * Creates a new <code>Author</code> object from a string which
	 * assumes an optional first and middle names but a maditory last
	 * name. It also assume no title or sur-title. The book list
	 * written by this author is initialized to null.
	 * 
	 * @param name Full name of the author which is broken down into
	 * potential a first, middle and last names.
	 * @return Author The <code>Author</code> object.
	 */
	public Author(String name) throws IllegalArgumentException {
		if (name == null)
			throw new IllegalArgumentException("Author must have a last name");

		title = firstname = middlename = lastname = surtitle = null;

		StringTokenizer tokedstr = new StringTokenizer(name, " ");
		int tokencount = tokedstr.countTokens();
		if (tokencount == 1) // Only a last name
			lastname = name;
		else {
			if (tokencount == 2) { // Only a first and last name
				firstname = new String(tokedstr.nextToken());
				lastname = new String(tokedstr.nextToken());
			} else { // First, middle and last names
				firstname = new String(tokedstr.nextToken());
				StringBuffer middle = new StringBuffer(name.length());
				// Get the middle name as every thing between first
				// and last tokens
				for (int i = 2; i < tokencount; i++) {
					middle.append(tokedstr.nextToken());
					middle.append(" ");

				};
				middlename = new String(middle);
				lastname = new String(tokedstr.nextToken());
			}
		};
		books = new LinkedList();
	};

	/**
	 * Creates a new <code>Author</code> object from the passed
	 * first and last names. The book list written by this author 
	 * is initialized to null.
	 * 
	 * @param firstname First name of the author.
	 * @param lastname Last name of the author.
	 * @return Author  The <code>Author</code> object.
	 */
	public Author(String firstname, String lastname)
		throws IllegalArgumentException {
		if (lastname == null)
			new IllegalArgumentException("Author must have a last name");
		this.firstname = firstname;
		this.lastname = lastname;
		title = middlename = surtitle = null;
		books = new LinkedList();
	};

	/**
	 * Creates a new <code>Author</code> object from the passed
	 * strings containing all the author name fields. The book list 
	 * written by this author is initialized to null. 
	 * 
	 * @param title Pre-name title of the author.
	 * @param firstname First name of the author.
	 * @param middlename Middle name of the author.
	 * @param lastname Last name of the author.
	 * @param surtitle Post-name title of the author.
	 * @return Author The <code>Author</code> object.
	 */
	public Author(
		String title,
		String firstname,
		String middlename,
		String lastname,
		String surtitle)
		throws IllegalArgumentException {
		if ((lastname == null) || (lastname.equals("")))
			new IllegalArgumentException("Author must have a last name");

		if (title.equals(""))
			this.title = null;
		else
			this.title = title;
		if (firstname.equals(""))
			this.firstname = null;
		else
			this.firstname = firstname;
		if (middlename.equals(""))
			this.middlename = null;
		else
			this.middlename = middlename;
		if (lastname.equals(""))
			this.lastname = null;
		else
			this.lastname = lastname;
		if (surtitle.equals(""))
			this.surtitle = null;
		else
			this.surtitle = surtitle;
		books = new LinkedList();
	};

	/**
	 * Creates a new <code>Author</code> object from the passed Document Object Model 
	 * (XML) node.
	 * 
	 * @param authorNode The DOM node containing the DOM structure/XML to define and
	 *                   Author object.
	 * @throws DOMException If DOM structure/XML contains an invalid element
	 */
	public Author(Node authorNode) throws DOMException {
		title = firstname = middlename = lastname = surtitle = null;
		books = new LinkedList();
		NodeList childNodes = authorNode.getChildNodes();
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
						if (n_name.equals(TAGNAME_FIRSTNAME)) {
							invalidtag = false;
							firstname = child.getNodeValue();
						}
						if (n_name.equals(TAGNAME_MIDDLENAME)) {
							invalidtag = false;
							middlename = child.getNodeValue();
						}
						if (n_name.equals(TAGNAME_LASTNAME)) {
							invalidtag = false;
							lastname = child.getNodeValue();
						}
						if (n_name.equals(TAGNAME_SURTITLE)) {
							invalidtag = false;
							surtitle = child.getNodeValue();
						}
						i = i + 2; // Need to skip the closing tag

						if (invalidtag) // Unknown node name for an author defintion
							throw new DOMException(
								DOMException.NOT_SUPPORTED_ERR,
								"Invalid XML node name for defining an author: "
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
							"Invalid XML node type for defining an author: "
								+ n.getNodeType());
					}
			};
		};
	}

	// Accessor Methods
	
	/**
	 * Returns the first name of the author which is everything
	 * but the last name.
	 * @return The author's first name.
	 */
	public String getFirstName() {
		return firstname;
	};
	
	/**
	 * Returns the last name of the author
	 * @return The author's last name
	 */
	public String getLastName() {
		return lastname;
	};
	
	/**
	 * Returns the full name of the author
	 * @return The author's full name
	 */
	public String getFullName() {
		return toString();
	};
	
	/**
	 * Returns the list of books authored by this author
	 * @return The list of books authored
	 */
	public LinkedList getBooks() {
		return books;
	}

	/**
	 * Returns the title name of the author (eg: "Dr.")
	 * @return Author's title
	 */
	public String getTitle() {
		return title;
	}

	/** 
	 * Returns the middle name of the author
	 * @return Author's middle name
	 */
	public String getMiddleName() {
		return middlename;
	}

	/**
	 * Return the sur title of the author (eg. "jr.")
	 * @return Author's surtitle
	 */
	public String getSurTitle() {
		return surtitle;
	}

	//Modification Methods
	
	/**
	 * Determines if the passed author object is different from the current
	 * author object. Any differences are incorporated into the curent author
	 * object, ie. the current other object values are changed to that of the
	 * passed author object. True is returned if they were different and the
	 * current author object modified. False otherwise.
	 * 
	 * @param auth The author object to compare against and use its values for
	 *             updating if different
	 * @return True if the current author object was modified. False otherwise
	 */
	public boolean isModified(Author auth) {
		boolean changed = false;

		if (surtitle != auth.getSurTitle()) {
			surtitle = auth.getSurTitle();
			changed = true;
		}
		if (firstname != auth.getFirstName()) {
			firstname = auth.getFirstName();
			changed = true;
		}
		if (middlename != auth.getMiddleName()) {
			middlename = auth.getMiddleName();
			changed = true;
		}
		if (lastname != auth.getLastName()) {
			lastname = auth.getLastName();
			changed = true;
		}
		if (title != auth.getTitle()) {
			title = auth.getTitle();
			changed = true;
		}

		return changed;
	}

	// Comparison Methods
	
	/**
	 * Determines if the passed object is equal to the calling
	 * Author object. They are equal if the last and first names are equal.
	 * Used for comparison in list operations.
	 * @param y An object.
	 * @return True is equal. False otherwise.
	 */
	public boolean equals(Object o) {
		if (!(o instanceof Author))
			return false;
		Author y = (Author) o;
		String y_firstname = y.getFirstName();
		if ((firstname == null) && (y_firstname != null))
			return false;
		if ((firstname == null) && (y_firstname == null) || (firstname.equalsIgnoreCase(y_firstname))
			&& lastname.equalsIgnoreCase(y.getLastName()))
			return true;
		else
			return false;
	};

	/**
	 * Determines if the passed Author object is equal to the calling
	 * Author object. They are equal if the last and first names are equal.
	 * @param y An Author object.
	 * @return True is equal. False otherwise.
	 */
	public boolean equals(Author y) {
		System.out.println("Calling equals on " + y.toString());
		if (firstname.equalsIgnoreCase(y.getFirstName())
			&& lastname.equalsIgnoreCase(y.getLastName()))
			return true;
		else
			return false;
	};

	/**
	 * Determines if the two passed Author objects are equal. They are
	 * equal if the last and first names are equal.
	 * @param x One of the Author objects to compare.
	 * @param y The other Author object to compare.
	 * @return True if equal. False otherwise.
	 */
	public static boolean equals(Author x, Author y) {
		if (x.getFirstName().equalsIgnoreCase(y.getFirstName())
			&& x.getLastName().equalsIgnoreCase(y.getLastName()))
			return true;
		else
			return false;
	};

	/**
	 * Compares the passed Author object against the calling Author
	 * object to determine if the have the same last name.
	 * @param y A passed Author object to compare against the caller.
	 * @return True if the last names are the same. False otherwise.
	 */
	public boolean isLastNameEqual(Author y) {
		if (lastname.equals(y.getLastName()))
			return true;
		else
			return false;
	}

	/**
	 * Compares the passed Author objects to determine if they have the
	 * same last name.
	 * @param x One Author object to compare.
	 * @param y The other Author object to compare.
	 * @return True if the last names are the same. False otherwise.
	 */
	public static boolean isLastNameEqual(Author x, Author y) {
		if (x.getLastName().equals(y.getLastName()))
			return true;
		else
			return false;
	}

	/**
	 * Add the passed Book object to the list of books authored
	 * by this person if it is not already in the list. 
	 * @param book Book to add to the list of authored books.
	 */
	public void addBook(Book book) {
		if ((books == null) || (!books.contains(book)))
			books.add(book);
	};

	/**
	 * Remove the passed book from the list of books authored by this author.
	 * 
	 * @param book Book to remove from the list of books authored
	 * @return True if removal successful
	 */
	public boolean removeBook(Book book) {
		if ((books != null) && (books.contains(book))) {
			books.remove(book);
			return true;
		} else
			return false;

	}

	// Output Methods

	/**
	 * Create a valid XML definition for representing the author object. 
	 * 
	 * @param indent The initial speace indentation for formating the xml
	 * @return A string with the formated xml author information
	 */
	public String toXML(String indent) {
		String indent1 = indent + " ";
		String xmlString = indent + "<" + TAGNAME_AUTHOR + ">\n";
		if (title != null)
			xmlString =
				xmlString
					+ indent1
					+ "<"
					+ TAGNAME_TITLE
					+ ">"
					+ title
					+ "</"
					+ TAGNAME_TITLE
					+ ">\n";
		if (firstname != null)
			xmlString =
				xmlString
					+ indent1
					+ "<"
					+ TAGNAME_FIRSTNAME
					+ ">"
					+ firstname
					+ "</"
					+ TAGNAME_FIRSTNAME
					+ ">\n";
		if (middlename != null)
			xmlString =
				xmlString
					+ indent1
					+ "<"
					+ TAGNAME_MIDDLENAME
					+ ">"
					+ middlename
					+ "</"
					+ TAGNAME_MIDDLENAME
					+ ">\n";
		xmlString =
			xmlString
				+ indent1
				+ "<"
				+ TAGNAME_LASTNAME
				+ ">"
				+ lastname
				+ "</"
				+ TAGNAME_LASTNAME
				+ ">\n";
		if (surtitle != null)
			xmlString =
				xmlString
					+ indent1
					+ "<"
					+ TAGNAME_SURTITLE
					+ ">"
					+ surtitle
					+ "</"
					+ TAGNAME_SURTITLE
					+ ">\n";
		xmlString = xmlString + indent + "</" + TAGNAME_AUTHOR + ">\n";
		return xmlString;
	}

	/**
	 * Dump Author information into a String for output.
	 * 
	 */
	public String toString() {
		String fullname = "";
		if (title != null)
			fullname = fullname + title + " ";
		if (firstname != null)
			fullname = fullname + firstname + " ";
		if (middlename != null)
			fullname = fullname + middlename + " ";
		if (lastname != null)
			fullname = fullname + lastname + " ";
		if (surtitle != null)
			fullname = fullname + surtitle + " ";
		return fullname.substring(0, fullname.length() - 1);
	};

	/**
	 * Dump Author information to the console.
	 *
	 */
	public void consoleOutput() {
		System.out.println(
			lastname
				+ " "
				+ surtitle
				+ ", "
				+ title
				+ " "
				+ firstname
				+ " "
				+ middlename);
	};
}
