package braindustry.type;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Table;
import arc.util.Scaling;
import arc.util.Time;
import arc.util.Tmp;
import braindustry.gen.Stealthc;
import braindustry.gen.Stealthc;
import braindustry.gen.Stealthc;
import braindustry.graphics.ModPal;
import mindustry.ai.types.LogicAI;
import mindustry.content.Blocks;
import mindustry.entities.Leg;
import mindustry.entities.abilities.Ability;
import mindustry.gen.*;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.graphics.Trail;
import mindustry.type.UnitType;
import mindustry.ui.Bar;
//import mindustry.ui.Cicon;
import mindustry.world.blocks.environment.Floor;

import static mindustry.Vars.state;
import static mindustry.Vars.ui;

public class StealthUnitType extends ModUnitType {
    public float stealthCooldown=60.f;
    public float stealthDuration=60.f;
    public float minHealth=-1,maxHealth=-1;
    public float alpha=1f;
    public static final float shadowTX = -12.0F;
    public static final float shadowTY = -13.0F;
    public static final float outlineSpace = 0.01F;
    private static final Vec2 legOffset = new Vec2();

    public StealthUnitType(String name) {
        super(name);
    }

    @Override
    public void init() {
        super.init();
        if (minHealth==-1){
            minHealth=health*0.25f;
        }
        if (maxHealth==-1){
            maxHealth=health*0.90f;
        }
    }
    public void drawShadow(Unit unit) {
        Draw.color(Pal.shadow);
        drawAlpha(unit);
        float e = Math.max(unit.elevation, this.visualElevation);
        Draw.rect(this.shadowRegion, unit.x + -12.0F * e, unit.y + -13.0F * e, unit.rotation - 90.0F);
        Draw.color();
    }

    @Override
    public void drawOutline(Unit unit) {
        Draw.reset();
        applyColor(unit);
        if (Core.atlas.isFound(this.outlineRegion)) {
            Draw.rect(this.outlineRegion, unit.x, unit.y, unit.rotation - 90.0F);
        }
    }

    @Override
    public void display(Unit unit, Table table) {
        table.table(t -> {
            t.left();
            t.add(new Image(uiIcon)).size(8 * 4).scaling(Scaling.fit);
            t.labelWrap(localizedName).left().width(190f).padLeft(5);
        }).growX().left();
        table.row();

        table.table(bars -> {
            bars.defaults().growX().height(20f).pad(4);

            bars.add(new Bar("stat.health", Pal.health, unit::healthf).blink(Color.white));
            bars.row();
            if (unit instanceof Stealthc){
                bars.add(new Bar("stat.special_power", ModPal.stealthBarColor, ((Stealthc) unit)::stealthf));
                bars.row();
            }

            if(state.rules.unitAmmo){
                bars.add(new Bar(ammoType.icon() + " " + Core.bundle.get("stat.ammo"), ammoType.barColor(), () -> unit.ammo / ammoCapacity));
                bars.row();
            }

            for(Ability ability : unit.abilities){
                ability.displayBars(unit, bars);
            }

            if(unit instanceof Payloadc ){
                Payloadc payload=unit.as();
                bars.add(new Bar("stat.payloadcapacity", Pal.items, () -> payload.payloadUsed() / unit.type().payloadCapacity));
                bars.row();

                float[] count = new float[]{-1};
                bars.table().update(t -> {
                    if(count[0] != payload.payloadUsed()){
                        payload.contentInfo(t, 8 * 2, 270);
                        count[0] = payload.payloadUsed();
                    }
                }).growX().left().height(0f).pad(0f);
            }
        }).growX();

        if(unit.controller() instanceof LogicAI){
            table.row();
            table.add(Blocks.microProcessor.emoji() + " " + Core.bundle.get("units.processorcontrol")).growX().wrap().left();
            table.row();
            table.label(() -> Iconc.settings + " " + (long)unit.flag + "").color(Color.lightGray).growX().wrap().left();
        }

        table.row();
    }

    public <T extends Unit & Legsc> void drawLegs(T unit) {
        this.applyColor(unit);
        Leg[] legs = ((Legsc)unit).legs();
        float ssize = (float)this.footRegion.width * Draw.scl * 1.5F;
        float rotation = ((Legsc)unit).baseRotation();

        for (Leg leg : legs) {
            Drawf.shadow(leg.base.x, leg.base.y, ssize);
        }

        for(int j = legs.length - 1; j >= 0; --j) {
            int i = j % 2 == 0 ? j / 2 : legs.length - 1 - j / 2;
            Leg leg = legs[i];
            float angle = ((Legsc)unit).legAngle(rotation, i);
            boolean flip = (float)i >= (float)legs.length / 2.0F;
            int flips = Mathf.sign(flip);
            Vec2 position = legOffset.trns(angle, this.legBaseOffset).add(unit);
            Tmp.v1.set(leg.base).sub(leg.joint).inv().setLength(this.legExtension);
            if (leg.moving && this.visualElevation > 0.0F) {
                float scl = this.visualElevation;
                float elev = Mathf.slope(1.0F - leg.stage) * scl;
                Draw.color(Pal.shadow);
                Draw.rect(this.footRegion, leg.base.x + -12.0F * elev, leg.base.y + -13.0F * elev, position.angleTo(leg.base));
                Draw.color();
            }

            Draw.rect(this.footRegion, leg.base.x, leg.base.y, position.angleTo(leg.base));
            Lines.stroke((float)this.legRegion.height * Draw.scl * (float)flips);
            Lines.line(this.legRegion, position.x, position.y, leg.joint.x, leg.joint.y, false);
            Lines.stroke((float)this.legBaseRegion.height * Draw.scl * (float)flips);
            Lines.line(this.legBaseRegion, leg.joint.x + Tmp.v1.x, leg.joint.y + Tmp.v1.y, leg.base.x, leg.base.y, false);
            if (this.jointRegion.found()) {
                Draw.rect(this.jointRegion, leg.joint.x, leg.joint.y);
            }

            if (this.baseJointRegion.found()) {
                Draw.rect(this.baseJointRegion, position.x, position.y, rotation);
            }
        }

        if (this.baseRegion.found()) {
            Draw.rect(this.baseRegion, unit.x, unit.y, rotation - 90.0F);
        }

        Draw.reset();
    }
    public void drawMech(Mechc mech) {
        Unit unit = (Unit)mech;
        Draw.reset();
        float e = unit.elevation;
        float sin = Mathf.lerp(Mathf.sin(mech.walkExtend(true), 0.63661975F, 1.0F), 0.0F, e);
        float extension = Mathf.lerp(mech.walkExtend(false), 0.0F, e);
        float boostTrns = e * 2.0F;
        Floor floor = unit.isFlying() ? Blocks.air.asFloor() : unit.floorOn();
        if (floor.isLiquid) {
            Draw.color(Color.white, floor.mapColor, 0.5F);
        }

        int[] var8 = Mathf.signs;
        int var9 = var8.length;

        for (int i : var8) {
            Draw.mixcol(Tmp.c1.set(this.mechLegColor).lerp(Color.white, Mathf.clamp(unit.hitTime)), Math.max(Math.max(0.0F, (float) i * extension / this.mechStride), unit.hitTime));
            drawAlpha(unit);
            Draw.rect(this.legRegion, unit.x + Angles.trnsx(mech.baseRotation(), extension * (float) i - boostTrns, -boostTrns * (float) i), unit.y + Angles.trnsy(mech.baseRotation(), extension * (float) i - boostTrns, -boostTrns * (float) i), (float) (this.legRegion.width * i) * Draw.scl, (float) this.legRegion.height * Draw.scl - Math.max(-sin * (float) i, 0.0F) * (float) this.legRegion.height * 0.5F * Draw.scl, mech.baseRotation() - 90.0F + 35.0F * (float) i * e);
        }

        Draw.mixcol(Color.white, unit.hitTime);
        if (floor.isLiquid) {
            Draw.color(Color.white, floor.mapColor, unit.drownTime() * 0.4F);
        } else {
            Draw.color(Color.white);
        }
        drawAlpha(unit);
        Draw.rect(this.baseRegion, unit, mech.baseRotation() - 90.0F);
        Draw.mixcol();
    }
    public void drawEngine(Unit unit) {
        if (unit.isFlying()) {
            float scale = unit.elevation;
            float offset = this.engineOffset / 2.0F + this.engineOffset / 2.0F * scale;
            if (unit instanceof Trailc) {
                Trail trail = ((Trailc)unit).trail();
                trail.draw(unit.team.color, (this.engineSize + Mathf.absin(Time.time, 2.0F, this.engineSize / 4.0F) * scale) * this.trailScl);
            }

            Draw.color(unit.team.color);
            drawAlpha(unit);
            Fill.circle(unit.x + Angles.trnsx(unit.rotation + 180.0F, offset), unit.y + Angles.trnsy(unit.rotation + 180.0F, offset), (this.engineSize + Mathf.absin(Time.time, 2.0F, this.engineSize / 4.0F)) * scale);
            Draw.color(Color.white);
            Fill.circle(unit.x + Angles.trnsx(unit.rotation + 180.0F, offset - 1.0F), unit.y + Angles.trnsy(unit.rotation + 180.0F, offset - 1.0F), (this.engineSize + Mathf.absin(Time.time, 2.0F, this.engineSize / 4.0F)) / 2.0F * scale);
            Draw.color();
        }
    }
    public void drawControl(Unit unit) {
        Draw.z(58.0F);
        Draw.color(Pal.accent, Color.white, Mathf.absin(4.0F, 0.3F));
        drawAlpha(unit);
        Lines.poly(unit.x, unit.y, 4, unit.hitSize + 1.5F);
        Draw.reset();
    }

    @Override
    public void draw(Unit unit) {
        if (unit instanceof Stealthc)alpha=((Stealthc) unit).getAlpha();
        super.draw(unit);
    }

    public void drawAlpha(Unit unit){
        if (unit instanceof Stealthc){
            ((Stealthc)unit).drawAlpha();
        }
    }
    public void drawCell(Unit unit) {
        this.applyColor(unit);
        Draw.color(this.cellColor(unit));
        drawAlpha(unit);
        Draw.rect(this.cellRegion, unit.x, unit.y, unit.rotation - 90.0F);
        Draw.reset();
    }
    @Override
    public void applyColor(Unit unit) {

        super.applyColor(unit);
        if (unit instanceof Stealthc){
            ((Stealthc)unit).drawAlpha();
        }
    }
}
