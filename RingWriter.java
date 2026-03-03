public final class RingWriter<T> {
    private final RingBuffer<T> buffer;

    RingWriter(RingBuffer<T> buffer) {
        this.buffer = buffer;
    }

    public void write(T item) {
        buffer.writeInternal(item);
    }
}