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

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class CheckCooldownCommand extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Member member = event.getMember();
        if (member == null) return;
        User user = event.getAuthor();


        String messageContent = event.getMessage().getContentRaw();

        if (messageContent.startsWith("!checkCooldown")) {
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

            if (args.length == 2) {
                String userId = args[1];

                try {
                    Long.parseLong(userId);
                } catch (NumberFormatException e) {
                    event.getChannel().sendMessage("Invalid ID.").queue();
                    return;
                }

                Member targetMember = event.getGuild().getMemberById(userId);
                if (targetMember == null) {
                    event.getChannel().sendMessage("The user is not present in the server.").queue();
                    return;
                }

                ArrayList<Document> cooldowns = Generator.get().getDatabaseManager().getCooldowns().find(new Document("userId", userId)).into(new ArrayList<>());
                if (cooldowns.isEmpty()) {
                    event.getChannel().sendMessage("The specified user has no active cooldowns.").queue();
                    return;
                }

                StringBuilder sb = new StringBuilder();
                sb.append("The user ").append(userId).append(" has the following active cooldowns:\n");
                for (Document cooldown : cooldowns) {
                    sb.append("- Type: ").append(cooldown.getString("type")).append("\n");
                }

                EmbedBuilder builder = new EmbedBuilder();
                builder.setTitle("Check Cooldown");
                builder.setColor(Utility.randomColor());
                builder.appendDescription("\n");
                builder.appendDescription(sb.toString());
                builder.appendDescription("\n");
                builder.appendDescription(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));


                event.getChannel().sendMessageEmbeds(builder.build()).queue();

            } else {
                event.getChannel().sendMessage("Usage: !checkCooldown <id_utente>").queue();
            }

        }
    }

}
