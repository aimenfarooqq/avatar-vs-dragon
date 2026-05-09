public class Scout extends Avatar{
    /**
     * Create a "default" GameObject.  Feel free to change these defaults,
     * or add other constructors to let you set them as arguments.
     *
     * @param location
     * @param imageFilename
     * @param height
     * @param width
     */
    private int shield;

    public Scout(Location location, String imageFilename, int height, int width) {
        super(location, imageFilename, height, width);
        shield = 8;
    }

    public void loseLife(){
        if(shield > 0){
            shield--;
        }
        else{
            lives --;
        }
    }
}
