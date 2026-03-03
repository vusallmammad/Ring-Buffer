import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class RingBuffer<T> {
    private final int capacity;
    private final Entry<T>[] entries;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private long writeSeq = 0;

    @SuppressWarnings("unchecked")
    public RingBuffer(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("capacity must be > 0");
        this.capacity = capacity;
        this.entries = (Entry<T>[]) new Entry[capacity];
        for (int i = 0; i < capacity; i++) {
            entries[i] = new Entry<>(-1, null);
        }
    }

    public int capacity() {
        return capacity;
    }

    public RingWriter<T> writer() {
        return new RingWriter<>(this);
    }

    public RingReader<T> createReaderAtOldest() {
        lock.readLock().lock();
        try {
            long oldest = oldestSeqUnsafe();
            return new RingReader<>(this, oldest);
        } finally {
            lock.readLock().unlock();
        }
    }

    public RingReader<T> createReaderAtLatest() {
        lock.readLock().lock();
        try {
            return new RingReader<>(this, writeSeq);
        } finally {
            lock.readLock().unlock();
        }
    }

    void writeInternal(T item) {
        Objects.requireNonNull(item, "item");
        lock.writeLock().lock();
        try {
            long seq = writeSeq;
            int idx = indexOf(seq);

            entries[idx].seq = seq;
            entries[idx].value = item;

            writeSeq = seq + 1;
        } finally {
            lock.writeLock().unlock();
        }
    }

    ReadResult<T> readInternal(RingReader<T> reader) {
        lock.readLock().lock();
        try {
            long currentWrite = writeSeq;
            long oldest = oldestSeqUnsafe();

            long missed = 0;
            long next = reader.nextSeqUnsafe();

            if (next < oldest) {
                missed = oldest - next;
                next = oldest;
                reader.setNextSeqUnsafe(next);
            }

            if (next >= currentWrite) {
                return ReadResult.empty();
            }

            int idx = indexOf(next);
            Entry<T> e = entries[idx];

            if (e.seq != next) {
                long adjustedOldest = oldestSeqUnsafe();
                if (next < adjustedOldest) {
                    missed += adjustedOldest - next;
                    next = adjustedOldest;
                    reader.setNextSeqUnsafe(next);
                }
                if (next >= writeSeq) return ReadResult.empty();

                idx = indexOf(next);
                e = entries[idx];
                if (e.seq != next) return ReadResult.empty();
            }

            T value = e.value;
            reader.setNextSeqUnsafe(next + 1);
            return ReadResult.value(value, missed);
        } finally {
            lock.readLock().unlock();
        }
    }

    private int indexOf(long seq) {
        return (int) (seq % capacity);
    }

    private long oldestSeqUnsafe() {
        long w = writeSeq;
        long oldest = w - capacity;
        return Math.max(0, oldest);
    }
}