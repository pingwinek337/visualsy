package pl.penguin.visuals;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

/** Dźwięk i cząsteczki po uderzeniu kogoś. */
public final class HitEffects {
	/** Pierwsze 12 to własne dźwięki moda (assets/penguinvisuals/sounds), reszta z gry. */
	public static final String[] SOUND_NAMES = {
		"Hitmarker", "Moneta", "Laser", "Katana", "Bonk", "Szkło", "Zap", "Headshot",
		"Retro 8-bit", "Bas 808", "Bąbel", "Magia",
		"Krytyk", "Level up", "Orb XP", "Dzwon"
	};
	private static final String[] SOUND_IDS = {
		"penguinvisuals:hit_hitmarker",
		"penguinvisuals:hit_coin",
		"penguinvisuals:hit_laser",
		"penguinvisuals:hit_katana",
		"penguinvisuals:hit_bonk",
		"penguinvisuals:hit_glass",
		"penguinvisuals:hit_zap",
		"penguinvisuals:hit_headshot",
		"penguinvisuals:hit_retro",
		"penguinvisuals:hit_bass",
		"penguinvisuals:hit_bubble",
		"penguinvisuals:hit_magic",
		"minecraft:entity.player.attack.crit",
		"minecraft:entity.player.levelup",
		"minecraft:entity.experience_orb.pickup",
		"minecraft:block.bell.use"
	};

	public static final String[] PARTICLE_NAMES = {
		"Krytyczne", "Magiczne", "Serca", "Płomienie", "Śnieg", "Iskry"
	};
	private static final ParticleEffect[] PARTICLES = {
		ParticleTypes.CRIT,
		ParticleTypes.ENCHANTED_HIT,
		ParticleTypes.HEART,
		ParticleTypes.FLAME,
		ParticleTypes.SNOWFLAKE,
		ParticleTypes.END_ROD
	};

	private HitEffects() {
	}

	public static void register() {
		AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if (world.isClient
				&& LicenseManager.isActive()
				&& entity instanceof LivingEntity
				&& player == MinecraftClient.getInstance().player) {
				onHit(world, entity);
			}
			return ActionResult.PASS;
		});
	}

	private static void onHit(World world, Entity target) {
		PenguinConfig cfg = PenguinConfig.get();

		if (cfg.hitSound) {
			playSound();
		}

		if (cfg.hitParticles) {
			ParticleEffect particle = PARTICLES[Math.floorMod(cfg.particleIndex, PARTICLES.length)];
			for (int i = 0; i < 8; i++) {
				world.addParticle(
					particle,
					target.getX() + world.random.nextGaussian() * 0.25,
					target.getBodyY(0.5) + world.random.nextGaussian() * 0.25,
					target.getZ() + world.random.nextGaussian() * 0.25,
					world.random.nextGaussian() * 0.05,
					0.05 + world.random.nextDouble() * 0.1,
					world.random.nextGaussian() * 0.05
				);
			}
		}
	}

	/** Gra aktualnie wybrany dźwięk (też jako próbka z menu). */
	public static void playSound() {
		PenguinConfig cfg = PenguinConfig.get();
		String id = SOUND_IDS[Math.floorMod(cfg.hitSoundIndex, SOUND_IDS.length)];
		int colon = id.indexOf(':');
		Identifier identifier = Identifier.of(id.substring(0, colon), id.substring(colon + 1));
		SoundEvent event = SoundEvent.of(identifier);
		MinecraftClient.getInstance().getSoundManager()
			.play(PositionedSoundInstance.master(event, cfg.hitPitch, cfg.hitVolume));
	}
}
