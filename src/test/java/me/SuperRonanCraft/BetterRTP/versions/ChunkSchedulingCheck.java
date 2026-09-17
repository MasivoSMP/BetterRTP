package me.SuperRonanCraft.BetterRTP.versions;

import com.tcoded.folialib.impl.ServerImplementation;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

import java.lang.reflect.Proxy;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/** Run with assertions enabled; no server or test framework required. */
public class ChunkSchedulingCheck {
    public interface AsyncWorld extends World {
        CompletableFuture<Chunk> getChunkAtAsync(int x, int z, boolean generate);
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        AtomicBoolean ownsRegion = new AtomicBoolean();
        AtomicInteger inspected = new AtomicInteger();
        AtomicReference<Runnable> pending = new AtomicReference<>();
        AtomicReference<CompletableFuture<Chunk>> load = new AtomicReference<>(new CompletableFuture<>());
        RuntimeException broken = new IllegalStateException("chunk load failed");
        ServerImplementation scheduler = (ServerImplementation) Proxy.newProxyInstance(
                ChunkSchedulingCheck.class.getClassLoader(), new Class<?>[]{ServerImplementation.class},
                (proxy, method, arguments) -> {
                    assert method.getName().equals("runAtLocation") : "Must use the destination region";
                    Location target = (Location) arguments[0];
                    assert target.getBlockX() == -84849 && target.getBlockZ() == -26369;
                    Consumer<WrappedTask> action = (Consumer<WrappedTask>) arguments[1];
                    CompletableFuture<Void> scheduled = new CompletableFuture<>();
                    pending.set(() -> {
                        ownsRegion.set(true);
                        try {
                            action.accept(null);
                            scheduled.complete(null);
                        } finally {
                            ownsRegion.set(false);
                        }
                    });
                    return scheduled;
                });
        java.lang.reflect.InvocationHandler worldHandler = (proxy, method, arguments) -> {
            if (method.getName().equals("getChunkAtAsync")) {
                assert (int) arguments[0] == -5304 && (int) arguments[1] == -1649;
                assert Boolean.TRUE.equals(arguments[2]);
                return load.get();
            }
            if (method.getName().equals("getChunkAt")) {
                assert ownsRegion.get() : "Synchronous chunk access outside its region";
                return null; // The callback need not inspect a real chunk to check ownership.
            }
            throw new UnsupportedOperationException(method.getName());
        };
        World modern = (World) Proxy.newProxyInstance(ChunkSchedulingCheck.class.getClassLoader(),
                new Class<?>[]{AsyncWorld.class}, worldHandler);
        Location location = new Location(modern, -84849, 69, -26369);
        Consumer<Chunk> inspect = chunk -> {
            assert ownsRegion.get() : "Chunk completion does not imply region ownership";
            inspected.incrementAndGet();
        };

        CompletableFuture<Void> result = AsyncHandler.withChunk(scheduler, location, inspect);
        assert pending.get() == null && !result.isDone();
        load.get().complete(null); // Simulate completion from an arbitrary thread.
        assert inspected.get() == 0 && !result.isDone();
        pending.getAndSet(null).run();
        result.join();
        assert inspected.get() == 1;

        load.set(new CompletableFuture<>());
        result = AsyncHandler.withChunk(scheduler, location, inspect);
        load.get().completeExceptionally(broken);
        assert result.isCompletedExceptionally();
        assert pending.get() == null && inspected.get() == 1 : "Never retry a failed load synchronously";

        load.set(CompletableFuture.completedFuture(null));
        result = AsyncHandler.withChunk(scheduler, location, chunk -> { throw broken; });
        pending.getAndSet(null).run();
        assert result.isCompletedExceptionally() : "Inspection failures must reach RTP cleanup";

        World legacy = (World) Proxy.newProxyInstance(ChunkSchedulingCheck.class.getClassLoader(),
                new Class<?>[]{World.class}, worldHandler);
        result = AsyncHandler.withChunk(scheduler, new Location(legacy, -84849, 69, -26369), inspect);
        assert !result.isDone();
        pending.getAndSet(null).run();
        result.join();
        assert inspected.get() == 2 : "Legacy loading must also use its owning scheduler";
        System.out.println("Chunk scheduling checks passed (async, legacy, negative coordinates, failures).");
    }
}
