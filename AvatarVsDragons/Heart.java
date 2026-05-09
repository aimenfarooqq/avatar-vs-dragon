public class Heart extends GameObject {
    private boolean active;

    public Heart(Location location) {
        super(location,"heart_nobg.png",40,40);
        active=true;
    }
    public void lose() {
        active=false;
    }
    public boolean isActive() {
        return active;
    }
    public void draw(SimpleCanvas canvas) {
        if (active){
            super.draw(canvas);
        }
    }
}