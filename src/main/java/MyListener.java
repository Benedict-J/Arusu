import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.requests.RestAction;
import org.apache.maven.lifecycle.Schedule;

import java.awt.*;
import java.io.*;
import java.nio.Buffer;
import java.time.OffsetTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MINUTES;

public class MyListener extends ListenerAdapter {

    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        super.onGuildMessageReceived(event);
        // Checks if the event is triggered by a bot.
        if (event.getAuthor().isBot()) return;


        // Take the message sent by user and convert it to arrays of integers.
        Message message = event.getMessage();
        String[] content = message.getContentRaw().split(" ");

        // Checks if first string inputted is equivalent to any of the command below.
        switch(content[0]) {
            case "!updates":
                showUpdates(event);
                break;
            case "!greet":
                greetMembers(event);
                break;
            case "!feature":
                showFeature(event);
                break;
            case "!expose":
                createExposeEmbed(event, content);
                break;
            case "!addExpose":
                addToExpose(event, content);
                break;
            case "!subscribe":
                addSubscription(event, content);
                break;
            case "!unsubscribe":
                removeSubscription(event, content[1]);
                break;
            case "!affection":
                showAffection(event);
                break;
            case "!play":
                // Used for playing music but currently on work.
                Guild guild = event.getGuild();
                VoiceChannel myChannel = guild.getVoiceChannelById("780802973609099307");
                AudioManager audioManager = guild.getAudioManager();
                audioManager.openAudioConnection(myChannel);

                audioManager.setSendingHandler(new MySendHandler());

                AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
                AudioSourceManagers.registerRemoteSources(playerManager);

                AudioPlayer player = playerManager.createPlayer();

                TrackScheduler trackScheduler = new TrackScheduler();
                player.addListener(trackScheduler);

                playerManager.loadItem("https://www.youtube.com/watch?v=k342-4fFRwQ&list=LL&index=1",
                        new AudioLoadResultHandler() {
                            @Override
                            public void trackLoaded(AudioTrack track) {
                                trackScheduler.queue(track);
                                player.playTrack(track);
                            }

                            @Override
                            public void playlistLoaded(AudioPlaylist playlist) {
                                for (AudioTrack track : playlist.getTracks()) {
                                    trackScheduler.queue(track);
                                }
                            }

                            @Override
                            public void noMatches() {

                            }

                            @Override
                            public void loadFailed(FriendlyException exception) {

                            }
                        });
                break;
        }

        if(event.getMessage().getMentionedMembers().contains(event.getGuild().getMemberById("326762999882842113"))) {
            event.getChannel().sendMessage("I've told you not to ping master when he's busy. Do you want to get ban?").queue();
        }
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
        String value = "\u2022 greet\n"
                +"\u2022 play (Under Construction)\n"
                +"\u2022 addExpose\n"
                +"\u2022 expose\n"
                +"\u2022 subscribe\n"
                +"\u2022 affection\n";

        String autoMsgList =
                "\u2022 Master's bedtime\n" +
                "\u2022 Remind to drink (subscribe) \n" +
                "\u2022 Welcome Back\n";

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Feature List");
        embed.setColor(Color.cyan);
        embed.addField("Commands for everyone", value, false);
        embed.addField("Automated message", autoMsgList, false);
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
            embed.setTitle(content[1]);
            embed.setColor(Color.cyan);
            embed.addField("Socials", value, false);
            channel.sendMessage(embed.build()).queue();
        } catch(IOException e) {
            System.exit(0);
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

    private void drinkHandler(GuildMessageReceivedEvent event, String memberId) {
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

    private void showUpdates(GuildMessageReceivedEvent event) {
        String value = "\u2022 Added a new subscribe feature\n" +
                       "Removed automatic guild message drink and replaced by subscribe feature\n" +
                       "users can now receive automatic private message to drink";

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Updates");
        embed.setColor(Color.cyan);
        embed.addField("Commands for everyone", value, false);
        event.getChannel().sendMessage(embed.build()).queue();
    }

    private void showAffection(GuildMessageReceivedEvent event) {
        int rng = (int) (Math.random() * 100);

        if(event.getAuthor().getId().equals("326762999882842113")) {
            event.getChannel().sendMessage("You don't have to ask master my affection for you is always 100%").queue();
            return;
        }

        if(rng < 50) {
            event.getChannel().sendMessage("Affection: " + rng + "%. Maybe a worm will love you more?").queue();
        } else {
            event.getChannel().sendMessage("Affection: " + rng + "%. Here's some love for you! :kissing_heart:").queue();
        }
    }
}
