package com.example.smile.sqlite;

import android.database.Cursor;

public interface DBCallback <T> {
    T cursorToInstance(Cursor cursor);
}
