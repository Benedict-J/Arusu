import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.MINUTES;

public class StartingListener extends ListenerAdapter {

    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        MessageChannel channel = event.getJDA().getTextChannelById("745634050672296040");
        channel.sendMessage("```New update:\n-Added !play and !skip command for music. (unstable feature)\n-Usage: !play [song name]\n-Example 1: !play butter\n-Example 2: !play bts butter```").queue();
        subscriptionsHandler(event);
    }

    private void subscriptionsHandler(ReadyEvent event) {
        try(BufferedReader reader = new BufferedReader(new FileReader("res/FileData/Subscribers.csv"))) {
            String tuple;
            while((tuple = reader.readLine()) != null) {
                String[] fields = tuple.split(",");
                switch(fields[1]) {
                    case "drink":
                        drinkHandler(event, fields[0]);
                        break;
                }
            }
        } catch (IOException e) {

        }
    }

    private void drinkHandler(ReadyEvent event, String memberId) {
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
}