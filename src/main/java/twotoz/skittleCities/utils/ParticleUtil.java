package twotoz.skittleCities.utils;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import twotoz.skittleCities.data.Region;

public class ParticleUtil {

    public static void visualizeRegion(Player player, Region region) {
        Location pos1 = region.getPos1();
        Location pos2 = region.getPos2();
        World world = region.getWorld();

        // Use block coordinates and add +1 to max to show the complete block boundary
        double minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        double maxX = Math.max(pos1.getBlockX(), pos2.getBlockX()) + 1;
        double minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        double maxY = Math.max(pos1.getBlockY(), pos2.getBlockY()) + 1;
        double minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        double maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ()) + 1;

        double step = 0.5;
        
        // Choose particle color based on whether it's a subclaim
        // SUBCLAIMS = BLUE (soul fire flame)
        // NORMAL CLAIMS = RED/ORANGE (flame)
        Particle particle = region.isSubclaim() ? Particle.SOUL_FIRE_FLAME : Particle.FLAME;

        // Bottom edges
        drawLine(player, world, minX, minY, minZ, maxX, minY, minZ, step, particle);
        drawLine(player, world, minX, minY, maxZ, maxX, minY, maxZ, step, particle);
        drawLine(player, world, minX, minY, minZ, minX, minY, maxZ, step, particle);
        drawLine(player, world, maxX, minY, minZ, maxX, minY, maxZ, step, particle);

        // Top edges
        drawLine(player, world, minX, maxY, minZ, maxX, maxY, minZ, step, particle);
        drawLine(player, world, minX, maxY, maxZ, maxX, maxY, maxZ, step, particle);
        drawLine(player, world, minX, maxY, minZ, minX, maxY, maxZ, step, particle);
        drawLine(player, world, maxX, maxY, minZ, maxX, maxY, maxZ, step, particle);

        // Vertical edges
        drawLine(player, world, minX, minY, minZ, minX, maxY, minZ, step, particle);
        drawLine(player, world, maxX, minY, minZ, maxX, maxY, minZ, step, particle);
        drawLine(player, world, minX, minY, maxZ, minX, maxY, maxZ, step, particle);
        drawLine(player, world, maxX, minY, maxZ, maxX, maxY, maxZ, step, particle);
    }

    private static void drawLine(Player player, World world, double x1, double y1, double z1, 
                                 double x2, double y2, double z2, double step, Particle particle) {
        double distance = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2) + Math.pow(z2 - z1, 2));
        int points = (int) (distance / step);

        for (int i = 0; i <= points; i++) {
            double ratio = (double) i / points;
            double x = x1 + (x2 - x1) * ratio;
            double y = y1 + (y2 - y1) * ratio;
            double z = z1 + (z2 - z1) * ratio;

            Location loc = new Location(world, x, y, z);
            player.spawnParticle(particle, loc, 1, 0, 0, 0, 0);
        }
    }
}
