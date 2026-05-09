public class Trophy extends GameObject{

    private boolean collected;
    public Trophy(Location location){
        super(location, "trophy.png",45,45);
        collected=false;
    }
    public boolean isCollected() {
        return collected;
    }
    public void collect() {
        collected=true;
    }

    public void draw(SimpleCanvas canvas) {
        if (!collected) {
            super.draw(canvas);
        }
    }
}