# Experiments In Accessing Windows IME From an Ubuntu WSL Java Program

The problem to solve is allowing Japanese input into the Book Library to track
Japanese books. Numerous experiments were conducted on simplified programs in
an attempt to get this functioning as documented here.

## Experiments With Swing Only and the `JTextField` Widget

Variations of the following code file modified from the Book Library project 
were the subject of these experiments

```
package com.amac.javajapan;

import java.awt.*;
import java.awt.event.*;
import java.awt.im.InputContext;
import javax.swing.*;
import java.util.*;
import java.io.*;


/**
 * This code is to test the ability to set Japanese input and
 * text output in a Swing JTextField.
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

		System.out.println("Program starting and setting locale to Japanese");
		Locale.setDefault(Locale.JAPANESE);
		System.out.println("At start default locale is: " + Locale.getDefault());
		System.out.println("At start file encoding is: " + System.getProperty(("file.encoding")));
	
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

	static InputContext currentContext;

	// Default window size parameters
	private static final int DEFAULT_WIDTH = 800;
	private static final int DEFAULT_HEIGHT = 600;
	private static final int BORDER_WIDTH = 30;

	private JTextField title;

	/**
	 * Constructor of the main window with input text field.
	 *
	 */
	public BookLibGuiFrame() {

		setTitle("Library Of Books");
		setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);

		Locale.setDefault(Locale.JAPANESE);
		currentContext = InputContext.getInstance();
		System.out.println("In GUI Frame, input context is locale:" + currentContext.getLocale());
		currentContext.selectInputMethod(new Locale("ja", "JP"));
		System.out.println("Again setting locale to Japanese in GUI Frame.");
		System.out.println("And checking the locale:" + Locale.getDefault());
		System.out.println("After setting currentContext, it is:" + currentContext.getLocale());
		Container contentPane = getContentPane();

		JPanel panel = new JPanel();
		contentPane.add(panel);
		boolean result = currentContext.selectInputMethod(Locale.JAPANESE);
		System.out.println("After adding panel to Pane, checking if local context is Japanese:" + result);
		panel.setLayout(new GridLayout(5, 2));
		panel.add(new JLabel("Title:"));
		title = new JTextField("");
		InputContext titleInputContext = title.getInputContext();
		if (titleInputContext == null) {
			System.out.println("foo context is null");
		} else {
			titleInputContext.selectInputMethod(new Locale("ja", "JP"));
		};
		// Initialize with Japanese writing
		setJapanese(title);
		panel.add(title);
		System.out.println(getLocale());

	}

	public void setJapanese(JTextField title) {
		Font japaneseFont = new Font("Noto Sans JP Regular", Font.PLAIN, 12); 
		title.setFont(japaneseFont);
		title.setLocale(Locale.JAPANESE);
		String str = "こんにちは世界"; 
		title.setText(str);
	}

}
```

A huge amount of potential solutions or clues were checking in the following list of links:


- https://docs.oracle.com/javase/7/docs/api/java/awt/im/InputContext.html#selectInputMethod(java.util.Locale)
- https://stackoverflow.com/questions/9903666/changing-ime-language-in-java-swing-application
- https://www.google.com/search?q=java+set+input+context+based+on+windows+11+ime+keyboard&client=firefox-b-1-d&sca_esv=4717ca0c091261fe&ei=Bp1oZ4bSCrjB0PEPwvyTkQY&start=10&sa=N&sstk=ATObxK5EIw9qO8D3dIHWpzpCJLiVSUafRK9nCEA3CO7g5OzbdBqW5MzvWfpZFfGp-mo7Le-CGcajgk1jdqidYdLL32gB6PCmKf-A0g&ved=2ahUKEwiG7f2vwLyKAxW4IDQIHUL-JGIQ8tMDegQICRAE&biw=1288&bih=821&dpr=1
- https://github.com/search?q=language%3AJava+selectInputMethod&type=code
- https://stackoverflow.com/questions/28231444/get-current-windows-keyboard-layout-in-java
- https://stackoverflow.com/questions/62289/read-write-to-windows-registry-using-java
- https://stackoverflow.com/questions/26551513/cannot-change-input-language-in-jtextfield
- https://www.google.com/search?q=does+openjdk+include+japanese+characters&client=firefox-b-1-d&sca_esv=898e8a150d101e06&ei=sTVrZ8G2Nvzw0PEPlcvTwQ4&oq=does+openinclude+japanese+characters&gs_lp=Egxnd3Mtd2l6LXNlcnAiJGRvZXMgb3BlbmluY2x1ZGUgamFwYW5lc2UgY2hhcmFjdGVycyoCCAIyChAhGKABGMMEGAoyChAhGKABGMMEGAoyChAhGKABGMMEGAoyChAhGKABGMMEGApIkThQuxRY6BZwAngBkAEAmAF_oAGfA6oBAzIuMrgBA8gBAPgBAZgCBaACswLCAgoQABiwAxjWBBhHmAMAiAYBkAYIkgcDMy4yoAeKHw&sclient=gws-wiz-serp
- https://www.google.com/search?client=firefox-b-1-d&q=how+to+ensure+jre+uses+utf8
- https://www.google.com/search?client=firefox-b-1-d&q=java+set+ime+for+JTextfield
- https://www.google.com/search?q=JTextField+%22selectInputMethod%22+returns+False&client=firefox-b-1-d&sca_esv=0e4caf73339aafce&ei=PUFrZ8HcCv7P0PEPjbe_qQI&start=10&sa=N&sstk=ATObxK6o3MgSMapFw4GquuhZ3XdrpI2QDmSDi2_nBjbleAeDdHQd_TuA4CDbg5miFrZoCKTvBs8Fc43VOJ3jkIDNqkNpoXsQLLpxEw&ved=2ahUKEwiB796hxcGKAxX-JzQIHY3bLyUQ8NMDegQICxAG&biw=1288&bih=821&dpr=1
- https://stackoverflow.com/questions/20180796/java-jtextfield-special-chars/20181009#20181009
- https://www.google.com/search?client=firefox-b-1-d&q=java+fonts+to+display+japanese

A number of questions were asked to determine where the issue laid. They were answered with code experiments:

1. Does java include Japanese Language set? - Yes
2. Is Hiragana include in UTF8? - Yes
3. What is the result of `System.getProperty(("file.encoding"))` in the executing program? - UTF-8
4. Does `JTextField` have a valid `InputContext` instance? No, `getInputContext()` returns null

Perhaps it is a result of not having a valid Japanese font available? To test we first determined
if there was a valid Japanese font family with the code:
```
import javafx.scene.text.Font;

public class PrintAllFonts {
    public static void main(String[] args) {
        //List<String> fonts = Font.getFontNames();

        for (String font : Font.getFontNames()) {
            System.out.println(font);
        }
    }
}
```

Ah ha! There is no valid Japanese font on the Ubuntu WSL system so we installed "Noto Sans JP" using the following instructions:

  1. Download font family from Google Fonts
  2. Create fonts directory:
       `% sudo mkdir /usr/share/fonts/truetype/notosansjp`
  3. Copy static fonts into the above newly created directory
  4. Update the font cache:
       `% sudo fc -cache -fv`

Now we attempt the display of Japanese text which was succesful (no more empty square boxes).
However this still does not allow the input of Japanese characters with the IME set to Japanese.

## Experiments With An Integrated Swing/JavaFX Approach

At this point, Swing seemed like a dead end. There is an updated Java Gui environment called JavaFX
so that was investigated. You can integrate JavaFX in Swing so if it did provide a solution for
text input, we could isolate the needed input panels to JavaFX and the rest of the already developed
code could remain in Swing. A promising solution!

The code used for this experiment:
```
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.TextField;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.util.Locale;

public class SwingFX {

    private static void initAndShowGUI() {
        // This method is invoked on the EDT thread
        JFrame frame = new JFrame("Swing and JavaFX");
        final JFXPanel fxPanel = new JFXPanel();
        frame.add(fxPanel);
        frame.setSize(300, 200);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                initFX(fxPanel);
            }
       });
    }

    private static void initFX(JFXPanel fxPanel) {
        // This method is invoked on the JavaFX thread
        Scene scene = createScene();
        fxPanel.setScene(scene);
    }

    private static Scene createScene() {
        Group  root  =  new  Group();
        Scene  scene  =  new  Scene(root, Color.ALICEBLUE);
        TextField  text  =  new  TextField();

        
        //text.setX(40);
        //text.setY(100);
	text.snapPositionX(40);
	text.snapPositionY(100);
        text.setFont(new Font(25));
        text.setText("Welcome JavaFX!");
        root.getChildren().add(text);

	Locale.setDefault(Locale.JAPANESE);
        String japaneseStr = "こんにちは世界";
	TextField japaneseText = new TextField();
	//japaneseText.setX(80);
	//japaneseText.setY(200);
	japaneseText.snapPositionX(48);
	japaneseText.snapPositionY(200);
	Font japaneseFont = new Font("Noto Sans JP Regular", 12);
	japaneseText.setFont(japaneseFont);
	japaneseText.setText(japaneseStr);
        root.getChildren().add(japaneseText);

        return (scene);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                initAndShowGUI();
            }
        });
    }
}
```

First off install JavaFX on Ubuntu:
  `% sudo apt-get install openjfx`

Now to run a sample JavaFX program:
  `% javac --add-modules javafx.controls --module-path $PATH_TO_FX HelloFX.java`
  `% java --add-modules javafx.controls --module-path $PATH_TO_FX HelloFX`

where the sample program is:
```
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class HelloFX extends Application {

    @Override
    public void start(Stage stage) {
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");
        Label l = new Label("Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".");
        Scene scene = new Scene(new StackPane(l), 640, 480);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}
```

Our final attempt to get to a solution involves running the integrated Swing/JavaFX sample program:
  `% javac --module-path $PATH_TO_FX --add-modules javafx.controls,javafx.swing -encoding utf-8 SwingFX.java`
  `% java --module-path $PATH_TO_FX --add-modules javafx.controls,javafx.swing -Dfile.encoding=utf-8 SwingFX`
  where `PATH_TO_FX=/usr/share/openjfx/lib`

This was a dead end. Time for a U-turn to only doing the development on Windows. First we need to test with the sample programs before proceeding back to the main project code.

## Installing JavaFX running integrated Swing/JavaFX sample program (Windows):

  1. Download javafx-sdk-17.0.15 sdk zip file from https://gluonhq.com/products/javafx/
  2. Unzip into your workspace
  3. Compile with appropriate options:

       `PS> javac --module-path PATH_TO_javafx-sdk-17.0.15\lib --add-modules javafx.controls,javafx.base,javafx.fxml,javafx.graphics,javafx.media,javafx.web,javafx.swing -encoding utf-8 SwingFX.java`

  4. Run with appropriate options:
      
      `PS> java "-Dfile.encoding=UTF-8" --module-path PATH_TO_javafx-sdk-17.0.15\lib\ --add-modules javafx.controls,javafx.base,javafx.fxml,javafx.graphics,javafx.media,javafx.web,javafx.swing --add-opens=javafx.graphics/javafx.scene=ALL-UNNAMED --add-exports javafx.base/com.sun.javafx.event=ALL-UNNAMED SwingFX`

   5. You now have access to Japanese IME characters!
