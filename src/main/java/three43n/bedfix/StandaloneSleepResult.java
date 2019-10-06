package three43n.bedfix;

import com.google.common.base.Predicate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class StandaloneSleepResult {

    public static EntityPlayer.SleepResult trySleep(EntityPlayer p, BlockPos bedLocation) {

        World world = p.getEntityWorld();

        if (!world.isRemote) {
            if (!p.isEntityAlive()) return EntityPlayer.SleepResult.OTHER_PROBLEM;
            if (!world.provider.isSurfaceWorld()) return EntityPlayer.SleepResult.NOT_POSSIBLE_HERE;
            if (world.isDaytime()) return EntityPlayer.SleepResult.NOT_POSSIBLE_NOW;
            if (!net.minecraftforge.event.ForgeEventFactory.fireSleepingTimeCheck(p, p.bedLocation))
                return EntityPlayer.SleepResult.NOT_POSSIBLE_NOW;
            if (!bedInRangeOfPlayer(p, bedLocation))
                return EntityPlayer.SleepResult.TOO_FAR_AWAY;
        }

        double d0 = 8.0D;
        double d1 = 5.0D;
        List<EntityMob> list = p.world.<EntityMob>getEntitiesWithinAABB(
                EntityMob.class,
                new AxisAlignedBB((double) bedLocation.getX() - 8.0D,
                        (double) bedLocation.getY() - 5.0D,
                        (double) bedLocation.getZ() - 8.0D,
                        (double) bedLocation.getX() + 8.0D,
                        (double) bedLocation.getY() + 5.0D,
                        (double) bedLocation.getZ() + 8.0D),
                new Predicate<EntityMob>() {
                    @Override
                    public boolean apply(@Nullable EntityMob input) {
                        if (input != null)
                            return input.isPreventingPlayerRest(p);
                        else return false;
                    }
                });

        if (!list.isEmpty()) {
            return EntityPlayer.SleepResult.NOT_SAFE;
        }

        return EntityPlayer.SleepResult.OK;
    }


    public static boolean bedInRangeOfPlayer(EntityPlayer p, BlockPos bedloc) {
        if (Math.abs(p.posX - (double) bedloc.getX()) <= 3.0D
                && Math.abs(p.posY - (double) bedloc.getY()) <= 2.0D
                && Math.abs(p.posZ - (double) bedloc.getZ()) <= 3.0D)
            return true;

        return false;
    }
}



