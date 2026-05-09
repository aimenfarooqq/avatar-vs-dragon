public class Sword extends GameObject{

    /**
     * Create a "default" GameObject.  Feel free to change these defaults,
     * or add other constructors to let you set them as arguments.
     *
     * @param location
     */
    public Sword(Location location) {
        super(location, "sword.png", 40, 40);
    }

    public void move(){
        setLocationX(getCenterX() + 15);
    }
}
