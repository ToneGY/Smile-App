package com.example.smile.entity.Elearning;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public class SerializableHashMap implements Serializable {
    private HashMap<Integer, List<AssignmentEntity>> map;

    public HashMap<Integer, List<AssignmentEntity>> getMap() {
        return map;
    }

    public void setMap(HashMap<Integer, List<AssignmentEntity>> map) {
        this.map = map;
    }
}
