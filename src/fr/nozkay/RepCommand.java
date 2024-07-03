package fr.nozkay;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class RepCommand extends Command {

    public RepCommand(){
        super("r", null,"rep");
    }

    @Override
    public void execute(CommandSender sender, String[] args){
        if(!(sender instanceof ProxiedPlayer)){
            return;
        }
        ProxiedPlayer pp = (ProxiedPlayer) sender;
        if(args.length == 0){
            pp.sendMessage(new TextComponent("§cFaire /r [joueur] [message]"));
            return;
        }
        if(Main.msg.containsKey(pp)){
            if(!Main.msg.get(pp).isConnected()){
                pp.sendMessage(new TextComponent("§cCette personne n'est pas connecté."));
            }else{
                ProxiedPlayer pd = Main.msg.get(pp);
                String msg = "";
                for(int i = 1; i != args.length; i++) msg += args[i] + " ";
                if(pd != pp){
                    if(msg != null){
                        pp.sendMessage(new TextComponent("§b" + pp.getName() + " §7»§b" + pd.getName() + ": §f" + msg));
                        pd.sendMessage(new TextComponent("§b" + pp.getName() + " §7»§b" + pd.getName() + ": §f" + msg));
                        Main.msg.put(pp,pd);
                    }
                }
            }
        }else{
            pp.sendMessage(new TextComponent("§cVous n'avez personne à qui répondre."));
        }
    }
}
