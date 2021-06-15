package Weather;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;

public class WeatherForecast {
    private String apiKey;

    public WeatherForecast() {
        apiKey = System.getenv("OSU_API_KEY");
    }

    public EmbedBuilder getCurrentWeatherEmbed(String city) {
        HttpResponse<String> currentWeatherResponse = Unirest.get("https://api.openweathermap.org/data/2.5/weather?" +
                                                                  "q=" + city + "&units=metric&appid=" + apiKey)
                                                             .asString();

        JSONObject currentWeatherData = new JSONObject(currentWeatherResponse.getBody());

        if(!currentWeatherData.getString("cod").equals("200")) { return null; }

        String weatherDescription = currentWeatherData.getJSONArray("weather")
                                                      .getJSONObject(0)
                                                      .getString("description");

        String iconId = currentWeatherData.getJSONArray("weather")
                                          .getJSONObject(0)
                                          .getString("icon");

        String temp = "";

        for(String word: weatherDescription.split(" ")) {
            temp += word.substring(0,1).toUpperCase() + word.substring(1) + " ";
        }

        weatherDescription = temp;

        String currentTemperature = currentWeatherData.getJSONObject("main").getString("temp") + "\u00B0C";
        String minTemperature = currentWeatherData.getJSONObject("main").getString("temp_min") + "\u00B0C";
        String maxTemperature = currentWeatherData.getJSONObject("main").getString("temp_max") + "\u00B0C";

        String cloudiness = currentWeatherData.getJSONObject("clouds").getString("all") + "%";
        String humidity = currentWeatherData.getJSONObject("main").getString("humidity") + "%";

        String name = currentWeatherData.getString("name");

        String embedMessage = "**Temperature**\n" +
                              "Current | **" + currentTemperature + "**\n" +
                              "Min | **" + minTemperature + "**\n" +
                              "Max | **" + maxTemperature + "**\n" +
                              "\n" +
                              "**Weather**\n" +
                              "Cloudiness | **" + cloudiness + "**\n" +
                              "Humidity | **" + humidity + "**\n" +
                              "\n" +
                              "";

        EmbedBuilder currentWeatherEmbed = new EmbedBuilder();
        currentWeatherEmbed.setTitle("Current Weather: " + name);
        currentWeatherEmbed.setThumbnail("http://openweathermap.org/img/wn/" + iconId + "@2x.png");
        currentWeatherEmbed.setColor(Color.cyan);
        currentWeatherEmbed.addField(weatherDescription,embedMessage,false);

        return currentWeatherEmbed;
    }
}
