package it.mathanalisys.generator.commands;

import it.mathanalisys.generator.Generator;
import it.mathanalisys.generator.utils.Utility;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.concurrent.TimeUnit;

public class StockAccountCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {

        if (event.getChannel().asTextChannel().getId().equals("1149294921610706984")) {

            if (event.getName().equals("stock")) {
                Member member = event.getMember();
                if (member == null || event.getGuild() == null) return;

                Role role = event.getGuild().getRoleById("1149214454844772372");
                if (Utility.hasRoleOrHigher(member, role)) return;

                long countFiles = Generator.get().getDatabaseManager().getFiles().countDocuments();
                long countFilesPlus = Generator.get().getDatabaseManager().getFilesPlus().countDocuments();
                long countFilesPlusPlus = Generator.get().getDatabaseManager().getFilesPlusPlus().countDocuments();

                EmbedBuilder builder = getBuilder(countFiles, countFilesPlus, countFilesPlusPlus);

                event.deferReply().queue();
                event.getHook().sendMessageEmbeds(builder.build()).queue(message -> message.delete().queueAfter(20, TimeUnit.SECONDS));
            }
        }
    }

    @NotNull
    private static EmbedBuilder getBuilder(long countFiles, long countFilesPlus, long countFilesPlusPlus) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Accounts Available");
        builder.appendDescription("\n");
        builder.appendDescription("Basic Account: " + countFiles + "\n");
        builder.appendDescription("Basic+ Account: " + countFilesPlus + "\n");
        builder.appendDescription("Basic++ Account: " + countFilesPlusPlus + "\n");
        builder.appendDescription("\n");
        builder.setColor(Color.CYAN);
        return builder;
    }

}
