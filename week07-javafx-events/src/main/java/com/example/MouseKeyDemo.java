package com.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class MouseKeyDemo extends Application {

    private double x = 250;
    private double y = 200;
    private static final double STEP = 15;

    @Override
    public void start(Stage primaryStage) {
        Pane root = new Pane();
        root.setStyle("-fx-background-color: #f4f4f4;");

        Circle player = new Circle(x, y, 20, Color.BLUE);
        Text mouseCoords = new Text(10, 20, "Mouse Coordinates: X=0, Y=0");
        root.getChildren().addAll(player, mouseCoords);

        root.setOnMouseMoved(event -> mouseCoords.setText(
                "Mouse Coordinates: X=" + event.getX() + ", Y=" + event.getY()));

        // JavaFX nodes use focusTraversable, not Swing's setFocusable.
        root.setFocusTraversable(true);
        root.setOnKeyPressed(event -> {
            KeyCode key = event.getCode();
            if (key == KeyCode.UP) y -= STEP;
            if (key == KeyCode.DOWN) y += STEP;
            if (key == KeyCode.LEFT) x -= STEP;
            if (key == KeyCode.RIGHT) x += STEP;

            x = Math.max(20, Math.min(x, 480));
            y = Math.max(20, Math.min(y, 380));
            player.setCenterX(x);
            player.setCenterY(y);
        });

        Scene scene = new Scene(root, 500, 400);
        primaryStage.setOnShown(event -> root.requestFocus());
        primaryStage.setTitle("Mouse and Key Events");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
