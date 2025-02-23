package braindustry.world.blocks.logic;

import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.struct.Seq;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import braindustry.content.ModFx;
import braindustry.gen.ModBuilding;
import braindustry.graphics.Drawm;
import braindustry.world.ModBlock;
import braindustry.world.blocks.DebugBlock;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.game.EventType;
import mindustry.gen.Building;
import mindustry.gen.Drawc;
import mindustry.gen.Groups;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.logic.LAccess;
import mindustry.logic.Ranged;
import mindustry.world.Tile;

import static arc.util.Tmp.v1;
import static mindustry.Vars.tilesize;

public class LaserRuler extends ModBlock implements DebugBlock {
    protected static final Seq<Runnable> drawRunners = new Seq<>();
    static Tile lastTaped;

    static {
        Events.on(EventType.TapEvent.class, e -> {
            lastTaped = e.tile;
            Building selectedTile = Vars.control.input.frag.config.getSelectedTile();
            if (selectedTile instanceof LaserRulerBuild) {
                LaserRulerBuild build = (LaserRulerBuild) selectedTile;
                if (build.tile == lastTaped) {
                    build.configure(null);
                    build.deselect();
                } else {
                    build.setTarget(lastTaped.pos());
                }
            }
        });
    }

    boolean laserRuler = false;

    public LaserRuler(String name) {
        super(name);
        update = true;
        this.destructible = true;
        configurable = true;
        this.<Integer, LaserRulerBuild>config(Integer.class, (build, i) -> {
            build.target = i;
        });
        this.<Point2, LaserRulerBuild>config(Point2.class, (tile, i) -> tile.target = Point2.pack(i.x + tile.tileX(), i.y + tile.tileY()));
        this.<LaserRulerBuild>configClear(build -> build.target = -1);
        size = 1;
    }

    public class LaserRulerBuild extends ModBuilding implements Ranged, Drawc {
        public final Seq<Tile> xtiles = new Seq<>();
        public final Seq<Tile> ytiles = new Seq<>();
        public int target = -1;

        @Override
        public Point2 config() {
            return Point2.unpack(target).sub(tile.x, tile.y);
        }

        @Override
        public void update() {
            super.update();
            if (!validTarget(target)) return;
            rebuildTiles();


        }

        public void rebuildTiles() {
            Tile target = targetTile();
            xtiles.clear();
            Vars.world.raycast(tile.x, tile.y, target.x, tile.y, (x, y) -> {
                xtiles.add(Vars.world.tile(x, y));
                return false;
            });
            ytiles.clear();
            Vars.world.raycast(target.x, tile.y, target.x, target.y, (x, y) -> {
                ytiles.add(Vars.world.tile(x, y));
                return false;
            });
        }

        protected void drawSelectedTile(Tile tile, Color color) {
            Drawf.select(tile.worldx(), tile.worldy(), tilesize / 2f, color);
//            Draw.color(color);
//            Lines.stroke(stroke);
//            float half = tilesize / 2f;
//            ModLines.rect(tile.worldx() - half, tile.worldy() - half, tilesize, tilesize);
        }

        protected void drawLinePart(Tile cur, Tile next, Color color) {
            drawLinePart(cur, next, color, color);
        }

        protected void drawLinePart(Tile cur, Tile next, Color color, Color divColor) {
            float x1 = cur.worldx(), y1 = cur.worldy();
            float x2 = next.worldx(), y2 = next.worldy();
            float rotation = Angles.angle(x1, y1, x2, y2);
            v1.trns(rotation + 90, 2.0f);
            float z = Draw.z();
//            Draw.z(z-0.01f);
            Draw.color(Pal.gray);
            Lines.stroke(3);
            Lines.line(x1, y1, x2, y2, false);
            Lines.line(x1 + v1.x, y1 + v1.y, x1 - v1.x, y1 - v1.y);
            Lines.line(x2 + v1.x, y2 + v1.y, x2 - v1.x, y2 - v1.y);
//            Draw.z(z);
            drawRunners.add(() -> {
                Draw.color(color);
                Lines.stroke(1f);
                Lines.line(x1, y1, x2, y2, false);
                v1.trns(rotation + 90, 2.0f);
                Draw.color(divColor);
                Lines.line(x1 + v1.x, y1 + v1.y, x1 - v1.x, y1 - v1.y);
                Lines.line(x2 + v1.x, y2 + v1.y, x2 - v1.x, y2 - v1.y);
            });
        }

        @Override
        public void add() {
            boolean preAdded = this.added;
            super.add();
            if (preAdded != added) {
                Groups.draw.add(this);
            }
        }

        @Override
        public void remove() {
            boolean preAdded = this.added;
            super.remove();
            if (preAdded != added) {
                Groups.draw.remove(this);
            }
        }

        @Override
        public float clipSize() {
            return dstToTarget() * 2;
        }

        @Override
        public void draw() {
            super.draw();
            if (!validTarget(target)) return;

            Tile target = targetTile();
            Color tenColor = Color.red.cpy().lerp(Color.white, 0.5f);
            Color color = Color.valueOf("877bad");//BF92F8 8A73C6 665C9F
            drawSelectedTile(target, color);
            drawRunners.clear();
            int counter = 0;
            for (int i = 0; i < xtiles.size - 1; i++) {
                Tile cur = xtiles.get(i), next = xtiles.get(i + 1);
                if (cur == null || next == null) continue;
                drawLinePart(cur, next, color, (counter++ + 10) % 10 == 0 ? tenColor : color);

            }
            for (int i = 0; i < ytiles.size - 1; i++) {
                Tile cur = ytiles.get(i), next = ytiles.get(i + 1);
                if (cur == null || next == null) continue;
                drawLinePart(cur, next, color, (counter++ + 10) % 10 == 0 ? tenColor : color);
            }
            drawRunners.each(Runnable::run);
            drawRunners.clear();
            Draw.draw(Layer.flyingUnit + 4, () -> {
                Tile targetTile = targetTile();
                Tmp.v1.trns(tile.angleTo(targetTile), size * tilesize);
                Drawm.drawLabel(x + Tmp.v1.x, y + Tmp.v1.y, Pal.heal, "" + dstTileToTarget());
                drawTiles(xtiles);
                drawTiles(ytiles);
            });

        }

        protected void drawTiles(Seq<Tile> tiles) {
            if (tiles.size > 2) {
                Tile tile = tiles.getFrac(0.5f);
                Drawm.drawLabel(tile.worldx(), tile.worldy(), Pal.heal, "" + (tiles.size - 2));
            }
        }

        public double sense(LAccess sensor) {
            Tile tile = targetTile();
            return switch (sensor) {
                case shootX -> tile == null ? -1 : tile.x;
                case shootY -> tile == null ? -1 : tile.y;
                case shooting -> tile != null ? 1 : 0;
                default -> super.sense(sensor);
            };

        }

        private Tile targetTile() {
            return validTarget(target) ? Vars.world.tile(target) : null;
        }

        @Override
        public Object senseObject(LAccess sensor) {
            return super.senseObject(sensor);
        }

        @Override
        public boolean onConfigureTileTapped(Building other) {
            return lastTaped == tile;
        }

        @Override
        public float range() {
            return dstToTarget();
        }

        private int dstTileToTarget() {
            if (!validTarget(target)) return -1;
            return (int) (dstToTarget() / tilesize);
        }

        private boolean validTarget(int pos) {
            return pos != -1 && Vars.world.tile(pos) != null &&
                   (Vars.world.tile(pos).x == tile.x || Vars.world.tile(pos).y == tile.y || true);
        }

        private float dstToTarget() {
            if (!validTarget(target)) return -1;
            Tile target = Vars.world.tile(this.target);
            return Mathf.dst(target.worldx(), target.worldy(), tile.worldx(), tile.worldy()) - 8f;
        }

        public void setTarget(int target) {
            if (validTarget(target)) {
                this.target = target;
                rebuildTiles();
                deselect();
            } else {
                Fx.unitCapKill.at(Vars.world.tile(target));

            }
            ModFx.selectTile.at(Vars.world.tile(target));
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.i(target);
        }

        @Override
        public byte version() {
            return 1;
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            if (revision == 0) return;
            target = read.i();
        }
    }
}
