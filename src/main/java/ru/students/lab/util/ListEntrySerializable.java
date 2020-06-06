package ru.students.lab.util;

import ru.students.lab.models.Dragon;

import java.io.Serializable;

/**
 * Class for sending back the entries of the map serialized
 * */
public class ListEntrySerializable implements Serializable {
    private static final long serialVersionUID = -770990256082901507L;
    private final int key;
    private final Dragon dragon;
    public ListEntrySerializable(int key, Dragon dragon) {
        this.key = key;
        this.dragon = dragon;
        this.dragon.setKey(this.key);
    }
    public Dragon getDragon() {
        return dragon;
    }

    public int getKey() {
        return key;
    }
}
