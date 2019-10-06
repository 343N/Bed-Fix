package three43n.bedfix;

import ibxm.Player;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.Logger;

import java.awt.*;

import static com.ibm.icu.util.Region.RegionType.WORLD;
import static net.minecraftforge.registries.GameData.ITEMS;

@Mod(modid = BedFix.ModID, name = BedFix.Name)
public class BedFix {
    public static final String ModID = "bedbugworkaround";
    public static final String Name = "Bed Bug Workaround";
    public static final String DESC = "Makes beds imitate bed behaviour without actually getting you in " +
            "the bed, avoiding a game-breaking bug.";

    private static Logger logger;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new BedFixHandlers());
    }

    public class BedFixHandlers {

        @SubscribeEvent
        public void overrideSleep(PlayerInteractEvent.RightClickBlock event) {

            boolean isRemote = event.getWorld().isRemote;
            Block b = event.getWorld().getBlockState(event.getPos()).getBlock();
            if (b != Blocks.BED) return;
            if (isRemote) return;


            BlockPos bpos = event.getPos();
            World w = event.getWorld();
            EntityPlayer p = event.getEntityPlayer();
            if (w.playerEntities.size() > 1) return;
            WorldProvider.WorldSleepResult wr = w.provider.canSleepAt(p, bpos);
            EntityPlayer.SleepResult pr = StandaloneSleepResult.trySleep(p, bpos);

            processSleepResults(event.getEntityPlayer(), w, bpos, wr, pr);


            event.setResult(Event.Result.DENY);
            if (event.isCancelable()) event.setCanceled(true);

        }
    }

    public void processSleepResults(EntityPlayer p, World w, BlockPos b, WorldProvider.WorldSleepResult wr,
                                    EntityPlayer.SleepResult pr) {

        if (wr == wr.BED_EXPLODES)
            explodeBed(b, w);
        if (wr == wr.DENY)
            p.sendStatusMessage(new TextComponentTranslation("tile.bed.noSleep", new Object[0]), true);
        if (wr == wr.ALLOW)
            if (pr == EntityPlayer.SleepResult.OK) {
                w.setWorldTime(0);
                w.getWorldInfo().setRaining(false);
                p.bedLocation = b;
                p.setSpawnPoint(p.bedLocation, false);
                new PlayerWakeUpEvent(p, true, true, true);
            } else printToPlayer(p, pr);

    }

    public void explodeBed(BlockPos b, World w) {
        if (w.getBlockState(b).getBlock() == Blocks.BED) w.setBlockToAir(b);

        w.newExplosion((Entity) null,
                (double) b.getX() + 0.5D,
                (double) b.getY() + 0.5D,
                (double) b.getZ() + 0.5D,
                5.0F,
                true,
                true);
    }

    public void printToPlayer(EntityPlayer p, EntityPlayer.SleepResult sr) {
        switch (sr) {
            case NOT_POSSIBLE_NOW:
                p.sendStatusMessage(new TextComponentTranslation("tile.bed.noSleep", new Object[0]), true);
                break;
            case NOT_SAFE:
                p.sendStatusMessage(new TextComponentTranslation("tile.bed.notSafe", new Object[0]), true);
                break;
            case TOO_FAR_AWAY:
                p.sendStatusMessage(new TextComponentTranslation("tile.bed.tooFarAway", new Object[0]), true);
                break;
        }
    }
}



