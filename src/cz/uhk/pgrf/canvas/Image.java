package cz.uhk.pgrf.canvas;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;

import javax.imageio.ImageIO;

public class Image {
	private BufferedImage image;
	private BufferedImage imageEdge;
	private BufferedImage imageGray;
	private int width;
	private int height;
	private InputStream is;
	private InputStream is2;
	private InputStream is3;
	private String filePath = null;
	private int[] edgeMat = { 1, 1, 1, 1, -8, 1, 1, 1, 1 };
	private ArrayList<Integer> edgeArray = new ArrayList<>();
	private ArrayList<Integer> edgeImageArray = new ArrayList<>();
	private int accurancy = 128;

	public Image() {
	}

	/**
	 * Naètení obrázku
	 */
	public void Load() {
		if (filePath == null) {
			return;
		} else {
			try {
				is = new FileInputStream(filePath);
				is2 = new FileInputStream(filePath);
				is3 = new FileInputStream(filePath);
				loadImage();
			} catch (FileNotFoundException e) {
				System.out.println("File path corupted");
				return;
			}
		}
	}

	private void loadImage() {
		try {
			setImage(ImageIO.read(is));
			setImageEdge(ImageIO.read(is2));
			setImageGray(ImageIO.read(is3));
			width = getImage().getWidth();
			height = getImage().getHeight();
			System.out.println("File loaded");
		} catch (Exception e) {
			System.out.println("File format corrupted");
			return;
		}
	}

	/**
	 * pøevedení na odstíny šedi
	 */
	public void makeGrayscale() {
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int rgb = image.getRGB(x, y);
				int r = (rgb >> 16) & 0xFF;
				int g = (rgb >> 8) & 0xFF;
				int b = (rgb & 0xFF);

				int grayLevel = (r + g + b) / 3;
				int gray = (grayLevel << 16) + (grayLevel << 8) + grayLevel;
				imageGray.setRGB(x, y, gray);
			}
		}

	}

	/**
	 * detekce hran
	 */
	public void edgeDetect() {
		edgeImageArray.clear();
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (y == 0 || y == height - 1 || x == 0 || x == width - 1) {
					edgeArray.add(0x000000);
					edgeImageArray.add(0);
				} else {
					int rgb0 = imageGray.getRGB(x - 1, y - 1);
					int rgb1 = imageGray.getRGB(x, y - 1);
					int rgb2 = imageGray.getRGB(x + 1, y - 1);

					int rgb3 = imageGray.getRGB(x - 1, y);
					int rgb4 = imageGray.getRGB(x, y);
					int rgb5 = imageGray.getRGB(x + 1, y);

					int rgb6 = imageGray.getRGB(x - 1, y);
					int rgb7 = imageGray.getRGB(x, y);
					int rgb8 = imageGray.getRGB(x + 1, y);
					
					int r0 = (rgb0 >> 16) & 0xFF;
					int r1 = (rgb1 >> 16) & 0xFF;
					int r2 = (rgb2 >> 16) & 0xFF;
					int r3 = (rgb3 >> 16) & 0xFF;
					int r4 = (rgb4 >> 16) & 0xFF;
					int r5 = (rgb5 >> 16) & 0xFF;
					int r6 = (rgb6 >> 16) & 0xFF;
					int r7 = (rgb7 >> 16) & 0xFF;
					int r8 = (rgb8 >> 16) & 0xFF;

					r0 = r0 * edgeMat[0];
					r1 = r1 * edgeMat[1];
					r2 = r2 * edgeMat[2];

					r3 = r3 * edgeMat[3];
					r4 = r4 * edgeMat[4];
					r5 = r5 * edgeMat[5];

					r6 = r6 * edgeMat[6];
					r7 = r7 * edgeMat[7];
					r8 = r8 * edgeMat[8];

					int edgeR = r0 + r1 + r2 + r3 + r4 + r5 + r6 + r7 + r8;
					if (edgeR <= accurancy) {
						edgeArray.add(0x000000);
						edgeImageArray.add(0);
					} else {
						edgeArray.add(0xffffff);
						edgeImageArray.add(1);
					}
				}
			}
		}
		int help = 0;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				imageEdge.setRGB(x, y, edgeArray.get(help));
				help++;
			}
		}
		edgeArray.clear();
	}

	/**
	 * vektorizace
	 */
	public void vectorize() {
		int[][] componentmap;
		int[][] bitimage;
		componentmap = new int[height][width];
		bitimage = new int[height][width];
		int help = 0;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				componentmap[y][x] = 0;
				bitimage[y][x] = edgeImageArray.get(help);
				help++;
			}
		}

		// nalezení component
		System.out.println("BitImage created and ComponentMap zero filled");
		int component_num = 1;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (y != 0 || y != height - 1 || x != 0 || x != width - 1) {
					if (bitimage[y][x] == 1) {
						if (componentmap[y - 1][x] == 0 && componentmap[y - 1][x - 1] == 0
								&& componentmap[y][x - 1] == 0 && componentmap[y + 1][x - 1] == 0) {
							componentmap[y][x] = component_num;
							component_num++;
						} else if (componentmap[y - 1][x] != 0 || componentmap[y - 1][x - 1] != 0
								|| componentmap[y][x - 1] != 0 || componentmap[y + 1][x - 1] != 0) {
							if (componentmap[y - 1][x] != 0 && componentmap[y - 1][x - 1] != 0
									&& componentmap[y][x - 1] != 0 && componentmap[y + 1][x - 1] != 0) {
								ArrayList<Integer> helpmin = new ArrayList<>();
								helpmin.add(componentmap[y - 1][x]);
								helpmin.add(componentmap[y - 1][x - 1]);
								helpmin.add(componentmap[y][x - 1]);
								helpmin.add(componentmap[y + 1][x - 1]);
								componentmap[y][x] = Collections.min(helpmin);
							} else {
								ArrayList<Integer> helpmax = new ArrayList<>();
								helpmax.add(componentmap[y - 1][x]);
								helpmax.add(componentmap[y - 1][x - 1]);
								helpmax.add(componentmap[y][x - 1]);
								helpmax.add(componentmap[y + 1][x - 1]);
								componentmap[y][x] = Collections.max(helpmax);
							}
						}
					}
				}
			}
		}

		// eliminace pøebyteèných komponent
		System.out.println("First component passage succeeded");
		System.out.println("Component count: " + component_num);
		int passage = 1;
		System.out.println("Doing component marging ");
		while (passage < component_num) {
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					if (y != 0 || y != height - 1 || x != 0 || x != width - 1) {
						if (componentmap[y][x] == 1) {
							if (componentmap[y - 1][x] != 0 && componentmap[y - 1][x - 1] != 0
									&& componentmap[y][x - 1] != 0 && componentmap[y + 1][x - 1] != 0) {
								ArrayList<Integer> helpmin = new ArrayList<>();
								helpmin.add(componentmap[y - 1][x]);
								helpmin.add(componentmap[y - 1][x - 1]);
								helpmin.add(componentmap[y][x - 1]);
								helpmin.add(componentmap[y + 1][x - 1]);
								componentmap[y][x] = Collections.min(helpmin);
							}
						}
					}
				}
			}
			passage++;
		}
		System.out.println("All components marging succeeded");

		// TODO Freeman chain code algorithm

		// testing file
		try {
			OutputStream os = new FileOutputStream("res/out/test_image_out.txt");
			for (int x = 0; x < height; x++) {
				for (int y = 0; y < width; y++) {
					String help1 = Integer.toString(componentmap[x][y]);
					os.write(help1.getBytes());
				}
				os.write('\n');
			}
			os.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public int getAccurancy() {
		return accurancy;
	}

	public void setAccurancy(int accurancy) {
		this.accurancy = accurancy;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public BufferedImage getImage() {
		return image;
	}

	public void setImage(BufferedImage image) {
		this.image = image;
	}

	public BufferedImage getImageEdge() {
		return imageEdge;
	}

	public void setImageEdge(BufferedImage imageEdge) {
		this.imageEdge = imageEdge;
	}

	public BufferedImage getImageGray() {
		return imageGray;
	}

	public void setImageGray(BufferedImage imageGray) {
		this.imageGray = imageGray;
	}
}
