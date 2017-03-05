package fr.hippo;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by hippolyte on 3/5/17.
 */
public class KvstoreMessage {
    private String value;

    @JsonProperty("created_at")
    private double createdAt;

    @JsonProperty("updated_at")
    private double updatedAt;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public double getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(double createdAt) {
        this.createdAt = createdAt;
    }

    public double getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(double updatedAt) {
        this.updatedAt = updatedAt;
    }
}
