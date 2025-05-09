package com.apcs.disunity.nodes.sprite;

import java.awt.image.BufferedImage;

import com.apcs.disunity.Game;
import com.apcs.disunity.math.Transform;
import com.apcs.disunity.nodes.Node;
import com.apcs.disunity.nodes.Node2D;
import com.apcs.disunity.resources.Image;
import com.apcs.disunity.resources.Resources;

/**
 * A 2d node that renders an image
 * 
 * @author Qinzhao Li
 */
public class Sprite extends Node2D {

    /* ================ [ FIELDS ] ================ */

    // Sprite image id
    private String image;

    // Constructors
    public Sprite(String image) { super(); this.image = image; }
    public Sprite(String image, Node<?>... children) { super(children); this.image = image; }
    public Sprite(String image, Transform transform, Node<?>... children) { super(transform, children); this.image = image; }
    public Sprite(String image, boolean visible) { super(visible); this.image = image; }
    public Sprite(String image, boolean visible, Node<?>... children) { super(visible, children); this.image = image; }
    public Sprite(String image, Transform transform, boolean visible, Node<?>... children) { super(transform, visible, children); this.image = image; }

    /* ================ [ METHODS ] ================ */

    // Get image
    public String getImage() { return image; }

    // Set image
    public void setImage(String image) { this.image = image; }

    /* ================ [ NODE ] ================ */

    @Override
    public void draw(Transform offset) {

        // Load sprite image
        BufferedImage img = Resources.loadResource(getImage(), Image.class).getBuffer();

        // Draw image to buffer
        Game.getInstance().getBuffer().drawImage(img, transform.apply(offset));

        // Draw children
        super.draw(offset);
    }

    public double getWidth() {
        BufferedImage img = Resources.loadResource(getImage(), Image.class).getBuffer();
        return img.getWidth();
    }

    public double getHeight() {
        BufferedImage img = Resources.loadResource(getImage(), Image.class).getBuffer();
        return img.getHeight();
    }

}
