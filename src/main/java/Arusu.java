import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.security.auth.login.LoginException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Arusu {
    public static int counter = 0;
    public static ArrayList<String[]> id;
    public static String botToken;
    public static String botOwnerId;
    public static String guildOwnerId;
    public static String guildId;
    public static String textChannelId;
    public static String friendWId;
    public static String botId;

    public static void main(String[] args) throws LoginException {
        new Arusu().readId();

        JDA api = JDABuilder
                .createDefault(botToken)
                .enableCache(CacheFlag.ACTIVITY)
                .enableIntents(GatewayIntent.GUILD_PRESENCES)
                .setMemberCachePolicy(MemberCachePolicy.ONLINE)
                .build();

        api.getPresence().setActivity(Activity.listening("discord events"));

        api.addEventListener(new StartingListener(), new EventListener());
    }

    // Hides important ids of guild and users and bot token (For personal use)
    private void readId() {
        try(BufferedReader reader = new BufferedReader(new FileReader("classifiedInfo.csv"))) {
            String line;
            while((line = reader.readLine()) != null) {
                String idName = line.split(",")[0];
                String id = line.split(",")[1];

                switch (idName) {
                    case "botToken":
                        botToken = id;
                        break;
                    case "botId":
                        botId = id;
                        break;
                    case "botOwnerId":
                        botOwnerId = id;
                        break;
                    case "guildOwnerId":
                        guildOwnerId = id;
                        break;
                    case "guildId":
                        guildId = id;
                        break;
                    case "textChannelId":
                        textChannelId = id;
                        break;
                    case "friendWId":
                        friendWId = id;
                        break;
                }
            }
        } catch (IOException e) {
            System.out.println("File failed to read");
        }
    }
}
