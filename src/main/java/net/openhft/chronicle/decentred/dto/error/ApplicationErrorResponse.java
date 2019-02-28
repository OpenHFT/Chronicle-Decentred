package net.openhft.chronicle.decentred.dto.error;

import net.openhft.chronicle.bytes.BytesIn;
import net.openhft.chronicle.bytes.BytesOut;
import net.openhft.chronicle.core.io.IORuntimeException;
import net.openhft.chronicle.decentred.dto.base.VanillaSignedMessage;
import net.openhft.chronicle.decentred.util.DtoParser;
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

    @Override
    public void readMarshallable(@NotNull WireIn wire) throws IORuntimeException {
        super.readMarshallable(wire);
        origMessage = wire.read("origMessage").object(VanillaSignedMessage.class);
    }

    @Override
    public void writeMarshallable(@NotNull WireOut wire) {
        super.writeMarshallable(wire);
        wire.write("origMessage").object(VanillaSignedMessage.class, origMessage);
    }

    @Override
    protected void writeMarshallable0(BytesOut bytes) {
        super.writeMarshallable0(bytes);
        origMessage.writeMarshallable(bytes);
    }

    @Override
    public void readMarshallable(BytesIn bytes) throws IORuntimeException {
        super.readMarshallable(bytes);
        origMessage = (VanillaSignedMessage) dtoParser.parseOne(bytes);
    }

    public DtoParser dtoParser() {
        return dtoParser;
    }

    public ApplicationErrorResponse dtoParser(DtoParser dtoParser) {
        this.dtoParser = dtoParser;
        return this;
    }
}
