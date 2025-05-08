package mindgarden.model;

import java.time.LocalDate;

public class Goal {
    private int id;
    private int userId;
    private String title;
    private String type;
    private String description;
    private LocalDate startDate;
    private LocalDate deadline;
    private boolean reminder;
    private boolean trackProgress;
    private boolean notifyOnCompletion;

    public Goal(String title, String type, String description, LocalDate startDate, LocalDate deadline,
                boolean reminder, boolean trackProgress, boolean notifyOnCompletion) {
        this.title = title;
        this.type = type;
        this.description = description;
        this.startDate = startDate;
        this.deadline = deadline;
        this.reminder = reminder;
        this.trackProgress = trackProgress;
        this.notifyOnCompletion = notifyOnCompletion;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public boolean isReminder() {
        return reminder;
    }

    public void setReminder(boolean reminder) {
        this.reminder = reminder;
    }

    public boolean isTrackProgress() {
        return trackProgress;
    }

    public void setTrackProgress(boolean trackProgress) {
        this.trackProgress = trackProgress;
    }

    public boolean isNotifyOnCompletion() {
        return notifyOnCompletion;
    }

    public void setNotifyOnCompletion(boolean notifyOnCompletion) {
        this.notifyOnCompletion = notifyOnCompletion;
    }

}
