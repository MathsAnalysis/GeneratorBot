package it.mathanalisys.generator;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

public class Generator {

    @Getter(AccessLevel.PUBLIC) private JDA jda;

    public Generator(){

    }

    @SneakyThrows
    private void loadSetup(){
        jda = JDABuilder.createDefault("").build();
        
        jda.awaitReady();
    }
}
