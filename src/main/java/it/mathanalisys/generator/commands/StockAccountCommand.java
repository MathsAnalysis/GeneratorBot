package it.mathanalisys.generator.commands;

import it.mathanalisys.generator.Generator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.util.concurrent.TimeUnit;

public class StockAccountCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {

        if (event.getChannel().asTextChannel().getId().equals("1149294921610706984")) {

            if (event.getName().equals("stock")) {
                Member member = event.getMember();
                if (member == null) return;

                Role role = event.getGuild().getRoleById("1149214454844772372");
                if (Generator.get().hasRoleOrHigher(member, role)) return;

                long countFiles = Generator.get().getDatabaseManager().getFiles().countDocuments();
                long countFilesPlus = Generator.get().getDatabaseManager().getFilesPlus().countDocuments();
                long countFilesPlusPlus = Generator.get().getDatabaseManager().getFilesPlusPlus().countDocuments();

                EmbedBuilder builder = new EmbedBuilder();

                builder.setTitle("Accounts Available")
                        .appendDescription(
                                "\n" +
                                        "Basic Account: " + countFiles + "\n" +
                                        "Basic+ Account: " + countFilesPlus + "\n" +
                                        "Basic++ Account: " + countFilesPlusPlus + "\n"
                                        + "\n"
                        ).setColor(Color.CYAN);

                event.deferReply().queue();
                event.getHook().sendMessageEmbeds(builder.build()).queue(message -> message.delete().queueAfter(20, TimeUnit.SECONDS));
            }
        }
    }

}
