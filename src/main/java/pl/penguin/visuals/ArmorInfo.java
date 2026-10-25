package pl.penguin.visuals;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;

import java.util.LinkedHashSet;
import java.util.Set;

/** Nazwa seta i wytrzymałość zbroi gracza. */
public final class ArmorInfo {
	private static final EquipmentSlot[] SLOTS = {
		EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
	};
	private static final String[] LETTERS = {"H", "K", "S", "B"};

	public String setName = "Brak";
	public int setColor = 0x8B95A7;
	public int current = 0;
	public int max = 0;
	/** Procent wytrzymałości każdej części (hełm, korpus, spodnie, buty); -1 = brak części. */
	public final int[] piecePercent = {-1, -1, -1, -1};

	public static ArmorInfo of(PlayerEntity player) {
		ArmorInfo info = new ArmorInfo();
		Set<String> materials = new LinkedHashSet<>();
		int pieces = 0;

		for (int i = 0; i < SLOTS.length; i++) {
			ItemStack stack = player.getEquippedStack(SLOTS[i]);
			if (stack.isEmpty()) {
				continue;
			}
			pieces++;
			String path = Registries.ITEM.getId(stack.getItem()).getPath();
			materials.add(path.replaceAll("_(helmet|chestplate|leggings|boots)$", ""));

			if (stack.isDamageable() && stack.getMaxDamage() > 0) {
				int maxDamage = stack.getMaxDamage();
				int left = Math.max(0, maxDamage - stack.getDamage());
				info.max += maxDamage;
				info.current += left;
				info.piecePercent[i] = left * 100 / maxDamage;
			} else {
				info.piecePercent[i] = 100;
			}
		}

		if (pieces == 0) {
			return info;
		}
		if (materials.size() == 1) {
			String material = materials.iterator().next();
			info.setName = displayName(material) + (pieces == 4 ? "" : " (" + pieces + "/4)");
			info.setColor = color(material);
		} else {
			info.setName = "Mieszany";
			info.setColor = 0xFFFFFF;
		}
		return info;
	}

	/** Procent wytrzymałości całego seta albo -1, gdy nie ma nic do pokazania. */
	public int percent() {
		return max <= 0 ? -1 : current * 100 / max;
	}

	public String piecesLine() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < LETTERS.length; i++) {
			if (i > 0) {
				sb.append("  ");
			}
			sb.append(LETTERS[i]).append(' ');
			sb.append(piecePercent[i] < 0 ? "-" : piecePercent[i] + "%");
		}
		return sb.toString();
	}

	private static String displayName(String material) {
		return switch (material) {
			case "netherite" -> "Netherite";
			case "diamond" -> "Diamentowy";
			case "iron" -> "Żelazny";
			case "golden" -> "Złoty";
			case "chainmail" -> "Kolczuga";
			case "leather" -> "Skórzany";
			case "turtle" -> "Żółwi";
			case "elytra" -> "Elytra";
			default -> {
				String text = material.replace('_', ' ');
				yield text.isEmpty() ? text : Character.toUpperCase(text.charAt(0)) + text.substring(1);
			}
		};
	}

	private static int color(String material) {
		return switch (material) {
			case "netherite" -> 0xB79BC2;
			case "diamond" -> 0x4DEEEA;
			case "iron" -> 0xD8D8D8;
			case "golden" -> 0xFFD83B;
			case "chainmail" -> 0xA0A8B0;
			case "leather" -> 0xC98A4B;
			case "turtle" -> 0x4CAF50;
			default -> 0xFFFFFF;
		};
	}
}
