package examplemod.examples;

import necesse.engine.GameEvents;
import necesse.engine.events.worldGeneration.GenerateIslandAnimalsEvent;
import necesse.engine.events.worldGeneration.GenerateIslandFloraEvent;
import necesse.engine.events.worldGeneration.GenerateIslandLayoutEvent;
import necesse.engine.events.worldGeneration.GenerateIslandStructuresEvent;
import necesse.engine.events.worldGeneration.GeneratedIslandAnimalsEvent;
import necesse.engine.events.worldGeneration.GeneratedIslandFloraEvent;
import necesse.engine.events.worldGeneration.GeneratedIslandLayoutEvent;
import necesse.engine.events.worldGeneration.GeneratedIslandStructuresEvent;
import necesse.engine.network.server.Server;
import necesse.engine.registries.ObjectRegistry;
import necesse.engine.registries.TileRegistry;
import necesse.engine.world.WorldEntity;
import necesse.entity.mobs.Mob;
import necesse.inventory.lootTable.LootItemInterface;
import necesse.inventory.lootTable.LootTable;
import necesse.inventory.lootTable.lootItem.ChanceLootItem;
import necesse.inventory.lootTable.lootItem.LootItemList;
import necesse.level.gameObject.GameObject;
import necesse.level.maps.biomes.Biome;
import necesse.level.maps.Level;
import necesse.level.maps.biomes.FishingLootTable;
import necesse.level.maps.biomes.FishingSpot;
import necesse.level.maps.generationModules.GenerationTools;
import necesse.level.maps.generationModules.IslandGeneration;
import necesse.level.maps.presets.RandomRuinsPreset;
public class ExampleBiome extends Biome {
    public static FishingLootTable forestSurfaceFish;
    public static LootItemInterface randomPortalDrop;
    public static LootItemInterface randomShadowGateDrop;

    public ExampleBiome() {
    }

    public Level getNewSurfaceLevel(int islandX, int islandY, float islandSize, Server server, WorldEntity worldEntity) {
        return new BiomeIslandLevel(islandX, islandY, islandSize, server, worldEntity);
    }

    public FishingLootTable getFishingLootTable(FishingSpot spot) {
        return spot.tile.level.getDimension() >= 0 ? forestSurfaceFish : super.getFishingLootTable(spot);
    }

    public LootTable getExtraMobDrops(Mob mob) {
        if (mob.isHostile && !mob.isBoss() && !mob.isSummoned) {
            if (mob.getLevel().getDimension() == -1) {
                return new LootTable(new LootItemInterface[]{randomPortalDrop, super.getExtraMobDrops(mob)});
            }

            if (mob.getLevel().getDimension() == -2) {
                return new LootTable(new LootItemInterface[]{randomShadowGateDrop, super.getExtraMobDrops(mob)});
            }
        }

        return super.getExtraMobDrops(mob);
    }

    static {
        forestSurfaceFish = (new FishingLootTable(defaultSurfaceFish)).addWater(120, "furfish");
        randomPortalDrop = new LootItemList(new LootItemInterface[]{new ChanceLootItem(0.01F, "mysteriousportal")});
        randomShadowGateDrop = new LootItemList(new LootItemInterface[]{new ChanceLootItem(0.004F, "shadowgate")});
    }

    private static class BiomeIslandLevel extends Level {
        public BiomeIslandLevel(int islandX, int islandY, float islandSize, Server server, WorldEntity worldEntity) {
            super(300, 300, islandX, islandY, 0, false, server, worldEntity);
            this.generateLevel(islandSize);
        }

        public void generateLevel(float islandSize) {
            int size = (int)(islandSize * 100.0F) + 20;
            IslandGeneration ig = new IslandGeneration(this, size);
            int waterTile = TileRegistry.getTileID("watertile");
            int sandTile = TileRegistry.getTileID("sandtile");
            int grassTile = TileRegistry.grassID;
            GameEvents.triggerEvent(new GenerateIslandLayoutEvent(this, islandSize, ig), (e) -> {
                ig.generateSimpleIsland(this.width / 2, this.height / 2, waterTile, grassTile, sandTile);
                ig.generateRiver(waterTile, grassTile, sandTile);
                ig.generateLakes(0.01F, waterTile, grassTile, sandTile);
                this.liquidManager.calculateHeights();
            });
            GameEvents.triggerEvent(new GeneratedIslandLayoutEvent(this, islandSize, ig));
            GameEvents.triggerEvent(new GenerateIslandFloraEvent(this, islandSize, ig), (e) -> {
                int oakTree = ObjectRegistry.getObjectID("oaktree");
                int spruceTree = ObjectRegistry.getObjectID("sprucetree");
                int grassObject = ObjectRegistry.getObjectID("grass");
                ig.generateCellMapObjects(0.35F, oakTree, grassTile, 0.08F);
                ig.generateCellMapObjects(0.35F, spruceTree, grassTile, 0.12F);
                ig.generateObjects(grassObject, grassTile, 0.4F);
                ig.generateObjects(ObjectRegistry.getObjectID("surfacerock"), -1, 0.001F, false);
                ig.generateObjects(ObjectRegistry.getObjectID("surfacerocksmall"), -1, 0.002F, false);
                ig.generateFruitGrowerSingle("appletree", 0.02F, new int[]{grassTile});
                ig.generateFruitGrowerVeins("blueberrybush", 0.04F, 8, 10, 0.1F, new int[]{grassTile});
                GenerationTools.generateRandomObjectVeinsOnTile(this, ig.random, 0.03F, 6, 12, grassTile, ObjectRegistry.getObjectID("wildfiremone"), 0.2F, false);
                GameObject waterPlant = ObjectRegistry.getObject(ObjectRegistry.getObjectID("watergrass"));
                GenerationTools.generateRandomVeins(this, ig.random, 0.15F, 12, 20, (level, tileX, tileY) -> {
                    if (ig.random.getChance(0.3F) && waterPlant.canPlace(level, tileX, tileY, 0) == null && level.liquidManager.isFreshWater(tileX, tileY)) {
                        waterPlant.placeObject(level, tileX, tileY, 0);
                    }

                });
            });
            GameEvents.triggerEvent(new GeneratedIslandFloraEvent(this, islandSize, ig));
            GameEvents.triggerEvent(new GenerateIslandStructuresEvent(this, islandSize, ig), (e) -> {
                GenerationTools.spawnRandomPreset(this, new RandomRuinsPreset(ig.random), false, false, ig.random, false, 40, 1);
            });
            GameEvents.triggerEvent(new GeneratedIslandStructuresEvent(this, islandSize, ig));
            GameEvents.triggerEvent(new GenerateIslandAnimalsEvent(this, islandSize, ig), (e) -> {
                ig.spawnMobHerds("sheep", ig.random.getIntBetween(25, 45), grassTile, 2, 6);
                ig.spawnMobHerds("cow", ig.random.getIntBetween(15, 35), grassTile, 2, 6);
            });
            GameEvents.triggerEvent(new GeneratedIslandAnimalsEvent(this, islandSize, ig));
            GenerationTools.checkValid(this);
        }
    }
}
