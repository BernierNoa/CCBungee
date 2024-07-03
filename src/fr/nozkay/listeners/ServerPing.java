package fr.nozkay.listeners;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import fr.nozkay.Enum.Chemin;
import fr.nozkay.Enum.ServerStatus;
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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class ServerPing implements Listener {

    private Map<String, Boolean> serverStatus = new HashMap<>();
    public static HashMap<String, ServerStatus> status = new HashMap<>();


    @EventHandler
    public void onPing(ProxyPingEvent e){
        if(status.get("lobby") == ServerStatus.OFFLINE){
            try {
                initializeServerLobby();
            } catch (IOException ee) {
                Main.getInstance().getProxy().getConsole().sendMessage("[Lobby] " + ee);
            }
        }
        ServerInfo uhcInfo = Main.getInstance().getProxy().getServerInfo("UHC");
        ServerInfo arcadeInfo = Main.getInstance().getProxy().getServerInfo("Arcade");
        updateServerStatus(uhcInfo.getName(),true);
        updateServerStatus(arcadeInfo.getName(),true);
        Main.getInstance().getProxy().getConsole().sendMessage("Liste des serveurs:");
        Main.getInstance().getProxy().getConsole().sendMessage(uhcInfo.getName() + "= Joueurs connéctés: " + uhcInfo.getPlayers().size());
        Main.getInstance().getProxy().getConsole().sendMessage(arcadeInfo.getName() + "= Joueurs connéctés: " + arcadeInfo.getPlayers().size());
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent event) {
        if(event.getTag().equals("CCChannel")) {
            Main.getInstance().getProxy().getLogger().info("Reçu !");
            final ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
            final String sub = in.readUTF();
            if (sub.equals("ServerStatus")) {
                final String uuidStr = in.readUTF();
                final UUID uuid = UUID.fromString(uuidStr);
                Main.getInstance().getProxy().getLogger().info("" + uuid);
                ProxiedPlayer p = Main.getInstance().getProxy().getPlayer(event.getReceiver().toString());
                final ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("ServerStatus");
                out.writeUTF("tkt");
                p.getServer().sendData("CCChannel", out.toByteArray());
                Main.getInstance().getProxy().getLogger().info("Envoyé !");
            }if(sub.equals("ServerCount")){
                final String server = in.readUTF();
                ProxiedPlayer p = Main.getInstance().getProxy().getPlayer(event.getReceiver().toString());
                ServerInfo uhcInfo = Main.getInstance().getProxy().getServerInfo(server);
                final ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("ServerCount");
                out.writeUTF(server);
                out.writeInt(uhcInfo.getPlayers().size());
                p.getServer().sendData("CCChannel", out.toByteArray());
                Main.getInstance().getProxy().getLogger().info("Envoyé !");
            }
        }
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
            Process proc = new ProcessBuilder(Chemin.uhc).start();
            int rc = proc.waitFor();
            ProxyServer.getInstance().broadcast("Script finished with exit code " + rc + " Error: " + proc.getErrorStream());

            BufferedReader read = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            while (read.ready()) {
                Main.getInstance().getProxy().getConsole().sendMessage(read.readLine());
            }
        }if(server.equalsIgnoreCase("UHC-2")){
            Process proc = new ProcessBuilder(Chemin.uhc2).start();
            int rc = proc.waitFor();
            ProxyServer.getInstance().broadcast("Script finished with exit code " + rc + " Error: " + proc.getErrorStream());

            BufferedReader read = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            while (read.ready()) {
                Main.getInstance().getProxy().getConsole().sendMessage(read.readLine());
            }
        }if(server.equalsIgnoreCase("UHC-3")){
            Process proc = new ProcessBuilder(Chemin.uhc3).start();
            int rc = proc.waitFor();
            ProxyServer.getInstance().broadcast("Script finished with exit code " + rc + " Error: " + proc.getErrorStream());

            BufferedReader read = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            while (read.ready()) {
                Main.getInstance().getProxy().getConsole().sendMessage(read.readLine());
            }
        }
    }


    private void updateServerStatus(String serverName, boolean isOnline) {
        serverStatus.put(serverName, isOnline);
    }
}
