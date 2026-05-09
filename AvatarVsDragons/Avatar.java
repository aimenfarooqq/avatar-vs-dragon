public class Avatar extends GameObject{
    protected int lives;
    protected int points;
    protected boolean jumping;
    protected int height;
    protected int width;
    protected int jumpingCount;
    private int groundY;


    /**
     * Create a "default" GameObject.  Feel free to change these defaults,
     * or add other constructors to let you set them as arguments.
     *
     * @param location
     * @param imageFilename
     */
    public Avatar(Location location, String imageFilename, int height, int width) {
        super(location, imageFilename, height, width);
        lives = 5;
        points = 0;
        jumping = false;
        jumpingCount = 0;
        groundY = 0;
    }

    public void move(){
        setLocationX(getCenterX() + 10);

    }

    public void jump(){
        if(!jumping){
            jumping = true;

            jumpingCount = 20;
        }

    }

    public void updateJump(){
        if(jumping) {

            if(jumpingCount > 10) {
                setLocationY(getCenterY() - 6);
            }
            else {
                    setLocationY(getCenterY() + 6);
            }
            jumpingCount--;

            if(jumpingCount == 0){
                setLocationY(groundY);
                jumping = false;
            }

        }
    }

    public void fall(){
        if(!jumping){
            if(getCenterY() < groundY){
                setLocationY(getCenterY() + 3);
            }
        }
    }

    public void stopJump(){ //for avatars not to fall of the platforms
        jumping = false;

        jumpingCount = 0;
    }

    public void setGroundY(int y) {

        groundY = y;
    }

    public boolean overlaps(GameObject other) {
        boolean overlapX=getRightX()>other.getLeftX() && getLeftX()<other.getRightX();
        boolean overlapY=getBottomY()>other.getTopY() && getTopY()<other.getBottomY();
        return overlapX && overlapY; //used chatgpt to see how to return two values
    }

    public boolean isFalling(){
        return jumpingCount <= 10;
    }

    public int getLives(){
        return lives;
    }

    public int getPoints(){
        return points;
    }

    public void addPoint(){
        points++;
    }

    public void loseLife(){
        lives--;
    }

}
