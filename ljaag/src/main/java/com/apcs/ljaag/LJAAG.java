package com.apcs.ljaag;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import com.apcs.disunity.App;
import com.apcs.disunity.Game;
import com.apcs.disunity.camera.Camera;
import com.apcs.disunity.input.Inputs;
import com.apcs.disunity.math.Transform;
import com.apcs.disunity.math.Vector2;
import com.apcs.disunity.nodes.Node2D;
import com.apcs.disunity.nodes.controller.Controller;
import com.apcs.disunity.nodes.sprite.Sprite;
import com.apcs.disunity.physics.Collider;
import com.apcs.disunity.scenes.Scenes;
import com.apcs.ljaag.nodes.BoundedBody;
import com.apcs.ljaag.nodes.action.WalkAction;
import com.apcs.ljaag.nodes.controller.PlayerController;

/*
REQUIRED:


Have a title screen with a Play button


Have two levels, winning the game after the second level is finished


Have three lives, losing the game when lives reaches zero


Pause the game before the ball starts moving, until the user clicks or presses the SPACE key


Have visual effects and sound effects for all interactions


Have game over and game won messages, followed by taking the player back to the main menu

 */

/**
 * Untitled game
 *
 * @author Aayushya Patel
 * @author Qinzhao Li
 * @author Sharvil Phadke
 * @author Toshiki Takeuchi
 */
public class LJAAG {

    /* ================ [ DRIVER ] ================ */
    
    public static void main(String[] args) {

        Vector2 bounds = Vector2.of(480, 270);
        BoundedBody player, ball;
        List<Supplier<BoundedBody.Bounds>> addedColliderBodies;
        Inputs.fromJSON("keybinds.json");
        Scenes.addScene("title", new Node2D(
                new Camera(),
                new Sprite("images/breakout_pieces_1.png")
            ) {
                @Override
                public void draw(Transform offset) {
                    super.draw(offset);
                    Game.getInstance().getBuffer().drawString("placeholder", new Transform());
                }
            }
        );
        
        Scenes.addScene("test", new Node2D(
            new Camera(),
            player = new BoundedBody(new Transform(Vector2.ZERO, bounds, 1),
                new Sprite("images/paddle.png"),
                new Collider(Vector2.ZERO),
                new PlayerController(),
                new WalkAction()
            ) {

                @Override
                public void initialize() {
                    super.initialize();
                    transform = new Transform(Vector2.of(0,100), Vector2.UNIT, 0);
                    Collider c = getChild(Collider.class);
                    Sprite s = getChild(Sprite.class);
                    c.setSize(Vector2.of(s.getWidth(), s. getHeight()));
                    c.setOffset(s.transform.pos);
                };
            },
            ball = new BoundedBody(
                addedColliderBodies = new LinkedList<>(List.of(
                    () -> new BoundedBody.Bounds(new Transform(Vector2.ZERO, bounds, 1),true),
                    () -> new BoundedBody.Bounds(
                        new Transform(
                            player.transform.pos,
                            Vector2.of(player.getChild(Sprite.class).getWidth(), player.getChild(Sprite.class).getHeight()),
                         0),
                     false)
                     )),
                (body, dir) -> {
                    body.setVel(body.getVel().mul(
                        Vector2.of(
                            switch (dir) {
                                case LEFT, RIGHT -> -1;
                                default -> 1;
                            },
                            switch (dir) {
                                case DOWN, UP -> -1;
                                default -> 1;
                            }
                        )
                    ));
                },
                new Sprite("images/ball.png", new Transform(Vector2.of(0, 0), Vector2.UNIT, 0)),
                new Collider(Vector2.ZERO),
                new Controller() {}
            ) {

                @Override
                public void initialize() {
                    super.initialize();
                    transform = new Transform(Vector2.of(100, 0), Vector2.UNIT, 0);
                    Collider c = getChild(Collider.class);
                    Sprite s = getChild(Sprite.class);
                    c.setSize(Vector2.of(s.getWidth(), s.getHeight()));
                    this.setVel(Vector2.of(100,100));
                };
            }
        ));

        Game game = new Game(
            bounds,
            "test"
        );

        Sprite model = new Sprite("images/brick.png",false);
        List<Supplier<BoundedBody.Bounds>> brickSuppliers = new ArrayList<>(1);
        brickSuppliers.add(() -> new BoundedBody.Bounds(new Transform(
            ball.transform.pos,
            Vector2.of(ball.getChild(Sprite.class).getWidth(), ball.getChild(Sprite.class).getHeight()),
         0), false));
        int padding = 1;
        
        for (int x = -bounds.xi; x < bounds.xi; x += model.getWidth() + padding) {
            for (int y = -bounds.yi; y < 0; y += model.getHeight() + padding) {
                final int xf = x, yf = y;
                Sprite brick = new Sprite("images/brick.png", new Transform(Vector2.ZERO, Vector2.UNIT, 0));
                Supplier<BoundedBody.Bounds> supplier;
                supplier = () -> new BoundedBody.Bounds(
                    new Transform(Vector2.of(xf,yf), Vector2.of(brick.getWidth(),brick.getHeight()),0), 
                    false, brick.isVisible()) {
                        @Override
                        public void notify(BoundedBody b, BoundedBody.Direction d) {
                            brick.setVisible(false);
                        }
                    };
                addedColliderBodies.add(supplier);
                Scenes.getScene().addChild(
                    new BoundedBody(
                        brickSuppliers,
                        (bo, d) -> {}, 
                        brick,
                        new Collider(Vector2.ZERO),
                        new Controller() {}
                    ) {

                        @Override
                        public void initialize() {
                            super.initialize();
                            transform = new Transform(Vector2.of(xf,yf), Vector2.UNIT, 0);
                            Collider c = getChild(Collider.class);
                            Sprite s = getChild(Sprite.class);
                            c.setSize(Vector2.of(s.getWidth(), s.getHeight()));
                        };
                    }.autoAlign(false)
                );
            }
        }

        new App(
            "P5 Phadke Sharvil Breakout",
            800, 
            450,
            game);

        game.start();
    }
}
