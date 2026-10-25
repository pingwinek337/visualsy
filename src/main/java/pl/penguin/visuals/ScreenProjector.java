package pl.penguin.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Vec3d;

/**
 * Zamienia pozycję w świecie na pozycję na ekranie (w pikselach skalowanego GUI).
 * Liczy to z kamery i FOV z ustawień, więc przy sprincie (zmiana FOV) może być lekko przesunięte.
 */
public final class ScreenProjector {
	private final Vec3d camPos;
	private final double[] forward;
	private final double[] right;
	private final double[] up;
	private final double tanHalfFov;
	private final double aspect;
	private final int width;
	private final int height;

	public ScreenProjector(MinecraftClient mc) {
		Camera camera = mc.gameRenderer.getCamera();
		camPos = camera.getPos();

		double yaw = Math.toRadians(camera.getYaw());
		double pitch = Math.toRadians(camera.getPitch());
		double sy = Math.sin(yaw);
		double cy = Math.cos(yaw);
		double sp = Math.sin(pitch);
		double cp = Math.cos(pitch);

		forward = new double[]{-sy * cp, -sp, cy * cp};
		right = new double[]{-cy, 0, -sy};
		up = new double[]{
			right[1] * forward[2] - right[2] * forward[1],
			right[2] * forward[0] - right[0] * forward[2],
			right[0] * forward[1] - right[1] * forward[0]
		};

		width = mc.getWindow().getScaledWidth();
		height = mc.getWindow().getScaledHeight();
		aspect = (double) width / (double) height;
		tanHalfFov = Math.tan(Math.toRadians(mc.options.getFov().getValue()) / 2.0);
	}

	/** Zapisuje x, y ekranu do out. Zwraca false, gdy punkt jest za kamerą. */
	public boolean project(Vec3d point, double[] out) {
		double dx = point.x - camPos.x;
		double dy = point.y - camPos.y;
		double dz = point.z - camPos.z;

		double x = dx * right[0] + dy * right[1] + dz * right[2];
		double y = dx * up[0] + dy * up[1] + dz * up[2];
		double z = dx * forward[0] + dy * forward[1] + dz * forward[2];
		if (z < 0.05) {
			return false;
		}

		double ndcX = x / (z * tanHalfFov * aspect);
		double ndcY = y / (z * tanHalfFov);
		out[0] = (ndcX * 0.5 + 0.5) * width;
		out[1] = (0.5 - ndcY * 0.5) * height;
		return true;
	}
}
