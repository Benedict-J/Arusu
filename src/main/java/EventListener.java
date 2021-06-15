import Osu.Osu;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.user.UserActivityEndEvent;
import net.dv8tion.jda.api.events.user.UserActivityStartEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class EventListener extends ListenerAdapter {
    private boolean osuGameStarted = false;
    private String userIDOsuActivity;
//    private final EmbedBuilder shopEmbed;
//    private String shopMsgId;
    private final CommandAction commandAction = new CommandAction();

    private final Osu osu = new Osu();

    public EventListener() {
//        shopEmbed = new EmbedBuilder();
//        shopEmbed.setTitle("Point Shop");
//        shopEmbed.addField("Welcome to the shop",
//                "1. Benomatic plays a csgo game - 1000 points\n" +
//                        "2. Claim Mizunoまい as slave - 5000 points\n" +
//                        "3. Automated messages of your osu game (one use) (**NOT** **READY**) - 300 points\n" +
//                        "4. Reset your discord server profile (**NOT** **READY**) - 5000 points\n" +
//                        "5. Take command of Arusu for a day - 1000 points (**NOT** **READY**)\n",
//                false);
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        super.onGuildMessageReceived(event);

        // Checks if the event is triggered by a bot.
        if (event.getAuthor().isBot()) {

//            // Add reaction to shop embed for users to use points
//            if (event.getMessage().getEmbeds().contains(shopEmbed.build())) {
//                shopMsgId = event.getMessage().getId();
//                event.getMessage().addReaction("U+0031 U+FE0F U+20E3").queue();
//                event.getMessage().addReaction("U+0032 U+FE0F U+20E3").queue();
//            }
            return;
        }

        // Take the message sent by user and convert it to arrays of integers.
        Message message = event.getMessage();
        String[] content = message.getContentRaw().split(" ");

        // It is expected that the command will be entered as the first string in the message.
        switch (content[0]) {
            case "!help":
                break;
            case "!listenToMyOsu":
                if (commandAction.checkOsuUserGameOn(event, osu)) userIDOsuActivity = event.getMember().getId();
                break;
            case "!reset":
                event.getGuild().getMemberById("780800869875974185").modifyNickname("Arusu").queue();
                break;
            case "!osubest":
                commandAction.giveOsuUserBestData(event, osu);
                break;
            case "!osuprofile":
                commandAction.giveOsuProfileData(event, osu);
                break;
            case "!weather":
                commandAction.getCurrentWeather(event);
                break;
            case "!leaderboard":
                commandAction.leaderboard(event);
                break;
            case "!roll":
                commandAction.rollRNG(event);
                break;
//            case "!shop":
//                event.getChannel().sendMessage(shopEmbed.build()).queue();
//                break;
//            case "!points":
//                commandAction.checkPoints(event);
//                break;
            case "!updates":
                commandAction.showUpdates(event);
                break;
            case "!greet":
                commandAction.greetMembers(event);
                break;
            case "!feature":
                commandAction.showFeature(event);
                break;
            case "!expose":
                commandAction.createExposeEmbed(event, content);
                break;
            case "!addExpose":
                commandAction.addToExpose(event, content);
                break;
            case "!subscribe":
                commandAction.addSubscription(event, content);
                break;
            case "!unsubscribe":
                commandAction.removeSubscription(event, content[1]);
                break;
            case "!affection":
                commandAction.showAffection(event);
                break;
            case "!play":
                commandAction.playMusic(event);
                break;
            case "!skip":
                commandAction.skipTrack(event.getChannel());
            default:
                commandAction.addPoints(event);
                break;
        }

        if (!message.getMentionedMembers().isEmpty()) {
            commandAction.replyForMentioned(event);
        }
    }

    @Override
    public void onUserActivityStart(@NotNull UserActivityStartEvent event) {
        super.onUserActivityStart(event);

        if (event.getMember().getId().equals(userIDOsuActivity)) {
            // Used to send message to discord server regarding osu recent plays.
            if (event.getNewActivity().getName().equals("osu!")) {
                if (event.getNewActivity().asRichPresence().getDetails() != null) {
                    osuGameStarted = true;
                } else if (osuGameStarted) {
                    osu.reportCurrentGameData(event);
                    osuGameStarted = false;
                }
            }
        }
    }

    @Override
    public void onUserActivityEnd(@NotNull UserActivityEndEvent event) {
        super.onUserActivityEnd(event);

        if (event.getMember().getId().equals(userIDOsuActivity)) {
            if (event.getOldActivity().asRichPresence().getDetails() == null) {
                if (event.getMember().getActivities().isEmpty()) {
                    userIDOsuActivity = null;
                } else if (!event.getMember().getActivities().get(0).getName().contains("osu")) {
                    userIDOsuActivity = null;
                }

                if(userIDOsuActivity == null) {
                    event.getJDA().getPresence().setActivity(Activity.listening("discord events"));
                }
            }
        }
    }

//    @Override
//    public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
//        super.onGuildMessageReactionAdd(event);
//
//        if (event.getUser().isBot()) return;
//
//        if (event.getMessageId().equals(shopMsgId)) {
//            int point = 0;
//            String text = "";
//
//            int cost = 0;
//            String reward = "";
//
//            switch (event.getReaction().getReactionEmote().getAsCodepoints()) {
//                case "U+31U+fe0fU+20e3":
//                    cost = 1000;
//                    reward = "User #0001 plays a csgo game";
//                    break;
//                case "U+32U+fe0fU+20e3":
//                    cost = 5000;
//                    reward = "Claim Mizunoまい as slave";
//                    break;
//            }
//
//            try (BufferedReader reader = new BufferedReader(new FileReader("res/FileData/PlayerPoints.csv"))) {
//                String line;
//
//                while ((line = reader.readLine()) != null) {
//                    String[] field = line.split(",");
//                    if (field[0].equals(event.getMember().getId())) {
//                        point = Integer.parseInt(field[2]);
//                    } else {
//                        text += (line + '\n');
//                    }
//                }
//            } catch (IOException e) {
//
//            }
//
//
//            if (point < cost) {
//                event.getChannel().sendMessage(event.getMember().getAsMention() + "You do not have enough points to trade").queue();
//            } else {
//                point -= cost;
//
//                try (BufferedWriter writer = new BufferedWriter(new FileWriter("res/FileData/PlayerPoints.csv"))) {
//                    writer.write(text + event.getMember().getId() + "," + event.getMember().getUser().getName() + ","
//                            + point + '\n');
//                } catch (IOException e) {
//
//                }
//
//                event.getChannel().sendMessage(event.getMember().getAsMention() +
//                        " You have claimed the reward: " + reward).queue();
//            }
//
//            event.getReaction().removeReaction().queue();
//        }
//    }

//    @Override
//    public void onUserUpdateOnlineStatus(@NotNull UserUpdateOnlineStatusEvent event) {
//        // Sends a message to text channel when user is online.
//        Member member = event.getMember();
//        MessageChannel channel;
//
//        if(event.getJDA().getGuildById(Arusu.guildId).getMemberById(event.getMember().getId()) != null) {
//            channel = event.getJDA().getTextChannelById(Arusu.textChannelId);
//        } else {
//            channel = event.getGuild().getDefaultChannel();
//        }
//
//        if (event.getNewOnlineStatus().equals(OnlineStatus.ONLINE)
//                && event.getOldOnlineStatus().equals(OnlineStatus.OFFLINE)
//                && !event.getUser().isBot()
//                && member.hasAccess((GuildChannel) channel)) {
//            Arusu.counter++;
//
//            // Counter used as event handler gets called twice instead of once.
//            if (Arusu.counter == 2) {
//                if (member.getId().equals(Arusu.friendWId)) {
//                    channel.sendMessage("はぁ,お帰りバカ").queue();
//                } else if (member.getId().equals(Arusu.botOwnerId)) {
//                    channel.sendMessage("お帰りなさい,マスタ").queue();
//                } else {
//                    channel.sendMessage("お帰りなさい" + member.getUser().getName()).queue();
//                }
//                Arusu.counter = 0;
//            }
//        }
//    }

    @Override
    public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent event) {
//        if (event.getEntity().getId().equals(Arusu.botOwnerId)) {
//            VoiceChannel channel = event.getChannelJoined();
//            AudioManager audioManager = event.getGuild().getAudioManager();
//            audioManager.openAudioConnection(channel);
//        } else {
//            MessageChannel channel = event.getGuild().getTextChannelById(Arusu.textChannelId);
//            if (channel != null && event.getChannelJoined().getMembers().isEmpty()) {
//                channel.sendMessage("What is everyone playing today?").queue();
//                channel.sendMessage("Looks like something fun is about to happen").queue();
//            }
//        }
    }

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
//        if (event.getEntity().getId().equals(Arusu.botOwnerId)) {
//            AudioManager audioManager = event.getGuild().getAudioManager();
//            audioManager.closeAudioConnection();
//        } else {
//            if (event.getChannelLeft().getMembers().isEmpty()) {
//                MessageChannel channel = event.getGuild().getTextChannelById(Arusu.textChannelId);
//                assert channel != null;
//                channel.sendMessage("I'll be waiting for the next time everyone plays again").queue();
//            }
//        }
    }
}
