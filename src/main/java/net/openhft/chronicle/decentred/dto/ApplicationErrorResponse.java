package net.openhft.chronicle.decentred.dto;

import net.openhft.chronicle.bytes.BytesIn;
import net.openhft.chronicle.bytes.BytesOut;
import net.openhft.chronicle.core.io.IORuntimeException;
import net.openhft.chronicle.decentred.util.DtoParser;
import net.openhft.chronicle.decentred.util.DtoRegistry;
import net.openhft.chronicle.wire.WireIn;
import net.openhft.chronicle.wire.WireOut;
import org.jetbrains.annotations.NotNull;

public class ApplicationErrorResponse extends VanillaSignedMessage<ApplicationErrorResponse> {
    private String reason;
    private transient DtoRegistry dtoRegistry;
    private transient DtoParser dtoParser;
    private transient SignedMessage origMessage;

    public String reason() {
        return reason;
    }

    public ApplicationErrorResponse reason(String reason) {
        assert !signed();
        this.reason = reason;
        return this;
    }

    public ApplicationErrorResponse init(SignedMessage origMessage, String reason) {
        assert !signed();
        this.origMessage = origMessage;
        this.reason = reason;
        return this;
    }

    public SignedMessage origMessage() {
        return origMessage;
    }

    @Override
    public void readMarshallable(@NotNull WireIn wire) throws IORuntimeException {
        super.readMarshallable(wire);
        origMessage = wire.read("origMessage").object(VanillaSignedMessage.class);
    }

    @Override
    public void writeMarshallable(@NotNull WireOut wire) {
        super.writeMarshallable(wire);
        wire.write("origMessage").object(origMessage);
    }

    @Override
    protected void readMarshallable0(BytesIn bytes) {
        super.readMarshallable0(bytes);
        int length = bytes.readInt();
        bytes.readLimit(bytes.readPosition() + length);
        if (dtoParser == null)
            dtoParser = dtoRegistry.get();
        origMessage = dtoParser.parseOne(bytes);
    }

    @Override
    protected void writeMarshallable0(BytesOut bytes) {
        super.writeMarshallable0(bytes);
        if (!origMessage.signed())
            throw new IllegalStateException("origMessage is not signed");
        bytes.comment("origMessage.length");
        VanillaSignedMessage vsm = (VanillaSignedMessage) origMessage;
        bytes.writeUnsignedInt(vsm.bytes.readRemaining());
        bytes.comment("origMessage");
        bytes.write(vsm.bytes);
    }

    @Override
    public ApplicationErrorResponse dtoRegistry(DtoRegistry dtoRegistry) {
        this.dtoRegistry = dtoRegistry;
        return this;
    }
}
