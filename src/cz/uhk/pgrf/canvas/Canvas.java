package cz.uhk.pgrf.canvas;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Tøída pro kreslení na plátno, interface plátna.
 * 
 * @author Tomáš Novák
 * @version 2016
 */

public class Canvas implements Runnable {

	private static int CLEAR_COLOR = 0xefefef;
	private JFrame frame;
	private JPanel panel;
	private BufferedImage img;
	private Image image;
	private Thread thread = null;
	static final int ACCURACY_MIN = 0;
	static final int ACCURACY_MAX = 255;
	static final int ACCURACY_INIT = 128;
	private JSlider accurancy;
	private JButton vectorizer;

	public Canvas(int width, int height) {
		/**
		 * Nastavení okna
		 */
		frame = new JFrame();
		frame.setTitle("PGRF2 - Vectorizer v1.2");
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		image = new Image();

		/**
		 * Vytvoøení toolbaru
		 */
		panel = new JPanel();
		panel.setPreferredSize(new Dimension(width, height));

		frame.add(panel);
		frame.pack();
		frame.setVisible(true);

		JToolBar tb = new JToolBar();
		frame.add(tb, BorderLayout.NORTH);
		vytvorTlacitka(tb);
	}

	/**
	 * Vytváøení interface (GUI)
	 * 
	 * @param kontejner
	 */
	private void vytvorTlacitka(JComponent kontejner) {
		JButton open = new JButton("Open Image");
		JButton orig = new JButton("ShowOriginal");
		JButton grayscale = new JButton("Greyscale");
		JButton edge = new JButton("ShowEdge");
		vectorizer = new JButton("Vectorize");
		JButton about = new JButton("About");

		accurancy = new JSlider(JSlider.HORIZONTAL, ACCURACY_MIN, ACCURACY_MAX, ACCURACY_INIT);

		accurancy.setMajorTickSpacing(10);
		accurancy.setMinorTickSpacing(1);
		accurancy.setPaintTicks(false);
		accurancy.setPaintLabels(false);

		kontejner.add(open);
		kontejner.add(orig);
		kontejner.add(grayscale);
		kontejner.add(edge);
		kontejner.add(vectorizer);
		kontejner.add(accurancy);
		kontejner.add(about);

		vectorizer.setEnabled(false);

		open.addActionListener(e -> open());
		orig.addActionListener(e -> orig());
		grayscale.addActionListener(e -> greyscale());
		edge.addActionListener(e -> edge());
		vectorizer.addActionListener(e -> vectorize());
		accurancy.addChangeListener(e -> accurancy());
		about.addActionListener(e -> about());
	}

	private void vectorize() {
		image.vectorize();
	}

	private void about() {
		JTextArea head = new JTextArea();
		JTextArea text = new JTextArea();
		Font font_h = new Font("Verdana", Font.BOLD, 12);
		Font font_t = new Font("Verdana", Font.PLAIN, 10);

		head.setFont(font_h);
		head.setText("PGRF2 - Vectorizer v1.2");
		head.setEditable(false);
		text.setFont(font_t);
		text.setEditable(false);
		text.setText(
				"Tento program byl vytvoøen v rámci pøedmìtu Poèítaèová grafika 2,\núèelem tohoto programu bylo pøedvést jeden z monıch postupù\npro vektorizaci bitmapovıch obrázkù.\n\nProgram je stále ve vıvoji.\nVytvoøil Tomáš Novák, 4/2017 Univerzita Hradec Králové\nPoslední úprava 27.4.2017");

		JDialog dialog_about = new JDialog(frame, "PGRF2 - Vectorizer v1.2", true);
		dialog_about.setLayout(new FlowLayout());
		JButton close = new JButton("OK");
		close.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dialog_about.setVisible(false);
			}
		});
		dialog_about.setSize(400, 200);
		dialog_about.add(head);
		dialog_about.add(text);
		dialog_about.add(close);
		dialog_about.setVisible(true);
	}

	private void orig() {
		img = image.getImage();

	}

	private void accurancy() {
		image.setAccurancy(accurancy.getValue());
	}

	private void edge() {
		image.edgeDetect();
		vectorizer.setEnabled(true);
		img = image.getImageEdge();
	}

	private void greyscale() {
		image.makeGrayscale();
		img = image.getImageGray();
	}

	/**
	 * Vıbìr obrázkù
	 * 
	 */
	private void open() {
		clear(CLEAR_COLOR);
		vectorizer.setEnabled(false);
		final JFileChooser chooser = new JFileChooser();
		FileFilter filter = new FileNameExtensionFilter("Image files", "JPG", "JPEG", "PNG");
		chooser.setFileFilter(filter);
		chooser.setCurrentDirectory(chooser.getFileSystemView().getParentDirectory(new File("res/out/")));
		int returnVal = chooser.showOpenDialog(chooser);
		String filePath = null;
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			clear(CLEAR_COLOR);
			filePath = chooser.getSelectedFile().getAbsolutePath();
			image.setFilePath(filePath);
			image.Load();
			img = image.getImage();
			resize();
		} else {
			System.out.println("User clicked CANCEL");
		}
	}

	/**
	 * Vyèištìní plátna
	 * 
	 * @param color
	 */
	public void clear(int color) {
		Graphics gr = img.getGraphics();
		gr.setColor(new Color(color));
		gr.fillRect(0, 0, img.getWidth(), img.getHeight());
	}

	/**
	 * Refresh, vykreslení prázdného plátna a startovací metoda
	 */
	public void present() {
		if (panel.getGraphics() != null)
			panel.getGraphics().drawImage(img, 0, 0, null);
	}

	private void resize() {
		if (image.getImage().getWidth() > 500 || image.getImage().getHeight() > 400) {
			panel.setSize(image.getImage().getWidth(), image.getImage().getHeight());
			frame.setSize(image.getImage().getWidth(), image.getImage().getHeight() + 60);
		}
	}

	public void first() {
		clear(CLEAR_COLOR);
		present();
	}

	public void start() {
		if (thread == null) {
			thread = new Thread(this);
			thread.start();
		}
	}

	public void stop() {
		thread = null;
	}

	public void run() {
		while (thread != null) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
			present();
		}
		thread = null;
	}

	public static void main(String[] args) {
		Canvas canvas = new Canvas(500, 400);
		SwingUtilities.invokeLater(() -> {
			SwingUtilities.invokeLater(() -> {
				SwingUtilities.invokeLater(() -> {
					SwingUtilities.invokeLater(() -> {
						canvas.first();
						canvas.start();
					});
				});
			});
		});
	}
}
