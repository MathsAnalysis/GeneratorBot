package it.mathanalisys.generator.commands;

import it.mathanalisys.generator.Generator;
import it.mathanalisys.generator.utils.DateUtils;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bson.Document;

import java.util.Objects;

public class ReclaimBasicAccountCommand extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String message = event.getMessage().getContentRaw();
        User user = event.getAuthor();

        if (user.isBot()) {
            return;
        }

        if (event.getChannel().asTextChannel().getId().equals("1149229611159400448")){
            if (Generator.get().hasRoleById(Objects.requireNonNull(event.getMember()), "1149189484399833179"))
                return;

            if (Generator.get().getDatabaseManager().isInCooldown(user.getId(), "basic_cooldown")) {
                int remainingSeconds = Generator.get().getDatabaseManager().getRemainingCooldown(user.getId(), "basic_cooldown");
                event.getChannel().sendMessage("You must wait " + DateUtils.niceTime(remainingSeconds) + " seconds before using this command again.").queue();
                return;
            }

            if (message.equalsIgnoreCase("!gen")) {
                Document document = Generator.get().getDatabaseManager().getAndRemoveRandomAccount();

                if (document == null) {
                    event.getChannel().sendMessage("I'm sorry, there are no accounts available at the moment.").queue();
                    return;
                }

                String response = "Ecco il tuo account: "
                        + document.getString("username") + ":"
                        + document.getString("password");
                event.getChannel().sendMessage(response).queue();

                // Set a new cooldown in the database
                Generator.get().getDatabaseManager().addCooldown(user.getId(), 900, "basic_cooldown");
            }
        }
    }
}