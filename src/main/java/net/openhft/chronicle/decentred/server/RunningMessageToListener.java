package net.openhft.chronicle.decentred.server;

import net.openhft.chronicle.decentred.api.MessageToListener;
import net.openhft.chronicle.decentred.internal.server.MultiMessageToListener;
import net.openhft.chronicle.decentred.internal.server.SingleMessageToListener;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface RunningMessageToListener extends MessageToListener {

    List<Runnable> runnables();

    static RunningMessageToListener createSingle(@NotNull MessageToListener server) {
        return new SingleMessageToListener(server);
    }

    static RunningMessageToListener createMulti(int count, @NotNull MessageToListener server) {
        return new MultiMessageToListener(count, server);
    }

}
