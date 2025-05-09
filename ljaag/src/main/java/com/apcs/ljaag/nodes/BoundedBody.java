package com.apcs.ljaag.nodes;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import com.apcs.disunity.annotations.Requires;
import com.apcs.disunity.math.Transform;
import com.apcs.disunity.math.Vector2;
import com.apcs.disunity.nodes.Node;
import com.apcs.disunity.nodes.body.Body;
import com.apcs.disunity.physics.Collider;

@Requires(nodes = {Collider.class})
public class BoundedBody extends Body {

    public enum Direction {
        LEFT,
        RIGHT,
        UP,
        DOWN
    }

    private List<Supplier<Bounds>> bounds = new LinkedList<>();
    private BiConsumer<BoundedBody, Direction> consumer;

    public BoundedBody(Transform bounds, BiConsumer<BoundedBody, Direction> collisionAction) {
        this.bounds.add(() -> new Bounds(bounds));
        consumer = collisionAction;
    }

    public BoundedBody(Transform bounds) {
        this(bounds, (body,dir) -> {});
    }

    public BoundedBody(Supplier<Transform> bounds, BiConsumer<BoundedBody, Direction> collisionAction) {
        this.bounds.add(() -> new Bounds(bounds.get()));
        consumer = collisionAction;
    }

    public BoundedBody(Supplier<Transform> bounds) {
        this(bounds, (body,dir) -> {});
    }
    

    public BoundedBody(List<Supplier<Bounds>> bounds, BiConsumer<BoundedBody, Direction> collisionAction) {
        this.bounds = bounds;
        consumer = collisionAction;
    }

    public BoundedBody(List<Supplier<Bounds>> bounds) {
        this(bounds, (body,dir) -> {});
    }

    public BoundedBody(List<Supplier<Bounds>> bounds, BiConsumer<BoundedBody, Direction> collisionAction, Node... children) {
        super(children);
        this.bounds = bounds;
        consumer = collisionAction;
    }

    public BoundedBody(List<Supplier<Bounds>> bounds, Node<?>... children) {
        this(bounds, (body,dir) -> {}, children);
    }

    public BoundedBody(Transform bounds, BiConsumer<BoundedBody, Direction> collisionAction, Node... children) {
        super(children);
        this.bounds.add(() -> new Bounds(bounds));
        consumer = collisionAction;
    }

    public BoundedBody(Transform bounds, Node... children) {
        this(bounds, (body,dir) -> {}, children);
    }


    public void addBounds(Supplier<Transform> t, boolean interior) {
        bounds.add(() -> new Bounds(t.get(), interior));
    }



    @Override
    public void update(double dt) {
        super.update(dt);
        for (Supplier<Bounds> boundsSupplier : bounds) {
            Bounds bounds = boundsSupplier.get();
            if (!bounds.isActive) continue;
            Collider c = getChild(Collider.class);

            double myLeftWallX = transform.pos.x - c.getBounds().getSize().x / 2;
            double myRightWallX = transform.pos.x + c.getBounds().getSize().x / 2;
            double myLowerWallY = transform.pos.y - c.getBounds().getSize().y / 2;
            double myUpperWallY = transform.pos.y + c.getBounds().getSize().y / 2;

            double theirLeftWallX = bounds.t.pos.x - bounds.t.scale.x / 2;
            double theirRightWallX = bounds.t.pos.x + bounds.t.scale.x / 2;
            double theirLowerWallY = bounds.t.pos.y - bounds.t.scale.y / 2;
            double theirUpperWallY = bounds.t.pos.y + bounds.t.scale.y / 2;

            if (bounds.interior) {
                
                if (myLeftWallX < theirLeftWallX) {
                    transform = transform.move(Vector2.of(theirLeftWallX - myLeftWallX, 0));
                    consumer.accept(this, Direction.LEFT);
                }

                if (myRightWallX > theirRightWallX) {
                    transform = transform.move(Vector2.of(theirRightWallX - myRightWallX, 0));
                    consumer.accept(this, Direction.RIGHT);
                }

                if (myLowerWallY < theirLowerWallY) {
                    transform = transform.move(Vector2.of(0, theirLowerWallY - myLowerWallY));
                    consumer.accept(this, Direction.DOWN);
                }

                if (myUpperWallY > theirUpperWallY) {
                    transform = transform.move(Vector2.of(0, theirUpperWallY - myUpperWallY));
                    consumer.accept(this, Direction.UP);
                }
            } else {

                if (myRightWallX > theirLeftWallX && myLeftWallX < theirLeftWallX && (myLowerWallY < theirUpperWallY && myUpperWallY > theirLowerWallY)) {
                    transform = transform.move(Vector2.of(theirLeftWallX - myRightWallX, 0));
                    consumer.accept(this, Direction.LEFT);
                }

                if (myLeftWallX < theirRightWallX && myRightWallX > theirRightWallX && (myLowerWallY < theirUpperWallY && myUpperWallY > theirLowerWallY)) {
                    transform = transform.move(Vector2.of(theirRightWallX - myLeftWallX, 0));
                    consumer.accept(this, Direction.RIGHT);
                }

                if (myLowerWallY < theirUpperWallY && myUpperWallY > theirUpperWallY && (myRightWallX > theirLeftWallX && myLeftWallX < theirRightWallX)) {
                    transform = transform.move(Vector2.of(0, theirUpperWallY - myLowerWallY));
                    consumer.accept(this, Direction.DOWN);
                }

                if (myUpperWallY > theirLowerWallY && myLowerWallY < theirLowerWallY && (myRightWallX > theirLeftWallX && myLeftWallX < theirRightWallX)) {
                    transform = transform.move(Vector2.of(0, theirLowerWallY - myUpperWallY));
                    consumer.accept(this, Direction.UP);
                }
                
            }
        }
    }

    public static class Bounds {
        private final Transform t;
        private final boolean interior;
        private boolean isActive;

        public Bounds(Transform t, boolean interior, boolean isActive) {
            this.t = t;
            this.interior = interior;
            this.isActive = isActive;
        }

        public Bounds(Transform t, boolean interior) {
            this(t, interior, true);
        }

        public Bounds(Transform t) {
            this(t, true, true);
        }

        public Transform getTransform() {
            return t;
        }

        public boolean isInterior() {
            return interior;
        }

        public boolean isIsActive() {
            return isActive;
        }

        public void setActive(boolean isActive) {
            this.isActive = isActive;
        }
    }
}
