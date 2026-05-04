package com.hooniegit.AeronJavaTools.Common;

import lombok.Getter;

/**
 * TagList 데이터를 객체 생성 없이 원시 타입으로 전송하기 위한 버퍼 클래스입니다.
 */
@Getter
public class PrimitiveTagBuffer {

    private final int[] ids;
    private final double[] values;
    private String timestamp;
    private int size = 0;

    public PrimitiveTagBuffer(int capacity) {
        this.ids = new int[capacity];
        this.values = new double[capacity];
    }

    public void add(int id, double value, String timestamp) {
        ids[size] = id;
        values[size] = value;
        if (size == 0) {
            this.timestamp = timestamp;
        }
        size++;
    }

    public void clear() {
        size = 0;
        timestamp = null;
    }

}
