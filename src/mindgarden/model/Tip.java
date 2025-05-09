package mindgarden.model;



public class Tip {
    private int id;
    private String content;

    public Tip(int id, String content) {
        this.id = id;
        this.content = content;
    }

    public int getId() { return id; }
    public String getContent() { return content; }
}
