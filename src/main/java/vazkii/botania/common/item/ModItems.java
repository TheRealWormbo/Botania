/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 * File Created @ [Jan 14, 2014, 5:17:47 PM (GMT)]
 */
package vazkii.botania.common.item;

import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemRecord;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistry;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.common.item.brew.ItemBrewFlask;
import vazkii.botania.common.item.brew.ItemBrewVial;
import vazkii.botania.common.item.brew.ItemIncenseStick;
import vazkii.botania.common.item.brew.ItemVial;
import vazkii.botania.common.item.equipment.armor.elementium.ItemElementiumBoots;
import vazkii.botania.common.item.equipment.armor.elementium.ItemElementiumChest;
import vazkii.botania.common.item.equipment.armor.elementium.ItemElementiumHelm;
import vazkii.botania.common.item.equipment.armor.elementium.ItemElementiumLegs;
import vazkii.botania.common.item.equipment.armor.manasteel.ItemManasteelBoots;
import vazkii.botania.common.item.equipment.armor.manasteel.ItemManasteelChest;
import vazkii.botania.common.item.equipment.armor.manasteel.ItemManasteelHelm;
import vazkii.botania.common.item.equipment.armor.manasteel.ItemManasteelLegs;
import vazkii.botania.common.item.equipment.armor.manaweave.ItemManaweaveBoots;
import vazkii.botania.common.item.equipment.armor.manaweave.ItemManaweaveChest;
import vazkii.botania.common.item.equipment.armor.manaweave.ItemManaweaveHelm;
import vazkii.botania.common.item.equipment.armor.manaweave.ItemManaweaveLegs;
import vazkii.botania.common.item.equipment.armor.terrasteel.ItemTerrasteelBoots;
import vazkii.botania.common.item.equipment.armor.terrasteel.ItemTerrasteelChest;
import vazkii.botania.common.item.equipment.armor.terrasteel.ItemTerrasteelHelm;
import vazkii.botania.common.item.equipment.armor.terrasteel.ItemTerrasteelLegs;
import vazkii.botania.common.item.equipment.bauble.*;
import vazkii.botania.common.item.equipment.tool.ItemEnderDagger;
import vazkii.botania.common.item.equipment.tool.ItemGlassPick;
import vazkii.botania.common.item.equipment.tool.ItemStarSword;
import vazkii.botania.common.item.equipment.tool.ItemThunderSword;
import vazkii.botania.common.item.equipment.tool.bow.ItemCrystalBow;
import vazkii.botania.common.item.equipment.tool.bow.ItemLivingwoodBow;
import vazkii.botania.common.item.equipment.tool.elementium.ItemElementiumAxe;
import vazkii.botania.common.item.equipment.tool.elementium.ItemElementiumPick;
import vazkii.botania.common.item.equipment.tool.elementium.ItemElementiumShears;
import vazkii.botania.common.item.equipment.tool.elementium.ItemElementiumShovel;
import vazkii.botania.common.item.equipment.tool.elementium.ItemElementiumSword;
import vazkii.botania.common.item.equipment.tool.manasteel.ItemManasteelAxe;
import vazkii.botania.common.item.equipment.tool.manasteel.ItemManasteelPick;
import vazkii.botania.common.item.equipment.tool.manasteel.ItemManasteelShears;
import vazkii.botania.common.item.equipment.tool.manasteel.ItemManasteelShovel;
import vazkii.botania.common.item.equipment.tool.manasteel.ItemManasteelSword;
import vazkii.botania.common.item.equipment.tool.terrasteel.ItemTerraAxe;
import vazkii.botania.common.item.equipment.tool.terrasteel.ItemTerraPick;
import vazkii.botania.common.item.equipment.tool.terrasteel.ItemTerraSword;
import vazkii.botania.common.item.interaction.thaumcraft.ItemElementiumHelmRevealing;
import vazkii.botania.common.item.interaction.thaumcraft.ItemManaInkwell;
import vazkii.botania.common.item.interaction.thaumcraft.ItemManasteelHelmRevealing;
import vazkii.botania.common.item.interaction.thaumcraft.ItemTerrasteelHelmRevealing;
import vazkii.botania.common.item.lens.*;
import vazkii.botania.common.item.material.ItemDye;
import vazkii.botania.common.item.material.ItemManaResource;
import vazkii.botania.common.item.material.ItemPestleAndMortar;
import vazkii.botania.common.item.material.ItemPetal;
import vazkii.botania.common.item.material.ItemRune;
import vazkii.botania.common.item.record.ItemRecordGaia1;
import vazkii.botania.common.item.record.ItemRecordGaia2;
import vazkii.botania.common.item.relic.ItemDice;
import vazkii.botania.common.item.relic.ItemFlugelEye;
import vazkii.botania.common.item.relic.ItemInfiniteFruit;
import vazkii.botania.common.item.relic.ItemKingKey;
import vazkii.botania.common.item.relic.ItemLokiRing;
import vazkii.botania.common.item.relic.ItemOdinRing;
import vazkii.botania.common.item.relic.ItemThorRing;
import vazkii.botania.common.item.rod.ItemCobbleRod;
import vazkii.botania.common.item.rod.ItemDirtRod;
import vazkii.botania.common.item.rod.ItemDiviningRod;
import vazkii.botania.common.item.rod.ItemExchangeRod;
import vazkii.botania.common.item.rod.ItemFireRod;
import vazkii.botania.common.item.rod.ItemGravityRod;
import vazkii.botania.common.item.rod.ItemMissileRod;
import vazkii.botania.common.item.rod.ItemRainbowRod;
import vazkii.botania.common.item.rod.ItemSkyDirtRod;
import vazkii.botania.common.item.rod.ItemSmeltRod;
import vazkii.botania.common.item.rod.ItemTerraformRod;
import vazkii.botania.common.item.rod.ItemTornadoRod;
import vazkii.botania.common.item.rod.ItemWaterRod;
import vazkii.botania.common.lib.LibItemNames;
import vazkii.botania.common.lib.LibMisc;
import vazkii.botania.common.lib.LibOreDict;

import java.util.EnumMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = LibMisc.MOD_ID)
public final class ModItems {
	public static final Item lexicon = new ItemLexicon();
	public static final Map<EnumDyeColor, Item> petals = new EnumMap<>(EnumDyeColor.class);
	public static final Map<EnumDyeColor, Item> dyes = new EnumMap<>(EnumDyeColor.class);
	static {
		for(EnumDyeColor color : EnumDyeColor.values()) {
			petals.put(color, new ItemPetal(color));
			dyes.put(color, new ItemDye(color));
		}
	}

	public static final Item pestleAndMortar = new ItemPestleAndMortar();
	public static final Item twigWand = new ItemTwigWand();
	
	public static final Item manaSteel = new ItemManaResource(LibItemNames.MANA_RESOURCE_NAMES[0]);
	public static final Item manaPearl = new ItemManaResource(LibItemNames.MANA_RESOURCE_NAMES[1]);
	public static final Item manaDiamond = new ItemManaResource(LibItemNames.MANA_RESOURCE_NAMES[2]);
	public static final Item livingwoodTwig = new ItemManaResource(LibItemNames.MANA_RESOURCE_NAMES[3]);
	public static final Item terrasteel = new ItemManaResource(LibItemNames.MANA_RESOURCE_NAMES[4]);
	public static final Item lifeEssence = new ItemManaResource(LibItemNames.MANA_RESOURCE_NAMES[5]);
	public static final Item redstoneRoot = new ItemManaResource(LibItemNames.MANA_RESOURCE_NAMES[6]);
	public static final Item elementium = new ItemManaResource(LibItemNames.MANA_RESOURCE_NAMES[7]);
	public static final Item pixieDust = new ItemManaResource(LibItemNames.MANA_RESOURCE_NAMES[8]);
	public static final Item dragonstone = new ItemManaResource(LibItemNames.MANA_RESOURCE_NAMES[9]);
	public static final Item placeholder = new ItemManaResource(LibItemNames.MANA_RESOURCE_NAMES[11]);
	public static final Item redString = new ItemManaResource(LibItemNames.MANA_RESOURCE_NAMES[12]);
	public static final Item dreamwoodTwig = new ItemManaResource(LibItemNames.MANA_RESOURCE_NAMES[13]);
	public static final Item gaiaIngot = new ItemManaResource(LibItemNames.MANA_RESOURCE_NAMES[14]);
	public static final Item enderAirBottle = new ItemManaResource(LibItemNames.MANA_RESOURCE_NAMES[15]);
	public static final Item manaString = new ItemManaResource(LibItemNames.MANA_RESOURCE_NAMES[16]);
	public static final Item manasteelNugget = new ItemManaResource(LibItemNames.MANA_RESOURCE_NAMES[17]);
	public static final Item terrasteelNugget = new ItemManaResource(LibItemNames.MANA_RESOURCE_NAMES[18]);
	public static final Item elementiumNugget = new ItemManaResource(LibItemNames.MANA_RESOURCE_NAMES[19]);
	public static final Item livingroot = new ItemManaResource(LibItemNames.MANA_RESOURCE_NAMES[20]);
	public static final Item pebble = new ItemManaResource(LibItemNames.MANA_RESOURCE_NAMES[21]);
	public static final Item manaweaveCloth = new ItemManaResource(LibItemNames.MANA_RESOURCE_NAMES[22]);
	public static final Item manaPowder = new ItemManaResource(LibItemNames.MANA_RESOURCE_NAMES[23]);
	
	public static final Item lensNormal = new ItemLens(LibItemNames.LENS_NAMES[0], new Lens(), ItemLens.PROP_NONE);
	public static final Item lensSpeed = new ItemLens(LibItemNames.LENS_NAMES[1], new LensSpeed(), ItemLens.PROP_NONE);
	public static final Item lensPower = new ItemLens(LibItemNames.LENS_NAMES[2], new LensPower(), ItemLens.PROP_POWER);
	public static final Item lensTime = new ItemLens(LibItemNames.LENS_NAMES[3], new LensTime(), ItemLens.PROP_NONE);
	public static final Item lensEfficiency = new ItemLens(LibItemNames.LENS_NAMES[4], new LensEfficiency(), ItemLens.PROP_NONE);
	public static final Item lensBounce = new ItemLens(LibItemNames.LENS_NAMES[5], new LensBounce(), ItemLens.PROP_TOUCH);
	public static final Item lensGravity = new ItemLens(LibItemNames.LENS_NAMES[6], new LensGravity(), ItemLens.PROP_ORIENTATION);
	public static final Item lensMine = new ItemLens(LibItemNames.LENS_NAMES[7], new LensMine(), ItemLens.PROP_TOUCH | ItemLens.PROP_ORIENTATION);
	public static final Item lensDamage = new ItemLens(LibItemNames.LENS_NAMES[8], new LensDamage(), ItemLens.PROP_DAMAGE);
	public static final Item lensPhantom = new ItemLens(LibItemNames.LENS_NAMES[9], new LensPhantom(), ItemLens.PROP_TOUCH);
	public static final Item lensMagnet = new ItemLens(LibItemNames.LENS_NAMES[10], new LensMagnet(), ItemLens.PROP_ORIENTATION);
	public static final Item lensExplosive = new ItemLens(LibItemNames.LENS_NAMES[11], new LensExplosive(), ItemLens.PROP_DAMAGE | ItemLens.PROP_TOUCH | ItemLens.PROP_INTERACTION);
	public static final Item lensInfluence = new ItemLens(LibItemNames.LENS_NAMES[12], new LensInfluence(), ItemLens.PROP_NONE);
	public static final Item lensWeight = new ItemLens(LibItemNames.LENS_NAMES[13], new LensWeight(), ItemLens.PROP_TOUCH | ItemLens.PROP_INTERACTION);
	public static final Item lensPaint = new ItemLens(LibItemNames.LENS_NAMES[14], new LensPaint(), ItemLens.PROP_TOUCH | ItemLens.PROP_INTERACTION);
	public static final Item lensFire = new ItemLens(LibItemNames.LENS_NAMES[15], new LensFire(), ItemLens.PROP_DAMAGE | ItemLens.PROP_TOUCH | ItemLens.PROP_INTERACTION);
	public static final Item lensPiston = new ItemLens(LibItemNames.LENS_NAMES[16], new LensPiston(), ItemLens.PROP_TOUCH | ItemLens.PROP_INTERACTION);
	public static final Item lensLight = new ItemLens(LibItemNames.LENS_NAMES[17], new LensLight(), ItemLens.PROP_TOUCH | ItemLens.PROP_INTERACTION);
	public static final Item lensWarp = new ItemLens(LibItemNames.LENS_NAMES[18], new LensWarp(), ItemLens.PROP_NONE);
	public static final Item lensRedirect = new ItemLens(LibItemNames.LENS_NAMES[19], new LensRedirect(), ItemLens.PROP_TOUCH | ItemLens.PROP_INTERACTION);
	public static final Item lensFirework = new ItemLens(LibItemNames.LENS_NAMES[20], new LensFirework(), ItemLens.PROP_TOUCH);
	public static final Item lensFlare = new ItemLens(LibItemNames.LENS_NAMES[21], new LensFlare(), ItemLens.PROP_CONTROL);
	public static final Item lensMessenger = new ItemLens(LibItemNames.LENS_NAMES[22], new LensMessenger(), ItemLens.PROP_POWER);
	public static final Item lensTripwire = new ItemLens(LibItemNames.LENS_NAMES[23], new LensTripwire(), ItemLens.PROP_CONTROL);
	public static final Item lensStorm = new ItemLens(LibItemNames.LENS_NAMES[24], new LensStorm(), ItemLens.PROP_NONE);
	
	public static final Item rune = new ItemRune();
	public static final Item manaTablet = new ItemManaTablet();
	public static final Item manaGun = new ItemManaGun();
	public static final Item manaCookie = new ItemManaCookie();
	public static final Item fertilizer = new ItemFertilizer();
	public static final Item grassSeeds = new ItemGrassSeeds();
	public static final Item dirtRod = new ItemDirtRod();
	public static final Item terraformRod = new ItemTerraformRod();
	public static final Item grassHorn = new ItemHorn(LibItemNames.GRASS_HORN);
	public static final Item leavesHorn = new ItemHorn(LibItemNames.LEAVES_HORN);
	public static final Item snowHorn = new ItemHorn(LibItemNames.SNOW_HORN);
	public static final Item manaMirror = new ItemManaMirror();
	public static final Item manasteelHelm = new ItemManasteelHelm();
	public static final Item manasteelHelmRevealing = new ItemManasteelHelmRevealing();
	public static final Item manasteelChest = new ItemManasteelChest();
	public static final Item manasteelLegs = new ItemManasteelLegs();
	public static final Item manasteelBoots = new ItemManasteelBoots();
	public static final Item manasteelPick = new ItemManasteelPick();
	public static final Item manasteelShovel = new ItemManasteelShovel();
	public static final Item manasteelAxe = new ItemManasteelAxe();
	public static final Item manasteelSword = new ItemManasteelSword();
	public static final Item manasteelShears = new ItemManasteelShears();
	public static final Item terrasteelHelm = new ItemTerrasteelHelm();
	public static final Item terrasteelHelmRevealing = new ItemTerrasteelHelmRevealing();
	public static final Item terrasteelChest = new ItemTerrasteelChest();
	public static final Item terrasteelLegs = new ItemTerrasteelLegs();
	public static final Item terrasteelBoots = new ItemTerrasteelBoots();
	public static final Item terraSword = new ItemTerraSword();
	public static final Item tinyPlanet = new ItemTinyPlanet();
	public static final Item manaRing = new ItemManaRing();
	public static final Item auraRing = new ItemAuraRing();
	public static final Item manaRingGreater = new ItemGreaterManaRing();
	public static final Item auraRingGreater = new ItemGreaterAuraRing();
	public static final Item travelBelt = new ItemTravelBelt();
	public static final Item knockbackBelt = new ItemKnockbackBelt();
	public static final Item icePendant = new ItemIcePendant();
	public static final Item lavaPendant = new ItemLavaPendant();
	public static final Item magnetRing = new ItemMagnetRing();
	public static final Item waterRing = new ItemWaterRing();
	public static final Item miningRing = new ItemMiningRing();
	public static final Item terraPick = new ItemTerraPick();
	public static final Item divaCharm = new ItemDivaCharm();
	public static final Item flightTiara = new ItemFlightTiara();
	public static final Item enderDagger = new ItemEnderDagger();
	public static final Item darkQuartz = new ItemMod(LibItemNames.QUARTZ_NAMES[0]);
	public static final Item manaQuartz = new ItemMod(LibItemNames.QUARTZ_NAMES[1]);
	public static final Item blazeQuartz = new ItemMod(LibItemNames.QUARTZ_NAMES[2]);
	public static final Item lavenderQuartz = new ItemMod(LibItemNames.QUARTZ_NAMES[3]);
	public static final Item redQuartz = new ItemMod(LibItemNames.QUARTZ_NAMES[4]);
	public static final Item elfQuartz = new ItemElven(LibItemNames.QUARTZ_NAMES[5]);
	public static final Item sunnyQuartz = new ItemMod(LibItemNames.QUARTZ_NAMES[6]);
	public static final Item waterRod = new ItemWaterRod();
	public static final Item elementiumHelm = new ItemElementiumHelm();
	public static final Item elementiumHelmRevealing = new ItemElementiumHelmRevealing();
	public static final Item elementiumChest = new ItemElementiumChest();
	public static final Item elementiumLegs = new ItemElementiumLegs();
	public static final Item elementiumBoots = new ItemElementiumBoots();
	public static final Item elementiumPick = new ItemElementiumPick();
	public static final Item elementiumShovel = new ItemElementiumShovel();
	public static final Item elementiumAxe = new ItemElementiumAxe();
	public static final Item elementiumSword = new ItemElementiumSword();
	public static final Item elementiumShears = new ItemElementiumShears();
	public static final Item openBucket = new ItemOpenBucket();
	public static final Item spawnerMover = new ItemSpawnerMover();
	public static final Item pixieRing = new ItemPixieRing();
	public static final Item superTravelBelt = new ItemSuperTravelBelt();
	public static final Item rainbowRod = new ItemRainbowRod();
	public static final Item tornadoRod = new ItemTornadoRod();
	public static final Item fireRod = new ItemFireRod();
	public static final Item vineBall = new ItemVineBall();
	public static final Item slingshot = new ItemSlingshot();
	public static final Item manaBottle = new ItemBottledMana();
	public static final Item laputaShard = new ItemLaputaShard();
	public static final Item necroVirus = new ItemVirus(LibItemNames.NECRO_VIRUS);
	public static final Item nullVirus = new ItemVirus(LibItemNames.NULL_VIRUS);
	public static final Item reachRing = new ItemReachRing();
	public static final Item skyDirtRod = new ItemSkyDirtRod();
	public static final Item itemFinder = new ItemItemFinder();
	public static final Item superLavaPendant = new ItemSuperLavaPendant();
	public static final Item enderHand = new ItemEnderHand();
	public static final Item glassPick = new ItemGlassPick();
	public static final Item spark = new ItemSpark();
	public static final Item sparkUpgrade = new ItemSparkUpgrade();
	public static final Item diviningRod = new ItemDiviningRod();
	public static final Item gravityRod = new ItemGravityRod();
	public static final Item manaInkwell = new ItemManaInkwell();
	public static final Item vial = new ItemVial(LibItemNames.VIAL);
	public static final Item flask = new ItemVial(LibItemNames.FLASK);
	public static final Item brewVial = new ItemBrewVial();
	public static final Item brewFlask = new ItemBrewFlask();
	public static final Item bloodPendant = new ItemBloodPendant();
	public static final Item missileRod = new ItemMissileRod();
	public static final Item holyCloak = new ItemHolyCloak();
	public static final Item unholyCloak = new ItemUnholyCloak();
	public static final Item balanceCloak = new ItemBalanceCloak();
	public static final Item craftingHalo = new ItemCraftingHalo();
	public static final Item blackLotus = new ItemBlackLotus();
	public static final Item monocle = new ItemMonocle();
	public static final Item clip = new ItemClip();
	public static final Item cobbleRod = new ItemCobbleRod();
	public static final Item smeltRod = new ItemSmeltRod();
	public static final Item worldSeed = new ItemWorldSeed();
	public static final Item spellCloth = new ItemSpellCloth();
	public static final Item thornChakram = new ItemThornChakram(LibItemNames.THORN_CHAKRAM);
	public static final Item flareChakram = new ItemThornChakram(LibItemNames.FLARE_CHAKRAM);
	public static final Item overgrowthSeed = new ItemOvergrowthSeed();
	public static final Item craftPattern = new ItemCraftPattern();
	public static final Item ancientWill = new ItemAncientWill();
	public static final Item corporeaSpark = new ItemCorporeaSpark();
	public static final Item livingwoodBow = new ItemLivingwoodBow();
	public static final Item crystalBow = new ItemCrystalBow();
	public static final Item cosmetic = new ItemBaubleCosmetic();
	public static final Item swapRing = new ItemSwapRing();
	public static final Item flowerBag = new ItemFlowerBag();
	public static final Item phantomInk = new ItemPhantomInk();
	public static final Item poolMinecart = new ItemPoolMinecart();
	public static final Item pinkinator = new ItemPinkinator();
	public static final Item infiniteFruit = new ItemInfiniteFruit();
	public static final Item kingKey = new ItemKingKey();
	public static final Item flugelEye = new ItemFlugelEye();
	public static final Item thorRing = new ItemThorRing();
	public static final Item odinRing = new ItemOdinRing();
	public static final Item lokiRing = new ItemLokiRing();
	public static final Item dice = new ItemDice();
	public static final Item keepIvy = new ItemKeepIvy();
	public static final Item blackHoleTalisman = new ItemBlackHoleTalisman();
	public static final Item recordGaia1 = new ItemRecordGaia1();
	public static final Item recordGaia2 = new ItemRecordGaia2();
	public static final Item temperanceStone = new ItemTemperanceStone();
	public static final Item incenseStick = new ItemIncenseStick();
	public static final Item terraAxe = new ItemTerraAxe();
	public static final Item waterBowl = new ItemWaterBowl();
	public static final Item obedienceStick = new ItemObedienceStick();
	public static final Item cacophonium = new ItemCacophonium();
	public static final Item slimeBottle = new ItemSlimeBottle();
	public static final Item starSword = new ItemStarSword();
	public static final Item exchangeRod = new ItemExchangeRod();
	public static final Item magnetRingGreater = new ItemGreaterMagnetRing();
	public static final Item thunderSword = new ItemThunderSword();
	public static final Item manaweaveHelm = new ItemManaweaveHelm();
	public static final Item manaweaveChest = new ItemManaweaveChest();
	public static final Item manaweaveLegs = new ItemManaweaveLegs();
	public static final Item manaweaveBoots = new ItemManaweaveBoots();
	public static final Item autocraftingHalo = new ItemAutocraftingHalo();
	public static final Item gaiaHead = new ItemGaiaHead();
	public static final Item sextant = new ItemSextant();
	public static final Item speedUpBelt = new ItemSpeedUpBelt();
	public static final Item baubleBox = new ItemBaubleBox();
	public static final Item dodgeRing = new ItemDodgeRing();
	public static final Item invisibilityCloak = new ItemInvisibilityCloak();
	public static final Item cloudPendant = new ItemCloudPendant();
	public static final Item superCloudPendant = new ItemSuperCloudPendant();
	public static final Item thirdEye = new ItemThirdEye();
	public static final Item astrolabe = new ItemAstrolabe();
	public static final Item goddessCharm = new ItemGoddessCharm();

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> evt) {
		IForgeRegistry<Item> r = evt.getRegistry();

		r.register(lexicon);
		petals.values().forEach(r::register);
		dyes.values().forEach(r::register);
		r.register(pestleAndMortar);
		r.register(twigWand);
		r.register(manaSteel);
		r.register(manaPearl);
		r.register(manaDiamond);
		r.register(livingwoodTwig);
		r.register(terrasteel);
		r.register(lifeEssence);
		r.register(redstoneRoot);
		r.register(elementium);
		r.register(pixieDust);
		r.register(dragonstone);
		r.register(placeholder);
		r.register(redString);
		r.register(dreamwoodTwig);
		r.register(gaiaIngot);
		r.register(enderAirBottle);
		r.register(manaString);
		r.register(manasteelNugget);
		r.register(terrasteelNugget);
		r.register(elementiumNugget);
		r.register(livingroot);
		r.register(pebble);
		r.register(manaweaveCloth);
		r.register(manaPowder);
		r.register(lensNormal);
		r.register(lensSpeed);
		r.register(lensPower);
		r.register(lensTime);
		r.register(lensEfficiency);
		r.register(lensBounce);
		r.register(lensGravity);
		r.register(lensMine);
		r.register(lensDamage);
		r.register(lensPhantom);
		r.register(lensMagnet);
		r.register(lensExplosive);
		r.register(lensInfluence);
		r.register(lensWeight);
		r.register(lensPaint);
		r.register(lensFire);
		r.register(lensPiston);
		r.register(lensLight);
		r.register(lensWarp);
		r.register(lensRedirect);
		r.register(lensFirework);
		r.register(lensFlare);
		r.register(lensMessenger);
		r.register(lensTripwire);
		r.register(lensStorm);
		r.register(rune);
		r.register(manaTablet);
		r.register(manaGun);
		r.register(manaCookie);
		r.register(fertilizer);
		r.register(grassSeeds);
		r.register(dirtRod);
		r.register(terraformRod);
		r.register(grassHorn);
		r.register(leavesHorn);
		r.register(snowHorn);
		r.register(manaMirror);
		r.register(manasteelHelm);
		r.register(manasteelHelmRevealing);
		r.register(manasteelChest);
		r.register(manasteelLegs);
		r.register(manasteelBoots);
		r.register(manasteelPick);
		r.register(manasteelShovel);
		r.register(manasteelAxe);
		r.register(manasteelSword);
		r.register(manasteelShears);
		r.register(terrasteelHelm);
		r.register(terrasteelHelmRevealing);
		r.register(terrasteelChest);
		r.register(terrasteelLegs);
		r.register(terrasteelBoots);
		r.register(terraSword);
		r.register(tinyPlanet);
		r.register(manaRing);
		r.register(auraRing);
		r.register(manaRingGreater);
		r.register(auraRingGreater);
		r.register(travelBelt);
		r.register(knockbackBelt);
		r.register(icePendant);
		r.register(lavaPendant);
		r.register(magnetRing);
		r.register(waterRing);
		r.register(miningRing);
		r.register(terraPick);
		r.register(divaCharm);
		r.register(flightTiara);
		r.register(enderDagger);
		r.register(darkQuartz);
		r.register(manaQuartz);
		r.register(blazeQuartz);
		r.register(lavenderQuartz);
		r.register(redQuartz);
		r.register(elfQuartz);
		r.register(sunnyQuartz);
		r.register(waterRod);
		r.register(elementiumHelm);
		r.register(elementiumHelmRevealing);
		r.register(elementiumChest);
		r.register(elementiumLegs);
		r.register(elementiumBoots);
		r.register(elementiumPick);
		r.register(elementiumShovel);
		r.register(elementiumAxe);
		r.register(elementiumSword);
		r.register(elementiumShears);
		r.register(openBucket);
		r.register(spawnerMover);
		r.register(pixieRing);
		r.register(superTravelBelt);
		r.register(rainbowRod);
		r.register(tornadoRod);
		r.register(fireRod);
		r.register(vineBall);
		r.register(slingshot);
		r.register(manaBottle);
		r.register(laputaShard);
		r.register(necroVirus);
		r.register(nullVirus);
		r.register(reachRing);
		r.register(skyDirtRod);
		r.register(itemFinder);
		r.register(superLavaPendant);
		r.register(enderHand);
		r.register(glassPick);
		r.register(spark);
		r.register(sparkUpgrade);
		r.register(diviningRod);
		r.register(gravityRod);
		r.register(manaInkwell);
		r.register(vial);
		r.register(flask);
		r.register(brewVial);
		r.register(brewFlask);
		r.register(bloodPendant);
		r.register(missileRod);
		r.register(holyCloak);
		r.register(unholyCloak);
		r.register(balanceCloak);
		r.register(craftingHalo);
		r.register(blackLotus);
		r.register(monocle);
		r.register(clip);
		r.register(cobbleRod);
		r.register(smeltRod);
		r.register(worldSeed);
		r.register(spellCloth);
		r.register(thornChakram);
		r.register(flareChakram);
		r.register(overgrowthSeed);
		r.register(craftPattern);
		r.register(ancientWill);
		r.register(corporeaSpark);
		r.register(livingwoodBow);
		r.register(crystalBow);
		r.register(cosmetic);
		r.register(swapRing);
		r.register(flowerBag);
		r.register(phantomInk);
		r.register(poolMinecart);
		r.register(pinkinator);
		r.register(infiniteFruit);
		r.register(kingKey);
		r.register(flugelEye);
		r.register(thorRing);
		r.register(odinRing);
		r.register(lokiRing);
		r.register(dice);
		r.register(keepIvy);
		r.register(blackHoleTalisman);
		r.register(recordGaia1);
		r.register(recordGaia2);
		r.register(temperanceStone);
		r.register(incenseStick);
		r.register(terraAxe);
		r.register(waterBowl);
		r.register(obedienceStick);
		r.register(cacophonium);
		r.register(slimeBottle);
		r.register(starSword);
		r.register(exchangeRod);
		r.register(magnetRingGreater);
		r.register(thunderSword);
		r.register(manaweaveHelm);
		r.register(manaweaveChest);
		r.register(manaweaveLegs);
		r.register(manaweaveBoots);
		r.register(autocraftingHalo);
		r.register(gaiaHead);
		r.register(sextant);
		r.register(speedUpBelt);
		r.register(baubleBox);
		r.register(dodgeRing);
		r.register(invisibilityCloak);
		r.register(cloudPendant);
		r.register(superCloudPendant);
		r.register(thirdEye);
		r.register(astrolabe);
		r.register(goddessCharm);

		registerOreDictionary();
	}
	
	private static void registerOreDictionary() {
		OreDictionary.registerOre(LibOreDict.LEXICON, lexicon);
		for(EnumDyeColor color : EnumDyeColor.values()) {
			OreDictionary.registerOre(LibOreDict.PETAL[color.getMetadata()], new ItemStack(petals.get(color)));
			OreDictionary.registerOre(LibOreDict.DYE[color.getMetadata()], new ItemStack(dyes.get(color)));
			OreDictionary.registerOre(LibOreDict.DYE_WILDCARD, new ItemStack(dyes.get(color)));
		}

		for(int i = 0; i < 16; i++) {
			OreDictionary.registerOre(LibOreDict.RUNE[i], new ItemStack(rune, 1, i));
		}


		OreDictionary.registerOre(LibOreDict.QUARTZ[0], new ItemStack(darkQuartz));
		OreDictionary.registerOre(LibOreDict.QUARTZ[1], new ItemStack(manaQuartz));
		OreDictionary.registerOre(LibOreDict.QUARTZ[2], new ItemStack(blazeQuartz));
		OreDictionary.registerOre(LibOreDict.QUARTZ[3], new ItemStack(lavenderQuartz));
		OreDictionary.registerOre(LibOreDict.QUARTZ[4], new ItemStack(redQuartz));
		OreDictionary.registerOre(LibOreDict.QUARTZ[5], new ItemStack(elfQuartz));
		OreDictionary.registerOre(LibOreDict.QUARTZ[6], new ItemStack(sunnyQuartz));

		OreDictionary.registerOre(LibOreDict.PESTLE_AND_MORTAR, pestleAndMortar);
		OreDictionary.registerOre(LibOreDict.MANA_STEEL, new ItemStack(manaSteel));
		OreDictionary.registerOre(LibOreDict.MANA_PEARL, new ItemStack(manaPearl));
		OreDictionary.registerOre(LibOreDict.MANA_DIAMOND, new ItemStack(manaDiamond));
		OreDictionary.registerOre(LibOreDict.LIVINGWOOD_TWIG, new ItemStack(livingwoodTwig));
		OreDictionary.registerOre(LibOreDict.TERRA_STEEL, new ItemStack(terrasteel));
		OreDictionary.registerOre(LibOreDict.LIFE_ESSENCE, new ItemStack(lifeEssence));
		OreDictionary.registerOre(LibOreDict.REDSTONE_ROOT, new ItemStack(redstoneRoot));
		OreDictionary.registerOre(LibOreDict.ELEMENTIUM, new ItemStack(elementium));
		OreDictionary.registerOre(LibOreDict.PIXIE_DUST, new ItemStack(pixieDust));
		OreDictionary.registerOre(LibOreDict.DRAGONSTONE, new ItemStack(dragonstone));
		OreDictionary.registerOre(LibOreDict.PLACEHOLDER, new ItemStack(placeholder));
		OreDictionary.registerOre(LibOreDict.RED_STRING, new ItemStack(redString));
		OreDictionary.registerOre(LibOreDict.DREAMWOOD_TWIG, new ItemStack(dreamwoodTwig));
		OreDictionary.registerOre(LibOreDict.GAIA_INGOT, new ItemStack(gaiaIngot));
		OreDictionary.registerOre(LibOreDict.ENDER_AIR_BOTTLE, new ItemStack(enderAirBottle));
		OreDictionary.registerOre(LibOreDict.MANA_STRING, new ItemStack(manaString));
		OreDictionary.registerOre(LibOreDict.MANASTEEL_NUGGET, new ItemStack(manasteelNugget));
		OreDictionary.registerOre(LibOreDict.TERRASTEEL_NUGGET, new ItemStack(terrasteelNugget));
		OreDictionary.registerOre(LibOreDict.ELEMENTIUM_NUGGET, new ItemStack(elementiumNugget));
		OreDictionary.registerOre(LibOreDict.ROOT, new ItemStack(livingroot));
		OreDictionary.registerOre(LibOreDict.PEBBLE, new ItemStack(pebble));
		OreDictionary.registerOre(LibOreDict.MANAWEAVE_CLOTH, new ItemStack(manaweaveCloth));
		OreDictionary.registerOre(LibOreDict.MANA_POWDER, new ItemStack(manaPowder));

		OreDictionary.registerOre(LibOreDict.VIAL, new ItemStack(vial));
		OreDictionary.registerOre(LibOreDict.FLASK, new ItemStack(flask));

		BotaniaAPI.blackListItemFromLoonium(lexicon);
		BotaniaAPI.blackListItemFromLoonium(overgrowthSeed);
		BotaniaAPI.blackListItemFromLoonium(blackLotus);
		for(Item i : Item.REGISTRY) {
			if("minecraft".equals(i.getRegistryName().getResourceDomain()) & i instanceof ItemRecord) {
				BotaniaAPI.blackListItemFromLoonium(i);
			}
		}

		OreDictionary.registerOre("rodBlaze", Items.BLAZE_ROD);
		OreDictionary.registerOre("powderBlaze", Items.BLAZE_POWDER);
	}
}
