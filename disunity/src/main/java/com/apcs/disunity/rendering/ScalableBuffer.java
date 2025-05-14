package com.apcs.disunity.rendering;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import com.apcs.disunity.math.Transform;
import com.apcs.disunity.math.Vector2;

/**
 * A scalable buffer to draw to
 * 
 * @author Qinzhao Li
 */
public class ScalableBuffer {

    /* ================ [ FIELDS ] ================ */

    // Image
    private BufferedImage image;
    private Graphics2D graphics;

    // Scale
    private Vector2 ratio;
    private double xScale, yScale;

    // Constructors
    public ScalableBuffer(Vector2 ratio) { this(ratio, ratio); }
    public ScalableBuffer(Vector2 ratio, Vector2 target) {
        // Set ratio
        this.ratio = ratio;

        // Refresh buffer
        this.setSize(target);
    }

    /* ================ [ METHODS ] ================ */

    // Refresh buffer
    public void setSize(Vector2 size) {
        size = Vector2.of(
            Math.min(size.x, size.y * ratio.x / ratio.y),
            Math.min(size.y, size.x * ratio.y / ratio.x)
        );

        // Create buffer
        image = new BufferedImage(
            size.xi, size.yi,
            BufferedImage.TYPE_INT_ARGB
        );
        graphics = image.createGraphics();

        // Set scale
        xScale = size.x / ratio.x;
        yScale = size.y / ratio.y;

        // White background
        graphics.setBackground(Color.WHITE);
    }

    // Clear buffer
    public void clear() { graphics.clearRect(0, 0, image.getWidth(), image.getHeight()); }

    // Getters
    public double getXScale() { return xScale; }
    public double getYScale() { return yScale; }
    public BufferedImage getImage() { return image; }

    /* ================ [ GRAPHICS ] ================ */

    // Draw image
    public void drawImage(Image img, Transform transform) {
        
        // Image dimensions
        double imgWidth = img.getWidth(null);
        double imgHeight = img.getHeight(null);

        // Center pivot
        Vector2 offset = Vector2.of(imgWidth, imgHeight)
            .mul(transform.scale)
            .mul(-0.5);
        Transform _transform = transform.move(offset);

        // Drawing inputs
        int xPos = (int) Math.round(_transform.pos.x * xScale);
        int yPos = (int) Math.round(_transform.pos.y * yScale);
        int width = (int) Math.round(imgWidth * _transform.scale.x * xScale);
        int height = (int) Math.round(imgHeight * _transform.scale.y * yScale);

        graphics.drawImage(img, xPos, yPos, width, height, null);
    }

    public void drawString(String text, Transform transform) {
        // improve later
        graphics.drawString(text, (int) transform.pos.x, (int) transform.pos.y);
    }

}
