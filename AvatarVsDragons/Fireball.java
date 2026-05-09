public class Fireball extends GameObject {
    private int speedX;
    private int speedY;
    public Fireball(Location location,String imageFilename,int height,int width,int speedX,int speedY) {
        super(location,imageFilename,height,width);
        this.speedX= speedX;
        this.speedY= speedY;
    }
    public void move() {
        setLocationX(getCenterX()+speedX);
        setLocationY(getCenterY()+ speedY);
    }
}
