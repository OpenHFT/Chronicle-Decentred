package net.openhft.chronicle.decentred.dto;

import net.openhft.chronicle.bytes.BytesStore;

import java.util.List;

public class AssignDelegatesRequest extends VanillaSignedMessage<AssignDelegatesRequest> {
    List<BytesStore> delegates;

    public List<BytesStore> delegates() {
        return delegates;
    }

    public AssignDelegatesRequest delegates(List<BytesStore> delegates) {
        this.delegates = delegates;
        return this;
    }
}
