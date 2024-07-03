package fr.nozkay;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class MsgCommand extends Command {

    public MsgCommand(){
        super("w", null,"msg","mp","tell");
    }

    @Override
    public void execute(CommandSender sender, String[] args){
        if(!(sender instanceof ProxiedPlayer)){
            return;
        }
        ProxiedPlayer pp = (ProxiedPlayer) sender;
        if(args.length == 0){
            pp.sendMessage(new TextComponent("§cFaire /msg [joueur] [message]"));
            return;
        }
        if(args.length > 1){
            String dest = args[0];
            if(ProxyServer.getInstance().getPlayer(dest) == null){
                pp.sendMessage(new TextComponent("§cLe joueur n'a pas été trouvé"));
            }else{
                ProxiedPlayer pd = ProxyServer.getInstance().getPlayer(dest);
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
        }
    }
}
