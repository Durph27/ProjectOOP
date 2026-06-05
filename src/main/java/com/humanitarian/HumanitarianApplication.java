package com.humanitarian;

import com.humanitarian.ui.MainFrame;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX application lifecycle.
 */
public class HumanitarianApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        MainFrame mainFrame = new MainFrame();

        Scene scene = new Scene(mainFrame, 1200, 800);

        // Null-safe CSS loading
        var cssResource = getClass().getResource("/styles/main.css");
        if (cssResource != null) {
            scene.getStylesheets().add(cssResource.toExternalForm());
        }

        primaryStage.setTitle("🌏 Phân Tích Mạng Xã Hội - Logistics Nhân Đạo");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }
}
