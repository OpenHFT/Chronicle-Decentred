package net.openhft.chronicle.decentred.dto;

public class ApplicationErrorResponse extends VanillaSignedMessage<ApplicationErrorResponse> {
    private VanillaSignedMessage origMessage;
    private String reason;

    public String reason() {
        return reason;
    }

    public ApplicationErrorResponse reason(String reason) {
        assert !signed();
        this.reason = reason;
        return this;
    }

    public ApplicationErrorResponse init(VanillaSignedMessage origMessage, String reason) {
        assert !signed();
        this.origMessage = origMessage;
        this.reason = reason;
        return this;
    }

    public VanillaSignedMessage origMessage() {
        return origMessage;
    }
}
