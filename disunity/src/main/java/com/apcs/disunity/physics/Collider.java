package com.apcs.disunity.physics;

import com.apcs.disunity.math.Vector2;
import com.apcs.disunity.nodes.Node;
import com.apcs.disunity.nodes.Node2D;
import com.apcs.disunity.nodes.UndrawnNode;

/**
 * A component that adds collision detection ability to a Node2D
 * @author Aayushya Patel
 */
public class Collider extends UndrawnNode {

    private Vector2 size;
    private Vector2 offset = Vector2.ZERO;
    private boolean isTrigger = false;

    // Collision floors
    private int layer = 1;

    // Generated ID for collision events
    private final int id;
    private static int nextId = 0;

    /**
     * Create new collider with the given size
     * @param size Size of the collision box
     */
    public Collider(Vector2 size) {
        this.size = size;
        this.id = nextId++;

        // Link with physics manager
        PhysicsManager.getInstance().registerCollider(this);
    }

    @Override
    public void initialize() {
        // Make sure parent is a Node2D
        // if (!(getParent() instanceof Node2D)) {
        //     throw new RuntimeException("Collider must be child of Node2D");
        // }

        super.initialize();
    }

    /**
     * Get the collision bounds in world space
     */
    public Rectangle getBounds() {
        // Get parent / validize that is Node2d for casting
        Node<?> parent = getParent();
        if (parent == null || !(parent instanceof Node2D)) {
            return new Rectangle(Vector2.ZERO, size);
        }

        Node2D node2D = (Node2D) parent;
        // Calculate position based on parent transform and offset
        Vector2 pos = node2D.transform.pos;
        pos.add(offset);
        pos.sub(size.mul(0.5));

        return new Rectangle(pos, size);
    }

    /**
     * Set the collision layer
     */
    public void setLayer(int layer) {
        this.layer = layer;
    }

    /**
     * Get the collision layer
     */
    public int getLayer() {
        return layer;
    }

    /**
     * Set whether this is a trigger collider (doesn't block movement)
     */
    public void setTrigger(boolean isTrigger) {
        this.isTrigger = isTrigger;
    }

    /**
     * Get whether this is a trigger collider
     */
    public boolean isTrigger() {
        return isTrigger;
    }

    /**
     * Set the offset from the parent's position
     */
    public void setOffset(Vector2 offset) {
        this.offset = offset;
    }

    /**
     * Get the offset from the parent's position
     */
    public Vector2 getOffset() {
        return offset;
    }

    /**
     * Set the size of the collider
     */
    public void setSize(Vector2 size) {
        this.size = size;
    }

    /**
     * Get the size of the collider
     */
    public Vector2 getSize() {
        return size;
    }

    /**
     * Get the unique ID of this collider
     */
    public int getId() {
        return id;
    }

    @Override
    public void update(double delta) {
        // Update children
        super.update(delta); // Updates children
    }
}
