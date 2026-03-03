import java.util.Optional;

public final class ReadResult<T> {
    private final Optional<T> value;
    private final long countMissed;

    private ReadResult(Optional<T> value, long countMissed) {
        this.value = value;
        this.countMissed = countMissed;
    }

    public static <T> ReadResult<T> empty() {
        return new ReadResult<>(Optional.empty(), 0);
    }

    public static <T> ReadResult<T> value(T v, long countMissed) {
        return new ReadResult<>(Optional.of(v), countMissed);
    }

    public Optional<T> value() {
        return value;
    }

    public long countMissed() {
        return countMissed;
    }

    @Override
    public String toString() {
        return "ReadResult{value=" + value + ", countMissed=" + countMissed + "}";
    }
}