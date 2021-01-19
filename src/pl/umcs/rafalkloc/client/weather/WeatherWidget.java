package pl.umcs.rafalkloc.client.weather;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.Map;

public class WeatherWidget extends VBox {

    public WeatherWidget()
    {
        super();
        createGui();
    }

    private void createGui()
    {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(5);
        grid.setPadding(new Insets(15, 15, 15, 15));

        ColumnConstraints c0 = new ColumnConstraints();
        c0.setHalignment(HPos.RIGHT);
        grid.getColumnConstraints().add(c0);

        // controls
        int row = 0;
        Label townLabel = new Label("Town:");
        townLabel.setAlignment(Pos.CENTER_RIGHT);
        grid.add(townLabel, 0, row);
        TextField townEdit = new TextField();
        townEdit.setPromptText("Type town...");
        grid.add(townEdit, 1, row++);

        Label temperatureLabel = new Label("Temperature [℃]:");
        grid.add(temperatureLabel, 0, row);
        Text temperatureText = new Text();
        temperatureText.setFont(Font.font("JetBrains Mono", FontWeight.BOLD, 18));
        grid.add(temperatureText, 1, row++);

        Label feelsLikeTemperatureLabel = new Label("Feels like temperature [℃]:");
        grid.add(feelsLikeTemperatureLabel, 0, row);
        Text feelsLikeTemperatureText = new Text();
        feelsLikeTemperatureText.setFont(Font.font("JetBrains Mono", FontWeight.BOLD, 18));
        grid.add(feelsLikeTemperatureText, 1, row++);

        Label pressureLabel = new Label("Pressure [hPa]:");
        grid.add(pressureLabel, 0, row);
        Text pressureText = new Text();
        pressureText.setFont(Font.font("JetBrains Mono", FontWeight.BOLD, 18));
        grid.add(pressureText, 1, row++);

        Button button = new Button("Show weather");
        button.setOnAction(actionEvent -> {
            Map<String, String> res = OpenWeatherMapAPI.getWeatherFromAPI(townEdit.getText());
            if (res != null) {
                temperatureText.setText(res.get("Temperature"));
                feelsLikeTemperatureText.setText(res.get("FeelTemperature"));
                pressureText.setText(res.get("Pressure"));
            } else {
                temperatureText.setText("");
                feelsLikeTemperatureText.setText("");
                pressureText.setText("");
            }
        });

        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn.getChildren().add(button);
        grid.add(hbBtn, 1, row++);

        getChildren().add(grid);
    }
}
