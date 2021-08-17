import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import java.util.*;
import java.io.*;

import com.opencsv.CSVWriter;

/**
 * The <code>BookLibGui</code> is the graphical interface allowing
 * the manipulation of <code>BookLibrary</code> objects. A main
 * window displays the contents of a book library. From this 
 * window, using menu options, you can add, modify, delete or search
 * for specific books and authours in the library.
 * 
 * @author amac
 * @version 1.0
 *
 * 
 */

public class BookLibGui {

	public static void main(String[] args) {
		// Ensure all frames and dialogs have standard window decorations
		// eg. Title bar, close button, etc
		JFrame.setDefaultLookAndFeelDecorated(true);
		JDialog.setDefaultLookAndFeelDecorated(true);

		BookLibGuiFrame frame = new BookLibGuiFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}

/**
 * Main window of the interface.
 * 
 */
class BookLibGuiFrame extends JFrame {

	// Default window size parameters
	private static final int DEFAULT_WIDTH = 800;
	private static final int DEFAULT_HEIGHT = 600;
	private static final int BORDER_WIDTH = 30;

	// Current open book library
	private BookLibrary bookLibrary;

	// Currently displayed book list. This could be the complete
	// library or a refined search list.
	private LinkedList<Book> currentDisplayBookList;

	// Indicates if displaying a refined search list
	private boolean displayingSearchList = false;

	// Indicates if the displayed search list was using case insensitive search
	private boolean caseInsensitiveDisplayedSearchList = false;

	// Indicates current sort, if any, order
	private boolean bookTitleAscendingSort = false;
	private boolean authorAscendingSort = false;
	private boolean publishDateAscendingSort = false;

	// Data modified flag. Used to signal before close need to save data
	private boolean dataModified = false;

	// File which holds the book library data in XML format
	private File dataFile = null;

	// Object holders used to pass between dialogs
	private BookSearchObject searchObj;
	private Book bookToAdd;

	// Window dialog objects
	private AuthorSearchDialog authorSearchDialog;
	private BookSearchDialog bookSearchDialog;
	private SeriesSearchDialog seriesSearchDialog;
	private AddModifyBookDialog addModifyBookDialog;

	// Panels displaying data
	private BookLibPanel bookLibPanel;

	//	**File** Menu Items		 
	private final JMenuItem newItem,
		openItem,
		closeItem,
		saveItem,
		saveAsItem,
		printItem;

	// **Export As...** Menu Items
	private final JMenuItem exportCSVItem,
		exportHtmlItem;

	// **Edit** Menu Items
	private final JMenuItem cutItem,
		copyItem,
		pasteItem,
		addBookItem,
		modifyBookItem,
		deleteBookItem;

	// **Search** Menu Items
	private final JMenuItem searchByAuthorItem,
		searchByBookItem,
		searchBySeriesItem;

	// **View** Menu Items
	final JMenuItem allBooksItem, sortByBookTitlesItem, sortByBookAuthorsItem, sortByBookPubslishDateItem;

	/**
	 * Opens the Save or Load file dialog based on the passed mode. The 
	 * book library file filter is applied to refine the dialog only to
	 * files with the approved extension. The file or null (indicating
	 * a cancelled selection) is returned.
	 * 
	 * @param mode FileDialog.LOAD for loading a book library file into the
	 *             GUI or FileDialog.SAVE to select a file to save the current
	 *             book library file into
	 * @return The file selected
	 */
	private File saveOrLoadFileDialog(int mode) {
		String dialogTitle;
		String fileName;
		String fileDir;
		if (mode == FileDialog.LOAD)
			dialogTitle = "Load Book Library File";
		else
			dialogTitle = "Save Book Library File";

		FileDialog fileDialog = new FileDialog(this, dialogTitle, mode);
		if (dataFile != null) {
			fileDialog.setFile(dataFile.getName());
			fileDialog.setDirectory(dataFile.getPath());
		};
		fileDialog.setFilenameFilter(new BookLibrary.fileNameFilter());
		fileDialog.setVisible(true);
		fileName = fileDialog.getFile();
		fileDialog.setVisible(false);
		if (fileName != null) {
			fileDir = fileDialog.getDirectory();
			return (new File(fileDir, fileName));

		} else
			return null;

	}

	/**
	* Toggles menu items which are enabled/disabled based on whether a book library
	* is now open. Items which can only be used when a book library is open should
	* only be enabled when a book library is open and disabled otherwise.
	* 
	* @param libraryopen Indicate if a book library is open (true).
	*/
	private void toggleMenuItems(boolean libraryopen) {
		if (libraryopen) {
			// Enable/disable any menu items that could now be used
			// with the book library being open
			newItem.setEnabled(false);
			openItem.setEnabled(false);
			saveItem.setEnabled(true);
			closeItem.setEnabled(true);
			saveAsItem.setEnabled(true);
			printItem.setEnabled(true);
			addBookItem.setEnabled(true);
			modifyBookItem.setEnabled(true);
			deleteBookItem.setEnabled(true);
			searchByAuthorItem.setEnabled(true);
			searchByBookItem.setEnabled(true);
			searchBySeriesItem.setEnabled(true);
			allBooksItem.setEnabled(true);
			sortByBookTitlesItem.setEnabled(true);
			sortByBookAuthorsItem.setEnabled(true);
			sortByBookPubslishDateItem.setEnabled(true);
		} else {
			// Enable/disable any menu items that could now be used
			// with the book library being closed
			newItem.setEnabled(true);
			openItem.setEnabled(true);
			saveItem.setEnabled(false);
			closeItem.setEnabled(false);
			saveAsItem.setEnabled(false);
			printItem.setEnabled(false);
			addBookItem.setEnabled(false);
			modifyBookItem.setEnabled(false);
			deleteBookItem.setEnabled(false);
			searchByAuthorItem.setEnabled(false);
			searchByBookItem.setEnabled(false);
			searchBySeriesItem.setEnabled(false);
			allBooksItem.setEnabled(false);
			sortByBookTitlesItem.setEnabled(false);
			sortByBookAuthorsItem.setEnabled(false);
			sortByBookPubslishDateItem.setEnabled(false);
		}
	}

	/**
	 * Constructor of the main window which creates the menus, key 
	 * board short cuts and associated event action handlers. 
	 *
	 */
	public BookLibGuiFrame() {

		setTitle("Library Of Books");
		setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);

		// Setup **File** Menu
		JMenu fileMenu = new JMenu("File");
		newItem = fileMenu.add("New...");
		newItem.setAccelerator(
			KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
		fileMenu.addSeparator();
		openItem = fileMenu.add("Open...");
		openItem.setAccelerator(
			KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
		saveItem = fileMenu.add("Save");
		saveItem.setAccelerator(
			KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
		saveItem.setEnabled(false);
		closeItem = fileMenu.add("Close");
		closeItem.setAccelerator(
			KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK));
		closeItem.setEnabled(false);
		fileMenu.addSeparator();
		saveAsItem = fileMenu.add("Save As...");
		saveAsItem.setEnabled(false);
		fileMenu.addSeparator();
		printItem = fileMenu.add(new TestAction("Print..."));
		printItem.setAccelerator(
			KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK));
		printItem.setEnabled(false);
		fileMenu.addSeparator();
		JMenu exportMenu = new JMenu("Export As ...");
		fileMenu.add(exportMenu);
		exportCSVItem = exportMenu.add("CSV ...");
		exportCSVItem.setEnabled(false);
		exportHtmlItem = exportMenu.add("Html ...");
		exportHtmlItem.setEnabled(false);
		fileMenu.addSeparator();

		// Set up all Action Listeners for each **File** menu item as an anonymous class
		newItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				dataFile = saveOrLoadFileDialog(FileDialog.SAVE);
				bookLibrary = new BookLibrary();
				currentDisplayBookList = null;
				dataModified = true;
				toggleMenuItems(true);
				exportCSVItem.setEnabled(true);
				exportHtmlItem.setEnabled(true);
				setVisible(true);

			}
		});

		openItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				dataFile = saveOrLoadFileDialog(FileDialog.LOAD);
				try {
					if (dataFile == null) {
						JOptionPane.showMessageDialog(
							null,
							"Action cancelled. No file selected.",
							"Information",
							JOptionPane.INFORMATION_MESSAGE);
						return;
					};
					bookLibrary = new BookLibrary(dataFile);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(
						null,
						"Unable to read file selected: "
							+ e.getMessage()
							+ "\nSee console output for debugging information.",
						"Error",
						JOptionPane.ERROR_MESSAGE);
					e.printStackTrace(System.err);
				}
				currentDisplayBookList = bookLibrary.getBookList();
				bookLibPanel.UpdateData(
					currentDisplayBookList,
					displayingSearchList);

				toggleMenuItems(true);
				exportCSVItem.setEnabled(true);
				exportHtmlItem.setEnabled(true);
				setVisible(true);
			}
		});

		saveItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				// Case where a new library created and never saved
				// we need to get a file name. If we get null, save
				// operation cancelled in dialog
				if (dataFile == null)
					dataFile = saveOrLoadFileDialog(FileDialog.SAVE);
				if (dataFile != null) {
					try {
						bookLibrary.writeXML(dataFile);
						dataModified = false;
					} catch (Exception e) {
						JOptionPane.showMessageDialog(
							null,
							"Unable to write book libray to file: "
								+ e.getMessage()
								+ "\nSee console output for debugging information.",
							"Error",
							JOptionPane.ERROR_MESSAGE);
						e.printStackTrace(System.err);
					};
				}
			}
		});

		closeItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (dataModified) {
					Object[] options = { "SAVE", "CLOSE" };
					int choice =
						JOptionPane.showOptionDialog(
							null,
							"Book Library changed. Do you want to save the changes?",
							"Warning",
							JOptionPane.DEFAULT_OPTION,
							JOptionPane.WARNING_MESSAGE,
							null,
							options,
							options[0]);
					if (choice == 0) {
						dataFile = saveOrLoadFileDialog(FileDialog.SAVE);
						if (dataFile != null) {
							try {
								bookLibrary.writeXML(dataFile);
							} catch (Exception e) {
								JOptionPane.showMessageDialog(
									null,
									"Unable to write book libray to file: "
										+ e.getMessage()
										+ "\nSee console output for debugging information.",
									"Error",
									JOptionPane.ERROR_MESSAGE);
								e.printStackTrace(System.err);
							};
						}
					};
				};
				bookLibrary = null;
				dataModified = false;
				Container contentPane = getContentPane();
				contentPane.remove(bookLibPanel);
				bookLibPanel = new BookLibPanel(BORDER_WIDTH);
				contentPane.add(bookLibPanel);
				contentPane.repaint();
				toggleMenuItems(false);
				exportCSVItem.setEnabled(false);
				exportHtmlItem.setEnabled(false);
				setVisible(true);
			}
		});

		saveAsItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				File tempf;
				tempf = saveOrLoadFileDialog(FileDialog.SAVE);
				if (tempf != null) {
					if (!tempf.equals(dataFile))
						dataFile = tempf;
					try {
						bookLibrary.writeXML(dataFile);
						dataModified = false;
					} catch (Exception e) {
						JOptionPane.showMessageDialog(
							null,
							"Unable to write book libray to file: "
								+ e.getMessage()
								+ "\nSee console output for debugging information.",
							"Error",
							JOptionPane.ERROR_MESSAGE);
						e.printStackTrace(System.err);
					};
				}
			}
		});

		exportCSVItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (bookLibPanel != null)
					bookLibPanel.exportToCSV();
			}
		});

		exportHtmlItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (bookLibPanel != null)
					bookLibPanel.exportToHTML();
			}
		});

		fileMenu.add(new AbstractAction("Exit") {
			public void actionPerformed(ActionEvent event) {
				if (dataModified) {
					Object[] options = { "EXIT", "SAVE AND EXIT", "CANCEL" };
					int choice =
						JOptionPane.showOptionDialog(
							null,
							"Book Library changed. Do you want to save the changes before exiting?",
							"Warning",
							JOptionPane.DEFAULT_OPTION,
							JOptionPane.WARNING_MESSAGE,
							null,
							options,
							options[0]);
					if (choice == 0)
						System.exit(0);
					if (choice == 1) {
						dataFile = saveOrLoadFileDialog(FileDialog.SAVE);
						if (dataFile != null) {
							try {
								bookLibrary.writeXML(dataFile);
							} catch (Exception e) {
								JOptionPane.showMessageDialog(
									null,
									"Unable to write book libray to file: "
										+ e.getMessage()
										+ "\nSee console output for debugging information.",
									"Error",
									JOptionPane.ERROR_MESSAGE);
								e.printStackTrace(System.err);
							};
						}
						System.exit(0);
					}
				} else
					System.exit(0);
			}
		});

		// Setup **Edit** menu
		JMenu editMenu = new JMenu("Edit");
		cutItem = editMenu.add(new TestAction("Cut"));
		cutItem.setAccelerator(
			KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK));
		cutItem.setEnabled(false);
		copyItem = editMenu.add(new TestAction("Copy"));
		copyItem.setAccelerator(
			KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
		copyItem.setEnabled(false);
		pasteItem = editMenu.add(new TestAction("Paste"));
		pasteItem.setAccelerator(
			KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK));
		pasteItem.setEnabled(false);
		editMenu.addSeparator();

		// Adding a book to the library
		// This should only be enabled when the entire book list is viewed.
		addBookItem = editMenu.add("Add Book...");
		addBookItem.setAccelerator(
			KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.SHIFT_DOWN_MASK));
		addBookItem.setEnabled(false);

		addBookItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				StringBuffer modified = new StringBuffer(8);
				if (addModifyBookDialog == null)
					addModifyBookDialog =
						new AddModifyBookDialog(
							BookLibGuiFrame.this,
							AddModifyBookDialog.ADD_BOOK);
				try {
					if (addModifyBookDialog
						.showDialog(
							AddModifyBookDialog.ADD_BOOK,
							null,
							modified)) {
						dataModified = true;
						bookToAdd = addModifyBookDialog.getBook();
						bookLibrary.addBook(bookToAdd);
						if (displayingSearchList)
							// Need to redo search list being displayed since the added
							// added book may now appear in the list
							currentDisplayBookList =
								bookLibrary.searchResults(searchObj, caseInsensitiveDisplayedSearchList);
						bookLibPanel.UpdateData(
							currentDisplayBookList,
							displayingSearchList);
						setVisible(true);
					}
				} catch (IllegalStateException e) {
					JOptionPane.showMessageDialog(
						null,
						"Book already exists in the book library.\n Addition cancelled.",
						"Error",
						JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		// Modify a book in the library
		modifyBookItem = editMenu.add("Modify Book...");
		modifyBookItem.setAccelerator(
			KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.SHIFT_DOWN_MASK));
		modifyBookItem.setEnabled(false);
		modifyBookItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				StringBuffer modified = new StringBuffer(8);
				Book bookselected = bookLibPanel.getBookSelected();
				if (bookselected == null)
					JOptionPane.showMessageDialog(
						null,
						"No book selected",
						"Book modify Error",
						JOptionPane.ERROR_MESSAGE);
				else {
					if (addModifyBookDialog == null)
						addModifyBookDialog =
							new AddModifyBookDialog(
								BookLibGuiFrame.this,
								AddModifyBookDialog.MODIFY_BOOK);
					if (addModifyBookDialog
						.showDialog(
							AddModifyBookDialog.MODIFY_BOOK,
							bookselected,
							modified))
						if (modified.toString().equals("NO"))
							JOptionPane.showMessageDialog(
								null,
								"No data in book was modified",
								"Book modify Warning",
								JOptionPane.WARNING_MESSAGE);
						else {
							dataModified = true;
							bookLibPanel.UpdateData(
								currentDisplayBookList,
								displayingSearchList);
							setVisible(true);
						}
				}
			}
		});
		editMenu.addSeparator();

		// Delete a book in the library
		deleteBookItem = editMenu.add("Delete Book...");
		deleteBookItem.setAccelerator(
			KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.SHIFT_DOWN_MASK));
		deleteBookItem.setEnabled(false);
		deleteBookItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				Book bookselected = bookLibPanel.getBookSelected();
				if (bookselected == null)
					JOptionPane.showMessageDialog(
						null,
						"No book selected",
						"Book delete Error",
						JOptionPane.ERROR_MESSAGE);
				else {
					Object[] options = { "OK", "CANCEL" };
					int choice =
						JOptionPane.showOptionDialog(
							null,
							"Click OK to delete the book '"
								+ bookselected.getTitle()
								+ "'?",
							"Warning",
							JOptionPane.DEFAULT_OPTION,
							JOptionPane.WARNING_MESSAGE,
							null,
							options,
							options[0]);
					if (choice == 0) {
						dataModified = true;
						bookLibrary.removeBook(bookselected);
						if (displayingSearchList)
							// Need to redo search list being displayed since the added
							// added book may now appear in the list
							currentDisplayBookList =
								bookLibrary.searchResults(searchObj, caseInsensitiveDisplayedSearchList);
						bookLibPanel.UpdateData(
							currentDisplayBookList,
							displayingSearchList);
					}
					setVisible(true);
				}

			}
		});

		// Setup **Search** Menu
		JMenu searchMenu = new JMenu("Search library");

		// Search by author
		searchByAuthorItem = searchMenu.add("By Author Info...");
		searchByAuthorItem.setEnabled(false);
		searchByAuthorItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (authorSearchDialog == null)
					authorSearchDialog =
						new AuthorSearchDialog(BookLibGuiFrame.this);
				if (authorSearchDialog.showDialog()) {
					searchObj = authorSearchDialog.getSearchObject();
					searchObj.consoleOutput();
					currentDisplayBookList =
						bookLibrary.searchResults(searchObj, authorSearchDialog.getCaseInsensitiveSearch());
					displayingSearchList = true;
					caseInsensitiveDisplayedSearchList = authorSearchDialog.getCaseInsensitiveSearch();
					bookLibPanel.UpdateData(
						currentDisplayBookList,
						displayingSearchList);
					setVisible(true);

				}
			}
		});

		// Search by book information
		searchByBookItem = searchMenu.add("By Book Info...");
		searchByBookItem.setEnabled(false);
		searchByBookItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (bookSearchDialog == null)
					bookSearchDialog =
						new BookSearchDialog(BookLibGuiFrame.this);
				if (bookSearchDialog.showDialog()) {
					searchObj = bookSearchDialog.getSearchObject();
					searchObj.consoleOutput();
					currentDisplayBookList =
						bookLibrary.searchResults(searchObj, bookSearchDialog.getCaseInsensitiveSearch());
					displayingSearchList = true;
					caseInsensitiveDisplayedSearchList = bookSearchDialog.getCaseInsensitiveSearch();
					bookLibPanel.UpdateData(
						currentDisplayBookList,
						displayingSearchList);
					setVisible(true);
				}
			}
		});

		// Search by series info
		searchBySeriesItem = searchMenu.add("By Series...");
		searchBySeriesItem.setEnabled(false);
		searchBySeriesItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (seriesSearchDialog == null)
					seriesSearchDialog =
						new SeriesSearchDialog(BookLibGuiFrame.this);
				if (seriesSearchDialog.showDialog()) {
					searchObj = seriesSearchDialog.getBookSearchObject();
					searchObj.consoleOutput();
					currentDisplayBookList =
						bookLibrary.searchResults(searchObj, seriesSearchDialog.getCaseInsensitiveSearch());
					displayingSearchList = true;
					caseInsensitiveDisplayedSearchList = seriesSearchDialog.getCaseInsensitiveSearch();
					bookLibPanel.UpdateData(
						currentDisplayBookList,
						displayingSearchList);
					setVisible(true);
				}
			}
		});

		// Setup **View** Menu
		JMenu viewMenu = new JMenu("View");

		// See all books
		allBooksItem = viewMenu.add("All Books");
		allBooksItem.setEnabled(false);
		allBooksItem.setAccelerator(
			KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK));
		allBooksItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (bookLibrary != null) {
					currentDisplayBookList = bookLibrary.getBookList();
					displayingSearchList = false;
					caseInsensitiveDisplayedSearchList = false;
					bookLibPanel.UpdateData(
						currentDisplayBookList,
						displayingSearchList);
					setVisible(true);
				}
			}
		});

		viewMenu.addSeparator();

		// Sort display list by book title (the default)
		sortByBookTitlesItem = viewMenu.add("Sort By Book Title");
		sortByBookTitlesItem.setEnabled(false);
		sortByBookTitlesItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (currentDisplayBookList != null) {
					// Handle flip between ascending and descending order sorts
					if (!bookTitleAscendingSort) {
						bookTitleAscendingSort = true;
						authorAscendingSort = false;
						publishDateAscendingSort = false;
						Collections.sort(currentDisplayBookList, new BookListCompare());
					} else {
						bookTitleAscendingSort = false;
						Collections.sort(currentDisplayBookList, Collections.reverseOrder());
					}
					bookLibPanel.UpdateData(
						currentDisplayBookList,
						displayingSearchList);
					setVisible(true);
				}
			}
		});

		// Sort display by book authors
		sortByBookAuthorsItem = viewMenu.add("Sort By Book Authors");
		sortByBookAuthorsItem.setEnabled(false);
		sortByBookAuthorsItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (currentDisplayBookList != null) {
					// Handle flip between ascending and descending order sorts
					if (!authorAscendingSort) {
						authorAscendingSort = true;
						bookTitleAscendingSort = false;
						publishDateAscendingSort = false;
						Collections.sort(
							currentDisplayBookList,
							new AuthorListCompare());
					} else {
						authorAscendingSort = false;
						Collections.sort(
							currentDisplayBookList,
							Collections.reverseOrder(new AuthorListCompare()));
					}
					bookLibPanel.UpdateData(
						currentDisplayBookList,
						displayingSearchList);
					setVisible(true);
				}
			}
		});

		// Sort display by book authors
		sortByBookPubslishDateItem = viewMenu.add("Sort By Book Publish Date");
		sortByBookPubslishDateItem.setEnabled(false);
		sortByBookPubslishDateItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (currentDisplayBookList != null) {
					// Handle flip between ascending and descending order sorts
					if (!publishDateAscendingSort) {
						publishDateAscendingSort = true;
						authorAscendingSort = false;
						bookTitleAscendingSort = false;
						Collections.sort(
							currentDisplayBookList,
							new PublishDateListCompare());
					} else {
						publishDateAscendingSort = false;
						Collections.sort(
							currentDisplayBookList,
							Collections.reverseOrder(new PublishDateListCompare()));
					}
					bookLibPanel.UpdateData(
						currentDisplayBookList,
						displayingSearchList);
					setVisible(true);
				}
			}
		});

		// See all authors
		//JMenuItem allAuthorsItem = viewMenu.add(new TestAction("All Authors"));

		// Complete menu setup
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		menuBar.add(searchMenu);
		menuBar.add(viewMenu);

		// Set up display panel for books
		Container contentPane = getContentPane();
		contentPane.setLayout(new FlowLayout());
		contentPane.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
		bookLibPanel = new BookLibPanel(BORDER_WIDTH);

		contentPane.add(bookLibPanel, BorderLayout.CENTER);
	}
}

/**
 * Dummy action class used as a filler until real actions defined.
 * 
 */
class TestAction extends AbstractAction {
	public TestAction(String name) {
		super(name);
	}
	public void actionPerformed(ActionEvent event) {
		System.out.println("Whahoo... " + getValue(Action.NAME) + " selected!");
	}
}

/**
 * Display panel for the library of books. Books are displayed in a tabular
 * format with each row being a single book entry. The actual list displayed
 * may be reduced to the results of a previous search.
 *
 */
class BookLibPanel extends JPanel {

	// Display layout values and widgets 
	private JLabel searchHeading;
	private static final String[] columnNames =
		{ "Book Title", "Authors", "Series", "Publish Date", "Cover", "ISBN" };
	private Object[][] bookData;
	private LinkedList<Book> bookList;
	private JTable bookTable;
	private JScrollPane scrollPane;
	private readOnlyTableModel readOnlyTM;
	private int width, height, border;
	private static int border_height = 50;

	/**
	 * Used by the library panel to define the tabular layout and
	 * properties of books being displayed as part of the 
	 * <code>JScrollPane</code> widget.
	 */
	class readOnlyTableModel extends AbstractTableModel {

		/**
		 * Return the number of rows in the display table
		 */
		public int getRowCount() {
			if (bookData == null)
				return 0;
			else
				return bookData.length;
		}

		/**
		 * Return the number of columns in the display table
		 */
		public int getColumnCount() {
			return columnNames.length;
		}

		/**
		 * Return the value at the selected location which is done 
		 * by mapping the row and column value into the 2D array
		 * of book data
		 */
		public Object getValueAt(int row, int col) {
			if (bookData == null)
				return (Object) "";
			else
				return bookData[row][col];
		}

		/**
		 * Return the name of the selected column
		 */
		public String getColumnName(int column) {
			return columnNames[column];
		}
	}

	// Constructors

	/**
	 * Create the panel for displaying the book library. The panel will
	 * reside in the calling frame with at least a buffer of size
	 * <code>border</code> around the panel.
	 * 
	 * @param setBorder The size of the buffer border around the book library
	 *                  display panel and the enclosing frame
	 */
	public BookLibPanel(int setBorder) {

		this.border = setBorder;
		// Create panel relative to parent container size
		Container parent = getParent();
		if (parent != null) {
			width = parent.getWidth() - this.border;
			height = parent.getHeight() - this.border;
		} else {
			height = 1;
			width = 1;
		}
		this.setSize(width, height);
		this.setLayout(new BorderLayout());
		searchHeading = new JLabel(" ", JLabel.CENTER);
		add(searchHeading);
		Object[][] bookData = { { "", "", "", "", "", "" }
		};
		bookList = null;
		bookTable = new JTable(bookData, columnNames);
		readOnlyTM = new readOnlyTableModel();
		bookTable.setModel(readOnlyTM);
		scrollPane =
			new JScrollPane(
				bookTable,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		bookTable.setPreferredScrollableViewportSize(
			new Dimension(width, height - border_height));
		bookTable.setColumnSelectionAllowed(false);
		bookTable.setRowSelectionAllowed(true);
		bookTable.setCellSelectionEnabled(false);
		add(scrollPane, BorderLayout.CENTER);

	}

	// Display methods

	/**
	 * Handle resize of parent window to ensure correct resize of panel display
	 */
	public void repaint() {
		Container parent = getParent();
		if ((parent != null) && (bookList != null)) {
			int w = parent.getWidth() - border;
			int h = parent.getHeight() - border;
			if ((w != width) || (h != height)) {

				// A reize to parent has occurred
				width = w;
				height = h;
				if (scrollPane != null) {
					scrollPane.setSize(width, height - border_height);
					scrollPane.setVisible(true);
				}
				this.setSize(width, height);
				this.UpdateData(this.bookList, false);
				this.setVisible(true);
				parent.setVisible(true);
			}
		}
	}

	/**
	 * Updates the books displayed in the panel based on the passed booklist. If this
	 * is a refined search book list, an indication of that is also displayed.
	 * 
	 * @param booklist The new list of books to display
	 * @param searchlist True if this is a refined list and that should be indicated
	 */
	public void UpdateData(LinkedList<Book> booklist, boolean searchlist) {

		// Ensure panel sized relative to parent container size
		Container parent = getParent();
		if (parent != null) {
			width = parent.getWidth() - border;
			height = parent.getHeight() - border;
		} else {
			height = 1;
			width = 1;
		}
		this.setSize(width, height);

		// Indicate if this is a refined search list
		if (searchlist)
			searchHeading.setText(
				"***Refined Search Listing Being Displayed***");
		else
			searchHeading.setText(" ");

		// Loop through the passed list of books and update the display
		// data in the panel
		bookList = booklist;
		bookData = new Object[bookList.size()][6];
		for (int i = 0; i < booklist.size(); i++) {
			Book book = (Book) booklist.get(i);
			bookData[i][0] = book.getTitle();
			LinkedList<Author> book_auths = book.getAuthors();
			String authors = null;
			for (int k = 0; k < book_auths.size(); k++) {
				Author auth = (Author) book_auths.get(k);
				if (authors == null)
					authors = auth.getFullName();
				else
					authors = authors + ", " + auth.getFullName();

			}
			bookData[i][1] = authors;
			bookData[i][2] = book.getSeries();
			int bookdate = book.getPublishYear();
			if (bookdate == Book.BADDATE)
				bookData[i][3] = "";
			else
				bookData[i][3] =
					Integer.toString(book.getPublishYear());
			bookData[i][4] = Book.COVERNAME[book.getCoverType()];
			bookData[i][5] = book.getISBN();
		};
		scrollPane.remove(bookTable);
		remove(scrollPane);
		bookTable = new JTable(bookData, columnNames);
		bookTable.setModel(readOnlyTM);
		scrollPane =
			new JScrollPane(
				bookTable,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		bookTable.setPreferredScrollableViewportSize(
			new Dimension(width, height - border_height));
		bookTable.setColumnSelectionAllowed(false);
		bookTable.setRowSelectionAllowed(true);
		bookTable.setCellSelectionEnabled(false);
		add(scrollPane);
		System.out.println("We have #books: " + booklist.size());
	}

	// Accessor methods

	/**
	 * Get and return the book selected in the in book library panel display
	 * 
	 * @return Book selected in book library panel display
	 */
	public Book getBookSelected() {
		int row = bookTable.getSelectedRow();
		if (row == -1)
			return null;
		else
			return (Book) bookList.get(row);
	}

	// Export methods

	/** 
	 * Export the current panel displayed to a file in CSV format.
	 * 
	 * The file must have a ".csv" extension. If the user does not 
	 * provide one, the extension is added. 
	 * 
	 * The file cannot already exist or an error message is given.
	 */
	public void exportToCSV() {

		File file;
		// Set up file dialog to save csv values and ensure correct extension and
		// non-existence
		String filename = JOptionPane.showInputDialog("New CSV file to export to");
		if (!filename.endsWith(".csv"))  {
			file = new File(filename + ".csv");
			if (file.exists()) {
				JOptionPane.showMessageDialog(
					null,
					"File already exisits: " + file.getName() + " .",
					"Error",
					JOptionPane.ERROR_MESSAGE);
				return;
			} 
		} else {
			file = new File(filename);
		}

		// Create new file
		try {
			file.createNewFile();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(
				null,
				"Unable to open " + file.getName() + " for writing:"
				+ e.getMessage()
				+ "\nSee console output for debugging information.",
				"Error",
				JOptionPane.ERROR_MESSAGE);
			e.printStackTrace(System.err);
			return;
		}

		// Set up as a FileWriter
		Writer fileWriter;
		try {
			fileWriter = new BufferedWriter(new OutputStreamWriter( new FileOutputStream(file), "UTF-8"));
		} catch (IOException e) {
			JOptionPane.showMessageDialog(
				null,
				"Unable to open " + file.getName() + " for writing:"
				+ e.getMessage()
				+ "\nSee console output for debugging information.",
				"Error",
				JOptionPane.ERROR_MESSAGE);
			e.printStackTrace(System.err);
			return;
		}

		// Setup and handle output in csv format
		CSVWriter csvWriter = new CSVWriter(fileWriter);
		csvWriter.writeNext(columnNames);

		ListIterator<Book> booklistIterator = bookList.listIterator();
		while (booklistIterator.hasNext()) {
			Book nextBook = booklistIterator.next();
			//String[] csvEntries = new String[6];
			String[] csvEntries = {nextBook.title, nextBook.getAuthorsString(",\n"), nextBook.series, String.valueOf(nextBook.publishYear), Book.COVERNAME[nextBook.coverType], nextBook.ISBN};
			csvWriter.writeNext(csvEntries);

		}
		try {
			csvWriter.flush();
			csvWriter.close();
			fileWriter.close();
			JOptionPane.showMessageDialog(
				null,
				"Book Information exported to " + file.getAbsolutePath(),
				"Information",
				JOptionPane.INFORMATION_MESSAGE);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(
				null,
				"Unable to write to and close " + file.getName() + " :"
				+ e.getMessage()
				+ "\nSee console output for debugging information.",
				"Error",
				JOptionPane.ERROR_MESSAGE);
			e.printStackTrace(System.err);
			return;
		}
	}

	/** Helper method to print a blank instead of "null" for nulls.
	 * 
	 * @param s The string to check for null.
	 * @return "" or the string.
	 */
	private static String nullToBlank(String s) {
		if (s == null)
			return "";
		else
			return s;
	}

	/** 
	 * Export the current panel displayed to a file in HTML format.
	 * 
	 * The file must have a ".html" extension. If the user does not 
	 * provide one, the extension is added. 
	 * 
	 * The file cannot already exist or an error message is given.
	 */
	public void exportToHTML() {

		File file;
		// Set up file dialog to save html values and ensure correct extension and
		// non-existence
		String filename = JOptionPane.showInputDialog("New HTML file to export to");
		if (!filename.endsWith(".html"))  {
			file = new File(filename + ".html");
			if (file.exists()) {
				JOptionPane.showMessageDialog(
					null,
					"File already exisits: " + file.getName() + " .",
					"Error",
					JOptionPane.ERROR_MESSAGE);
				return;
			} 
		} else {
			file = new File(filename);
		}

		// Create new file
		try {
			file.createNewFile();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(
				null,
				"Unable to open " + file.getName() + " for writing:"
				+ e.getMessage()
				+ "\nSee console output for debugging information.",
				"Error",
				JOptionPane.ERROR_MESSAGE);
			e.printStackTrace(System.err);
			return;
		}

		// Set up as a FileWriter
		Writer fileWriter;
		try {
		    fileWriter = new BufferedWriter(new OutputStreamWriter( new FileOutputStream(file), "UTF-8"));
		} catch (IOException e) {
			JOptionPane.showMessageDialog(
				null,
				"Unable to open " + file.getName() + " for writing:"
				+ e.getMessage()
				+ "\nSee console output for debugging information.",
				"Error",
				JOptionPane.ERROR_MESSAGE);
			e.printStackTrace(System.err);
			return;
		}

		// Output in html header information 
		try {
			fileWriter.write("<HTML xmlns=\"http://www.w3.org/TR/REC-html40\">\n");
			fileWriter.write("<HEAD>\n");
			fileWriter.write("  <META http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>\n");
			fileWriter.write("  <TITLE>Book Library</TITLE>\n");
			fileWriter.write("</HEAD>\n");
			fileWriter.write("<BODY>\n");
			fileWriter.write("  <H1>Book Library</H1>\n");
			fileWriter.write("  <table  width=\"100%\" border=\"5\" cellspacing=\"3\">\n");
			fileWriter.write("  <tr width=\"100%\">\n");
			fileWriter.write("    <th width=\"25%\">Title</th>\n");
			fileWriter.write("    <th width=\"20%\">Author(s)</th>\n");
			fileWriter.write("    <th width=\"25%\">Series</th>\n");
			fileWriter.write("    <th width=\"10%\">Publish Year</th>\n");
			fileWriter.write("    <th width=\"10%\">Cover</th>\n");
			fileWriter.write("    <th width=\"10%\">ISBN</th>\n");
			fileWriter.write("  </tr>\n");

			// Output each Book as an entry in the html table
			ListIterator<Book> booklistIterator = bookList.listIterator();
			while (booklistIterator.hasNext()) {
				Book nextBook = booklistIterator.next();
				fileWriter.write("  <tr width=\"100%\">\n");
				fileWriter.write("    <td width=\"25%\">" + nextBook.title + "</td>\n");
				fileWriter.write("    <td width=\"20%\">" + nextBook.getAuthorsString(",</br>") + "</td>\n");
				fileWriter.write("    <td width=\"25%\">" + BookLibPanel.nullToBlank(nextBook.series) + "</td>\n");
				fileWriter.write("    <td width=\"10%\">" + String.valueOf(nextBook.publishYear) + "</td>\n");
				fileWriter.write("    <td width=\"10%\">" + Book.COVERNAME[nextBook.coverType] + "</td>\n");
				fileWriter.write("    <td width=\"10%\">" + BookLibPanel.nullToBlank(nextBook.ISBN) + "</td>\n");
				fileWriter.write("  <tr>\n");
			}

			// Output html closing information
			fileWriter.write("  </table>\n");
			fileWriter.write("</body>\n");
			fileWriter.write("</html>\n");

			// Flush, close and notify success 
			fileWriter.flush();
			fileWriter.close();
			JOptionPane.showMessageDialog(
				null,
				"Book Information exported to " + file.getAbsolutePath(),
				"Information",
				JOptionPane.INFORMATION_MESSAGE);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(
				null,
				"Unable to write to and close " + file.getName() + " :"
				+ e.getMessage()
				+ "\nSee console output for debugging information.",
				"Error",
				JOptionPane.ERROR_MESSAGE);
			e.printStackTrace(System.err);
			return;
		}
	}
}

/**
 * Window dialog to add a new book to the library or modify a selected book
 * in the library. In the addition mode, the book object
 * will be created and held by this dialog object. In the modification mode,
 * the book to modify is passed to the dialog to display the data. You must call 
 * <code>showDialog</code> to invoke the dialog and check the
 * boolean return code to see if a book object was created or modified. If
 * created, use <code>getBook</code> method to get the created book 
 * object. If modified, the passed book object is modified directly.
 *
 */
class AddModifyBookDialog extends JDialog {

	// Inidicate if adding a book or modifying a currently
	// selected book
	public static int ADD_BOOK = 0;
	public static int MODIFY_BOOK = 1;

	// Widget elements and data fields
	private JTextField title, series, isbn, dateStr;
	private JComboBox<String> coverCombo;
	private boolean ok;
	private Book book;
	private JTextField authorTitle[];
	private JTextField authorFirst[];
	private JTextField authorMiddle[];
	private JTextField authorLast[];
	private JTextField authorSur[];

	private JPanel buttonPanel;
	private JButton addButton, modifyButton, cancelButton;

	private int curDialogType;
	private boolean bookModified;

	// Constructors

	/**
	 * Creates an add book dialog if that is indicated by <code>dialogType</code>. 
	 * This dialog will contain blanks fields to be filled in. Otherwise it will
	 * create a modify book dialog pre-populating the data fields with the values
	 * of the currently selected book.
	 * 
	 * @param owner The parent window calling this dialog
	 * @param dialogType Whether this should be an add book dialog or a modify book
	 *                   dialog
	 */
	public AddModifyBookDialog(JFrame owner, int dialogType) {
		super(owner, "Add Book", true);
		if (dialogType == MODIFY_BOOK)
			setTitle("Modify Book");
		initLayout(dialogType);
		curDialogType = dialogType;

	}

	// Display methods

	/**
	 * Display the appropriate dialog; either the add a new book or modify the information
	 * of the selected book as indicated by the <code>dialogType</code>. If a book is
	 * added, the new <code>Book</code> object is created. If a book modification dialog
	 * is asked for, <code>modified</code> will indicate if any modifications of the book
	 * data occurred.
	 * 
	 * @param dialogType Either an add new book dialog (<code>ADD_BOOK</code>) or a modify
	 *                   the selected book (<code>MODIFY_BOOK</code>)
	 * @param book Will be the new <code>Book</code> object in the case of an add book 
	 *             dialog or the selected book to modify in a modify book dialog
	 * @param modified Indicator if the passed book data was modified, ie. a successful
	 *                 modify book dialog
	 * 
	 * @return True unless the dialog was cancelled
	 */
	public boolean showDialog(
		int dialogType,
		Book book,
		StringBuffer modified) {
		ok = false;

		if (book != null)
			setBookInfo(book);

		// Do we have to change the current panel layout?
		if (dialogType != curDialogType) {
			if (dialogType == ADD_BOOK) {
				setTitle("Add Book");
				removeModifyButton();
				removeCancelButton();
				addAddButton();
				addCancelButton();
			} else {
				setTitle("Modify Book");
				removeAddButton();
				removeCancelButton();
				addModifyButton();
				addCancelButton();
			}
		};
		curDialogType = dialogType;
		setVisible(true);
		if (bookModified)
			modified.insert(0, "YES");
		else
			modified.insert(0, "NO");
		return ok;
	}

	// Accessor methods

	/**
	 * Return the created book object. Should be invoked by the class calling the 
	 * dialog
	 */
	public Book getBook() {
		return book;
	}

	// Private helper methods

	/**
	 * Layouts out the generic panel used both both the add and modification
	 * of a book situations
	 *
	 */
	private void initLayout(int dialogType) {

		Container contentPane = getContentPane();

		// Set up fields for entry of new book data
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(5, 2));
		panel.add(new JLabel("Title:"));
		//JTextField title = new JTextField("");
		//title.getInputContext().selectInputMethod(new Locale("ja", "JP"));
		panel.add(title = new JTextField(""));
		panel.add(title);
		panel.add(new JLabel("Series:"));
		panel.add(series = new JTextField(""));
		panel.add(new JLabel("ISBN:"));
		panel.add(isbn = new JTextField(""));
		panel.add(new JLabel("Date:"));
		panel.add(dateStr = new JTextField(""));
		panel.add(new JLabel("Book Cover Type:"));
		coverCombo = new JComboBox<String>();
		coverCombo.setEditable(false);
		for (int i = 0; i < Book.MAXREALCOVERS; i++)
			coverCombo.addItem(Book.COVERNAME[i]);
		panel.add(coverCombo);
		contentPane.add(panel, BorderLayout.NORTH);

		// Sub-panel used to get display formatted nicely
		JPanel authorHeadingPanel = new JPanel();
		authorHeadingPanel.setPreferredSize(new Dimension(200, 50));
		authorHeadingPanel.add(new JLabel("Author Names", JLabel.CENTER));

		// Sub-panel for the author entries since there may be several
		// authors for a book.
		JPanel authorEntriesPanel = new JPanel();
		authorEntriesPanel.setLayout(new GridLayout(Book.MAXAUTHORS + 1, 5));
		authorEntriesPanel.add(new JLabel("Title", JLabel.CENTER));
		authorEntriesPanel.add(new JLabel("First Name", JLabel.CENTER));
		authorEntriesPanel.add(new JLabel("Middle Names", JLabel.CENTER));
		authorEntriesPanel.add(new JLabel("Last Name", JLabel.CENTER));
		authorEntriesPanel.add(new JLabel("SurTitle", JLabel.CENTER));
		authorTitle = new JTextField[Book.MAXAUTHORS];
		authorFirst = new JTextField[Book.MAXAUTHORS];
		authorMiddle = new JTextField[Book.MAXAUTHORS];
		authorLast = new JTextField[Book.MAXAUTHORS];
		authorSur = new JTextField[Book.MAXAUTHORS];
		for (int i = 0; i < Book.MAXAUTHORS; i++) {
			authorEntriesPanel.add(authorTitle[i] = new JTextField(""));
			authorEntriesPanel.add(authorFirst[i] = new JTextField(""));
			authorEntriesPanel.add(authorMiddle[i] = new JTextField(""));
			authorEntriesPanel.add(authorLast[i] = new JTextField(""));
			authorEntriesPanel.add(authorSur[i] = new JTextField(""));

		}
		JPanel authorPanel = new JPanel();
		authorPanel.setLayout(new BoxLayout(authorPanel, BoxLayout.Y_AXIS));
		authorPanel.add(authorHeadingPanel);
		authorPanel.add(authorEntriesPanel);
		contentPane.add(authorPanel, BorderLayout.CENTER);

		// Add buttons to dialog
		buttonPanel = new JPanel();
		if (dialogType == ADD_BOOK)
			addAddButton();
		else
			addModifyButton();

		addCancelButton();

		contentPane.add(buttonPanel, BorderLayout.SOUTH);
		pack();
	}

	/**
	 * Auxillary method to clear all data entry fields
	 */
	private void Clear() {
		title.setText("");
		series.setText("");
		isbn.setText("");
		dateStr.setText("");
		for (int i = 0; i < Book.MAXAUTHORS; i++) {
			authorTitle[i].setText("");
			authorFirst[i].setText("");
			authorMiddle[i].setText("");
			authorLast[i].setText("");
			authorSur[i].setText("");
		}
		coverCombo.setSelectedIndex(Book.HARDCOVER);
	}

	/**
	 * Auxillary method to add the add button to the button panel and 
	 * provide the appropriate action events
	 */
	private void addAddButton() {
		//		Invoke create of book object by the **Add** button
		addButton = new JButton("Add");
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {

				if (createBook()) {
					ok = true;
					Clear();
					setVisible(false);
				};
			}
		});
		buttonPanel.add(addButton);
	}

	/**
	 * Auxillary method to add the cancel button to the button panel and
	 * provide the appropriate action events
	 */
	private void addCancelButton() {
		//	Clear information and exit dialog by the **Cancel** button
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				Clear();
				setVisible(false);
			}
		});
		buttonPanel.add(cancelButton);
	}

	/**
	 * Auxillary method to add the modify button to the button panel and
	 * provide the appropriate action events
	 */
	private void addModifyButton() {
		//		Invoke create of book object by the **Modify** button
		modifyButton = new JButton("Modify");
		modifyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {

				ok = true;
				if (modifyBook()) {
					bookModified = true;
					Clear();
					setVisible(false);
				} else {
					bookModified = false;
					Clear();
					setVisible(false);
				}
			}
		});
		buttonPanel.add(modifyButton);
	}

	/**
	 * Auxillary method to remove the add button from the button panel
	 */
	private void removeAddButton() {
		buttonPanel.remove(addButton);
	}

	/**
	 * Auxillary method to remove the modify button from the button panel
	 */
	private void removeModifyButton() {
		buttonPanel.remove(modifyButton);
	}

	/**
	 * Auxillary method to remove the cancel button from the button panel
	 */
	private void removeCancelButton() {
		buttonPanel.remove(cancelButton);
	}

	/**
	 * Place the information from the passed book into the appropriate
	 * panel data fields
	 */
	private void setBookInfo(Book book) {
		title.setText(book.getTitle());
		series.setText(book.getSeries());
		isbn.setText(book.getISBN());
		int bookdate = book.getPublishYear();
		if (bookdate == Book.BADDATE)
			dateStr.setText("");
		else
			dateStr.setText(String.valueOf(bookdate));
		LinkedList<Author> auths = book.getAuthors();
		for (int i = 0; i < auths.size(); i++) {
			authorTitle[i].setText(((Author) auths.get(i)).getTitle());
			authorFirst[i].setText(((Author) auths.get(i)).getFirstName());
			authorMiddle[i].setText(((Author) auths.get(i)).getMiddleName());
			authorLast[i].setText(((Author) auths.get(i)).getLastName());
			authorSur[i].setText(((Author) auths.get(i)).getSurTitle());
		}
		coverCombo.setSelectedIndex(book.getCoverType());
		this.book = book;

	}

	/**
	 * Create new book object from the information entered in the dialog
	 */
	private boolean createBook() {
		String b_title, b_series, b_isbn, b_dateStr;
		int b_date, b_cover;
		LinkedList<Author> auth_list = new LinkedList<>();

		b_title = title.getText();
		if (b_title.toString().equals("")) {

			JOptionPane.showMessageDialog(
				null,
				"A book must have a title",
				"Book Create Error",
				JOptionPane.ERROR_MESSAGE);
			return false;
		};

		b_series = series.getText();
		b_isbn = isbn.getText();

		b_dateStr = dateStr.getText();
		if (b_dateStr.toString().equals(""))
			b_date = Book.BADDATE;
		else
			b_date = Integer.parseInt(b_dateStr);

		b_cover = coverCombo.getSelectedIndex();

		if (authorLast[0].getText().equals("")) {

			JOptionPane.showMessageDialog(
				null,
				"A book must have at least one author with a last name",
				"Book Create Error",
				JOptionPane.ERROR_MESSAGE);
			return false;
		};

		int i = 0;
		while ((i < Book.MAXAUTHORS)
			&& (!authorLast[i].getText().equals(""))) {
			String a_title = authorTitle[i].getText();
			String a_first = authorFirst[i].getText();
			String a_middle = authorMiddle[i].getText();
			String a_last = authorLast[i].getText();
			String a_sur = authorSur[i].getText();
			Author auth = new Author(a_title, a_first, a_middle, a_last, a_sur);
			auth_list.add(auth);
			i++;
		};

		book = new Book(b_title, b_series, auth_list, b_isbn, b_date, b_cover);
		book.consoleOutput();
		return true;
	}

	/**
	 * Get modified data and determine if any changes were made. Changes will be
	 * directly updated in the book itself which has been stored by the panel as
	 * its private member book.
	 */
	private boolean modifyBook() {

		boolean changed = false;

		// createBook() will store a new Book object in the the class variable
		// "book" so we want to save the original object and update it if necessary
		// after the call to createBook()
		Book orig_book = book;
		if (createBook() && orig_book.isModified(book))
			changed = true;
		book = orig_book;
		return changed;
	}

}

/**
 * Window dialog to get the information to search on a particular
 * author in the library. You must call <code>showDialog</code> to
 * invoke the dialog and check the boolean return code to see if 
 * a book search object was created. If created, use
 * <code>getSearchObject</code> to get the created search object.
 *
 */
class AuthorSearchDialog extends JDialog {

	private JTextField lastname, firstname;
	private boolean ok;
	private BookSearchObject searchObj;
	private boolean caseInsensitive;

	// Constructors

	/**
	 * Create and lay out the dialog for an author search
	 */
	public AuthorSearchDialog(JFrame owner) {
		super(owner, "Search By Author", true);
		Container contentPane = getContentPane();

		// Set up fields for author search information entry
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(3, 2));
		panel.add(new JLabel("Last Name:"));
		panel.add(lastname = new JTextField(""));
		panel.add(new JLabel("First Name:"));
		panel.add(firstname = new JTextField(""));
		JCheckBox caseInsensitiveCheckBox = new JCheckBox("Case Insensitive Search?");
		panel.add(caseInsensitiveCheckBox);
		contentPane.add(panel, BorderLayout.CENTER);

		// Invoke create of book search object with **Search** button
		JButton searchButton = new JButton("Search");
		searchButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				ok = true;
				createSearchObject();
				caseInsensitive = caseInsensitiveCheckBox.isSelected();
				setVisible(false);
			}
		});

		// Clear entry fields with **Clear** button
		JButton clearButton = new JButton("Clear");
		clearButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				lastname.setText("");
				firstname.setText("");
			}
		});

		// Cancel search dialog with **Cancel** button
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				setVisible(false);
			}
		});

		// Add buttons to panel
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(searchButton);
		buttonPanel.add(clearButton);
		buttonPanel.add(cancelButton);
		contentPane.add(buttonPanel, BorderLayout.SOUTH);
		pack();
	}

	// Display methods

	/**
	 * Display the dialog and return true if a search object gets created
	 * 
	 * @return True if a search object successfully created.
	 */
	public boolean showDialog() {
		ok = false;
		setVisible(true);
		return ok;
	}

	// Accessor methods

	/**
	 * Return the created author search object
	 * 
	 * @return The search object with the author search criteria
	 */
	public BookSearchObject getSearchObject() {
		return searchObj;
	}

	/**
	 * Return if we want a case insensitive serach
	 * 
	 * @return true or false
	 */
	public boolean getCaseInsensitiveSearch() {
		return caseInsensitive;
	}

	// Helper methods

	/**
	 * Create the search object from the information
	 * entered in the dialog
	 */
	private void createSearchObject() {
		searchObj = new BookSearchObject();
		searchObj.setSearchType(BookSearchObject.AUTHORSEARCH);
		searchObj.setLastName(lastname.getText());
		searchObj.setFirstName(firstname.getText());
	}

}

/**
 * Window dialog to get the information to search on a particular
 * book in the library. You must call <code>showDialog</code> to
 * invoke the dialog and check the boolean return code to see if 
 * a book search object was created. If created, use
 * <code>getSearchObject</code> to get the created search object.
 *
 */
class BookSearchDialog extends JDialog {
	private JTextField title, lastname, isbn, date;
	private JComboBox<String> coverCombo;
	private boolean ok;
	private BookSearchObject searchObj;
	private boolean caseInsensitive;

	// Constructors

	/**
	 * Create and lay out dialog for a book search 
	 */
	public BookSearchDialog(JFrame owner) {
		super(owner, "Search By Book", true);
		Container contentPane = getContentPane();

		// Set up fields for book search information entry
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(6, 2));
		panel.add(new JLabel("Title:"));
		panel.add(title = new JTextField(""));
		panel.add(new JLabel("Author's Last Name:"));
		panel.add(lastname = new JTextField(""));
		panel.add(new JLabel("ISBN:"));
		panel.add(isbn = new JTextField(""));
		panel.add(new JLabel("Publish Date:"));
		panel.add(date = new JTextField(""));
		panel.add(new JLabel("Book Cover:"));
		coverCombo = new JComboBox<String>();
		coverCombo.setEditable(false);
		for (int i = 0; i < Book.COVERNAME.length; i++)
			coverCombo.addItem(Book.COVERNAME[i]);
		coverCombo.setSelectedIndex(Book.ANYCOVER);
		panel.add(coverCombo);
		JCheckBox caseInsensitiveCheckBox = new JCheckBox("Case Insensitive Search?");
		panel.add(caseInsensitiveCheckBox);

		contentPane.add(panel, BorderLayout.CENTER);

		// Invoke create of book search object with **Search** button
		JButton searchButton = new JButton("Search");
		searchButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				ok = true;
				createSearchObject();
				caseInsensitive = caseInsensitiveCheckBox.isSelected();
				setVisible(false);
			}
		});

		// Clear entry fields with **Clear** button
		JButton clearButton = new JButton("Clear");
		clearButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				title.setText("");
				lastname.setText("");
				isbn.setText("");
				date.setText("");
				coverCombo.setSelectedIndex(Book.HARDCOVER);
			}
		});

		// Cancel search dialog with **Cancel** button
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				setVisible(false);
			}
		});

		// Add buttons to panel
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(searchButton);
		buttonPanel.add(clearButton);
		buttonPanel.add(cancelButton);
		contentPane.add(buttonPanel, BorderLayout.SOUTH);
		pack();
	}

	// Display methods

	/**
	 * Display dialog and return true if a search object gets created
	 * 
	 * @return True if a search object successfully created.
	 */
	public boolean showDialog() {
		ok = false;
		setVisible(true);
		return ok;
	}

	// Accessor methods

	/** 
	 * Return the created book search object
	 * 
	 * @return The search object with the book search criteria
	 */
	public BookSearchObject getSearchObject() {
		return searchObj;
	}

	/**
	 * Return if we want a case insensitive serach
	 * 
	 * @return true or false
	 */
	public boolean getCaseInsensitiveSearch() {
		return caseInsensitive;
	}

	// Helper methods

	/**
	 * Create the search object from the information entered
	 * in the dialog
	 */
	private void createSearchObject() {
		searchObj = new BookSearchObject();
		searchObj.setSearchType(BookSearchObject.BOOKSEARCH);
		searchObj.setTitle(title.getText());
		searchObj.setLastName(lastname.getText());
		searchObj.setISBN(isbn.getText());
		String dateStr = date.getText();
		if (dateStr.equals(""))
			searchObj.setDate(Book.BADDATE);
		else
			searchObj.setDate(Integer.parseInt(dateStr));
		searchObj.setCoverType(coverCombo.getSelectedIndex());
	}

}

/**
 * Window dialog to get the information to search for a book series
 * in the library. You must call <code>showDialog</code> to
 * invoke the dialog and check the boolean return code to see if 
 * a book search object was created. If created, use
 * <code>getSearchObject</code> to get the created search object.
 *
 */
class SeriesSearchDialog extends JDialog {
	private JTextField series;
	private boolean ok;
	private BookSearchObject searchObj;
	private boolean caseInsensitive;

	// Constructors

	/**
	 * Create and layout a dialog for a book series search
	 */
	public SeriesSearchDialog(JFrame owner) {
		super(owner, "Search By Series", true);
		Container contentPane = getContentPane();

		// Set up fields for a book series search information entry
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(2, 2));
		panel.add(new JLabel("Series Title:"));
		panel.add(series = new JTextField(""));
		JCheckBox caseInsensitiveCheckBox = new JCheckBox("Case Insensitive Search?");
		panel.add(caseInsensitiveCheckBox);

		contentPane.add(panel, BorderLayout.CENTER);

		// Invoke create of book search object with **Search** button
		JButton searchButton = new JButton("Search");
		searchButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				ok = true;
				createBookSearchObject();
				caseInsensitive = caseInsensitiveCheckBox.isSelected();
				setVisible(false);
			}
		});

		// Clear entry fields with **Clear** button
		JButton clearButton = new JButton("Clear");
		clearButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				series.setText("");
			}
		});

		// Cancel search dialog with **Cancel** button
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				setVisible(false);
			}
		});

		// Add buttons to panel
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(searchButton);
		buttonPanel.add(clearButton);
		buttonPanel.add(cancelButton);
		contentPane.add(buttonPanel, BorderLayout.SOUTH);
		pack();
	}

	// Display methods

	/**
	 * Display dialog and return true if the search object gets created
	 * 
	 * @return Trues if search object successfully created.
	 */
	public boolean showDialog() {
		ok = false;
		setVisible(true);
		return ok;
	}

	// Accessor methods

	/**
	 * Return the created book series search object
	 * 
	 * @return The search object with the book series search criteria
	 */
	public BookSearchObject getBookSearchObject() {
		return searchObj;
	}

	/**
	 * Return if we want a case insensitive serach
	 * 
	 * @return true or false
	 */
	public boolean getCaseInsensitiveSearch() {
		return caseInsensitive;
	}

	// Helper methods

	/**
	 * Create the search object from the information
	 * enetered in the dialog
	 */
	private void createBookSearchObject() {
		searchObj = new BookSearchObject();
		searchObj.setSearchType(BookSearchObject.SERIESSEARCH);
		searchObj.setSeries(series.getText());
	}

}
