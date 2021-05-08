package dansplugins.factionsystem.commands;

import dansplugins.factionsystem.ChunkManager;
import dansplugins.factionsystem.DynmapManager;
import dansplugins.factionsystem.LocaleManager;
import dansplugins.factionsystem.commands.abs.SubCommand;
import dansplugins.factionsystem.data.EphemeralData;
import dansplugins.factionsystem.data.PersistentData;
import dansplugins.factionsystem.events.FactionDisbandEvent;
import dansplugins.factionsystem.objects.Faction;
import dansplugins.factionsystem.utils.ArgumentParser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

public class DisbandCommand extends SubCommand {

    public DisbandCommand() {
        super(new String[]{
                "disband", LOCALE_PREFIX + "CmdDisband"
        }, false);
    }

    /**
     * Method to execute the command for a player.
     *
     * @param player who sent the command.
     * @param args   of the command.
     * @param key    of the sub-command (e.g. Ally).
     */
    @Override
    public void execute(Player player, String[] args, String key) {

    }

    /**
     * Method to execute the command.
     *
     * @param sender who sent the command.
     * @param args   of the command.
     * @param key    of the command.
     */
    @Override
    public void execute(CommandSender sender, String[] args, String key) {
        final Faction disband;
        final boolean self;
        if (args.length == 0) {
            if (!checkPermissions(sender, "mf.disband")) return;
            if (!(sender instanceof Player)) { // ONLY Players can be in a Faction
                sender.sendMessage(translate(getText("OnlyPlayersCanUseCommand")));
                return;
            }
            disband = getPlayerFaction(sender);
            self = true;
            if (disband.getPopulation() != 1) {
                sender.sendMessage(translate("&c" + getText("AlertMustKickAllPlayers")));
                return;
            }
        } else {
            if (!checkPermissions(sender, "mf.disband.others", "mf.admin")) return;
            disband = getFaction(String.join(" ", args));
            self = false;
        }
        if (disband == null) {
            sender.sendMessage(translate("&c" + getText("FactionNotFound")));
            return;
        }
        final int factionIndex = data.getFactions().indexOf(disband);
        if (self) {
            sender.sendMessage(translate("&a" + getText("FactionSuccessfullyDisbanded")));
            ephemeral.getPlayersInFactionChat().remove(((Player) sender).getUniqueId());
        }
        else sender.sendMessage(translate("&a" + getText("SuccessfulDisbandment", disband.getName())));
        removeFaction(factionIndex, self ? ((OfflinePlayer) sender) : null);
    }

    @Deprecated
    public boolean deleteFaction(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (sender.hasPermission("mf.disband")) {
                if (args.length > 1) {
                    if (player.hasPermission("mf.disband.others") || player.hasPermission("mf.admin")) {

                        String factionName = ArgumentParser.getInstance().createStringFromFirstArgOnwards(args);

                        for (int i = 0; i < PersistentData.getInstance().getFactions().size(); i++) {

                            if (PersistentData.getInstance().getFactions().get(i).getName().equalsIgnoreCase(factionName)) {

                                removeFaction(i, player);
                                player.sendMessage(ChatColor.GREEN + "" + String.format(LocaleManager.getInstance().getText("SuccessfulDisbandment"), factionName));
                                return true;

                            }

                        }
                        player.sendMessage(ChatColor.RED + LocaleManager.getInstance().getText("FactionNotFound"));
                        return false;
                    }
                    else {
                        sender.sendMessage(ChatColor.RED + String.format(LocaleManager.getInstance().getText("PermissionNeeded"), "mf.disband.others"));
                        return false;
                    }

                }

                boolean owner = false;
                for (int i = 0; i < PersistentData.getInstance().getFactions().size(); i++) {
                    if (PersistentData.getInstance().getFactions().get(i).isOwner(player.getUniqueId())) {
                        if (PersistentData.getInstance().getFactions().get(i).getPopulation() == 1) {
                            EphemeralData.getInstance().getPlayersInFactionChat().remove(player.getUniqueId());
                            removeFaction(i, player);
                            player.sendMessage(ChatColor.GREEN + LocaleManager.getInstance().getText("FactionSuccessfullyDisbanded"));
                            return true;
                        }
                        else {
                            player.sendMessage(ChatColor.RED + LocaleManager.getInstance().getText("AlertMustKickAllPlayers"));
                            return false;
                        }
                    }
                }

                if (!owner) {
                    player.sendMessage(ChatColor.RED + LocaleManager.getInstance().getText("AlertMustBeOwnerToUseCommand"));
                    return false;
                }
            }
            else {
                sender.sendMessage(ChatColor.RED + String.format(LocaleManager.getInstance().getText("PermissionNeeded"), "mf.disband"));
                return false;
            }
        }
        return false;
    }

    private void removeFaction(int i, OfflinePlayer disbandingPlayer) {

        Faction disbandingThisFaction = PersistentData.getInstance().getFactions().get(i);
        String nameOfFactionToRemove = disbandingThisFaction.getName();
        FactionDisbandEvent event = new FactionDisbandEvent(
                disbandingThisFaction,
                disbandingPlayer
        );
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            // TODO Add a message (maybe).
            return;
        }

        // remove claimed land objects associated with this faction
        ChunkManager.getInstance().removeAllClaimedChunks(nameOfFactionToRemove, PersistentData.getInstance().getClaimedChunks());
        DynmapManager.getInstance().updateClaims();

        // remove locks associated with this faction
        PersistentData.getInstance().removeAllLocks(PersistentData.getInstance().getFactions().get(i).getName());


        for (Faction faction : PersistentData.getInstance().getFactions()) {

            // remove records of alliances/wars associated with this faction
            if (faction.isAlly(nameOfFactionToRemove)) {
                faction.removeAlly(nameOfFactionToRemove);
            }
            if (faction.isEnemy(nameOfFactionToRemove)) {
                faction.removeEnemy(nameOfFactionToRemove);
            }

            // remove liege and vassal references associated with this faction
            if (faction.isLiege(nameOfFactionToRemove)) {
                faction.setLiege("none");
            }

            if (faction.isVassal(nameOfFactionToRemove)) {
                faction.removeVassal(nameOfFactionToRemove);
            }

        }

        PersistentData.getInstance().getFactions().remove(i);
    }

}
