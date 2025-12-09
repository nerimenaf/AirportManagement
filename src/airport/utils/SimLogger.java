package airport.utils;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

public class SimLogger {
    private final TextArea textArea;
    private final DateTimeFormatter fmt =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    public SimLogger(TextArea textArea) {
        this.textArea = textArea;
    }

    public void log(String msg) {
        String line = "[" + LocalTime.now().format(fmt) + "] " + msg + "\n";
        Platform.runLater(() -> textArea.appendText(line));
    }
}