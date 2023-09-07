package it.mathanalisys.generator.utils;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;

public class CommandDeletion {

    private final JDA jda;

    public CommandDeletion(JDA jda) {
        this.jda = jda;
    }

    public void deleteCommand(String commandName) {
        Guild guild = jda.getGuildById("1146820473892638791");

        if (guild != null) {
            guild.retrieveCommands().queue(commands -> commands.stream()
                    .filter(command -> command.getName().equalsIgnoreCase(commandName))
                    .forEach(command -> {
                        guild.deleteCommandById(command.getId()).queue();
                        System.out.println("Command " + commandName + " deleted.");
                    }));
        }
    }
}
