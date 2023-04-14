package com.github.karixdev.scheduleservice.shared.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RealmAccessMap extends HashMap<String, List<String>> {
    public RealmAccessMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public RealmAccessMap(int initialCapacity) {
        super(initialCapacity);
    }

    public RealmAccessMap() {
        super();
    }

    public RealmAccessMap(Map<? extends String, ? extends List<String>> m) {
        super(m);
    }
}
