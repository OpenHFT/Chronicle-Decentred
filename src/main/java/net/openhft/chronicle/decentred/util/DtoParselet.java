package net.openhft.chronicle.decentred.util;

import net.openhft.chronicle.bytes.BytesIn;
import net.openhft.chronicle.core.util.ObjectUtils;
import net.openhft.chronicle.decentred.dto.base.VanillaSignedMessage;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Objects;

class DtoParselet<T> {
    private final Method method;
    private final int protocol;
    private final int midValue;
    private final VanillaSignedMessage vsm;

    public DtoParselet(@NotNull Method method, int protocol, int midValue) {
        this.method = method;
        this.protocol = protocol;
        this.midValue = midValue;
        this.vsm = createVSM(method, protocol, midValue);
    }

    public DtoParselet(@NotNull DtoParselet parselet) {
        this.method = parselet.method;
        this.protocol = parselet.protocol;
        this.midValue = parselet.midValue;
        try {
            this.vsm = createVSM(method, protocol, midValue);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    @NotNull
    private static VanillaSignedMessage createVSM(Method method, int protocol, int messageType) {
        @SuppressWarnings("unchecked")
        final Class<VanillaSignedMessage> type = (Class) method.getParameterTypes()[0];
        final VanillaSignedMessage vsm = ObjectUtils.newInstance(type);
        return vsm.protocol(protocol).messageType(messageType);
    }

    public void parse(BytesIn bytes, T listener) {
        vsm.readMarshallable(bytes);
        try {
            /* System.out.println(String.format("Parslet invoking %s.%s(%s)%n",
                listener.getClass().getName(),
                method.getName(),
                method.getParameterTypes()[0].getSimpleName())
            ); */
            method.invoke(listener, vsm);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public Object parse(BytesIn bytes) {
        vsm.readMarshallable(bytes);
        return vsm;
    }

    @Override
    public String toString() {
        return "DtoParselet{" +
                "method=" + method +
                ", protocol=" + protocol +
                ", midValue=" + midValue +
                ", vsm=" + vsm +
                '}';
    }
}
