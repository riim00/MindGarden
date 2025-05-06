package mindgarden.model;

public class User {
    private int id;
    private String username;
    private String email;
    private String password;

    // Constructors
    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public User(int id, String username, String email, String password) {
        this(username, email, password);
        this.id = id;
    }

    // Getters
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
}