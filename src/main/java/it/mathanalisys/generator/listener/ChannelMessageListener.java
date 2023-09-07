package it.mathanalisys.generator.listener;

import it.mathanalisys.generator.Generator;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bson.Document;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChannelMessageListener extends ListenerAdapter {

    public static ExecutorService basic_data = Executors.newSingleThreadExecutor();

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!Generator.get().getHighestRoleFrom(event.getMember()).getId().equals("1149214454844772372"))
            return;

        if (event.getChannel().getId().equals("1149215610333577229") && !event.getMessage().getAttachments().isEmpty()) {
            Generator.get().getDatabaseManager().getFiles().drop();

            event.getMessage().getAttachments().forEach(attachment -> attachment.retrieveInputStream().thenAccept(stream -> {
                basic_data.submit(() -> {
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

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

            }).exceptionally(t -> {
                t.printStackTrace();
                return null;
            }));
        }
    }
}