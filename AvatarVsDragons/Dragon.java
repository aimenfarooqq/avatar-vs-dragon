import java.util.ArrayList;

public class Dragon extends GameObject {
    protected int speedX;
    protected int speedY;
    private int moveTimer;
    private int canvasWidth;
    private int canvasHeight;
    private ArrayList<Fireball>fireballs;
    private int shootTimer;

    public Dragon(Location location,String imageFilename,int height,int width,
                  int canvasWidth,int canvasHeight) {
        super(location,imageFilename,height,width);
        this.canvasWidth= canvasWidth;
        this.canvasHeight= canvasHeight;
        this.fireballs= new ArrayList<Fireball>();
        pickNewDirection();
        moveTimer= 60;
        shootTimer= 50;
    }

    /** Pick a random speed in X and Y. */
    private void pickNewDirection() {
        int newspeedX= (int)(Math.random()* 4)+2;
        int newspeedy= (int)(Math.random() * 4)+2;
        if (Math.random()< 0.5) newspeedX=-newspeedX;
        if (Math.random()< 0.5) newspeedy=-newspeedy;
        speedX= newspeedX;
        speedY= newspeedy;
    }

    public void update(Platform[] platforms) {
        moveTimer= moveTimer-1;
        if (moveTimer<=0) {
            moveTimer= 40+(int)(Math.random()*60);
            pickNewDirection();
        }
        setLocationX(getCenterX()+speedX);
        setLocationY(getCenterY()+speedY);
        if (getLeftX()<=0){
            setLocationX(getWidth()/2);
            speedX=Math.abs(speedX);
        }
        if (getRightX()>= canvasWidth){
            setLocationX(canvasWidth-getWidth()/2);
            speedX=-Math.abs(speedX);
        }
        if (getTopY() <= 0){
            setLocationY(getHeight()/2);
            speedY=Math.abs(speedY);
        }
        if (getBottomY() >= canvasHeight){
            setLocationY(canvasHeight-getHeight()/2);
            speedY=-Math.abs(speedY);
        }
        for (Platform p : platforms) {
            boolean overlapX= getRightX()>p.getX() && getLeftX()< p.getX()+p.getWidth();
            boolean overlapY= getBottomY()>p.getY() && getTopY()<p.getY() + p.getHeight();
            if (overlapX && overlapY) {
                speedX= -speedX;
                speedY= -speedY;
                setLocationX(getCenterX()+speedX* 3);
                setLocationY(getCenterY()+speedY* 3);
                break;
            }
        }

        shootTimer--;
        if (shootTimer<= 0) {
            shootTimer= getShootCooldown();
            shootFireball();
        }

        for (int i= fireballs.size()-1; i>=0; i--) {
            Fireball fb= fireballs.get(i);
            fb.move();
            if (fb.getCenterX()< 0 || fb.getCenterX()> canvasWidth ||
                    fb.getCenterY()< 0 || fb.getCenterY()> canvasHeight) {
                fireballs.remove(i);
            }
        }
    }

    protected void shootFireball() {
        double angle=Math.random()*2*Math.PI;   // any direction
        int spd=getFireballSpeed();
        int fbSpeedX=(int)Math.round(Math.cos(angle)*spd);
        int fbSpeedY=(int)Math.round(Math.sin(angle)*spd);
        Location loc=new Location(getCenterX(),getCenterY());
        fireballs.add(new Fireball(loc,"fireball1.png",30,30,fbSpeedX,fbSpeedY));
    }

    protected int getFireballSpeed() {
        return 7;
    }
    protected int getShootCooldown() {
        return 60;
    }

    public void drawAll(SimpleCanvas canvas) {
        draw(canvas);
        for (Fireball fb:fireballs)fb.draw(canvas);
    }

    public ArrayList<Fireball> getFireballs() {
        return fireballs;
    }
}
