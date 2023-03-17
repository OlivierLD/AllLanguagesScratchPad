package scratch;

import utils.WeatherUtil;

public class DewPoint {

    public static void main(String... args) {
        double airTemp = 20.0;
        double relHum = 65.0;

        double dewPointTemp = WeatherUtil.dewPointTemperature(relHum, airTemp);
        double rounded = Math.round(dewPointTemp * 10) / 10.0;  // Round to 1 decimal.
        System.out.printf("Dew Point Temp: %f\272C => Rounded %f\272C\n", dewPointTemp, rounded);
    }

}
