import java.util.concurrent.*;

public final class Main {
    public static void main(String[] args) throws Exception {
        RingBuffer<String> buf = new RingBuffer<>(5);
        RingWriter<String> writer = buf.writer();

        RingReader<String> r1 = buf.createReaderAtOldest();
        RingReader<String> r2 = buf.createReaderAtOldest();

        ExecutorService pool = Executors.newCachedThreadPool();

        pool.submit(() -> {
            for (int i = 0; i < 30; i++) {
                writer.write("msg-" + i);
                sleep(80);
            }
        });

        pool.submit(() -> consume("R1", r1, 40));
        pool.submit(() -> consume("R2", r2, 160)); 

        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);
    }

    private static void consume(String name, RingReader<String> reader, int delayMS) {
        for (int i = 0; i < 40; i++) {
            ReadResult<String> rr = reader.read();
            rr.value().ifPresent(v -> {
                if (rr.countMissed() > 0) {
                    System.out.println(name + " missed=" + rr.countMissed() + " then read " + v);
                } else {
                    System.out.println(name + " read " + v);
                }
            });
            sleep(delayMS);
        }
    }

    private static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}