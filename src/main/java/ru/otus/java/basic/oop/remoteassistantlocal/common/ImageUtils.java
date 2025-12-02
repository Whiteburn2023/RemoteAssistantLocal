package ru.otus.java.basic.oop.remoteassistantlocal.common;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

public class ImageUtils {
    public static byte[] imageToBytes(BufferedImage image, float quality) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            // Конвертируем в JPEG с настройкой качества
            ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality);

            writer.setOutput(ImageIO.createImageOutputStream(baos));
            writer.write(null, new IIOImage(image, null, null), param);
            writer.dispose();

            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static BufferedImage scaleImage(BufferedImage original, int maxWidth) {
        double scale = (double) maxWidth / original.getWidth();
        int newWidth = (int) (original.getWidth() * scale);
        int newHeight = (int) (original.getHeight() * scale);

        BufferedImage scaled = new BufferedImage(newWidth, newHeight, original.getType());
        Graphics2D g2d = scaled.createGraphics();
        g2d.drawImage(original, 0, 0, newWidth, newHeight, null);
        g2d.dispose();

        return scaled;
    }
}
