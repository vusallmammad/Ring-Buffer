final class Entry<T> {
    long seq;
    T value;

    Entry(long seq, T value) {
        this.seq = seq;
        this.value = value;
    }
}