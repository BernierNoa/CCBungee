package fr.nozkay.listeners;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PlayerJoin implements Listener {

    @EventHandler
    public void onJoin(PostLoginEvent e){
        ProxiedPlayer pp = e.getPlayer();
        pp.setTabHeader(new TextComponent("\n §9Bienvenue sur le serveur §6La Garderie §9! \n Ping: §f" + pp.getPing() + " ms\n"), new TextComponent("\n§9Site Web: §6lagarderiemc.fr \n§9Discord: §6dsc.gg/lagarderieuhc"));
    }
}
