package net.openhft.chronicle.decentred.server;

import net.openhft.chronicle.core.Maths;
import net.openhft.chronicle.decentred.api.MessageListener;
import net.openhft.chronicle.decentred.dto.SignedMessage;

public class MultiMessageListener implements RunningMessageListener {
    final SingleMessageListener[] messageWriters;
    private final int mask;

    public MultiMessageListener(int count, MessageListener xclServer) {
        count = Maths.nextPower2(count, 2);
        this.mask = count - 1;
        this.messageWriters = new SingleMessageListener[count];
        for (int i = 0; i < count; i++)
            messageWriters[i] = new SingleMessageListener(xclServer);
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
