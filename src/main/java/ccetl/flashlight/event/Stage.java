package ccetl.flashlight.event;

public enum Stage {
    PRE("Pre"),
    POST("Post");

    private final String name;

    Stage(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
