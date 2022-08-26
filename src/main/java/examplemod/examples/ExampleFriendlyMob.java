package examplemod.examples;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;
import necesse.engine.registries.MobRegistry.Textures;
import necesse.engine.tickManager.TickManager;
import necesse.entity.mobs.MobDrawable;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.ai.behaviourTree.BehaviourTreeAI;
import necesse.entity.mobs.ai.behaviourTree.leaves.PlayerFollowerAINode;
import necesse.entity.mobs.summon.summonFollowingMob.petFollowingMob.PetFollowingMob;
import necesse.gfx.camera.GameCamera;
import necesse.gfx.drawOptions.DrawOptions;
import necesse.gfx.drawOptions.texture.TextureDrawOptions;
import necesse.gfx.drawables.OrderableDrawables;
import necesse.gfx.gameTexture.GameTexture;
import necesse.level.maps.Level;
import necesse.level.maps.light.GameLight;

public class ExampleFriendlyMob extends PetFollowingMob {
    // Loaded in examplemod.ExampleMod.initResources()
    public static GameTexture texture;

    public ExampleFriendlyMob() {
        super(10);
        this.setSpeed(50.0F);
        this.setFriction(2.0F);
        this.moveAccuracy = 10;
        this.collision = new Rectangle(-10, -7, 20, 14);
        this.hitBox = new Rectangle(-12, -14, 24, 24);
        this.selectBox = new Rectangle(-16, -30, 32, 36);
    }

    public void init() {
        super.init();
        this.ai = new BehaviourTreeAI(this, new PlayerFollowerAINode(480, 32));
    }

    public void clientTick() {
        super.clientTick();
        this.getLevel().lightManager.refreshParticleLightFloat(this.x, this.y, 270.0F, 0.5F);
    }

    protected void playDeathSound() {
    }

    public void addDrawables(List<MobDrawable> list, OrderableDrawables tileList, OrderableDrawables topList, Level level, int x, int y, TickManager tickManager, GameCamera camera, PlayerMob perspective) {
        super.addDrawables(list, tileList, topList, level, x, y, tickManager, camera, perspective);
        GameLight light = level.getLightLevel(x / 32, y / 32);
        int drawX = camera.getDrawX(x) - 16;
        int drawY = camera.getDrawY(y) - 30;
        Point sprite = this.getAnimSprite(x, y, this.dir);
        drawY += this.getBobbing(x, y);
        drawY += this.getLevel().getTile(x / 32, y / 32).getMobSinkingAmount(this);
        final DrawOptions options = texture.initDraw().sprite(sprite.x, sprite.y, 32).light(light).pos(drawX, drawY);
        list.add(new MobDrawable() {
            public void draw(TickManager tickManager) {
                options.draw();
            }
        });
        this.addShadowDrawables(tileList, x, y, light, camera);
    }

    protected TextureDrawOptions getShadowDrawOptions(int x, int y, GameLight light, GameCamera camera) {
        GameTexture shadowTexture = Textures.human_baby_shadow;
        int res = shadowTexture.getHeight();
        int drawX = camera.getDrawX(x) - res / 2;
        int drawY = camera.getDrawY(y) - res / 2;
        drawY += this.getBobbing(x, y);
        return shadowTexture.initDraw().sprite(this.dir, 0, res).light(light).pos(drawX, drawY);
    }

    public int getRockSpeed() {
        return 10;
    }
}
