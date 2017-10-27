package pinz;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import java.io.*;
import java.net.*;
import javax.imageio.*;

public class Pinz extends JFrame {

    BufferedImage image;
    JLabel promptLabel;
    JTextField prompt;
    JButton promptButton;
    JFileChooser fileChooser;
    JButton loadButton;
    JButton toGrayscaleButton;
    JButton processingButton;
    JScrollPane scrollPane;
    JLabel imgLabel;
    JLabel oknoLabel;
    JTextField oknoText;
    boolean isbin;

    public Pinz() {
        super("Image processing");
        isbin = false;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Container contentPane = getContentPane();
        JPanel inputPanel = new JPanel();
        promptLabel = new JLabel("Filename:");
        inputPanel.add(promptLabel);
        prompt = new JTextField(20);
        inputPanel.add(prompt);
        promptButton = new JButton("Browse");
        inputPanel.add(promptButton);
        contentPane.add(inputPanel, BorderLayout.NORTH);
        fileChooser = new JFileChooser();
        promptButton.addActionListener((ActionEvent e) -> {
            int returnValue
                    = fileChooser.showOpenDialog(null);
            if (returnValue
                    == JFileChooser.APPROVE_OPTION) {
                File selectedFile
                        = fileChooser.getSelectedFile();
                if (selectedFile != null) {
                    prompt.setText(selectedFile.getAbsolutePath());
                }
            }
        });

        imgLabel = new JLabel();
        scrollPane = new JScrollPane(imgLabel);
        scrollPane.setPreferredSize(new Dimension(1000, 650));
        contentPane.add(scrollPane, BorderLayout.CENTER);

        JPanel outputPanel = new JPanel();
        loadButton = new JButton("Load");
        outputPanel.add(loadButton);
        loadButton.addActionListener((ActionEvent e) -> {
            try {
                String name1 = prompt.getText();
                File file = new File(name1);
                if (file.exists()) {
                    image = ImageIO.read(file.toURL());
                    if (image == null) {
                        System.err.println("Invalid input file format");
                    } else {
                        imgLabel.setIcon(new ImageIcon(image));
                    }
                } else {
                    System.err.println("Bad filename");
                }
            } catch (MalformedURLException mur) {
                System.err.println("Bad filename");
            } catch (IOException ioe) {
                System.err.println("Error reading file");
            }
        });

        toGrayscaleButton = new JButton("Grayscale");
        outputPanel.add(toGrayscaleButton);
        toGrayscaleButton.addActionListener((ActionEvent e) -> {
            toGrayscale(image);
            imgLabel.setIcon(new ImageIcon(image));
        });

        processingButton = new JButton("Binarization");
        outputPanel.add(processingButton);
        processingButton.addActionListener((ActionEvent e) -> {
            int okno = Integer.parseInt(oknoText.getText());
            if (okno <= 0 || okno % 2 == 0)
                JOptionPane.showMessageDialog(null, "Rozmiar okna musi być nieparzyste i większe od zera.");
            else {
                if (isbin) {
                    loadButton.doClick();
                    toGrayscaleButton.doClick();
                }
                Binarization(image, okno);
                imgLabel.setIcon(new ImageIcon(image));
                isbin = true;
            }
        });
        
        oknoLabel = new JLabel();
        outputPanel.add(oknoLabel);
        oknoLabel.setText("Okno:");
        
        oknoText = new JTextField(3);
        oknoText.setText("3");
        outputPanel.add(oknoText);

        contentPane.add(outputPanel, BorderLayout.SOUTH);
    }

    private static void Binarization(BufferedImage img, int okno) {
        int w = img.getWidth(null);
        int h = img.getHeight(null);
        
        // Tworzenie pomocniczej tablicy o wymiarach takich samych jak wczytany obraz
        int obraz2d[][] = new int[w][h];
        
        // Liczenie z wielkosci okna ile pikseli otoczenia bedzie sprawdzane z kazdej strony
        int s = (okno - 1) / 2;
        
        // Tworzenie tablicy o wymiarach takich samych jak wczytany obraz do przechowywania wynikow (255 lub 0)
        int obrazbin[][] = new int[w][h];
        
        // Dla kazdego piksela wczytanego obrazu
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int rgb = img.getRGB(x, y);
                
                // Obliczanie wartosci poszczegolnych komponentow czerwonego, zielonego i niebieskiego
                int r = (rgb & 0x00ff0000) >>> 16;
                int g = (rgb & 0x0000ff00) >>> 8;
                int b = rgb & 0x000000ff;
                
                // Wpisywanie do tablicy pomocniczej poziomu jasnosci danego piksela
                obraz2d[x][y] = (r + g + b) / 3;
            }
        }
        
        // Dla kazdego piksela wczytanego obrazu (poza pikselami po bokach (zaleznie od wybranej wielkosci okna)
        for (int x = s; x < w-s; x++) {
            for (int y = s; y < h-s; y++) {
                
                // Pomocniczo wartosci poczatkowe
                int min = 255;
                int max = 0;
                
                // Sprawdzanie otoczenia pojedynczych pikseli
                for (int k = x-s; k<=x+s; k++) {
                    for (int l=y-s; l<=y+s; l++) {
                        
                        // Szukanie najjasniejszego i najciemniejszego piksela
                        if (obraz2d[k][l] > max)
                            max = obraz2d[k][l];
                        if (obraz2d[k][l] < min)
                            min = obraz2d[k][l];
                    }
                }
                
                // Wyciaganie sredniej
                double T = (min+max)/2;
                
                // Zapisywanie wyniku (czarny, jesli wartosc badanego piksela jest wieksza od wyliczonej sredniej lub bialy w przeciwnym wypadku)
                if (obraz2d[x][y] > T)
                    obrazbin[x][y] = 255;
                else
                    obrazbin[x][y] = 0;
            }
        }
        // Dla kazdego wyliczonego elementu tablicy wynikowej
        for (int x = s; x < w-s; x++) {
            for (int y = s; y < h-s; y++) {
                
                // Jesli jest rowny 1, przypisujemy kolor czarny, w przeciwnym wypadku bialy
                int lvl = obrazbin[x][y];
                img.setRGB(x, y, new Color(lvl, lvl, lvl).getRGB());
            }
        }
    }

    private static void toGrayscale(BufferedImage img) {
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
        ColorConvertOp op = new ColorConvertOp(cs, null);
        op.filter(img, img);

    }

    public static void main(String args[]) {
        JFrame frame = new Pinz();
        frame.pack();
        frame.show();
    }
}
