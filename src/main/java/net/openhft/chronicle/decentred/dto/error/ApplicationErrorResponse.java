package net.openhft.chronicle.decentred.dto.error;

import net.openhft.chronicle.bytes.BytesIn;
import net.openhft.chronicle.bytes.BytesOut;
import net.openhft.chronicle.core.io.IORuntimeException;
import net.openhft.chronicle.decentred.dto.base.TransientFieldHandler;
import net.openhft.chronicle.decentred.dto.base.VanillaSignedMessage;
import net.openhft.chronicle.decentred.dto.chainevent.EndOfRoundBlockEvent;
import net.openhft.chronicle.decentred.util.DecentredUtil;
import net.openhft.chronicle.decentred.util.DtoParser;
import net.openhft.chronicle.decentred.util.LongU32Writer;
import net.openhft.chronicle.wire.Marshallable;
import net.openhft.chronicle.wire.WireIn;
import net.openhft.chronicle.wire.WireOut;
import org.jetbrains.annotations.NotNull;

public final class ApplicationErrorResponse extends VanillaSignedMessage<ApplicationErrorResponse> {
    private String reason;
    private transient VanillaSignedMessage origMessage;
    private transient DtoParser dtoParser;

    public String reason() {
        return reason;
    }

    public ApplicationErrorResponse reason(String reason) {
        assertNotSigned();
        this.reason = reason;
        return this;
    }

    public ApplicationErrorResponse init(VanillaSignedMessage origMessage, String reason) {
        assertNotSigned();
        this.origMessage = origMessage;
        this.reason = reason;
        return this;
    }

    public VanillaSignedMessage origMessage() {
        return origMessage;
    }


    public DtoParser dtoParser() {
        return dtoParser;
    }

    public ApplicationErrorResponse dtoParser(DtoParser dtoParser) {
        this.dtoParser = dtoParser;
        return this;
    }

    // Handling of transient fields

    private static final TransientFieldHandler<ApplicationErrorResponse> TRANSIENT_FIELD_HANDLER = new CustomTransientFieldHandler();

    @Override
    public TransientFieldHandler<ApplicationErrorResponse> transientFieldHandler() {
        return TRANSIENT_FIELD_HANDLER;
    }

    private static final class CustomTransientFieldHandler implements TransientFieldHandler<ApplicationErrorResponse> {

        @Override
        public void reset(ApplicationErrorResponse original) {
            original.origMessage = null;
            original.dtoParser = null;
        }

        @Override
        public void copy(@NotNull ApplicationErrorResponse original, @NotNull ApplicationErrorResponse target) {
            target.origMessage = original.origMessage;
            target.dtoParser = original.dtoParser;
        }

        @Override
        public void deepCopy(@NotNull ApplicationErrorResponse original, @NotNull ApplicationErrorResponse target) {
            copy(original, target); // Signed messages are immutable
        }

        @Override
        public void writeMarshallableInternal(ApplicationErrorResponse original, BytesOut bytes) {
            original.origMessage.writeMarshallable(bytes);
        }

        @Override
        public void readMarshallable(ApplicationErrorResponse original, WireIn wire) {
            original.origMessage = wire.read("origMessage").object(VanillaSignedMessage.class);
        }

        @Override
        public void writeMarshallable(@NotNull ApplicationErrorResponse original, @NotNull WireOut wire) {
            wire.write("origMessage").object(VanillaSignedMessage.class, original.origMessage);
        }

        @Override
        public void readMarshallable(@NotNull ApplicationErrorResponse original, @NotNull BytesIn bytes) {
            original.origMessage = (VanillaSignedMessage) original.dtoParser.parseOne(bytes);
        }
    }


}
