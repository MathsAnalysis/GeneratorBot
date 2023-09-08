package it.mathanalisys.generator.commands;

import it.mathanalisys.generator.Generator;
import it.mathanalisys.generator.utils.Utility;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;


public class ResetCommand extends ListenerAdapter {


    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        User user = event.getAuthor();
        Member member = event.getMember();

        if (member == null) return;
        String messageContent = event.getMessage().getContentRaw();
        if (messageContent.startsWith("!reset")) {
            String[] args = messageContent.split("\\s+");

            Role targetRole = event.getGuild().getRoleById("1149189658761240599");

            if (Utility.hasRoleOrHigher(member, targetRole)) {
                user.openPrivateChannel().queue(privateChannel -> {
                    EmbedBuilder builder = new EmbedBuilder();
                    builder.setColor(Color.red);
                    builder.appendDescription("(✘) You don't have the required role to use this command.");
                    privateChannel.sendMessageEmbeds(builder.build()).queue(sentMessage -> sentMessage.delete().queueAfter(10, TimeUnit.SECONDS), throwable -> {
                        if (throwable instanceof ErrorResponseException) {
                            event.getChannel().sendMessage(user.getAsMention() + " I can't send you a private message. Please make sure your DMs are open.").queue(message1 -> message1.delete().queueAfter(10, TimeUnit.SECONDS));
                        }
                    });
                });

                event.getMessage().delete().queueAfter(10, TimeUnit.SECONDS);
                return;
            }

            if (args.length == 3) {
                String userId = args[1];
                String cooldownType = args[2];

                try {
                    Long.parseLong(userId);
                } catch (NumberFormatException e) {
                    event.getChannel().sendMessage("L'ID not valid").queue();
                    return;
                }

                Document cooldownEntry = Generator.get().getDatabaseManager().getCooldowns().find(new Document("userId", userId).append("type", cooldownType)).first();
                if (cooldownEntry == null) {
                    event.getChannel().sendMessage("The specified user does not have a cooldown of type " + cooldownType).queue();
                    return;
                }

                Generator.get().getDatabaseManager().getCooldowns().deleteOne(new Document("userId", userId).append("type", cooldownType));

                EmbedBuilder builder = new EmbedBuilder();
                builder.setTitle("Reset Cooldown");
                builder.appendDescription("\n");
                builder.appendDescription("Cooldown for user " + userId + " of type " + cooldownType + " has been reset.");
                builder.setColor(Utility.randomColor());
                builder.appendDescription(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
                event.getChannel().asTextChannel().sendMessageEmbeds(builder.build()).queue();

            } else {
                event.getChannel().sendMessage("Usage: !reset <id_utente> <cooldown>").queue();
            }
        }
    }





}
