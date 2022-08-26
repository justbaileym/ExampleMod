package examplemod.examples;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;
import necesse.engine.registries.TileRegistry;
import necesse.engine.registries.MobRegistry.Textures;
import necesse.engine.tickManager.TickManager;
import necesse.entity.mobs.MobDrawable;
import necesse.entity.mobs.MobSpawnLocation;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.ai.behaviourTree.BehaviourTreeAI;
import necesse.entity.mobs.ai.behaviourTree.trees.CritterAI;
import necesse.entity.mobs.friendly.critters.CritterMob;
import necesse.entity.particle.FleshParticle;
import necesse.entity.particle.Particle.GType;
import necesse.gfx.camera.GameCamera;
import necesse.gfx.drawOptions.DrawOptions;
import necesse.gfx.drawOptions.texture.TextureDrawOptions;
import necesse.gfx.drawables.OrderableDrawables;
import necesse.gfx.gameTexture.GameTexture;
import necesse.level.maps.Level;
import necesse.level.maps.TilePosition;
import necesse.level.maps.light.GameLight;

public class ExampleCritterMob extends CritterMob {
    // Loaded in examplemod.ExampleMod.initResources()
    public static GameTexture bodyTexture;
    public static GameTexture shadowTexture;

    public ExampleCritterMob() {
        this.setSpeed(15.0F);
        this.setFriction(3.0F);
        this.setSwimSpeed(1.0F);
        this.collision = new Rectangle(-10, -7, 20, 14);
        this.hitBox = new Rectangle(-12, -14, 24, 24);
        this.selectBox = new Rectangle(-16, -28, 32, 34);
    }

    public void init() {
        super.init();
        this.ai = new BehaviourTreeAI(this, new CritterAI());
    }

    public int getTileWanderPriority(TilePosition pos) {
        if (pos.tileID() == TileRegistry.waterID) {
            return 1000;
        } else {
            int height = pos.level.liquidManager.getHeight(pos.tileX, pos.tileY);
            return height >= 0 && height <= 3 ? 1000 : super.getTileWanderPriority(pos);
        }
    }

    public void spawnDeathParticles(float knockbackX, float knockbackY) {
        for(int i = 0; i < 5; ++i) {
            this.getLevel().entityManager.addParticle(new FleshParticle(this.getLevel(), Textures.duck.body, i, 8, 32, this.x, this.y, 20.0F, knockbackX, knockbackY), GType.IMPORTANT_COSMETIC);
        }

    }

    public void addDrawables(List<MobDrawable> list, OrderableDrawables tileList, OrderableDrawables topList, Level level, int x, int y, TickManager tickManager, GameCamera camera, PlayerMob perspective) {
        super.addDrawables(list, tileList, topList, level, x, y, tickManager, camera, perspective);
        GameLight light = level.getLightLevel(x / 32, y / 32);
        int drawX = camera.getDrawX(x) - 30;
        int drawY = camera.getDrawY(y) - 48;
        Point sprite = this.getAnimSprite(x, y, this.dir);
        drawY += this.getBobbing(x, y);
        drawY += this.getLevel().getTile(x / 32, y / 32).getMobSinkingAmount(this);
        if (this.inLiquid(x, y)) {
            drawY -= 12;
        }

        final DrawOptions options = bodyTexture.initDraw().sprite(sprite.x, sprite.y, 64).light(light).pos(drawX, drawY);
        list.add(new MobDrawable() {
            public void draw(TickManager tickManager) {
                options.draw();
            }
        });
        TextureDrawOptions shadow = shadowTexture.initDraw().sprite(0, this.dir, 64).light(light).pos(drawX, drawY);
        tileList.add((tm) -> {
            shadow.draw();
        });
    }

    protected int getRockSpeed() {
        return 8;
    }

    public MobSpawnLocation checkSpawnLocation(MobSpawnLocation location) {
        return location.checkNotLevelCollides().checkTile((tileX, tileY) -> {
            int tileID = this.getLevel().getTileID(tileX, tileY);
            if (tileID == TileRegistry.waterID) {
                return true;
            } else {
                int height = this.getLevel().liquidManager.getHeight(tileX, tileY);
                return height >= 0 && height <= 3;
            }
        });
    }
}
