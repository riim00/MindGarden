package mindgarden.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a mood entry recorded by the user.
 */
public class MoodEntry {

    private int id;
    private String moodType;
    private LocalDateTime timestamp;
    private String notes;

    /**
     * Constructor for loading an entry without notes (optional).
     */
    public MoodEntry(int id, String moodType, LocalDateTime timestamp) {
        this(id, moodType, timestamp, null);
    }

    /**
     * Full constructor for MoodEntry.
     */
    public MoodEntry(int id, String moodType, LocalDateTime timestamp, String notes) {
        this.id = id;
        this.moodType = moodType;
        this.timestamp = timestamp;
        this.notes = notes;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMoodType() {
        return moodType;
    }

    public void setMoodType(String moodType) {
        this.moodType = moodType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "MoodEntry{" +
                "id=" + id +
                ", moodType='" + moodType + '\'' +
                ", timestamp=" + timestamp +
                ", notes='" + notes + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MoodEntry)) return false;
        MoodEntry that = (MoodEntry) o;
        return id == that.id &&
                Objects.equals(moodType, that.moodType) &&
                Objects.equals(timestamp, that.timestamp) &&
                Objects.equals(notes, that.notes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, moodType, timestamp, notes);
    }
}
