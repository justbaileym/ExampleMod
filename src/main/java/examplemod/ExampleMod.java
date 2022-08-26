package examplemod;

import examplemod.examples.*;
import necesse.engine.commands.CommandsManager;
import necesse.engine.modLoader.annotations.ModEntry;
import necesse.engine.registries.*;
import necesse.gfx.gameTexture.GameTexture;
import necesse.inventory.recipe.Ingredient;
import necesse.inventory.recipe.Recipe;
import necesse.inventory.recipe.Recipes;
import necesse.level.maps.biomes.Biome;

@ModEntry
public class ExampleMod {

    public void init() {
        System.out.println("Hello world from my example mod!");

        // Register our biomes
        BiomeRegistry.registerBiome("examplebiome", new ExampleBiome(), 1000, "forest");

        // Register our tiles
        TileRegistry.registerTile("exampletile", new ExampleTile(), 1, true);

        // Register our objects
        ObjectRegistry.registerObject("exampleobject", new ExampleObject(), 2, true);
        ObjectRegistry.registerObject("examplesummoningobject", new ExampleSummoningObject(), 2, true);

        // Register our items
        ItemRegistry.registerItem("exampleitem", new ExampleMaterialItem(), 10, true);
        ItemRegistry.registerItem("examplesword", new ExampleSwordItem(), 20, true);
        ItemRegistry.registerItem("examplestaff", new ExampleProjectileWeapon(), 30, true);

        // Register our mob
        MobRegistry.registerMob("examplemob", ExampleMob.class, true);
        MobRegistry.registerMob("examplefriendlymob", ExampleFriendlyMob.class, false);
        MobRegistry.registerMob("examplecrittermob", ExampleCritterMob.class, false);

        // Register our projectile
        ProjectileRegistry.registerProjectile("exampleprojectile", ExampleProjectile.class, "exampleprojectile", "exampleprojectile_shadow");

        // Register our buff
        BuffRegistry.registerBuff("examplebuff", new ExampleBuff());

        PacketRegistry.registerPacket(ExamplePacket.class);
    }

    public void initResources() {
        ExampleCritterMob.bodyTexture = GameTexture.fromFile("mobs/examplecrittermobbody");
        ExampleCritterMob.shadowTexture = GameTexture.fromFile("mobs/examplecrittermobshadow");
        ExampleFriendlyMob.texture = GameTexture.fromFile("mobs/examplefriendlymob");
        ExampleMob.texture = GameTexture.fromFile("mobs/examplemob");
    }

    public void postInit() {
        // Add recipes
        // Example item recipe, crafted in inventory for 2 iron bars
        Recipes.registerModRecipe(new Recipe(
                "exampleitem",
                1,
                RecipeTechRegistry.NONE,
                new Ingredient[]{
                        new Ingredient("ironbar", 2)
                }
        ).showAfter("woodboat")); // Show recipe after wood boat recipe
        // Example sword recipe, crafted in iron anvil using 4 example items and 5 copper bars
        Recipes.registerModRecipe(new Recipe(
                "examplesword",
                1,
                RecipeTechRegistry.IRON_ANVIL,
                new Ingredient[]{
                        new Ingredient("exampleitem", 4),
                        new Ingredient("copperbar", 5)
                }
        ));
        // Example staff recipe, crafted in workstation using 4 example items and 10 gold bars
        Recipes.registerModRecipe(new Recipe(
                "examplestaff",
                1,
                RecipeTechRegistry.WORKSTATION,
                new Ingredient[]{
                        new Ingredient("exampleitem", 4),
                        new Ingredient("goldbar", 10)
                }
        ).showAfter("exampleitem")); // Show the recipe after example item recipe

        // Add out example mob to default cave mobs.
        // Spawn tables use a ticket/weight system. In general, common mobs have about 100 tickets.
        Biome.defaultCaveMobs
                .add(100, "examplemob");

        Biome.defaultSurfaceCritters.add(100, "examplecrittermob");

        // Register our server chat command
        CommandsManager.registerServerCommand(new ExampleChatCommand());
    }

}
