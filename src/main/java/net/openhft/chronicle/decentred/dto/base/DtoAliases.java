package net.openhft.chronicle.decentred.dto.base;

import net.openhft.chronicle.core.pool.ClassAliasPool;
import net.openhft.chronicle.decentred.dto.*;
import net.openhft.chronicle.decentred.dto.blockevent.EndOfRoundBlockEvent;
import net.openhft.chronicle.decentred.dto.blockevent.TransactionBlockEvent;
import net.openhft.chronicle.decentred.dto.blockevent.TransactionBlockGossipEvent;
import net.openhft.chronicle.decentred.dto.blockevent.TransactionBlockVoteEvent;
import net.openhft.chronicle.decentred.dto.address.CreateAddressEvent;
import net.openhft.chronicle.decentred.dto.address.CreateAddressRequest;
import net.openhft.chronicle.decentred.dto.chainlifecycle.CreateChainRequest;
import net.openhft.chronicle.decentred.dto.address.InvalidationEvent;
import net.openhft.chronicle.decentred.dto.error.ApplicationErrorResponse;

public enum DtoAliases {;

    static {
        ClassAliasPool.CLASS_ALIASES.addAlias(
                ApplicationErrorResponse.class,
                CreateAddressRequest.class,
                CreateAddressEvent.class,
                CreateChainRequest.class,
                InvalidationEvent.class,
                TransactionBlockEvent.class,
                TransactionBlockGossipEvent.class,
                TransactionBlockVoteEvent.class,
                EndOfRoundBlockEvent.class,
                VerificationEvent.class
        );
    }

    public static void addAliases() {
        // static init block does everything.
    }
}
