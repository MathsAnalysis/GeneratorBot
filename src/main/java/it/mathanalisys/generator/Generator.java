package it.mathanalisys.generator;

import it.mathanalisys.generator.backend.DatabaseManager;
import it.mathanalisys.generator.commands.reclaim.ReclaimBasicAccountCommand;
import it.mathanalisys.generator.commands.reclaim.ReclaimBasicPlusAccountCommand;
import it.mathanalisys.generator.commands.reclaim.ReclaimBasicPlusPlusAccountCommand;
import it.mathanalisys.generator.commands.StockAccountCommand;
import it.mathanalisys.generator.listener.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import org.bson.Document;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Generator {

    @Getter(AccessLevel.PUBLIC) private JDA jda;
    @Getter(AccessLevel.PUBLIC) private static Generator instance;
    @Getter(AccessLevel.PUBLIC) private DatabaseManager databaseManager;

    @Getter private final ScheduledExecutorService remove_cooldown_thread = Executors.newScheduledThreadPool(1);



    protected String TOKEN = "MTE0OTI4NTA1NjU2OTk0NjIzMg.GieHDo.7IaPLkH5el7LknLuQkexLL2xiRtWX8Ub7fcPXQ";

    public Generator(){
        instance = this;

        loadSetup();
        loadListener();
        loadManager();
    }

    private void loadManager(){
        databaseManager = new DatabaseManager();
        remove_cooldown_thread.scheduleAtFixedRate(this::removeExpiredCooldowns, 1, 15, TimeUnit.MINUTES);
    }

    @SneakyThrows
    private void loadSetup(){
        jda = JDABuilder.createDefault(TOKEN)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .setChunkingFilter(ChunkingFilter.ALL)
                .setAutoReconnect(true)
                .setActivity(Activity.of(Activity.ActivityType.PLAYING, "Generating Account"))
                .build();
        jda.awaitReady();
    }

    private void loadListener(){
        jda.addEventListener(new ShutdownListener());
        jda.addEventListener(new StockAccountCommand());

        // TODO: System for account
        jda.addEventListener(new ChannelMessageListener());
        jda.addEventListener(new ChannelMessagePlusListener());
        jda.addEventListener(new ChannelMessagePlusPlusListener());
        //end

        // TODO: Sytstem for claim account
        jda.addEventListener(new ReclaimBasicAccountCommand());
        jda.addEventListener(new ReclaimBasicPlusAccountCommand());
        jda.addEventListener(new ReclaimBasicPlusPlusAccountCommand());
        //end


        Guild guild = jda.getGuildById("1146820473892638791");
        if (guild == null)return;
        jda.upsertCommand("stock", "shows the availability of everything").queue();


    }

    public boolean hasRoleOrHigher(Member member, Role targetRole) {
        List<Role> memberRoles = member.getRoles();

        List<Role> guildRoles = member.getGuild().getRoles();
        int targetIndex = guildRoles.indexOf(targetRole);

        for (Role role : memberRoles) {
            if (guildRoles.indexOf(role) <= targetIndex) {
                return false;
            }
        }

        return true;
    }

    private void removeExpiredCooldowns() {
        long currentTimestamp = System.currentTimeMillis();
        Document query = new Document("expiryTimestamp", new Document("$lt", currentTimestamp));

        // Contiamo quanti cooldown scaduti ci sono prima di eliminarli
        long count = Generator.get().getDatabaseManager().getCooldowns().countDocuments(query);
        System.out.println("Trovati " + count + " cooldown scaduti.");

        Generator.get().getDatabaseManager().getCooldowns().deleteMany(query);

        // Contiamo quanti cooldown scaduti ci sono dopo averli eliminati
        long countAfterDelete = Generator.get().getDatabaseManager().getCooldowns().countDocuments(query);
        System.out.println("Dopo la rimozione, rimangono " + countAfterDelete + " cooldown scaduti.");
    }

    public static Generator get() {
        return instance;
    }




}
