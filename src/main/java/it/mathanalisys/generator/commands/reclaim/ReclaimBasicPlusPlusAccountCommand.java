package it.mathanalisys.generator.commands.reclaim;

import it.mathanalisys.generator.Generator;
import it.mathanalisys.generator.utils.DateUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bson.Document;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class ReclaimBasicPlusPlusAccountCommand extends ListenerAdapter {


    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String message = event.getMessage().getContentRaw();
        User user = event.getAuthor();
        Member member = event.getMember();

        if (member == null) return;

        if (user.isBot()) {
            return;
        }

        if (event.getChannel().asTextChannel().getId().equals("1149215591232720946")){
            if (message.equalsIgnoreCase("!gen")) {
                Document document = Generator.get().getDatabaseManager().getAndRemoveAccountPlusPlus();

                Role targetRole = event.getGuild().getRoleById("1149189860612120596");

                if (Generator.get().hasRoleOrHigher(member, targetRole)) {
                    user.openPrivateChannel().queue(privateChannel -> {
                        EmbedBuilder builder = new EmbedBuilder();
                        builder.setColor(Color.red);
                        builder.appendDescription("(✘) You don't have the required role to use this command.");
                        privateChannel.sendMessageEmbeds(builder.build()).queue(sentMessage ->
                                sentMessage.delete().queueAfter(10, TimeUnit.SECONDS), throwable -> {
                            if (throwable instanceof ErrorResponseException) {
                                event.getChannel().sendMessage(user.getAsMention() + " I can't send you a private message. Please make sure your DMs are open.").queue();
                            }
                        });
                    });
                    event.getMessage().delete().queueAfter(10, TimeUnit.SECONDS);
                    return;
                }

                if (Generator.get().getDatabaseManager().isInCooldown(user.getId(), "basic_plus_plus_cooldown")) {
                    int remainingSeconds = Generator.get().getDatabaseManager().getRemainingCooldown(user.getId(), "basic_plus_plus_cooldown");
                    event.getChannel().sendMessage("You must wait " + DateUtils.getSexyTime(remainingSeconds) + " before using this command again.").queue();
                    return;
                }


                if (document == null) {
                    event.getChannel().sendMessage("I'm sorry, there are no accounts available at the moment.").queue();
                    return;
                }

                user.openPrivateChannel().queue(privateChannel -> {
                    EmbedBuilder builder = new EmbedBuilder();
                    builder.setTitle("Account");
                    builder.setColor(Color.blue);
                    builder.appendDescription("\n");
                    builder.appendDescription("Username: " + document.getString("username") + "\n");
                    builder.appendDescription("Password: " + document.getString("password") + "\n");
                    builder.appendDescription("\n");
                    builder.appendDescription("Cooldown: 3 hours" + "\n");
                    builder.appendDescription("Type: Basic++");
                    builder.appendDescription("\n");
                    builder.appendDescription("\n");
                    builder.setFooter(new SimpleDateFormat("MMMM dd, yyyy hh:mm:ss a").format(new Date()), null);

                    privateChannel.sendMessageEmbeds(builder.build()).queue(null, throwable -> {
                        if (throwable instanceof ErrorResponseException) {
                            event.getChannel().sendMessage(user.getAsMention() + " I can't send you a private message. Please make sure your DMs are open.").queue();
                        }
                    });
                });

                event.getMessage().delete().queueAfter(5, TimeUnit.SECONDS);
                Generator.get().getDatabaseManager().addCooldown(user.getId(), 10800, user.getName(), "basic_plus_plus_cooldown");
            }
        }
    }
}
