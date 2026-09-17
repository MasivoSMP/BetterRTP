package me.SuperRonanCraft.BetterRTP.versions;

import com.tcoded.folialib.impl.ServerImplementation;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import me.SuperRonanCraft.BetterRTP.BetterRTP;
import org.bukkit.entity.Entity;
import org.bukkit.Chunk;
import org.bukkit.Location;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class AsyncHandler {

    public static void async(Runnable runnable) {
        getFolia().runAsync(task -> runnable.run());
    }

    public static void sync(Runnable runnable) {
        getFolia().runNextTick(task -> runnable.run());
    }

    public static void syncAtEntity(Entity entity, Runnable runnable) {
        getFolia().runAtEntity(entity, task -> runnable.run());
    }

    /** Load asynchronously when available; always inspect chunks on their owning region. */
    public static CompletableFuture<Void> withChunk(Location location, Consumer<Chunk> action) {
        return withChunk(getFolia(), location, action);
    }

    @SuppressWarnings("unchecked")
    static CompletableFuture<Void> withChunk(ServerImplementation scheduler, Location location, Consumer<Chunk> action) {
        CompletableFuture<Void> result = new CompletableFuture<>();
        try {
            // Capability detection also works with 26.x, unlike PaperLib's 1.x version parser.
            Method load = location.getWorld().getClass().getMethod("getChunkAtAsync", int.class, int.class, boolean.class);
            CompletableFuture<Chunk> loaded = (CompletableFuture<Chunk>) load.invoke(location.getWorld(),
                    location.getBlockX() >> 4, location.getBlockZ() >> 4, true);
            loaded.whenComplete((chunk, failure) -> {
                if (failure != null) result.completeExceptionally(failure);
                else inspectChunk(scheduler, location, action, result);
            });
        } catch (NoSuchMethodException legacyServer) {
            inspectChunk(scheduler, location, action, result);
        } catch (Exception failure) {
            result.completeExceptionally(failure instanceof InvocationTargetException ? failure.getCause() : failure);
        }
        return result;
    }

    private static void inspectChunk(ServerImplementation scheduler, Location location, Consumer<Chunk> action,
                                     CompletableFuture<Void> result) {
        try {
            scheduler.runAtLocation(location, task -> {
                try {
                    // Reacquire on the owner in case the chunk unloaded after async completion.
                    action.accept(location.getChunk());
                    result.complete(null);
                } catch (Throwable failure) {
                    result.completeExceptionally(failure);
                }
            }).exceptionally(failure -> {
                result.completeExceptionally(failure);
                return null;
            });
        } catch (Exception failure) {
            result.completeExceptionally(failure);
        }
    }

    public static WrappedTask asyncLater(Runnable runnable, long ticks) {
        return getFolia().runLaterAsync(runnable, ticks);
    }
    public static WrappedTask syncLater(Runnable runnable, long ticks) {
        return getFolia().runLater(runnable, ticks);
    }

    private static ServerImplementation getFolia() {
        return BetterRTP.getInstance().getFoliaHandler().get();
    }
}
