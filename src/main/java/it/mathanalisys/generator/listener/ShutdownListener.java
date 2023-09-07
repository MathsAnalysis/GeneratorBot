package it.mathanalisys.generator.listener;

import it.mathanalisys.generator.Generator;
import net.dv8tion.jda.api.events.session.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class ShutdownListener extends ListenerAdapter {

    @Override
    public void onShutdown(@NotNull ShutdownEvent event) {
        ChannelMessageListener.basic_data.shutdown();
        ChannelMessagePlusListener.basic_data_plus.shutdown();
        ChannelMessagePlusPlusListener.basic_data_plus_plus.shutdown();

        Generator.get().getRemove_cooldown_thread().shutdown();

    }
}
