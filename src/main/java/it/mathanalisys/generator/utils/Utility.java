package it.mathanalisys.generator.utils;

import it.mathanalisys.generator.Generator;
import it.mathanalisys.generator.main.GeneratorMain;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import org.bson.Document;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@UtilityClass
public class Utility {

    private static final Random random = new Random();


    public static boolean isUser(User user, String id){
        return user.getId().equals(id);
    }

    public static boolean hasRoleOrHigher(Member member, Role targetRole) {
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

    public static void removeExpiredCooldowns() {
        long currentTimestamp = System.currentTimeMillis();
        Document query = new Document("expiryTimestamp", new Document("$lt", currentTimestamp));
        Generator.get().getDatabaseManager().getCooldowns().deleteMany(query);
    }

    public static Color randomColor() {
        int red = random.nextInt(256);
        int green = random.nextInt(256);
        int blue = random.nextInt(256);
        return new Color(red, green, blue);
    }

}
