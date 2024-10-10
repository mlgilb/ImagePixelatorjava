import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImagePixelator {
    private JFrame frame;
    private JLabel imageLabel;
    private BufferedImage originalImage;
    private BufferedImage pixelatedImage;
    private boolean isPixelated = false;

    public ImagePixelator() {
        frame = new JFrame("Image Pixelator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 600);
        frame.setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel();
        JButton loadButton = new JButton("Load Image");
        JButton toggleButton = new JButton("Toggle Pixelation");
        JButton saveButton = new JButton("Save Pixelated Image");

        buttonPanel.add(loadButton);
        buttonPanel.add(toggleButton);
        buttonPanel.add(saveButton);

        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(JLabel.CENTER);

        frame.add(buttonPanel, BorderLayout.SOUTH);
        frame.add(imageLabel, BorderLayout.CENTER);

        loadButton.addActionListener(new LoadButtonListener());
        toggleButton.addActionListener(new ToggleButtonListener());
        saveButton.addActionListener(new SaveButtonListener());

        frame.setVisible(true);
    }

    private class LoadButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try {
                    originalImage = ImageIO.read(file);
                    originalImage = resizeImage(originalImage, 256, 256);
                    pixelatedImage = pixelateImage(originalImage);
                    imageLabel.setIcon(new ImageIcon(originalImage));
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame, "Error loading image.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private class ToggleButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (originalImage != null) {
                isPixelated = !isPixelated;
                imageLabel.setIcon(new ImageIcon(isPixelated ? pixelatedImage : originalImage));
            }
        }
    }

    private class SaveButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (pixelatedImage != null) {
                JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showSaveDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    try {
                        ImageIO.write(pixelatedImage, "png", file);
                        JOptionPane.showMessageDialog(frame, "Image saved successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(frame, "Error saving image.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }

    private BufferedImage pixelateImage(BufferedImage source) {
        int width = source.getWidth();
        int height = source.getHeight();
        BufferedImage pixelated = new BufferedImage(width, height, source.getType());

        int pixelSize = 16; // Larger pixel size for a more 8-bit effect

        for (int y = 0; y < height; y += pixelSize) {
            for (int x = 0; x < width; x += pixelSize) {
                int avgColor = getAverageColor(source, x, y, pixelSize);
                for (int dy = 0; dy < pixelSize && y + dy < height; dy++) {
                    for (int dx = 0; dx < pixelSize && x + dx < width; dx++) {
                        pixelated.setRGB(x + dx, y + dy, avgColor);
                    }
                }
            }
        }

        return pixelated;
    }

    private int getAverageColor(BufferedImage img, int x, int y, int pixelSize) {
        int width = img.getWidth();
        int height = img.getHeight();
        int r = 0, g = 0, b = 0, count = 0;

        for (int dy = 0; dy < pixelSize && y + dy < height; dy++) {
            for (int dx = 0; dx < pixelSize && x + dx < width; dx++) {
                int rgb = img.getRGB(x + dx, y + dy);
                r += (rgb >> 16) & 0xff;
                g += (rgb >> 8) & 0xff;
                b += rgb & 0xff;
                count++;
            }
        }

        r /= count;
        g /= count;
        b /= count;

        return (r << 16) | (g << 8) | b;
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        Image scaledImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.drawImage(scaledImage, 0, 0, null);
        g2d.dispose();
        return resizedImage;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ImagePixelator::new);
    }
}