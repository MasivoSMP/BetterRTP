package me.SuperRonanCraft.BetterRTP.references.customEvents;

import org.bukkit.entity.Player;

import lombok.Getter;
import me.SuperRonanCraft.BetterRTP.player.rtp.RTPPlayer;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.worlds.RTPWorld;

//Called when an rtp is finding a valid location
@Getter public class RTP_FailedEvent extends RTPEvent {

    Player p;
    RTPWorld world;
    int attempts;

    public RTP_FailedEvent(RTPPlayer rtpPlayer) {
        this(rtpPlayer.getPlayer(), rtpPlayer.getWorldPlayer(), rtpPlayer.getAttempts());
    }

    public RTP_FailedEvent(Player player, RTPWorld world, int attempts) {
        this.p = player;
        this.world = world;
        this.attempts = attempts;
    }
}
