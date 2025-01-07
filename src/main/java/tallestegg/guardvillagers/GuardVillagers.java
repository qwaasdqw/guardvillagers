package tallestegg.guardvillagers;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import tallestegg.guardvillagers.client.GuardSounds;
import tallestegg.guardvillagers.common.entities.Guard;
import tallestegg.guardvillagers.configuration.GuardConfig;
import tallestegg.guardvillagers.loot_tables.GuardLootTables;
import tallestegg.guardvillagers.networking.GuardFollowPacket;
import tallestegg.guardvillagers.networking.GuardOpenInventoryPacket;
import tallestegg.guardvillagers.networking.GuardSetPatrolPosPacket;

@Mod(GuardVillagers.MODID)
public class GuardVillagers {
    public static final String MODID = "guardvillagers";

    public GuardVillagers(ModContainer container, IEventBus modEventBus) {
        container.registerConfig(ModConfig.Type.COMMON, GuardConfig.COMMON_SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, GuardConfig.CLIENT_SPEC);
        container.registerConfig(ModConfig.Type.STARTUP, GuardConfig.STARTUP_SPEC);
        modEventBus.addListener(this::setup);
        NeoForge.EVENT_BUS.register(HandlerEvents.class);
        GuardEntityType.ENTITIES.register(modEventBus);
        GuardItems.ITEMS.register(modEventBus);
        GuardSounds.SOUNDS.register(modEventBus);
        GuardLootTables.LOOT_ITEM_CONDITION_TYPES.register(modEventBus);
        GuardLootTables.LOOT_ITEM_FUNCTION_TYPES.register(modEventBus);
        GuardStats.STATS.register(modEventBus);
        modEventBus.addListener(this::addAttributes);
        modEventBus.addListener(this::addCreativeTabs);
        modEventBus.addListener(this::register);
    }


    private void register(final RegisterPayloadHandlersEvent event) {
        PayloadRegistrar reg = event.registrar(MODID).versioned("2.0.2");
        reg.playToServer(GuardSetPatrolPosPacket.TYPE, GuardSetPatrolPosPacket.STREAM_CODEC, GuardSetPatrolPosPacket::setPatrolPosition);
        reg.playToClient(GuardOpenInventoryPacket.TYPE, GuardOpenInventoryPacket.STREAM_CODEC, GuardOpenInventoryPacket::handle);
        reg.playToServer(GuardFollowPacket.TYPE, GuardFollowPacket.STREAM_CODEC, GuardFollowPacket::handle);
    }

    public static boolean hotvChecker(Player player, Guard guard) {
        return player.hasEffect(MobEffects.HERO_OF_THE_VILLAGE) && GuardConfig.COMMON.giveGuardStuffHOTV.get()
                || !GuardConfig.COMMON.giveGuardStuffHOTV.get() || guard.getPlayerReputation(player) > GuardConfig.COMMON.reputationRequirement.get() && !player.level().isClientSide();
    }

    public static boolean canFollow(Player player) {
        return GuardConfig.COMMON.followHero.get() && player.hasEffect(MobEffects.HERO_OF_THE_VILLAGE) || !GuardConfig.COMMON.followHero.get();
    }

    @SubscribeEvent
    private void addCreativeTabs(final BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(GuardItems.GUARD_SPAWN_EGG.get());
            event.accept(GuardItems.ILLUSIONER_SPAWN_EGG.get());
        }
    }

    @SubscribeEvent
    private void setup(final FMLCommonSetupEvent event) {
    }

    @SubscribeEvent
    private void addAttributes(final EntityAttributeCreationEvent event) {
        event.put(GuardEntityType.GUARD.get(), Guard.createAttributes().build());
    }

    // Turns something like modid:john to simply john
    public static String removeModIdFromVillagerType(String stringWithModId) {
        String[] parts = stringWithModId.split(":");
        if (parts.length <= 1)
            return parts[0];
        else
            return parts[1];
    }

    @Mod(value = GuardVillagers.MODID, dist = Dist.CLIENT)
    public static class GuardVillagersClient {
        public GuardVillagersClient(ModContainer container, IEventBus modEventBus) {
            container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        }
    }
}
