import Osu.Osu;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Arusu extends ListenerAdapter {
    public static void main(String[] args) throws LoginException {
        JDA api = JDABuilder
                .createDefault(System.getenv("BOT_TOKEN"))
                .enableCache(CacheFlag.ACTIVITY)
                .enableIntents(GatewayIntent.GUILD_PRESENCES)
                .setMemberCachePolicy(MemberCachePolicy.ONLINE)
                .addEventListeners(new Arusu())
                .build();

        api.getPresence().setActivity(Activity.listening("discord events"));

//        api.addEventListener(new StartingListener(), new EventListener());
    }

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

        CommandAction commandAction = new CommandAction();

        // It is expected that the command will be entered as the first string in the message.
        switch (content[0]) {
            case "!help":
                break;
//            case "!listenToMyOsu":
//                if (commandAction.checkOsuUserGameOn(event, osu)) userIDOsuActivity = event.getMember().getId();
//                break;
            case "!reset":
                event.getGuild().getMemberById("780800869875974185").modifyNickname("Arusu").queue();
                break;
//            case "!osubest":
//                commandAction.giveOsuUserBestData(event, osu);
//                break;
            case "!osuprofile":
                commandAction.giveOsuProfileData(event, new Osu());
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
}
