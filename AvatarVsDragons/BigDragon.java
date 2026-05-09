public class BigDragon extends Dragon {

    public BigDragon(Location location,String imageFilename,int height,int width,int canvasWidth,int canvasHeight){
        super(location,imageFilename,height,width,canvasWidth,canvasHeight);
    }
    @Override
    protected void shootFireball() {
        double angle=Math.random()*2*Math.PI;
        int spd=getFireballSpeed();
        int fbSpeedX=(int) Math.round(Math.cos(angle)*spd);
        int fbSpeedY=(int) Math.round(Math.sin(angle)*spd);
        Location loc=new Location(getCenterX(),getCenterY());
        getFireballs().add(new Fireball(loc,"fireball2.png",55,55,fbSpeedX,fbSpeedY));
    }

    @Override
    protected int getFireballSpeed() {
        return 10;
    }

    @Override
    protected int getShootCooldown() {
        return 90;
    }
}