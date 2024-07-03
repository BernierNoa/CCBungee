package fr.nozkay;

import fr.nozkay.Enum.Chemin;
import fr.nozkay.Enum.ServerStatus;
import fr.nozkay.listeners.PlayerJoin;
import fr.nozkay.listeners.ServerPing;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class Main extends Plugin {

    private static Main instance;
    public static HashMap<ProxiedPlayer, ProxiedPlayer> msg = new HashMap<>();

    @Override
    public void onEnable(){
        instance =this;
        getProxy().getConsole().sendMessage("Démarrage du CCBungee");
        this.getProxy().registerChannel("CCChannel");
        getProxy().getPluginManager().registerListener(this, new ServerPing());
        getProxy().getPluginManager().registerListener(this, new PlayerJoin());
        getProxy().getPluginManager().registerCommand(this, new MsgCommand());
        getProxy().getPluginManager().registerCommand(this, new RepCommand());
        initializeServerStatus();
    }

    private void initializeServerStatus() {
        for (Map.Entry<String, ServerInfo> entry : ProxyServer.getInstance().getServers().entrySet()) {
            String serverName = entry.getKey();
            ServerInfo serverInfo = entry.getValue();
            int playerCount = serverInfo.getPlayers().size();
            ServerPing.status.put(serverName, ServerStatus.OFFLINE);

            getLogger().info("Initial server status: " + serverName + " is " + "offline");
        }
    }



    public static Main getInstance(){
        return instance;
    }
}
