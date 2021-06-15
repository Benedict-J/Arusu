package Osu;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONException;
import kong.unirest.json.JSONObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.user.UserActivityStartEvent;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class Osu {
    private String apiKey;
    private String listenToUser;

    public Osu() {
        apiKey = System.getenv("OSU_API_KEY");
    }

    public String[] retrieveProfileData(String userName) throws JSONException {
        HttpResponse<String> response = Unirest.get("https://osu.ppy.sh/api/get_user?k=" + apiKey + "&u=" + userName)
                .header("Content-type","application/json")
                .asString();

        JSONArray jsonArray = new JSONArray(response.getBody());

        if(response.getBody().equals("[]")) {
            return null;
        }

        JSONObject userObject = jsonArray.getJSONObject(0);

        String accuracy = String.format("%.2f",userObject.getDouble("accuracy"));

        int timePlayed = userObject.getInt("total_seconds_played") / 3600;

        String bodyMsg = "UserID: " + userObject.getString("user_id") + "\n" +
                         "Rank: **#" + userObject.getString("pp_rank") + "**\n" +
                         "Country Rank: **" + userObject.getString("pp_country_rank") + "**\n" +
                         "PP: **" + userObject.getString("pp_raw") + "**\n" +
                         "Accuracy: **" + accuracy + "%**\n";

        String bodyMsg2 = "Play count: " + userObject.getString("playcount") + '\n' +
                          "Time Played: " + timePlayed + " hours \n";

        String bodyMsg3 = "SS: " + userObject.getString("count_rank_ss") + "\n" +
                          "SS (silver): " + userObject.getString("count_rank_ssh") + "\n" +
                          "S: " + userObject.getString("count_rank_s") + "\n" +
                          "S (silver): " + userObject.getString("count_rank_sh") + "\n" +
                          "A: " + userObject.getString("count_rank_a");

        String[] bodyMsgColumns;

        String footerMsg = "Since: " + userObject.getString("join_date");

        return new String[] {bodyMsg,bodyMsg3,bodyMsg2,footerMsg};
    }

    public void reportCurrentGameData(UserActivityStartEvent event) throws JSONException {
        System.out.println(listenToUser);
        HttpResponse<String> response = Unirest.get("https://osu.ppy.sh/api/get_user_recent?" +
                                                    "k=" + apiKey +
                                                    "&u=" + listenToUser)
                                               .header("Content-type","application/json")
                                               .asString();

        JSONArray playHistoryJArray = new JSONArray(response.getBody());

        JSONObject latestGameHistory = playHistoryJArray.getJSONObject(0);

        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        LocalDateTime latestMatchTime = LocalDateTime.parse(latestGameHistory.getString("date"), dateFormat);

        long currentTime = Calendar.getInstance().getTime().toInstant().atOffset(ZoneOffset.UTC).toEpochSecond();

        // Adds extra layer of checking by checking the game history time comparing to current time.
        if(currentTime - latestMatchTime.toInstant(ZoneOffset.UTC).getEpochSecond() >= 5) {
            return;
        }

        // Stores the beatmap's grade.
        String rank = latestGameHistory.getString("rank");

        // The rank F means a fail or game not completed.
        if(rank.equals("F")) {
            return;
        }

        int enabledMod = latestGameHistory.getInt("enabled_mods");

        String mods = "";

        // Get all mods used by the player.
        while(enabledMod != 0) {
            System.out.println(enabledMod);
            if(enabledMod - Mod.ScoreV2.getNumValue() >= 0) {
                return;
            } else if(enabledMod - Mod.DoubleTime.getNumValue() >= 0) {
                mods += "DoubleTime";
                enabledMod -= Mod.DoubleTime.getNumValue();
            } else if(enabledMod - Mod.HardRock.getNumValue() >= 0){
                mods += "HardRock";
                enabledMod -= Mod.HardRock.getNumValue();
            } else if(enabledMod - Mod.Hidden.getNumValue() >= 0) {
                mods += "Hidden";
                enabledMod -= Mod.Hidden.getNumValue();
            }

            if(enabledMod != 0) {
                mods += (", ");
            }
        }

        if(mods.equals("")) {
            mods = "None";
        }

        String beatmapId = latestGameHistory.getString("beatmap_id");
        String score = latestGameHistory.getString("score");

        HttpResponse<String> beatmapScoreResponse = Unirest.get("https://osu.ppy.sh/api/get_scores?" +
                                                                "k=" + apiKey +
                                                                "&u=" + listenToUser +
                                                                "&b=" + beatmapId)
                                                           .header("Content-type","application/json")
                                                           .asString();

        JSONObject bestScoreData = new JSONArray(beatmapScoreResponse.getBody()).getJSONObject(0);

        String highScore = bestScoreData.getString("score");

        // Do an early break of the code to prevent unnecessary request and only reply to high scores to prevent spam.
        if(!score.equals(highScore)) {
            return;
        }

        int pp = Math.round(bestScoreData.getFloat("pp"));

        String ppMessage = "You placed a high score with **" + pp + "** pp!";

        HttpResponse<String> beatmapResponse = Unirest.get("https://osu.ppy.sh/api/get_beatmaps?" +
                                                           "k=" + apiKey +
                                                           "&b=" + beatmapId)
                                                      .header("Content-type","application/json")
                                                      .asString();

        JSONObject beatmap = new JSONArray(beatmapResponse.getBody()).getJSONObject(0);

        String bStarDifficulty = String.format("%.2f",beatmap.getDouble("difficultyrating"));

        double count50 = latestGameHistory.getInt("count50");
        double count100 = latestGameHistory.getInt("count100");
        double count300 = latestGameHistory.getInt("count300");
        double countMiss = latestGameHistory.getInt("countmiss");

        // Accuracy of a beatmap is calculated using the formula given by osu!.
        double accuracy = (((count300 * 300) + (count100 * 100) + (count50 * 50))
                         / ((count300 + count100 + count50 + countMiss) * 300)) * 100;

        String message = "User: **" + listenToUser + "**\n" +
                         "Stars: **" + bStarDifficulty + "**\n" +
                         "Mods: " + mods + '\n' +
                         "Rank: **" + rank + "**\n" +
                         "Score: " + score + '\n' +
                         "Combo: **" + latestGameHistory.getString("maxcombo") + "**\n" +
                         "Accuracy: **" + String.format("%.2f",accuracy) + "%**\n";

        String scoreMessage = "300s: " + (int) count300 + '\n' +
                              "100s: " + (int) count100 + '\n' +
                              "50s:  " + (int) count50 + '\n'+
                              "Miss: " + (int) countMiss;


        // Creates the embed to be displayed after the game has ended
        EmbedBuilder recentGame = new EmbedBuilder();
        recentGame.setThumbnail(Objects.requireNonNull(Objects.requireNonNull(event.getNewActivity().asRichPresence()).getLargeImage()).getUrl());
        recentGame.setImage("https://assets.ppy.sh/beatmaps/"
                + beatmap.getString("beatmapset_id")
                + "/covers/cover.jpg");
        recentGame.setTitle(beatmap.getString("title") + " [" + beatmap.getString("version") + ']');
        recentGame.addField("",message,true);
        recentGame.addField("",scoreMessage,true);


        event.getGuild().getDefaultChannel().sendMessage(recentGame.build()).queue();

        event.getGuild().getDefaultChannel().sendMessage(ppMessage).queue();
    }

    public String retrieveTopPlaysData(String userName) throws JSONException {
        HttpResponse<String> response = Unirest.get("https://osu.ppy.sh/api/get_user_best?" +
                                                    "k=" + apiKey +
                                                    "&u=" + userName)
                                               .header("Content-type", "application/json")
                                               .asString();

        if(response.getBody().equals("[]")) {
            return "User not found";
        }

        JSONArray jsonArray = new JSONArray(response.getBody());

        String result = "";

        for(int i = 0; i < jsonArray.length(); i++) {
            JSONObject playData = jsonArray.getJSONObject(i);

            String beatmapId = playData.getString("beatmap_id");

            HttpResponse<String> beatmapResponse = Unirest.get("https://osu.ppy.sh/api/get_beatmaps?" +
                                                               "k=" + apiKey +
                                                               "&b=" + beatmapId)
                                                          .header("Content-type", "application/json")
                                                          .asString();

            JSONObject beatmapData = new JSONArray(beatmapResponse.getBody()).getJSONObject(0);

            String songTitle = beatmapData.getString("title") +
                                " [" + beatmapData.getString("version") + "]";

            int pp = (int) Math.round(playData.getDouble("pp"));

            double count300 = playData.getInt("count300");
            double count100 = playData.getInt("count100");
            double count50 = playData.getInt("count50");
            double countMiss = playData.getInt("countmiss");

            double accuracy = ((count300 * 300) + (count100 * 100) + (count50 * 50)) /
                              ((count300 + count100 + count50 + countMiss) * 300) * 100;


            result += (songTitle + "\n**" + String.format("%.2f",accuracy) + "%**\t**" + pp + "** pp\n\n");
        }

        return result;
    }

    public void setListenToUser(String listenToUser) {
        this.listenToUser = listenToUser;
    }
}
