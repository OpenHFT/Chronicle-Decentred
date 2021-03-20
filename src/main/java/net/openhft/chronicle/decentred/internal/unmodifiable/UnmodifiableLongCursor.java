package net.openhft.chronicle.decentred.internal.unmodifiable;

import com.koloboke.collect.LongCursor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.function.LongConsumer;

import static net.openhft.chronicle.decentred.internal.unmodifiable.ThrowUtil.newUnsupportedOperationException;

public final class UnmodifiableLongCursor implements LongCursor {

    private final LongCursor inner;

    public UnmodifiableLongCursor(@NotNull LongCursor inner) {
        this.inner = inner;
    }

    @Override public void forEachForward(@Nonnull LongConsumer action) { inner.forEachForward(action); }
    @Override public long elem() { return inner.elem(); }
    @Override public boolean moveNext() { return inner.moveNext(); }
    @Override public void remove() { throw newUnsupportedOperationException(); }
}
