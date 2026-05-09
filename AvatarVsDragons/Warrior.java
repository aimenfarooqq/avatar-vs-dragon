public class Warrior extends Avatar{
    /**
     * Create a "default" GameObject.  Feel free to change these defaults,
     * or add other constructors to let you set them as arguments.
     *
     * @param location
     * @param imageFilename
     * @param height
     * @param width
     */
    public Warrior(Location location, String imageFilename, int height, int width) {
        super(location, imageFilename, height, width);

    }

    public Sword throwSword(){
        return new Sword(new Location(getCenterX(), getCenterY()));
    }
}
