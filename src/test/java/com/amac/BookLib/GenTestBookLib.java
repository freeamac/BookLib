/*
 * Created on Nov 26, 2006
 *
 */

// import Book;
import java.util.*;

/**
 * @author amac
 *
 * 
 */
public class GenTestBookLib {
	//	Data Members
	protected BookLibrary bookList;

	// Constructors
	public GenTestBookLib() {

		bookList = new BookLibrary();
		Book book =
			new Book(
				"The Sword Of Shanara",
				"The Sword Of Shanara",
				"Terry Brooks",
				"0-345-45375-1",
				1991,
				Book.HARDCOVER);
		bookList.addBook(book);
		book =
			new Book(
				"The Heritage Of Shanara",
				"The Heritage Of Shanara",
				"Terry Brooks",
				"0-345-46554-7",
				2003,
				Book.HARDCOVER);
		bookList.addBook(book);
		book =
			new Book(
				"Heaven's Reach",
				"Earth Clan",
				"David Brin",
				"0-553-10174-9",
				1998,
				Book.HARDCOVER);
		bookList.addBook(book);
		book =
			new Book(
				"She Is The Darkness",
				"Black Company",
				"Glen Cook",
				"0-312-85907-4",
				1996,
				Book.HARDCOVER);
		bookList.addBook(book);
		book =
			new Book(
				"Bleak Seasons",
				"Black Company",
				"Glen Cook",
				"0-312-86105-2",
				1996,
				Book.HARDCOVER);
		bookList.addBook(book);
		LinkedList authours = new LinkedList();
		Author author1 = new Author("Andrew MacLeod");
		Author author2 = new Author("Greg MacLeod");
		authours.add(author1);
		authours.add(author2);
		book = new Book("A Family History", " ", authours, " ", 1990, Book.SOFTCOVER);
		bookList.addBook(book);
		
		
	};

	// Get the list
	public BookLibrary getBookLib() {
		return bookList;
	};

}
