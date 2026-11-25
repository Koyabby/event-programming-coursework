package com.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class EventPropagationDemo extends Application {

    @Override
    public void start(Stage primaryStage) {
        Label logLabel = new Label("Click the button...");
        Button btn = new Button("Click Me!");

        VBox root = new VBox(20, logLabel, btn);
        root.setStyle("-fx-padding: 20; -fx-alignment: center;");

        // Capturing phase: the filter runs before the event reaches the button.
        root.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            System.out.println("1. [FILTER] Root intercepted the click!");
            // Call event.consume() here to stop the event reaching the button.
        });

        // Bubbling phase: this handler runs after the target button handles the click.
        root.addEventHandler(MouseEvent.MOUSE_CLICKED, event ->
                System.out.println("3. [HANDLER] Root saw the click bubbling up!"));

        btn.setOnMouseClicked(event -> {
            System.out.println("2. [HANDLER] Button was clicked!");
            logLabel.setText("Button was clicked!");
        });

        Scene scene = new Scene(root, 400, 300);
        primaryStage.setTitle("Event Propagation");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
