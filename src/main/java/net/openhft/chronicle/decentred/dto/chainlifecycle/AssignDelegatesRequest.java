package net.openhft.chronicle.decentred.dto.chainlifecycle;

import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.decentred.dto.base.VanillaSignedMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;

// Support for this will be added later.
public class AssignDelegatesRequest extends VanillaSignedMessage<AssignDelegatesRequest> {

    private List<BytesStore> delegates;

    public List<BytesStore> delegates() {
        return Collections.unmodifiableList(delegates);
    }

    public AssignDelegatesRequest delegates(List<BytesStore> delegates) {
        assertNotSigned();
        this.delegates = new ArrayList<>(requireNonNull(delegates));
        return this;
    }
}
