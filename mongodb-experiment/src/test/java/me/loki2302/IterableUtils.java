package me.loki2302;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class IterableUtils {
    public static <T> Stream<T> streamFromIterable(Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    public static <T> List<T> listFromIterable(Iterable<T> iterable) {
        return streamFromIterable(iterable).collect(Collectors.toList());
    }

    public static <T> Set<T> setFromIterable(Iterable<T> iterable) {
        return streamFromIterable(iterable).collect(Collectors.toSet());
    }
}
