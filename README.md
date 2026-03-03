# RingBuffer — Lock-Based Concurrent Ring Buffer in Java

A lightweight, thread-safe, generic **ring buffer** implementation in Java.  
Multiple independent readers can consume messages while a single writer publishes items.  
Slow readers are **not** blocked but they simply skip missed entries.

---

## Project Overview

This project implements a **concurrent ring buffer** designed for a **single-writer / multi-reader** pattern.

Key characteristics:
- **Fixed capacity** — the buffer holds at most `N` entries at a time (old ones are overwritten)
- **Non-blocking readers** — a slow reader is never blocked by the writer. Instead, it simply skips ahead and is told how many entries it missed (`countMissed`).
- **Multiple independent readers** — each `RingReader` tracks its own read position independently.
- **Thread safety** — a `ReadWriteLock` (`ReentrantReadWriteLock`) guards all state.

---

## Design & Class Responsibilities

| Class | Responsibility |
|---|---|
| `RingBuffer<T>` | Core data structure. Owns the fixed-size `Entry[]` array, the global `writeSeq` counter, and the `ReadWriteLock`. Exposes `writeInternal()` and `readInternal()` used by writer/reader delegates. |
| `Entry<T>` | Data holder: stores a sequence number (`seq`) and a value (`value`). One slot in the ring array. |
| `RingWriter<T>` | Thin facade over `RingBuffer`. Exposes a single `write(T item)` method. Separates write concern from the buffer internals. |
| `RingReader<T>` | Tracks a reader's current position (`nextSeq`). Exposes `read()` which delegates to `RingBuffer.readInternal()`. Each reader is independent. |
| `ReadResult<T>` | Immutable value object returned by every `read()`. Wraps an `Optional<T>` value and a `countMissed` long indicating how many entries the reader skipped over. |
| `Main` | Demo / test harness. Creates a buffer of capacity 5, one writer and two readers (R1 fast, R2 slow), runs them concurrently and prints output. |

### Key Design Decisions

- **Sequence numbers over raw indices** — every write is assigned a increasing `seq`. Index in array is `seq % capacity`. This makes missed-entry detection trivial: compare the reader's `nextSeq` against `oldestSeq = writeSeq - capacity`.
- **Missed entry detection** — if a reader's `nextSeq` has been overwritten, the buffer advances the reader to `oldestSeq` and records how many entries were missed.
- **ReadWriteLock** — multiple readers can read concurrently (shared read lock); the writer takes an exclusive write lock. This is correct because Java's `ReentrantReadWriteLock` prevents reads during writes and vice versa.

## How to Run / Test

### Project Structure

```
ringbuffer/
├── Entry.java
├── Main.java
├── ReadResult.java
├── RingBuffer.java
├── RingReader.java
├── RingWriter.java
└── README.md
```

### Compile

```bash
# From the project root directory
javac *.java
```

### Run

```bash
java Main
```

### Expected Output

You will see interleaved output from two readers (where R1 is fast, and R2 is slow):

```
R1 read msg-0
R1 read msg-1
R2 read msg-0
R1 read msg-2
R1 read msg-3
R2 missed=3 then read msg-4
...
```
