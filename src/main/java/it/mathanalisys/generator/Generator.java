package it.mathanalisys.generator;

import it.mathanalisys.generator.backend.DatabaseManager;
import it.mathanalisys.generator.commands.CheckCooldownCommand;
import it.mathanalisys.generator.commands.ResetCommand;
import it.mathanalisys.generator.commands.StockAccountCommand;
import it.mathanalisys.generator.commands.reclaim.ReclaimBasicAccountCommand;
import it.mathanalisys.generator.commands.reclaim.ReclaimBasicPlusAccountCommand;
import it.mathanalisys.generator.commands.reclaim.ReclaimBasicPlusPlusAccountCommand;
import it.mathanalisys.generator.config.Configuration;
import it.mathanalisys.generator.listener.ChannelMessageListener;
import it.mathanalisys.generator.listener.ChannelMessagePlusListener;
import it.mathanalisys.generator.listener.ChannelMessagePlusPlusListener;
import it.mathanalisys.generator.listener.ShutdownListener;
import it.mathanalisys.generator.utils.CommandDeletion;
import it.mathanalisys.generator.utils.Utility;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Generator {

    @Getter(AccessLevel.PUBLIC) private static Generator instance;
    @Getter(AccessLevel.PUBLIC) private DatabaseManager databaseManager;
    @Getter(AccessLevel.PUBLIC) private Configuration configuration;

    @Getter(AccessLevel.PUBLIC) private JDA jda;

    @Getter private final ScheduledExecutorService remove_cooldown_thread = Executors.newScheduledThreadPool(1);

    public static Guild GUILD_ID;

    protected CommandDeletion commandDeletion;




    protected String TOKEN = "MTE0OTI4NTA1NjU2OTk0NjIzMg.GieHDo.7IaPLkH5el7LknLuQkexLL2xiRtWX8Ub7fcPXQ";

    public Generator(){
        instance = this;

        loadSetup();
        loadListener();
        loadManager();
    }

    private void loadManager(){
        this.configuration = new Configuration("configuration");
        databaseManager = new DatabaseManager();
        remove_cooldown_thread.scheduleAtFixedRate(Utility::removeExpiredCooldowns, 1, 15, TimeUnit.MINUTES);

        GUILD_ID = jda.getGuildById("1146820473892638791");

        this.commandDeletion = new CommandDeletion(jda);
        this.commandDeletion.deleteCommand("clear");
        this.commandDeletion.deleteCommand("clearchat");
    }

    @SneakyThrows
    private void loadSetup(){
        jda = JDABuilder.createDefault(TOKEN)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .setChunkingFilter(ChunkingFilter.ALL)
                .setStatus(OnlineStatus.ONLINE)
                .setAutoReconnect(true)
                .setActivity(Activity.of(Activity.ActivityType.PLAYING, "Generating Account"))
                .build()
                .awaitReady();
    }

    private void loadListener(){
        jda.addEventListener(new ShutdownListener());

        // System for moderation
        jda.addEventListener(new StockAccountCommand());
        jda.addEventListener(new ResetCommand());
        jda.addEventListener(new CheckCooldownCommand());

        // System for account
        jda.addEventListener(new ChannelMessageListener());
        jda.addEventListener(new ChannelMessagePlusListener());
        jda.addEventListener(new ChannelMessagePlusPlusListener());
        //end

        // Sytstem for claim account
        jda.addEventListener(new ReclaimBasicAccountCommand());
        jda.addEventListener(new ReclaimBasicPlusAccountCommand());
        jda.addEventListener(new ReclaimBasicPlusPlusAccountCommand());
        //end



        if (GUILD_ID == null)return;
        jda.upsertCommand("stock", "shows the availability of everything").queue();
    }

    public static Generator get() {
        return instance;
    }




}
