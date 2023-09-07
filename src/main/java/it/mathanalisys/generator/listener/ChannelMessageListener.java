package it.mathanalisys.generator.listener;

import it.mathanalisys.generator.Generator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bson.Document;

import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ChannelMessageListener extends ListenerAdapter {

    public static ExecutorService basic_data = Executors.newSingleThreadExecutor();

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Member member = event.getMember();
        if (member == null) return;

        if (event.getChannel().getId().equals("1149215610333577229") && !event.getMessage().getAttachments().isEmpty()) {
            Generator.get().getDatabaseManager().getFiles().drop();

            Role role = event.getGuild().getRoleById("1149214454844772372");
            if (Generator.get().hasRoleOrHigher(member, role)) return;

            event.getMessage().getAttachments().forEach(attachment -> attachment.retrieveInputStream().thenAccept(stream -> basic_data.submit(() -> {
                List<Document> docsToInsert = new ArrayList<>();

                try (BufferedReader br = new BufferedReader(new InputStreamReader(stream))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        String[] parts = line.split(":");
                        if (parts.length == 2) {
                            Document doc = new Document("username", parts[0].trim()).append("password", parts[1].trim());
                            docsToInsert.add(doc);
                        }
                    }

                    Generator.get().getDatabaseManager().getFiles().insertMany(docsToInsert);
                    System.out.println(docsToInsert.size() + " account inseriti nel database.");

                    TextChannel otherChannel = event.getJDA().getTextChannelById("1149397104851165304");
                    EmbedBuilder builder = new EmbedBuilder();
                    builder.setColor(Color.BLUE);
                    builder.setTitle("Log Account");
                    builder.appendDescription("\n");
                    builder.appendDescription("Total accounts: " + docsToInsert.size() + "\n");
                    builder.appendDescription("Type: Basic\n");
                    builder.appendDescription("\n");
                    builder.setFooter(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));


                    if (otherChannel != null) {
                        otherChannel.sendMessageEmbeds(builder.build()).queue();
                    }

                    event.getMessage().delete().queueAfter(30, TimeUnit.SECONDS);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            })).exceptionally(t -> {
                t.printStackTrace();
                return null;
            }));

        }
    }
}