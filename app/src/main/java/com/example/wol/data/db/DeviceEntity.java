package com.example.wol.data.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "devices",
        foreignKeys = @ForeignKey(
                entity = NetworkEntity.class,
                parentColumns = "id",
                childColumns = "networkId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {
                @Index(value = {"networkId", "name"}, unique = true),
                @Index("networkId")
        }
)
public class DeviceEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public long networkId;

    @NonNull
    public String name;

    @NonNull
    public String ipAddress;

    @NonNull
    public String macAddress;

    public int checkPort;

    public long createdAt;

    public DeviceEntity() {
    }

    public DeviceEntity(long networkId,
                        @NonNull String name,
                        @NonNull String ipAddress,
                        @NonNull String macAddress,
                        int checkPort,
                        long createdAt) {
        this.networkId = networkId;
        this.name = name;
        this.ipAddress = ipAddress;
        this.macAddress = macAddress;
        this.checkPort = checkPort;
        this.createdAt = createdAt;
    }
}