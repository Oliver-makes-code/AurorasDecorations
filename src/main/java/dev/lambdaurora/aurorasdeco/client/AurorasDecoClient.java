/*
 * Copyright (c) 2021 LambdAurora <aurora42lambda@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package dev.lambdaurora.aurorasdeco.client;

import dev.lambdaurora.aurorasdeco.AurorasDeco;
import dev.lambdaurora.aurorasdeco.block.BlackboardBlock;
import dev.lambdaurora.aurorasdeco.block.HangingFlowerPotBlock;
import dev.lambdaurora.aurorasdeco.block.StumpBlock;
import dev.lambdaurora.aurorasdeco.block.big_flower_pot.PottedPlantType;
import dev.lambdaurora.aurorasdeco.client.model.BakedSignPostModel;
import dev.lambdaurora.aurorasdeco.client.particle.AmethystGlintParticle;
import dev.lambdaurora.aurorasdeco.client.renderer.*;
import dev.lambdaurora.aurorasdeco.client.screen.CopperHopperScreen;
import dev.lambdaurora.aurorasdeco.client.screen.SawmillScreen;
import dev.lambdaurora.aurorasdeco.client.screen.ShelfScreen;
import dev.lambdaurora.aurorasdeco.hook.TrinketsHooks;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoPackets;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoParticles;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import dev.lambdaurora.aurorasdeco.registry.WoodType;
import dev.lambdaurora.aurorasdeco.resource.AurorasDecoPack;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.minecraft.block.TallPlantBlock;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.color.world.FoliageColors;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.particle.LavaEmberParticle;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import static dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry.*;

/**
 * Represents the Aurora's Decorations client mod.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
@Environment(EnvType.CLIENT)
public class AurorasDecoClient implements ClientModInitializer {
    public static final AurorasDecoPack RESOURCE_PACK = new AurorasDecoPack(ResourceType.CLIENT_RESOURCES);
    public static final ModelIdentifier BLACKBOARD_MASK = new ModelIdentifier(AurorasDeco.id("blackboard_mask"),
            "inventory");

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void onInitializeClient() {
        BlockEntityRendererRegistry.INSTANCE.register(AurorasDecoRegistry.BOOK_PILE_BLOCK_ENTITY_TYPE,
                BookPileEntityRenderer::new);
        BlockEntityRendererRegistry.INSTANCE.register(AurorasDecoRegistry.SHELF_BLOCK_ENTITY_TYPE,
                ShelfBlockEntityRenderer::new);
        BlockEntityRendererRegistry.INSTANCE.register(SIGN_POST_BLOCK_ENTITY_TYPE,
                SignPostBlockEntityRenderer::new);
        BlockEntityRendererRegistry.INSTANCE.register(AurorasDecoRegistry.WALL_LANTERN_BLOCK_ENTITY_TYPE,
                LanternBlockEntityRenderer::new);
        BlockEntityRendererRegistry.INSTANCE.register(AurorasDecoRegistry.WIND_CHIME_BLOCK_ENTITY_TYPE,
                WindChimeBlockEntityRenderer::new);

        EntityRendererRegistry.INSTANCE.register(AurorasDecoRegistry.FAKE_LEASH_KNOT_ENTITY_TYPE,
                FakeLeashKnotEntityRenderer::new);
        EntityRendererRegistry.INSTANCE.register(AurorasDecoRegistry.SEAT_ENTITY_TYPE,
                SeatEntityRenderer::new);

        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutoutMipped(),
                AurorasDecoRegistry.BURNT_VINE_BLOCK);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(),
                AurorasDecoRegistry.AMETHYST_LANTERN_BLOCK,
                AurorasDecoRegistry.BRAZIER_BLOCK,
                AurorasDecoRegistry.COPPER_SULFATE_BRAZIER_BLOCK,
                AurorasDecoRegistry.COPPER_SULFATE_CAMPFIRE_BLOCK,
                AurorasDecoRegistry.COPPER_SULFATE_LANTERN_BLOCK,
                AurorasDecoRegistry.COPPER_SULFATE_TORCH_BLOCK,
                AurorasDecoRegistry.COPPER_SULFATE_WALL_TORCH_BLOCK,
                DAFFODIL,
                AurorasDecoRegistry.SAWMILL_BLOCK,
                AurorasDecoRegistry.SOUL_BRAZIER_BLOCK,
                AurorasDecoRegistry.WIND_CHIME_BLOCK
        );

        ParticleFactoryRegistry.getInstance().register(AurorasDecoParticles.AMETHYST_GLINT, AmethystGlintParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(AurorasDecoParticles.COPPER_SULFATE_FLAME, FlameParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(AurorasDecoParticles.COPPER_SULFATE_LAVA, LavaEmberParticle.Factory::new);

        StumpBlock.streamLogStumps()
                .forEach(block -> BlockRenderLayerMap.INSTANCE.putBlock(block, RenderLayer.getCutout()));

        ClientPlayNetworking.registerGlobalReceiver(AurorasDecoPackets.SIGN_POST_OPEN_GUI, AurorasDecoPackets.Client::handleSignPostOpenGuiPacket);

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            PottedPlantType.stream()
                    .forEach(plantType -> {
                        if (plantType.isEmpty()) return;

                        BlockRenderLayerMap.INSTANCE.putBlock(plantType.getPot(), RenderLayer.getCutoutMipped());

                        if (plantType.getPlant() instanceof TallPlantBlock) {
                            ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) ->
                                            world != null && pos != null ? BiomeColors.getGrassColor(world, pos)
                                                    : GrassColors.getColor(0.5D, 1.0D),
                                    plantType.getPot());
                        } else {
                            var originalColorProvider = ColorProviderRegistry.BLOCK.get(plantType.getPlant());
                            if (originalColorProvider != null)
                                ColorProviderRegistry.BLOCK.register(originalColorProvider, plantType.getPot());
                        }
                    });
            HangingFlowerPotBlock.stream().forEach(block -> {
                BlockRenderLayerMap.INSTANCE.putBlock(block, RenderLayer.getCutout());
                var colorProvider = ColorProviderRegistry.BLOCK.get(block.getFlowerPot());
                if (colorProvider != null)
                    ColorProviderRegistry.BLOCK.register(colorProvider, block);
            });
            StumpBlock.streamLogStumps()
                    .forEach(block -> {
                        var leavesComponent = block.getWoodType().getComponent(WoodType.ComponentType.LEAVES);
                        if (leavesComponent == null) return;

                        var blockColorProvider = leavesComponent.getBlockColorProvider();
                        if (blockColorProvider != null) {
                            ColorProviderRegistry.BLOCK.register(blockColorProvider, block);
                        }
                        var itemColorProvider = leavesComponent.getItemColorProvider();
                        if (itemColorProvider != null) {
                            ColorProviderRegistry.ITEM.register(itemColorProvider, block);
                        }
                    });
        });

        this.registerBlackboardItemRenderer(BLACKBOARD_BLOCK);
        this.registerBlackboardItemRenderer(CHALKBOARD_BLOCK);
        this.registerBlackboardItemRenderer(WAXED_BLACKBOARD_BLOCK);
        this.registerBlackboardItemRenderer(WAXED_CHALKBOARD_BLOCK);

        ScreenRegistry.register(COPPER_HOPPER_SCREEN_HANDLER_TYPE, CopperHopperScreen::new);
        ScreenRegistry.register(SAWMILL_SCREEN_HANDLER_TYPE, SawmillScreen::new);
        ScreenRegistry.register(SHELF_SCREEN_HANDLER_TYPE, ShelfScreen::new);

        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) ->
                        world != null && pos != null
                                ? BiomeColors.getFoliageColor(world, pos) : FoliageColors.getDefaultColor(),
                BURNT_VINE_BLOCK);

        EntityModelLayerRegistry.registerModelLayer(WindChimeBlockEntityRenderer.WIND_CHIME_MODEL_LAYER,
                WindChimeBlockEntityRenderer::getTexturedModelData);

        ModelLoadingRegistry.INSTANCE.registerModelProvider(RenderRule::reload);
        ModelLoadingRegistry.INSTANCE.registerVariantProvider(resourceManager -> new BakedSignPostModel.Provider());

        TrinketsHooks.init(BLACKBOARD_BLOCK.asItem(), WAXED_BLACKBOARD_BLOCK.asItem(),
                CHALKBOARD_BLOCK.asItem(), WAXED_CHALKBOARD_BLOCK.asItem());
    }

    private void registerBlackboardItemRenderer(BlackboardBlock blackboard) {
        var id = Registry.BLOCK.getId(blackboard);
        var modelId = new ModelIdentifier(new Identifier(id.getNamespace(), id.getPath() + "_base"),
                "inventory");
        BuiltinItemRendererRegistry.INSTANCE.register(blackboard, new BlackboardItemRenderer(modelId));
        ModelLoadingRegistry.INSTANCE.registerModelProvider((manager, out) -> {
            out.accept(modelId);
            out.accept(BLACKBOARD_MASK);
        });
    }
}
