public final class RingReader<T> {
    private final RingBuffer<T> buffer;
    private long nextSeq;

    RingReader(RingBuffer<T> buffer, long startSeq) {
        this.buffer = buffer;
        this.nextSeq = startSeq;
    }

    public ReadResult<T> read() {
        return buffer.readInternal(this);
    }

    public long position() {
        return nextSeq;
    }

    long nextSeqUnsafe() {
        return nextSeq;
    }

    void setNextSeqUnsafe(long v) {
        this.nextSeq = v;
    }
}