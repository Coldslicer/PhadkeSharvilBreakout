package com.apcs;

import java.io.IOException;
import java.util.Scanner;
import java.util.function.Supplier;

import com.apcs.engine.Actor;
import com.apcs.engine.Sound;
import com.apcs.engine.Util;
import com.apcs.engine.World;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * JavaFX App
 */
public class App extends Application {

    public static final double MULTI = 1;


    private static final int NUM_WORLDS = 1;
    private int index = 1;
    private Supplier<String> fileNames = () -> String.format("worlds/world_%d.txt", index++);
    int lives = 3;

    private Stage s;
    private Scene start;
    private Scene lose;
    private Text t;
    private Actor paddle;
    private Actor background;
    private Actor ball;

    @Override
    public void start(Stage stage) throws IOException {
        Sound.init();
        s = stage;
        World w = new World();
        loadLevel(w, fileNames.get());
        Button b;
        start = new Scene(
            new VBox(
                new ImageView("images/breakout_pieces_1.png"),
                new Text("BREAKOUT"),
                b = new Button("START")
            )
        );
        Scene s = new Scene(
            new BorderPane(new StackPane(new Group(w)), t = new Text("Lives: 3"), null, null, null)
        );
        Button b2;
        stage.setScene(
            lose = new Scene(
                new VBox(
                    new Text("YOU LOSE!"),
                    b2 = new Button("Return to Start")
                )
            )
        );
        b2.setOnAction((e) -> stage.setScene(start));
        b.setOnAction((e) -> {
            stage.setScene(s);
            w.clear(Actor.class);
            index = 1;
            loadLevel(w, fileNames.get());
        });
        stage.setScene(start);
        stage.show();
        stage.setOnCloseRequest((e) -> w.stop());
        w.setOnMousePressed((e) -> w.start());
    }

    public static void main(String[] args) {
        launch();
    }

    public static Actor constructPaddle(World w) {
        return new Actor("images/paddle.png") {

            public static final double SPEED = 2 * MULTI;

            @Override
            public void act(double dt) {
                if (w.isKeyPressed(KeyCode.A) || w.isKeyPressed(KeyCode.LEFT)) {
                    move(-SPEED, 0);
                    w.moveAll(+SPEED, 0);
                }
                if (w.isKeyPressed(KeyCode.D) || w.isKeyPressed(KeyCode.RIGHT)) {
                    move(+SPEED, 0);
                    w.moveAll(-SPEED, 0);
                }

                if (getBoundsInParent().getMinX() <= 0) {
                    setX(0);
                }
                if (getBoundsInParent().getMaxX() >= getWorld().getWidth()) {
                    setX(getWorld().getWidth() - getWidth());
                }

            }
        };
    }

    public Actor constructBrick(double x, double y) {
        return new Actor("images/brick.png") {

            @Override
            public void addedToWorld() {
                super.addedToWorld();
                move(x, y);
            }

            @Override
            public void act(double dt) {
                super.act(dt);

                if (getIntersectingObjects(Actor.class).contains(ball)) {
                    World w = getWorld();
                    if (w.getAllObjects(Actor.class).size() == 3) {
                        w.clear(Actor.class);
                        if (index >= NUM_WORLDS) {
                            new Sound("breakout_sounds/game_won.wav").play();
                            Button b;
                            s.setScene(
                                new Scene(
                                    new VBox(
                                        new Text("YOU WIN!"),
                                        b = new Button("Return to Start")
                                    )
                                )
                            );
                            b.setOnAction((e) -> s.setScene(start));
                        } else {
                            loadLevel(getWorld(), fileNames.get());
                        }
                    }
                    else {
                        getWorld().remove(this);
                    }
                }

            }
        };
    }

    public Actor constructBall(World w, Actor p) {
        return new Actor("images/ball.png") {
            double dx = MULTI;
            double dy = MULTI;

            @Override
            public void act(double dt) {
                super.act(dt);
                move(dx,dy);

                for (Actor a : getIntersectingObjects(Actor.class)) {
                    if (a == background) continue;
                    snapOutOf(a);
                    Util.Direction d = this.getPrimaryIntersectionDirection(a);
                    switch (d) {
                        case NORTH, SOUTH -> dy *= -1;
                        case EAST, WEST -> dx *= -1;
                    }
                    if (a != paddle) {
                        new Sound("breakout_sounds/brick_hit.wav").play();
                    } else {
                        new Sound("breakout_sounds/ball_bounce.wav").play();
                    }
                }
                boolean bounced = false;
                if (getBoundsInParent().getMinX() <= 0) {
                    setX(0);
                    dx *= -1;
                    bounced = true;
                }
                if (getBoundsInParent().getMaxX() >= getWorld().getWidth()) {
                    setX(getWorld().getWidth() - getWidth());
                    dx *= -1;
                    bounced = true;
                }
                if (getBoundsInParent().getMinY() <= 0) {
                    setY(0);
                    dy *= -1;
                    bounced = true;
                }
                if (getBoundsInParent().getMaxY() >= getWorld().getHeight()) {
                    setY(getWorld().getHeight() - getHeight());
                    dy *= -1;
                    bounced = true;

                    setY(p.getY() - p.getHeight());
                    setX(p.getX() + p.getWidth() / 2);
                    w.stop();
                    lives--;
                    t.setText("Lives: "+lives);
                    if (lives == 0) {
                        new Sound("breakout_sounds/game_lost.wav").play();
                        s.setScene(lose);
                        lives = 0;
                    } else {
                        new Sound("breakout_sounds/loose_life.wav").play();
                    }
                }

                if (bounced) {
                    new Sound("breakout_sounds/ball_bounce.wav").play();
                }

            }
        };
    }

    public World loadLevel(World w, String path) {
        w.add(background = new Actor("images/background.png"));
        Actor model = constructBrick(0, 0);
        int maxX = 0, maxY = 0;
        try (Scanner s = new Scanner(App.class.getClassLoader().getResourceAsStream(path))) {
            int x = 0, y = 0;
            while (s.hasNextLine()) {
                for (char c : s.nextLine().toCharArray()) {
                    switch (c) {
                        case 'X' -> {
                            w.add(constructBrick(x * (model.getWidth() + 1), y * (model.getHeight() + 1)));
                        }
                        default -> {}
                    }
                    maxY = Math.max(maxY, y);
                    maxX = Math.max(maxX, x);
                    x++;
                }
                y++;
                x = 0;
            }
        }

        paddle = constructPaddle(w);
        ball = constructBall(w, paddle);

        double paddleX = maxX * (model.getWidth() + 1) / 4 - paddle.getWidth() / 2;
        double paddleY = (maxY + 3) * model.getHeight();

        paddle.setX(paddleX);
        paddle.setY(paddleY);

        ball.setX(paddleX + paddle.getWidth() / 2);
        ball.setY(paddleY - paddle.getHeight());
        
        
        w.add(paddle, ball);
        return w;
    }

}