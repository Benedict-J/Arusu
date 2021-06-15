import Music.GuildMusicManager;
import Osu.Osu;
import Weather.WeatherForecast;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;

import java.awt.*;
import java.io.*;
import java.time.OffsetTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.MINUTES;

public class CommandAction {
    private String shopMsgId;

    private final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
    private final Map<Long, GuildMusicManager> musicManagers = new HashMap<>();

    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);

    public CommandAction() {
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
    }


    public void addReactionToPointShop(GuildMessageReceivedEvent event, EmbedBuilder shopEmbed) {
        if(event.getMessage().getEmbeds().contains(shopEmbed.build())) {
            shopMsgId = event.getMessage().getId();
            event.getMessage().addReaction("U+0031U+FE0FU+20E3").queue();
            event.getMessage().addReaction("U+0032U+FE0FU+20E3").queue();

        }
        return;
    }

    public void greetMembers(GuildMessageReceivedEvent event) {
        MessageChannel channel = event.getChannel();
        int hour = OffsetTime.now().getHour();
        String greeting;

        if(hour < 12 && hour > 5) {
            greeting = "Good Morning";
        } else if(hour >= 12 && hour < 17) {
            greeting = "Good Afternoon";
        } else {
            greeting = "Good Evening";
        }

        // Send message to discord according to username.
        if(event.getAuthor().getName().equals("jaegerDager")) {
            channel.sendMessage(greeting + " Master").queue();
            channel.sendFile(new File("res/images/arusu-greet.jpg")).queue();
        } else if(event.getAuthor().getName().equals("Wil")){
            channel.sendMessage("Don't message me you creep").queue();
            channel.sendFile(new File("res/images/arusu-mad.jpg")).queue();
        } else {
            channel.sendMessage(greeting + " " + event.getAuthor().getName()).queue();
        }
    }

    public void showFeature(GuildMessageReceivedEvent event) {
        String value = "\u2022 greet\n" +
                       "\u2022 play (Under Construction)\n" +
                       "\u2022 addExpose\n" +
                       "\u2022 expose\n" +
                       "\u2022 subscribe\n" +
                       "\u2022 unsubscribe\n" +
                       "\u2022 affection\n" +
                       "\u2022 weather (**new**)\n" +
                       "\u2022 osuprofile (**new**)\n" +
                       "\u2022 osubest (**new**)\n" +
                       "\u2022 leaderboard (**new**)\n" +
                       "\u2022 roll (**new**)\n" +
                       "\u2022 listenToMyOsu (**new**)\n" +
                       "\u2022 points (**new**)\n" +
                       "\u2022 shop (**new**)\n";

        String autoMsgList = "\u2022 Master's bedtime\n" +
                             "\u2022 Remind to drink (subscribe) \n" +
                             "\u2022 Welcome Back\n";

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Feature List");
        embed.setColor(Color.cyan);
        embed.addField("Commands for everyone", value,true);
        embed.addField("Automated message", autoMsgList,true);
        File file = new File("res/images/arusu-spirit.jpg");
        embed.setThumbnail("attachment://arusu-spirit.jpg");
        embed.setFooter("Master is working hard on my features");
        event.getChannel().sendFile(file).embed(embed.build()).queue();
    }

    public void createExposeEmbed(GuildMessageReceivedEvent event, String[] content) {
        MessageChannel channel = event.getChannel();

        String value = "";

        if(content.length < 2) {
            return;
        }

        try(BufferedReader reader = new BufferedReader(new FileReader("res/FileData/friends.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String name = line.split(",")[0];
                String result = line.split(",")[1];
                if (name.equals(content[1])) {
                    value += (result + '\n');
                }
            }
            if(value.equals("")) {
                channel.sendMessage("I don't have information regarding "
                        + content[1]).queue();
                return;
            }

            // Creates the embed.
            EmbedBuilder embed = new EmbedBuilder();
            embed.setDescription("shop");
            embed.setTitle(content[1]);
            embed.setColor(Color.cyan);
            embed.addField("Socials", value, false);
            channel.sendMessage(embed.build()).queue();
        } catch(IOException e) {
            System.exit(0);
        }
    }

    public void rollRNG(GuildMessageReceivedEvent event) {
        String[] content = event.getMessage().getContentRaw().split(" ");

        if(content.length > 1) {
            for(char c: content[1].toCharArray()) {
                if(!Character.isDigit(c)) {
                    event.getChannel().sendMessage("You must input a number").queue();
                    return;
                }
            }

            int maxNumber = Integer.parseInt(content[1]);

            int tensMultiple = 10;

            while(tensMultiple < maxNumber) {
                tensMultiple *= 10;
            }

            event.getChannel().sendMessage("" + (int) ((Math.random() * tensMultiple) % maxNumber)).queue();
        } else {
            event.getChannel().sendMessage((int) (Math.random() * 100) + "").queue();
        }
    }

    public void checkPoints(GuildMessageReceivedEvent event) {
        try(BufferedReader reader = new BufferedReader(new FileReader("res/FileData/PlayerPoints.csv"))) {
            String line;
            while((line = reader.readLine()) != null) {
                String[] field = line.split(",");
                if(field[0].equals(event.getMember().getId())) {
                    event.getChannel().sendMessage(event.getMember().getAsMention() + " You have "
                            + field[2] + " points").queue();
                    return;
                }
            }
        } catch(IOException e) {
            System.out.println("");
        }

        try(BufferedWriter writer = new BufferedWriter(new FileWriter("res/FileData/PlayerPoints.csv",true))) {
            writer.write(event.getMember().getId() + "," + event.getMember().getUser().getName()
                    + "," + 0 + "\n");
            event.getChannel().sendMessage(event.getMember().getAsMention() + " You have 0 points").queue();
        } catch(IOException e) {

        }
    }

    public void addPoints(GuildMessageReceivedEvent event) {
        int point = 0;
        String text = "";

        try(BufferedReader reader = new BufferedReader(new FileReader("res/FileData/PlayerPoints.csv"))) {
            String line;
            while((line = reader.readLine()) != null) {
                String[] field = line.split(",");
                if(field[0].equals(event.getMember().getId())) {
                    System.out.println(event.getChannel() + ", " + event.getMember().getId() + ", " + event.getMessage().getContentRaw());

                    point = Integer.parseInt(field[2]) + 4;
                } else {
                    text += (line + '\n');
                }
            }
        } catch (IOException e) {

        }

        if(point == 0) {
            return;
        }

        try(BufferedWriter writer = new BufferedWriter(new FileWriter("res/FileData/PlayerPoints.csv"))) {
            writer.write(text + (event.getMember().getId() + "," + event.getMember().getUser().getName()
                    + "," + point + '\n'));
        } catch(IOException e) {

        }
    }

    public void addToExpose(GuildMessageReceivedEvent event, String[] content) {
        MessageChannel channel = event.getChannel();

        if(content.length < 3) {
            channel.sendMessage("Please follow the following format\n"
                    + "!addExpose <name> <details>").queue();
            return;
        }

        String text = "";
        for(int i = 2; i < content.length; i++) {
            text += (content[i] + " ");
        }

        String[] banNames = new String[]{"ben", "jaeger", "jeff", "dragonoir"};

        for(String name: banNames) {
            if(content[1].toLowerCase().contains(name)) {
                channel.sendMessage("You cannot expose master!").queue();
                return;
            }
        }

        // Checks if the data exist in the csv file.
        try(BufferedReader reader = new BufferedReader(new FileReader("res/FileData/friends.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String name = line.split(",")[0];
                String result = line.split(",")[1];

                if(text.equals(result)) {
                    channel.sendMessage("I got that stored already").queue();
                }
            }
        } catch(IOException e) {

        }

        text = "";

        // Appends data to the file.
        try(BufferedWriter writer = new BufferedWriter(new FileWriter("res/FileData/friends.csv", true))) {
            text = content[1] + ",";
            if(content.length > 2) {
                for(int i = 2; i < content.length; i++) {
                    text += (content[i] + " ");
                }
            }
            text += '\n';
            writer.write(text);
        } catch(IOException e) {
            System.out.println("File fail to load");
        }
    }

    public void addSubscription(GuildMessageReceivedEvent event, String[] content) {
        MessageChannel channel = event.getChannel();

        if(content.length < 2) {
            channel.sendMessage("Please specify the subscription service you want").queue();
            return;
        }

        try(BufferedReader reader = new BufferedReader(new FileReader("res/FileData/Subscribers.csv"))) {
            String line;
            while((line = reader.readLine()) != null) {
                String[] tuple = line.split(",");
                if(tuple[0].equals(event.getMember().getId()) && tuple[1].equals(content[1])) {
                    event.getChannel().sendMessage("You are already subscribed to this subscription").queue();
                    return;
                }
            }
        } catch (IOException e) {

        }

        // Records user's subscription by their Id and subscription type
        try(BufferedWriter writer = new BufferedWriter(new FileWriter("res/FileData/Subscribers.csv", true))) {
            String memberId = event.getMember().getId();
            String tuple = memberId + "," + content[1] + '\n';
            writer.write(tuple);
            drinkHandler(event, memberId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeSubscription(GuildMessageReceivedEvent event, String subscription) {
        String text = "";

        try(BufferedReader reader = new BufferedReader(new FileReader("res/FileData/Subscribers.csv"))) {
            String line;
            while((line = reader.readLine()) != null) {
                String[] tuples = line.split(",");
                if(tuples[0].equals(event.getMember().getId())
                        && tuples[1].equals(subscription)) {
                    continue;
                }
                text += tuples + "\n";
            }
        } catch (IOException e) {

        }

        try(BufferedWriter writer = new BufferedWriter(new FileWriter("res/FileData/Subscribers.csv"))) {
            writer.write(text);
        } catch (IOException e) {

        }
    }

    public void drinkHandler(GuildMessageReceivedEvent event, String memberId) {
        User user = event.getJDA().getUserById(memberId);
        String message = "Don't forget to hydrate yourselves";

        final Runnable reminder = new Runnable() {
            public void run() {
                user.openPrivateChannel()
                        .flatMap(channel -> channel.sendMessage(message))
                        .queue();
            }
        };

        final ScheduledFuture<?> drink =
                scheduler.scheduleWithFixedDelay(reminder,
                        0, 15, MINUTES);
    }

    public void showUpdates(GuildMessageReceivedEvent event) {
        String value = "\u2022 Added a new subscribe feature\n" +
                "Removed automatic guild message drink and replaced by subscribe feature\n" +
                "users can now receive automatic private message to drink";

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Updates");
        embed.setColor(Color.cyan);
        embed.addField("Commands for everyone", value, false);
        event.getChannel().sendMessage(embed.build()).queue();
    }

    public void showAffection(GuildMessageReceivedEvent event) {
//        int rng = (int) (Math.random() * 100);
//
//        if(event.getAuthor().getId().equals(Arusu.botOwnerId)) {
//            event.getChannel().sendMessage("You don't have to ask master my affection for you is always 100%").queue();
//            return;
//        } else if(event.getAuthor().getId().equals(Arusu.guildOwnerId)) {
//            event.getChannel().sendMessage("Master's boss obviously gets 1000% of the love.").queue();
//        }
//
//        if(rng < 50) {
//            event.getChannel()
//                    .sendMessage("Affection: " + rng + "%. Maybe a worm will love you more?")
//                    .queue();
//        } else {
//            event.getChannel()
//                    .sendMessage("Affection: " + rng + "%. Here's some love for you! :kissing_heart:")
//                    .queue();
//        }
    }

    public void replyForMentioned(GuildMessageReceivedEvent event) {
//        // Sends message when bot owner gets mentioned by a member's message
//        List<Member> mentionedMembers = event.getMessage().getMentionedMembers();
//        Member botOwner = event.getGuild().getMemberById(Arusu.botOwnerId);
//        Member bot = event.getGuild().getMemberById("780800869875974185");
//
//        if(mentionedMembers.contains(botOwner)) {
//            if (event.getMember().equals(botOwner)) {
//                event.getChannel()
//                        .sendMessage("What are you trying to do master? :KannaWhat:")
//                        .queue();
//            } else if (!botOwner.getOnlineStatus().equals(OnlineStatus.ONLINE)
//                    || !botOwner.getActivities().equals(Activity.ActivityType.DEFAULT)) {
//                event.getChannel()
//                        .sendMessage("I've told you not to ping master when he's busy. Do you want to get ban?")
//                        .queue();
//            }
//        }
    }

    public void giveOsuProfileData(GuildMessageReceivedEvent event, Osu osu) {
        String[] content = event.getMessage().getContentRaw().split(" ");

        if(content.length < 2) {
            return;
        }

        String[] result = osu.retrieveProfileData(content[1]);

        if(result == null) {
            event.getChannel().sendMessage("User not found").queue();
            return;
        }

        String userID = result[0].split("\n")[0].split(" ")[1];

        EmbedBuilder osuProfile = new EmbedBuilder();

        osuProfile.setTitle("Osu.Osu Profile");
        osuProfile.setThumbnail("http://s.ppy.sh/a/" + userID);
        osuProfile.addField(content[1], result[0],true);
        osuProfile.addField("", result[1], true);
        osuProfile.addField("", result[2], true);
        osuProfile.setFooter(result[3]);

        event.getChannel().sendMessage(osuProfile.build()).queue();
    }

    public void giveOsuUserBestData(GuildMessageReceivedEvent event, Osu osu) {
        String[] content = event.getMessage().getContentRaw().split(" ");

        if (content.length < 2) {
            return;
        }

        String result = osu.retrieveTopPlaysData(content[1]);

        if (result.equals("User not found")) {
            event.getChannel().sendMessage("User not found").queue();
            return;
        }

        EmbedBuilder osuUserBest = new EmbedBuilder();
        osuUserBest.addField("",result,false);

        event.getChannel().sendMessage(osuUserBest.build()).queue();
    }

    public boolean checkOsuUserGameOn(GuildMessageReceivedEvent event, Osu osu) {
        if(event.getMember().getActivities().isEmpty()) {
            event
                    .getChannel()
                    .sendMessage("You have to be on osu first and it must show on your game activity to work")
                    .queue();
            return false;
        }

        Activity activity = event.getMember().getActivities().get(0);

        // Check if the member's activity is osu
        if(!activity.getName().contains("osu")) {
            event
                    .getChannel()
                    .sendMessage("You have to be on osu first and it must show on your game activity to work")
                    .queue();
            return false;
        } else {
            String username = "";

            try {
                username = activity.asRichPresence().getLargeImage().getText().split(" ")[0];
            } catch (NullPointerException e) {
                event.getChannel().sendMessage("Your game activity in discord is lacking info. Try to activate the rich presence in osu! if it is off").queue();
                return false;
            }

            osu.setListenToUser(username);
            event.getChannel().sendMessage("OK! I am watching your osu game").queue();

            // Change bot's status to let members know which osu is the bot listening to.
            event
                    .getJDA()
                    .getPresence()
                    .setActivity(Activity.listening(event.getMember().getEffectiveName() + "'s osu!"));
            return true;
        }
    }

    public void leaderboard(GuildMessageReceivedEvent event) {
        ArrayList<String[]> users = new ArrayList<String[]>();
        try(BufferedReader reader = new BufferedReader(new FileReader("res/FileData/PlayerPoints.csv"))) {
            String line;
            while((line = reader.readLine()) != null) {
                users.add(line.split(","));
            }
        } catch (IOException e) {

        }

        String[] temp;

        for(int i = 0; i < users.size(); i++) {
            for(int j = i+1; j < users.size(); j++) {
                int n1 = Integer.parseInt(users.get(i)[2]);
                int n2 = Integer.parseInt(users.get(j)[2]);

                if(n1 < n2) {
                    temp = users.get(i);
                    users.set(i, users.get(j));
                    users.set(j, temp);
                }
            }
        }

        int userCount = 0;

        String field = "";

        String[] rankEmote = {":first_place: ",":second_place: ",":third_place: "};

        while(userCount < 5) {
            if(userCount < 3) {
                field += (rankEmote[userCount]);
            }

            field += (users.get(userCount)[1] + " - " + users.get(userCount)[2] + "\n");
            userCount++;
        }

        EmbedBuilder leaderboard = new EmbedBuilder();
        leaderboard.setTitle("Point Leaderboard");
        leaderboard.addField("Top 5 Users",field,false);
        event.getChannel().sendMessage(leaderboard.build()).queue();
    }

    public void getCurrentWeather(GuildMessageReceivedEvent event) {
        String[] content = event.getMessage().getContentRaw().split(" ");

        if(content.length < 2) {
            EmbedBuilder invalid = new EmbedBuilder();
            invalid.setDescription("Please specify the city `!weather <city>`");
            invalid.setColor(Color.cyan);

            event.getChannel().sendMessage(invalid.build()).queue();
            return;
        }

        String weatherParameter = content[1];

        EmbedBuilder currentWeatherEmbed;

        if((currentWeatherEmbed = new WeatherForecast().getCurrentWeatherEmbed(weatherParameter)) == null) {
            EmbedBuilder invalid = new EmbedBuilder();
            invalid.setDescription("City not found");
            invalid.setColor(Color.cyan);

            event.getChannel().sendMessage(invalid.build()).queue();
        } else {
            event.getChannel().sendMessage(currentWeatherEmbed.build()).queue();
        }
    }

    private synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
        long guildId = Long.parseLong(guild.getId());
        GuildMusicManager musicManager = musicManagers.get(guildId);

        if (musicManager == null) {
            musicManager = new GuildMusicManager(playerManager);
            musicManagers.put(guildId, musicManager);
        }

        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;
    }

    public void playMusic(GuildMessageReceivedEvent event) {
        String[] content = event.getMessage().getContentRaw().split(" ");

        if(content.length < 2) {
            event.getChannel().sendMessage("Invalid").queue();
            return;
        }

        String videoName = event.getMessage().getContentRaw().replaceFirst("!play","");

        event.getMessage().delete().queue();

        String trackUrl;
        TextChannel channel = event.getChannel();

        GuildMusicManager musicManager = getGuildAudioPlayer(event.getChannel().getGuild());

        System.out.println(videoName);

        HttpResponse<String> response = Unirest.get("https://youtube.googleapis.com/youtube/v3/search?part=snippet&maxResults=1" +
                                                    "&q=" + videoName +
                                                    "&key=AIzaSyBJBYVVg0s3coI-xCOFBDZXA3hTCKGbOkw")
                                                .header("Content-type","application/json")
                                                .asString();

        JSONObject videoData = new JSONObject(response.getBody());

        System.out.println(videoData);

        String id = videoData.getJSONArray("items").getJSONObject(0).getJSONObject("id").getString("videoId");

        trackUrl = "https://www.youtube.com/watch?v=" + id;

        System.out.println(id);


        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
//                channel.sendMessage("Adding to queue " + track.getInfo().title).queue();

                play(channel.getGuild(), musicManager, track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AudioTrack firstTrack = playlist.getSelectedTrack();

                if (firstTrack == null) {
                    firstTrack = playlist.getTracks().get(0);
                }

//                channel.sendMessage("Adding to queue " + firstTrack.getInfo().title + " (first track of playlist " + playlist.getName() + ")").queue();

                play(channel.getGuild(), musicManager, firstTrack);
            }

            @Override
            public void noMatches() {
                channel.sendMessage("Nothing found by " + trackUrl).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                channel.sendMessage("Could not play: " + exception.getMessage()).queue();
            }
        });
    }

    private void play(Guild guild, GuildMusicManager musicManager, AudioTrack track) {
        connectToFirstVoiceChannel(guild.getAudioManager());

        musicManager.scheduler.queue(track);
    }

    public void skipTrack(TextChannel channel) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        musicManager.scheduler.nextTrack();

        channel.sendMessage("Skipped to next track.").queue();
    }

    private static void connectToFirstVoiceChannel(AudioManager audioManager) {
        if (!audioManager.isConnected() && !audioManager.isAttemptingToConnect()) {
            for (VoiceChannel voiceChannel : audioManager.getGuild().getVoiceChannels()) {
                audioManager.openAudioConnection(voiceChannel);
                break;
            }
        }
    }
}
