package wiresegal.fabled.wrappers;

import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author WireSegal
 * Created at 4:06 PM on 4/15/18.
 */
public class CommandExecutor implements ICommandSender {
    @Nonnull
    private final EntityLivingBase sender;

    public CommandExecutor(@Nonnull EntityLivingBase sender) {
        this.sender = sender;
    }

    @Override
    public boolean canUseCommand(int permLevel, @Nonnull String commandName) {
        return permLevel <= 2; // The same level as a command block.
    }

    // Wrapper methods

    @Nonnull
    @Override
    public String getName() {
        return sender.getName();
    }

    @Nonnull
    @Override
    public World getEntityWorld() {
        return sender.world;
    }

    @Nullable
    @Override
    public MinecraftServer getServer() {
        return sender.getServer();
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return sender.getDisplayName();
    }

    @Override
    public void sendMessage(ITextComponent component) {
        sender.sendMessage(component);
    }

    @Nonnull
    @Override
    public BlockPos getPosition() {
        return sender.getPosition();
    }

    @Nonnull
    @Override
    public Vec3d getPositionVector() {
        return sender.getPositionVector();
    }

    @Nullable
    @Override
    public Entity getCommandSenderEntity() {
        return sender;
    }

    @Override
    public void setCommandStat(CommandResultStats.Type type, int amount) {
        sender.setCommandStat(type, amount);
    }
}
