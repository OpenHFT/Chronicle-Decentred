package net.openhft.chronicle.decentred.internal.server;

import net.openhft.chronicle.core.Maths;
import net.openhft.chronicle.decentred.api.MessageToListener;
import net.openhft.chronicle.decentred.dto.base.SignedMessage;
import net.openhft.chronicle.decentred.server.RunningMessageToListener;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

public final class MultiMessageToListener implements RunningMessageToListener {

    private final SingleMessageToListener[] messageWriters;
    private final List<Runnable> runnables;
    private final int mask;

    public MultiMessageToListener(int count, @NotNull MessageToListener server) {
        count = Maths.nextPower2(count, 2);
        this.mask = count - 1;
        messageWriters = IntStream.range(0, count)
            .mapToObj(i -> new SingleMessageToListener(server))
            .toArray(SingleMessageToListener[]::new);
        runnables = Stream.of(messageWriters)
            .map(Runnable.class::cast)
            .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    @Override
    public void onMessageTo(long address, SignedMessage message) {
        messageWriters[(int) (Maths.agitate(address) & mask)].onMessageTo(address, message);
    }

    @Override
    public List<Runnable> runnables() {
        return runnables;
    }
}
