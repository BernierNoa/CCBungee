package fr.nozkay.listeners;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import fr.nozkay.Enum.Chemin;
import fr.nozkay.Enum.RankPlayer;
import fr.nozkay.Enum.ServerStatus;
import fr.nozkay.FileManager;
import fr.nozkay.Main;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.*;
import java.util.*;


public class ServerPing implements Listener {

    private Map<String, Boolean> serverStatus = new HashMap<>();
    public static HashMap<String, ServerStatus> status = new HashMap<>();
    FileManager file;
    private final List<RankPlayer> gradeHierarchy = Arrays.asList(RankPlayer.ADMIN, RankPlayer.RESP, RankPlayer.DEV, RankPlayer.MODO, RankPlayer.STAFF, RankPlayer.HOST, RankPlayer.JOUEUR);

    @EventHandler
    public void onPing(ProxyPingEvent e) throws IOException, InterruptedException {
        file = new FileManager();
        file.loadGrades();
        Map<ProxiedPlayer, RankPlayer> playerGrades = file.getGrades();

        if(status.get("lobby") == ServerStatus.OFFLINE){
            try {
                initializeServerLobby();
            } catch (IOException ee) {
                Main.getInstance().getProxy().getConsole().sendMessage("[Lobby] " + ee);
            }
        }if(status.get("UHC-1") == ServerStatus.DEMARRAGE || status.get("UHC") == ServerStatus.DEMARRAGE){
            startUHCserver("UHC");
        }if(status.get("UHC-2") == ServerStatus.DEMARRAGE){
            startUHCserver("UHC-2");
        }if(status.get("UHC-3") == ServerStatus.DEMARRAGE){
            startUHCserver("UHC-3");
        }
        for(ProxiedPlayer pp: Main.getInstance().getProxy().getPlayers()){
            if(pp.getServer().getInfo().getName().equalsIgnoreCase("lobby")){
                pp.setTabHeader(new TextComponent("\n §9Bienvenue sur le serveur §6La Garderie §9! \n Ping: §f" + pp.getPing() + " ms\n"), new TextComponent("\n§9Site Web: §6lagarderiemc.fr \n§9Discord: §6dsc.gg/lagarderieuhc"));
            }
        }
    }

    public List<ProxiedPlayer> sortPlayersByGrade(Map<ProxiedPlayer, RankPlayer> playerGrades) {
        List<ProxiedPlayer> players = new ArrayList<>(playerGrades.keySet());
        players.sort(Comparator.comparingInt(p -> getGradeIndex(playerGrades.getOrDefault(p, RankPlayer.JOUEUR))));
        return players;
    }

    private int getGradeIndex(RankPlayer grade) {
        return gradeHierarchy.indexOf(grade);
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent event) throws IOException, InterruptedException {
        if(event.getTag().equals("CCChannel")) {
            final ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
            final String sub = in.readUTF();
            if (sub.equals("ServerStatus")) {
                final String serv = in.readUTF();
                ProxiedPlayer p = Main.getInstance().getProxy().getPlayer(event.getReceiver().toString());
                final ByteArrayDataOutput out = ByteStreams.newDataOutput();
                Main.getInstance().getProxy().getLogger().info(" " + serv);
                out.writeUTF("ServerStatus");
                out.writeUTF(serv);
                out.writeUTF(changeStatusToText(status.get(serv)));
                p.getServer().sendData("CCChannel", out.toByteArray());
            }if(sub.equals("ServerCount")){
                final String server = in.readUTF();
                ProxiedPlayer p = Main.getInstance().getProxy().getPlayer(event.getReceiver().toString());
                ServerInfo uhcInfo = Main.getInstance().getProxy().getServerInfo(server);
                final ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("ServerCount");
                out.writeUTF(server);
                out.writeInt(uhcInfo.getPlayers().size());
                p.getServer().sendData("CCChannel", out.toByteArray());
            }if(sub.equals("ServerOpenning")){
                final String server = in.readUTF();
                if(status.get(server) == ServerStatus.OFFLINE){
                    status.put(server, ServerStatus.DEMARRAGE);
                }
            }if(sub.equals("ServerClose")){
                final String server = in.readUTF();
                status.put(server, ServerStatus.OFFLINE);
            }if(sub.equals("GameStart")){
                final String server = in.readUTF();
                if(status.get(server) != ServerStatus.OFFLINE){
                    status.put(server, ServerStatus.START);
                }
            }
        }
    }

    public String changeStatusToText(ServerStatus s){
        if(s.equals(ServerStatus.OFFLINE)){
            return "Fermee";
        }else if(s.equals(ServerStatus.ATTENTE)){
            return "En Attente";
        }else if(s.equals(ServerStatus.ONLINE)){
            return "Ouvert";
        }else if(s.equals(ServerStatus.DEMARRAGE)){
            return "En Démarrage";
        }else if(s.equals(ServerStatus.START)){
            return "En Jeu";
        }
        return "null";
    }

    public void initializeServerLobby() throws IOException {
        status.put("lobby",ServerStatus.ATTENTE);
        String serverDirectory = "/home/minecraft/servers/lobby/";

        ProcessBuilder builder = new ProcessBuilder(Chemin.lobby);
        builder.directory(new File(serverDirectory));
        builder.redirectErrorStream(true);

        Process proc = builder.start();
        Thread outputThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    Main.getInstance().getProxy().getLogger().info("§9CCLobby >> " + line);
                }
            } catch (IOException e) {
                Main.getInstance().getProxy().getLogger().severe("Error reading server process output: " + e.getMessage());
            }
        });

        outputThread.start();
        status.put("lobby",ServerStatus.ONLINE);

        try {
            int rc = proc.waitFor();
            Main.getInstance().getProxy().getLogger().info("Script finished with exit code " + rc);
        } catch (InterruptedException e) {
            Main.getInstance().getProxy().getLogger().severe("Script interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    private void startUHCserver(String server) throws IOException, InterruptedException {
        if(server.equalsIgnoreCase("UHC-1") || server.equalsIgnoreCase("UHC")){
            status.put("UHC",ServerStatus.ATTENTE);
            String serverDirectory = "/home/minecraft/servers/UHC/";

            ProcessBuilder builder = new ProcessBuilder(Chemin.lobby);
            builder.directory(new File(serverDirectory));
            builder.redirectErrorStream(true);

            Process proc = builder.start();
            Thread outputThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        Main.getInstance().getProxy().getLogger().info("§eCCUHC-1 >> " + line);
                    }
                } catch (IOException e) {
                    Main.getInstance().getProxy().getLogger().severe("Error reading server process output: " + e.getMessage());
                }
            });

            outputThread.start();
            status.put("UHC",ServerStatus.ONLINE);

            try {
                int rc = proc.waitFor();
                Main.getInstance().getProxy().getLogger().info("Script finished with exit code " + rc);
            } catch (InterruptedException e) {
                Main.getInstance().getProxy().getLogger().severe("Script interrupted: " + e.getMessage());
                Thread.currentThread().interrupt();
            }
        }if(server.equalsIgnoreCase("UHC-2")){
            status.put("UHC-2",ServerStatus.ATTENTE);
            String serverDirectory = "/home/minecraft/servers/UHC-2/";

            ProcessBuilder builder = new ProcessBuilder(Chemin.lobby);
            builder.directory(new File(serverDirectory));
            builder.redirectErrorStream(true);

            Process proc = builder.start();
            Thread outputThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        Main.getInstance().getProxy().getLogger().info("§cCCUHC-2 >> " + line);
                    }
                } catch (IOException e) {
                    Main.getInstance().getProxy().getLogger().severe("Error reading server process output: " + e.getMessage());
                }
            });

            outputThread.start();
            status.put("UHC-2",ServerStatus.ONLINE);

            try {
                int rc = proc.waitFor();
                Main.getInstance().getProxy().getLogger().info("Script finished with exit code " + rc);
            } catch (InterruptedException e) {
                Main.getInstance().getProxy().getLogger().severe("Script interrupted: " + e.getMessage());
                Thread.currentThread().interrupt();
            }
        }if(server.equalsIgnoreCase("UHC-3")){
            status.put("UHC-3",ServerStatus.ATTENTE);
            String serverDirectory = "/home/minecraft/servers/UHC-3/";

            ProcessBuilder builder = new ProcessBuilder(Chemin.lobby);
            builder.directory(new File(serverDirectory));
            builder.redirectErrorStream(true);

            Process proc = builder.start();
            Thread outputThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        Main.getInstance().getProxy().getLogger().info("§aCCUHC-3 >> " + line);
                    }
                } catch (IOException e) {
                    Main.getInstance().getProxy().getLogger().severe("Error reading server process output: " + e.getMessage());
                }
            });

            outputThread.start();
            status.put("UHC-3",ServerStatus.ONLINE);

            try {
                int rc = proc.waitFor();
                Main.getInstance().getProxy().getLogger().info("Script finished with exit code " + rc);
            } catch (InterruptedException e) {
                Main.getInstance().getProxy().getLogger().severe("Script interrupted: " + e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
    }


    private void updateServerStatus(String serverName, boolean isOnline) {
        serverStatus.put(serverName, isOnline);
    }
}
