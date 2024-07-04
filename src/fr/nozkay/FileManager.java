package fr.nozkay;

import fr.nozkay.Enum.RankPlayer;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.io.*;
import java.util.*;

public class FileManager {

    private File file;
    private Map<ProxiedPlayer, RankPlayer> grades = new HashMap<ProxiedPlayer,RankPlayer>();

    public FileManager() {
        String filePath = "/home/minecraft/servers/lobby/plugins/CCLobby/grade.txt";
        file = new File(filePath);

        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void loadGrades() {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" = ");
                if (parts.length == 2) {
                    UUID playerUUID = UUID.fromString(parts[0]);
                    String grade = parts[1];

                    ProxiedPlayer player = ProxyServer.getInstance().getPlayer(playerUUID);
                    if (player != null) {
                        if(grade.equalsIgnoreCase("ADMIN")){
                            grades.put(player, RankPlayer.ADMIN);
                        }if(grade.equalsIgnoreCase("DEV")){
                            grades.put(player, RankPlayer.DEV);
                        }if(grade.equalsIgnoreCase("STAFF")){
                            grades.put(player, RankPlayer.STAFF);
                        }if(grade.equalsIgnoreCase("Resp")){
                            grades.put(player, RankPlayer.RESP);
                        }if(grade.equalsIgnoreCase("MODO")){
                            grades.put(player, RankPlayer.MODO);
                        }if(grade.equalsIgnoreCase("HOST")){
                            grades.put(player, RankPlayer.HOST);
                        }if(grade.equalsIgnoreCase("JOUEUR")){
                            grades.put(player, RankPlayer.JOUEUR);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<ProxiedPlayer, RankPlayer> getGrades() {
        return grades;
    }


}
