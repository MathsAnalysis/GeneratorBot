package it.mathanalisys.generator;

import it.mathanalisys.generator.backend.DatabaseManager;
import it.mathanalisys.generator.commands.ReclaimBasicAccountCommand;
import it.mathanalisys.generator.commands.ReclaimBasicPlusAccountCommand;
import it.mathanalisys.generator.commands.ReclaimBasicPlusPlusAccountCommand;
import it.mathanalisys.generator.listener.ChannelMessageListener;
import it.mathanalisys.generator.listener.ChannelMessagePlusListener;
import it.mathanalisys.generator.listener.ChannelMessagePlusPlusListener;
import it.mathanalisys.generator.listener.ShutdownListener;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.internal.utils.Checks;
import org.bson.Document;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Generator {

    @Getter(AccessLevel.PUBLIC) private JDA jda;
    @Getter(AccessLevel.PUBLIC) private static Generator instance;
    @Getter(AccessLevel.PUBLIC) private DatabaseManager databaseManager;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


    protected String TOKEN = "MTE0OTE5MTc1NzkwNTU0MzE4OA.Gr3Gzh.329B2RUzGKUurOHdDckpfgg75NuWZ_jwWly1xk";

    public Generator(){
        instance = this;

        loadSetup();
        loadListener();
        loadManager();
    }

    private void loadManager(){
        databaseManager = new DatabaseManager();
        removeExpiredCooldowns();
        scheduler.scheduleAtFixedRate(this::removeExpiredCooldowns, 1, 1, TimeUnit.HOURS);
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

    }

    public boolean hasRoleOrHigher(Member member, Role targetRole) {
        List<Role> memberRoles = member.getRoles();

        List<Role> guildRoles = member.getGuild().getRoles();
        int targetIndex = guildRoles.indexOf(targetRole);

        for (Role role : memberRoles) {
            if (guildRoles.indexOf(role) <= targetIndex) {
                return true;
            }
        }

        return false;
    }

    private void removeExpiredCooldowns() {
        long currentTimestamp = System.currentTimeMillis();
        Document query = new Document("expiryTimestamp", new Document("$lt", currentTimestamp));
        Generator.get().getDatabaseManager().getCooldowns().deleteMany(query);
    }

    public static Generator get() {
        return instance;
    }


}
