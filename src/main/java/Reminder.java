import net.dv8tion.jda.api.entities.MessageChannel;


import java.time.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.*;

public class Reminder {
    public static final ZoneOffset localZone = ZoneOffset.from(OffsetTime.now());

    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);

    public void remindSleep(MessageChannel channel) {
        String sleep = "Its time to go to bed master";

        final Runnable reminder = new Runnable() {
            public void run() {
                channel.sendMessage(sleep).queue();
            }
        };

        // bedtime initial settings in UST timezone
        OffsetTime bedTimeUST = OffsetTime.MIN.withHour(23).withMinute(0);
        OffsetTime bedTime = bedTimeUST.withOffsetSameLocal(localZone);
        long duration = Duration.between(OffsetTime.now(), bedTime).toMinutes();

        final ScheduledFuture<?> beeperHandle =
                scheduler.schedule(reminder, duration, MINUTES);
    }

    public void remindDrink(MessageChannel channel) {
        String hydrate = "Don't forget to hydrate yourselves";

        final Runnable reminder = new Runnable() {
            public void run() {
                channel.sendMessage(hydrate).queue();
            }
        };

        final ScheduledFuture<?> drinkHandle =
                scheduler.scheduleWithFixedDelay(reminder,
                        10, 15, MINUTES);
    }
}
