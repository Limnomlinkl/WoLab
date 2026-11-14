package com.example.wol.data.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "networks", indices = {@Index(value = "name", unique = true)})
public class NetworkEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String name;

    public long createdAt;

    public NetworkEntity() {
    }

    public NetworkEntity(@NonNull String name, long createdAt) {
        this.name = name;
        this.createdAt = createdAt;
    }
}