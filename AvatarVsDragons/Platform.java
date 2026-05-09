import java.awt.*;

public class Platform {//this class is for creating invisible platforms so that the avatars can jump and stay on top of the platform
    private int x;
    private int y;
    private int height;
    private int width;

    public Platform(int x, int y, int height, int width){
        this.x = x;
        this.y = y;
        this.height= height;
        this.width = width;
    }

    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public void draw(SimpleCanvas canvas){
        canvas.setPenColor(Color.RED);

        canvas.drawFilledRectangle(x, y, width, height);

    }

    public boolean isTop(Avatar avatar){
        return avatar.getBottomY() >= y - 5 && avatar.getBottomY() <= y + 30 &&
                avatar.getCenterX() >= x && avatar.getCenterX() <= x + width ;
    }
    //used chatgpt
    public boolean isWall(Avatar avatar){
        return avatar.getRightX()>= x && avatar.getRightX() <= x + 10 &&
                avatar.getBottomY() >= y + 10 && avatar.getTopY() <= y + height;
    }
}
