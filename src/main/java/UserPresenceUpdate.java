import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.user.update.UserUpdateOnlineStatusEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class UserPresenceUpdate extends ListenerAdapter {
    @Override
    public void onUserUpdateOnlineStatus(@NotNull UserUpdateOnlineStatusEvent event) {
        // Sends a message to text channel when user is online.
        if(event.getMember().getOnlineStatus().equals(OnlineStatus.ONLINE)) {
            MessageChannel channel = event.getJDA()
                    .getTextChannelById("745634050672296040");
            if(event.getOldOnlineStatus().equals(OnlineStatus.OFFLINE) &&
            !event.getUser().isBot() &&
                    event.getMember().hasAccess(event.getJDA().getTextChannelById("745634050672296040"))) {
                Main.counter++;
                if(Main.counter == 2) {
                    if(event.getUser().getId().equals("95492173562130432")) {
                        channel.sendMessage("Oh look the idiot is back").queue();
                    } else {
                        channel.sendMessage("Welcome Back " +
                                event.getMember().getUser().getName()).queue();
                    }
                    Main.counter = 0;
                }
            }

        }
    }
}
