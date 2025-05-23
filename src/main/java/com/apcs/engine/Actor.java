package com.apcs.engine;

import java.util.LinkedList;
import java.util.List;

import com.apcs.engine.Util.Direction;

import javafx.geometry.Bounds;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Actor extends ImageView {

    private World world;

    public Actor() { }
    public Actor(Image image) { super(image); }
    public Actor(String string) { super(string); }

    public void addedToWorld() { }
    public void act(double dt) { }

    public double getWidth() { return this.getImage().getWidth(); }
    public double getHeight() { return this.getImage().getHeight(); }

    void setWorld(World w) { world = w; }
    public final World getWorld() { return world; }

    public <T extends Actor> T getOneIntersectingObject(Class<T> type) {
        for (T obj : world.getAllObjects(type)) {
            if (obj != this && obj.intersects(this.getBoundsInParent())) {
                return obj;
            }
        }
        return null;
    }

    public <T extends Actor> List<T> getIntersectingObjects(Class<T> type) {
        List<T> objects = new LinkedList<>(world.getAllObjects(type));
        objects.removeIf((elem) -> elem == this || !elem.intersects(this.getBoundsInParent()));
        return objects;
        
    }

    public Direction getPrimaryIntersectionDirection(Actor a) {
        if (a == null) return null;
        Bounds myBounds = getBoundsInParent();
        Bounds theirBounds = a.getBoundsInParent();
        double leftDiff = theirBounds.getMaxX() - myBounds.getMinX();
        double rightDiff = myBounds.getMaxX() - theirBounds.getMinX();
        double lowDiff = theirBounds.getMaxY() - myBounds.getMinY();
        double upDiff = myBounds.getMaxY() - theirBounds.getMinY();
        double min = Math.min(
                Math.min(leftDiff, rightDiff),
                Math.min(lowDiff, upDiff)
        );

        if (min == leftDiff) return Direction.WEST;
        if (min == rightDiff) return Direction.EAST;
        if (min == lowDiff) return Direction.SOUTH;
        if (min == upDiff) return Direction.NORTH;
        return null;

    }

    public void snapOutOf(Actor a) {
        if (a == null) return;
        Bounds myBounds = getBoundsInParent();
        Bounds theirBounds = a.getBoundsInParent();
        double leftDiff = theirBounds.getMaxX() - myBounds.getMinX();
        double rightDiff = myBounds.getMaxX() - theirBounds.getMinX();
        double lowDiff = theirBounds.getMaxY() - myBounds.getMinY();
        double upDiff = myBounds.getMaxY() - theirBounds.getMinY();
        switch (getPrimaryIntersectionDirection(a)) {
            case EAST -> {
                move(rightDiff, 0);
            }
            case NORTH -> {
                move(upDiff, 0);
            }
            case SOUTH -> {
                move(lowDiff, 0);
            }
            case WEST -> {
                move(leftDiff, 0);
            }
            default -> { }
            
        }
    }
    
    public void move(double dx, double dy) {
        this.setX(this.getX() + dx);
        this.setY(this.getY() + dy);
    }
}
