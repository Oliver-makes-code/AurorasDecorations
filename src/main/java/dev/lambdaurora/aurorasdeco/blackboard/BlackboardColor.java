/*
 * Copyright (c) 2021 - 2022 LambdAurora <email@lambdaurora.dev>
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

package dev.lambdaurora.aurorasdeco.blackboard;

import dev.lambdaurora.aurorasdeco.registry.AurorasDecoPlants;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a blackboard color.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class BlackboardColor {
	private static final Int2ObjectMap<BlackboardColor> COLORS = new Int2ObjectOpenHashMap<>();
	private static final Object2ObjectMap<Item, BlackboardColor> ITEM_TO_COLOR = new Object2ObjectOpenHashMap<>();

	public static final int COLOR_MASK /**/ = 0b1111111100000000;
	public static final int SATURATION_MASK = 0b0000000010000000;
	public static final int SHADE_MASK /**/ = 0b0000000001110000;

	public static final BlackboardColor EMPTY = new BlackboardColor(0, 0x00000000, Items.PAPER);
	public static final byte FREE_COLOR_SPACE = (byte) (DyeColor.values().length + 1);
	public static final BlackboardColor SWEET_BERRIES = new BlackboardColor(FREE_COLOR_SPACE, 0xffbb0000, Items.SWEET_BERRIES);
	public static final BlackboardColor GLOW_BERRIES = new BlackboardColor(FREE_COLOR_SPACE + 1, 0xffff9737, Items.GLOW_BERRIES);
	public static final BlackboardColor LAVENDER = new BlackboardColor(FREE_COLOR_SPACE + 3, 0xffb886db, AurorasDecoPlants.LAVENDER.item());

	public static final int BLUEBERRIES_COLOR = 0xff006ac6;

	private final byte id;
	private final int color;
	private final Item item;

	private BlackboardColor(int id, int color, Item item) {
		this.id = (byte) id;
		this.color = color;
		this.item = item;

		COLORS.put(id, this);
		ITEM_TO_COLOR.put(item, this);
	}

	public static BlackboardColor byId(int color) {
		return COLORS.getOrDefault(color, EMPTY);
	}

	public static BlackboardColor fromRaw(int color) {
		return byId((color & COLOR_MASK) >> 8);
	}

	public static @Nullable BlackboardColor fromItem(Item item) {
		return ITEM_TO_COLOR.get(item);
	}

	public byte getId() {
		return this.id;
	}

	/**
	 * Returns the raw id with shading of this color.
	 *
	 * @param shade the shade
	 * @return the raw id
	 */
	public short toRawId(int shade, boolean saturated) {
		if (this == EMPTY) return 0;

		short id = (short) (this.getId() << 8);
		if (saturated) id |= SATURATION_MASK;
		id |= MathHelper.clamp(shade, 0, 7) << 4;
		return id;
	}

	/**
	 * {@return the color in the ARGB format}
	 */
	public int getColor() {
		return this.color;
	}

	public Item getItem() {
		return this.item;
	}

	/**
	 * {@return the render color in the ABGR format}
	 *
	 * @param shade the shade
	 */
	public int getRenderColor(int shade, boolean saturated) {
		if (this.getId() == 0)
			return this.getColor();

		int factor = switch (shade) {
			case 1 -> 220;
			case 2 -> 180;
			case 3 -> 135;
			default -> 255;
		};

		int color = saturated ? this.getSaturated() : this.getColor();
		int red = (color >> 16 & 255) * factor / 255;
		int green = (color >> 8 & 255) * factor / 255;
		int blue = (color & 255) * factor / 255;
		return 0xff000000 | blue << 16 | green << 8 | red;
	}

	public static int getRenderColor(int color) {
		return fromRaw(color).getRenderColor(getShadeFromRaw(color), getSaturationFromRaw(color));
	}

	public static int getShadeFromRaw(int color) {
		return (color & SHADE_MASK) >> 4;
	}

	public static boolean getSaturationFromRaw(int color) {
		return (color & SATURATION_MASK) != 0;
	}

	private int getSaturated() {
		final int value = 1;

		int color = this.getColor();
		int red = color >> 16 & 255;
		int green = color >> 8 & 255;
		int blue = color & 255;

		float gray = 0.2989f * red + 0.5870f * green + 0.1140f * blue;

		red = MathHelper.clamp((int) (-gray * value + red * (1 + value)), 0, 255);
		green = MathHelper.clamp((int) (-gray * value + green * (1 + value)), 0, 255);
		blue = MathHelper.clamp((int) (-gray * value + blue * (1 + value)), 0, 255);

		return 0xff000000 | red << 16 | green << 8 | blue;
	}

	public static BlackboardColor fromDye(DyeItem dyeItem) {
		var color = dyeItem.getColor();

		if (COLORS.containsKey(color.getId() + 1)) {
			return COLORS.get(color.getId() + 1);
		}

		int red = (int) (color.getColorComponents()[0] * 255.f);
		int green = (int) (color.getColorComponents()[1] * 255.f);
		int blue = (int) (color.getColorComponents()[2] * 255.f);
		return new BlackboardColor(color.getId() + 1, 0xff000000 | (red << 16) | (green << 8) | blue, dyeItem);
	}

	public static void tryRegisterColorFromItem(Identifier id, Item item) {
		if (item instanceof DyeItem dyeItem) {
			fromDye(dyeItem);
		} else if (id.getNamespace().equals("ecotones") && id.getPath().equals("blueberries")) {
			new BlackboardColor(FREE_COLOR_SPACE + 2, BLUEBERRIES_COLOR, item);
		}
	}
}