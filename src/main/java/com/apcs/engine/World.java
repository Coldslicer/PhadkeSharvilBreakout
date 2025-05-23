package com.apcs.engine;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javafx.animation.AnimationTimer;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;

public class World extends Pane {

    // move to a seperate constants file
    static final double PERIOD_NS = 2E7;

    private final Set<KeyCode> typed = new HashSet<>();

    private List<Actor> scheduledRemovals = new LinkedList<>();

    AnimationTimer gameloop = new AnimationTimer() {

        private double time;

        @Override
        public void handle(long now) {
            double dt;
            while ((dt = System.nanoTime() - time) > PERIOD_NS) {
                act(dt);
                distribute(dt);
                time += PERIOD_NS;
            }
            getChildren().removeAll(scheduledRemovals.toArray(Actor[]::new));
            scheduledRemovals.clear();
        }

        @Override
        public void start() {
            time = System.nanoTime();
            super.start();
        }
        
    };

    boolean gameloopStopped = true;
    boolean initialized = false;

    public World() {
        this(new Node[0]);
    }

    public World(Node... nodes) {
        super(nodes);
        for (Node n : nodes) {
            if (n instanceof Actor a) {
                a.setWorld(this);
            }
        }

        setOnKeyPressed((e) -> typed.add(e.getCode()));
        setOnKeyReleased((e) -> typed.remove(e.getCode()));
        this.heightProperty().addListener(
            (o, old, neu) -> {
                if (!initialized) {
                    onDimensionsInitialized();
                    initialized = true;
                }
            }
        );

    }

    

    public void act(double dt) { }

    void distribute(double dt) {
        for (Actor a : getAllObjects(Actor.class)) {
            a.act(dt);
        }
    }

    public void add(Actor... actors) {
        for (Actor a : actors) {
            a.setWorld(this);
            a.addedToWorld();
            this.getChildren().add(a);
        }
    }

    public void remove(Actor a) {
        scheduledRemovals.add(a);
    }

    public void removeAll(Actor... a) {
        scheduledRemovals.addAll(Arrays.asList(a));
    }

    public void clear(Class<?> c) {
        getChildren().removeIf((a) -> c.isInstance(a));
    }

    public boolean isKeyPressed(KeyCode code) {
        return typed.contains(code);
    }

    public void start() {
        requestFocus();
        gameloop.start();
        gameloopStopped = false;
    }

    public boolean isStopped() {
        return gameloopStopped;
    }

    public void stop() {
        gameloop.stop();
        gameloopStopped = true;
    }

    @SuppressWarnings("unchecked")
    public <T extends Node> List<T> getAllObjectsAt(double x, double y, Class<T> type) {
        List<T> objects = new LinkedList<>();
        for (Node n : this.getChildren()) {
            if (type.isInstance(n) && n.contains(x,y)) {
                objects.add((T) n);
            }
        }
        return objects;
    }

    @SuppressWarnings("unchecked")
    public <T extends Node> List<T> getAllObjects(Class<T> type) {
        List<T> objects = new LinkedList<>();
        for (Node n : this.getChildren()) {
            if (type.isInstance(n)) {
                objects.add((T) n);
            }
        }
        return objects;
    }

    public void moveAll(double dx, double dy) {
        for (Actor a : getAllObjects(Actor.class)) {
            a.move(dx, dy);
        }
    }

    public void onDimensionsInitialized() {}
    
}
