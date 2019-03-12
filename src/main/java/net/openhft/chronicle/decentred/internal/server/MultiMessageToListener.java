package net.openhft.chronicle.decentred.internal.server;

import net.openhft.chronicle.core.Maths;
import net.openhft.chronicle.decentred.api.MessageToListener;
import net.openhft.chronicle.decentred.dto.base.SignedMessage;
import net.openhft.chronicle.decentred.server.RunningMessageToListener;
import net.openhft.chronicle.decentred.server.SingleMessageToListener;

public class MultiMessageToListener implements RunningMessageToListener {
    final SingleMessageToListener[] messageWriters;
    private final int mask;

    public MultiMessageToListener(int count, MessageToListener xclServer) {
        count = Maths.nextPower2(count, 2);
        this.mask = count - 1;
        this.messageWriters = new SingleMessageToListener[count];
        for (int i = 0; i < count; i++)
            messageWriters[i] = new SingleMessageToListener(xclServer);
    }

    @Override
    public void onMessageTo(long address, SignedMessage message) {
        messageWriters[(int) (Maths.agitate(address) & mask)].onMessageTo(address, message);

    }

    @Override
    public Runnable[] runnables() {
        return messageWriters;
    }
}
