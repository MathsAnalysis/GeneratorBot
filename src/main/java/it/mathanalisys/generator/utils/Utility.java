package it.mathanalisys.generator.utils;

import it.mathanalisys.generator.Generator;
import it.mathanalisys.generator.main.GeneratorMain;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class Utility {

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

        long count = Generator.get().getDatabaseManager().getCooldowns().countDocuments(query);
        System.out.println("Trovati " + count + " cooldown scaduti.");

        Generator.get().getDatabaseManager().getCooldowns().deleteMany(query);

        long countAfterDelete = Generator.get().getDatabaseManager().getCooldowns().countDocuments(query);
        System.out.println("Dopo la rimozione, rimangono " + countAfterDelete + " cooldown scaduti.");
    }

}
