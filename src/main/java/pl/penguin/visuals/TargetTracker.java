package pl.penguin.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.Optional;

/** Znajduje gracza, na którego patrzysz (dalej niż zasięg uderzenia, ale nie przez ściany). */
public final class TargetTracker {
	private TargetTracker() {
	}

	public static PlayerEntity find(MinecraftClient mc, float tickDelta, double range) {
		if (mc.world == null || mc.player == null) {
			return null;
		}

		Vec3d start = mc.player.getCameraPosVec(tickDelta);
		Vec3d direction = mc.player.getRotationVec(tickDelta);
		Vec3d end = start.add(direction.multiply(range));

		PlayerEntity best = null;
		Vec3d bestHit = null;
		double bestDistance = Double.MAX_VALUE;

		for (PlayerEntity other : mc.world.getPlayers()) {
			if (other == mc.player || other.isSpectator()) {
				continue;
			}
			Box box = other.getBoundingBox().expand(0.1);
			Optional<Vec3d> hit = box.raycast(start, end);
			if (hit.isEmpty()) {
				continue;
			}
			double distance = start.squaredDistanceTo(hit.get());
			if (distance < bestDistance) {
				bestDistance = distance;
				best = other;
				bestHit = hit.get();
			}
		}

		if (best == null) {
			return null;
		}

		// czy coś nie zasłania gracza
		BlockHitResult block = mc.world.raycast(new RaycastContext(
			start, bestHit,
			RaycastContext.ShapeType.COLLIDER,
			RaycastContext.FluidHandling.NONE,
			mc.player
		));
		return block.getType() == HitResult.Type.MISS ? best : null;
	}
}
