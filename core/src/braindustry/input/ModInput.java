package braindustry.input;

import arc.Core;
import arc.Events;
import arc.util.Nullable;
import braindustry.annotations.BDAnnotations;
import braindustry.entities.ModUnits;
import braindustry.gen.BDCall;
import braindustry.gen.Stealthc;
import mindustry.annotations.Annotations;
import mindustry.game.EventType;
import mindustry.gen.Building;
import mindustry.gen.Payloadc;
import mindustry.gen.Player;
import mindustry.gen.Unit;
import mindustry.net.Administration;
import mindustry.net.ValidateException;
import mma.annotations.ModAnnotations;

import static mindustry.Vars.net;
import static mindustry.Vars.netServer;

public class ModInput {
    @ModAnnotations.Remote(targets = Annotations.Loc.both, called = Annotations.Loc.both, forward = true)
    public static void tileConfig(Player player, Building build, @Nullable Object value) {
//        Log.info("tileConfig(player: @,build: @,value: @)",player,build,value);
        if (build == null) return;
        if (net.server() && (!ModUnits.canInteract(player, build) ||
                             !netServer.admins.allowAction(player, Administration.ActionType.configure, build.tile, action -> action.config = value)))
            throw new ValidateException(player, "Player cannot configure a tile.");
        build.configured(player == null || player.dead() ? null : player.unit(), value);
        Core.app.post(() -> Events.fire(new EventType.ConfigEvent(build, player, value)));
    }

    @ModAnnotations.Remote(targets = Annotations.Loc.both, called = Annotations.Loc.server)
    public static void requestUnitPayload(Player player, Unit target) {
        if (player == null || target instanceof Stealthc) return;

        Unit unit = player.unit();
        Payloadc pay = (Payloadc) unit;

        if (target.isAI() && target.isGrounded() && pay.canPickup(target)
                && target.within(unit, unit.type.hitSize * 2f + target.type.hitSize * 2f)) {
            BDCall.pickedUnitPayload(unit, target);
        }
    }

    @ModAnnotations.Remote(targets = Annotations.Loc.server, called = Annotations.Loc.server)
    public static void pickedUnitPayload(Unit unit, Unit target) {
        if (target != null && unit instanceof Payloadc && !(target instanceof Stealthc)) {
            ((Payloadc) unit).pickup(target);
        } else if (target != null && !(target instanceof Stealthc)) {
            target.remove();
        }
    }

}
