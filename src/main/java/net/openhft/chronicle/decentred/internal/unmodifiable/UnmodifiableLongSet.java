package net.openhft.chronicle.decentred.internal.unmodifiable;

import com.koloboke.collect.LongCursor;
import com.koloboke.collect.LongIterator;
import com.koloboke.collect.set.LongSet;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.LongConsumer;
import java.util.function.LongPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static net.openhft.chronicle.decentred.internal.unmodifiable.ThrowUtil.newUnsupportedOperationException;

public final class UnmodifiableLongSet implements LongSet {

    private final LongSet inner;

    public UnmodifiableLongSet(@NotNull LongSet inner) {
        this.inner = inner;
    }

    @Override @Deprecated public boolean add(@Nonnull Long e) { throw newUnsupportedOperationException(); }
    @Override @Nonnull @Deprecated public LongIterator iterator() { return inner.iterator(); } // The iterator returned does not support remove()
    @Override @Deprecated public boolean contains(Object o) { return inner.contains(o); }
    @Override public boolean contains(long v) { return inner.contains(v); }
    @Override @Nonnull @Deprecated public Object[] toArray() { return inner.toArray(); }
    @Override @Nonnull @Deprecated public <T> T[] toArray(@Nonnull T[] array) { return inner.toArray(array); }
    @Override @Nonnull public long[] toLongArray() { return inner.toLongArray(); }
    @Override @Nonnull public long[] toArray(@Nonnull long[] a) { return inner.toArray(a); }
    @Override @Nonnull public LongCursor cursor() { return new UnmodifiableLongCursor(inner.cursor()); }
    @Override @Deprecated public void forEach(@Nonnull Consumer<? super Long> action) { inner.forEach(action); }
    @Override public void forEach(@Nonnull LongConsumer action) { inner.forEach(action); }
    @Override public boolean forEachWhile(@Nonnull LongPredicate predicate) { return inner.forEachWhile(predicate); }
    @Override public boolean add(long e) { throw newUnsupportedOperationException(); }
    @Override @Deprecated public boolean remove(Object o) { throw newUnsupportedOperationException(); }
    @Override public boolean removeLong(long v) { throw newUnsupportedOperationException(); }
    @Override @Deprecated public boolean removeIf(@Nonnull Predicate<? super Long> filter) { throw newUnsupportedOperationException(); }
    @Override public boolean removeIf(@Nonnull LongPredicate filter) { throw newUnsupportedOperationException(); }
    @Override public int size() { return inner.size(); }
    @Override public boolean isEmpty() { return inner.isEmpty(); }
    @Override public boolean containsAll(@NotNull Collection<?> c) { return inner.containsAll(c); }
    @Override public boolean addAll(@NotNull Collection<? extends Long> c) { throw newUnsupportedOperationException(); }
    @Override public boolean removeAll(@NotNull Collection<?> c) { throw newUnsupportedOperationException(); }
    @Override public boolean retainAll(@NotNull Collection<?> c) { throw newUnsupportedOperationException(); }
    @Override public void clear() { throw newUnsupportedOperationException(); }
    @Override public boolean equals(Object o) { return inner.equals(o); }
    @Override public int hashCode() { return inner.hashCode(); }
    @Override public Spliterator<Long> spliterator() { return inner.spliterator(); }
    @Override public Stream<Long> stream() { return inner.stream(); }
    @Override public Stream<Long> parallelStream() { return inner.parallelStream(); }
    @Override public long sizeAsLong() { return inner.sizeAsLong(); }
    @Override public boolean ensureCapacity(long minSize) { throw newUnsupportedOperationException(); }
    @Override public boolean shrink() { throw newUnsupportedOperationException(); }

}
