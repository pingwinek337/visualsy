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
	public static final String[] SOUND_NAMES = {
		"Pling", "Orb XP", "Krytyk", "Strzała", "Level up", "Dzwon", "Kurczak", "Kot"
	};
	private static final String[] SOUND_IDS = {
		"block.note_block.pling",
		"entity.experience_orb.pickup",
		"entity.player.attack.crit",
		"entity.arrow.hit_player",
		"entity.player.levelup",
		"block.bell.use",
		"entity.chicken.ambient",
		"entity.cat.ambient"
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
		SoundEvent event = SoundEvent.of(Identifier.ofVanilla(id));
		MinecraftClient.getInstance().getSoundManager()
			.play(PositionedSoundInstance.master(event, cfg.hitPitch, cfg.hitVolume));
	}
}
