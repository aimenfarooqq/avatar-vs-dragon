import java.awt.*;

/** A GameObject represents a generic object on the game canvas.  It could be
 * something that represents you (your character or player), another character who
 * might be a friend or an enemy, or some object in the environment that you might
 * interact with in some way: food, a wall, something you can pick up and carry
 * around, a vehicle you can get in and drive around, a projectile of some sort, etc.
 *
 * Each object has an (x, y) location, an image filename used to display
 * the object on the canvas, a width and height (images will be automatically
 * resized to fit in the width/height dimensions), and a border color.  The border
 * might be used to show some additional information about an object, such as whether
 * it is "on" or "active" or how much energy it has left.  The border color could
 * be used in a variety of different ways, or you can set it to null to hide the border.
 */
public class GameObject {
    /** The location of the CENTER of the object (image). */
    private Location location;

    /** The image filename to use for this object. */
    private String imageFilename;

    /** The border color to draw on the image. */
    private Color borderColor;

    /** The width and height of the image. */
    private int height, width;

    /** Create a "default" GameObject.  Feel free to change these defaults,
     * or add other constructors to let you set them as arguments.
     */
    public GameObject(Location location, String imageFilename, int height, int width) {
        this.location = location;
        this.imageFilename = imageFilename;
        this.height = height;
        this.width = width;
        borderColor = null;
    }




    /** GameObjects are drawn as an image with a 2-pixel border.  Feel free to change
     * this code here or override in a subclass.
     */
    public void draw(SimpleCanvas canvas)
    {
        // Can adjust the size of the border with the 2 and 4 below.
        if (borderColor != null) {  // draw if color is not null, otherwise we have no border.
            canvas.drawFilledRectangle(getLeftX() - 2, getTopY() - 2, width + 4, height + 4);
        }
        canvas.drawImage(getLeftX(), getTopY(), imageFilename, width, height);

    }

    /**
     * Return true if this GameObject overlaps the other GameObject.  You can do this by
     * using some geometry & math.  Work it out on paper.  Hint: two objects overlap if they
     * overlap in the X dimension and the Y dimension.
     */
    public boolean overlaps(GameObject other) {
        boolean overlapX=getRightX()>other.getLeftX() && getLeftX()< other.getRightX();
        boolean overlapY=getBottomY()> other.getTopY() && getTopY()< other.getBottomY();
        return overlapX && overlapY;
    }

    /** Return true if this point (x, y) is inside the bounds of this GameObject. */
    public boolean isInside(int x, int y) {
        if (x > getLeftX() && x < getRightX() && y > getTopY() && y < getBottomY()){
            return true;
        }
        else {
            return false;
        }
    }

    /** Get the x-coordinate of the left edge of this object. */
    public int getLeftX() {
        return location.getX() - width/2;
    }

    /** Get the x-coordinate of the right edge of this object. */
    public int getRightX() {
        return location.getX() + width/2;
    }

    /** Get the y-coordinate of the top edge of this object. */
    public int getTopY() {
        return location.getY() - height/2;
    }

    /** Get the y-coordinate of the bottom edge of this object. */
    public int getBottomY() {
        return location.getY() + height/2;
    }

    /** Get the x-coordinate of the right edge of this object. */
    public int getCenterX() {
        return location.getX();
    }

    /** Get the y-coordinate of the top edge of this object. */
    public int getCenterY() {
        return location.getY();
    }

    /** Get this GameObject's location. */
    public Location getLocation() {
        return location;
    }

    /** Set this GameObject's location. */
    public void setLocation(Location location) {
        this.location = location;
    }

    /** Set this GameObject's location's x coordinate. */
    public void setLocationX(int x) {
        this.location.setX(x);
    }

    /** Set this GameObject's location's y coordinate. */
    public void setLocationY(int y) {
        this.location.setY(y);
    }

    /** Get this GameObject's image filename. */
    public String getImageFilename() {
        return imageFilename;
    }

    /** Set this GameObject's location. */
    public void setImageFilename(String imageFilename) {
        this.imageFilename = imageFilename;
    }

    /** Get this GameObject's border color.  null indicates no border. */
    public Color getBorderColor() {
        return borderColor;
    }

    /** Set this GameObject's border color.  null indicates no border. */
    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
    }
    
    /** Remove this GameObject's border entirely (equivalent to setting it to null). */
    public void removeBorder() {
        this.borderColor = null;
    }

    /** Get this GameObject's height. */
    public int getHeight() {
        return height;
    }

    /** Set this GameObject's height. */
    public void setHeight(int height) {
        this.height = height;
    }

    /** Get this GameObject's width. */
    public int getWidth() {
        return width;
    }

    /** Set this GameObject's width. */
    public void setWidth(int width) {
        this.width = width;
    }
}
