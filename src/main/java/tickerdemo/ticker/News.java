package tickerdemo.ticker;

public class News {
    private final long id;
    private final boolean breaking;
    private final long time;
    private final String description;
    private final String text;

    public long getId() {
        return id;
    }

    public boolean isBreaking() {
        return breaking;
    }

    public long getTime() {
        return time;
    }

    public String getDescription() {
        return description;
    }

    public String getText() {
        return text;
    }

    public News(long id, boolean breaking, long time, String description, String text) {
        this.id = id;
        this.breaking = breaking;
        this.time = time;
        this.description = description;
        this.text = text;
    }
}
