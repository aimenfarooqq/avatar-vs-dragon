import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

/** This class represents a generic game on a canvas.  */
public class Game{
    private SimpleCanvas canvas;
    private boolean isVisible;
    private Avatar avatar1;
    private Avatar avatar2;
    private Avatar avatar3;
    private Avatar selectedAvatar;
    private Platform ground;
    private Platform platform1;
    private Platform platform2;
    private Platform platform3;
    private Platform platform4;
    private Platform platform5;
    private Platform platform6;
    private Platform platform7;
    private Platform platform8;
    private Platform platform9;
    private Platform platform10;
    private ArrayList<Sword> swords;
    private boolean spacePressed;
    protected boolean inCave=false;
    private Dragon dragon1;
    private Dragon dragon2;
    private BigDragon bigDragon;
    private Platform[] platforms;
    private Dragon dragon3;
    private Dragon dragon4;
    private Dragon dragon5;
    private boolean gameOver=false;
    private Heart[] hearts;
    private Coin[] coins;
    private Trophy trophy;
    private int score;
    private boolean gameWon = false;
    private boolean dragon1Alive= true;
    private boolean dragon2Alive= true;
    private boolean dragon3Alive= true;
    private boolean dragon4Alive= true;
    private boolean dragon5Alive= true;
    private boolean bigDragonAlive= true;

    // add more instance variables here as necessary.

    /** Construct a new instance of the game with a given width and height. */
    public Game(int width, int height)
    {
        isVisible = false;
        spacePressed = false;
        canvas = new SimpleCanvas(width, height, "My Game");
        swords = new ArrayList<Sword>();
        Location loc1 = new Location(320, 320);
        Location loc2 = new Location(1010, 320);
        Location loc3 = new Location(630, 540);


        // Create game objects here if they should appear at the beginning of the game.
        avatar1 = new Warrior(loc1, "avatar1.png", 80, 80);
        avatar2 = new Sorcerer(loc2, "avatar2.png", 80, 80);
        avatar3 = new Avatar(loc3, "avatar3(cut).png", 69, 65);

        ground = new Platform(42, 544, 23, 332);
        platform1 = new Platform(42, 227, 23, 135);
        platform2 = new Platform(155, 376, 23, 60);
        platform3 = new Platform(330, 214, 23, 117);
        platform4 = new Platform(484, 298, 23, 261);
        platform5 = new Platform(222, 396, 23, 169);
        platform6 = new Platform(940, 500, 20, 261);
        platform7 = new Platform(802, 377, 23, 198);
        platform8 = new Platform(393, 520, 23, 187);
        platform9 = new Platform(601, 544, 23, 321);
        platform10 = new Platform(947, 209, 23, 310);
        platforms=new Platform[]{ground,platform1,platform2,platform3,platform4,platform5,platform6,platform7,platform8,platform9,platform10};
        dragon1=new Dragon(new Location(300,150),"dragon_nobg.png",80,80, 1280,640);
        dragon2=new Dragon(new Location(900,120),"dragon_nobg.png",80,80,1280,640);
        bigDragon=new BigDragon(new Location(640,80),"boss_dragon_nobg.png",120,120,1280,640);
        dragon3=new Dragon(new Location(200,500), "dragon_nobg.png",80,80,1280,640);
        dragon4=new Dragon(new Location(600,450), "dragon_nobg.png",80,80,1280,640);
        dragon5=new Dragon(new Location(900,500), "dragon_nobg.png",80,80,1280,640);
        hearts=new Heart[4];
        hearts[0]=new Heart(new Location(40,30));
        hearts[1]=new Heart(new Location(90,30));
        hearts[2]=new Heart(new Location(140,30));
        hearts[3]=new Heart(new Location(190,30));
        coins = new Coin[5];
        coins[0]= new Coin(new Location(80, 200));
        coins[1]= new Coin(new Location(370,187));
        coins[2]= new Coin(new Location(550,270));
        coins[3]= new Coin(new Location(260,370));
        coins[4]= new Coin(new Location(860,350));
        trophy= new Trophy(new Location(1167,467));
        score= 0;
    }

    /** Draw the state of the game on the canvas. */
    public void draw()
    {
        if (!inCave){
            canvas.clear(); // always clear first.
            canvas.drawImage(0, 0, "starting image.png", 1280, 640);
            // Call draw() on your GameObjects here.
            canvas.setPenColor(Color.RED);
            canvas.drawString(500, 70, "Choose your avatar.", 38);
            avatar1.draw(canvas);
            avatar2.draw(canvas);
            avatar3.draw(canvas);
        }
        else {
            canvas.clear();
            canvas.drawImage(0, 0, "Cave.png",1280, 640);

            selectedAvatar.draw(canvas);
            if (dragon1Alive){
                dragon1.drawAll(canvas);
            }
            if (dragon2Alive){
                dragon2.drawAll(canvas);
            }
            if (bigDragonAlive){
                bigDragon.drawAll(canvas);
            }
            if (dragon3Alive){
                dragon3.drawAll(canvas);
            }
            if (dragon4Alive){
                dragon4.drawAll(canvas);
            }
            if (dragon5Alive){
                dragon5.drawAll(canvas);
            }
            for (int i=0; i<hearts.length;i++) {
                hearts[i].draw(canvas);
            }

            if(swords!= null){
                for (Sword sword: swords){
                    sword.draw(canvas);
                }
            }
            for (int i=0; i<coins.length;i++) {
                coins[i].draw(canvas);
            }
            trophy.draw(canvas);
            canvas.setPenColor(Color.YELLOW);
            canvas.drawString(20,70,"Score: "+score,28);
        }

        // Show the window if needs be.
        if (!isVisible) {
            canvas.show();
            isVisible = true;
        }
        else {
            canvas.update();
        }
    }

    /** Is the game over? */
    private boolean isGameOver() {
        return gameOver||gameWon; //used chatgpt to see what condition to set for gameover based on game
    }

    /** Start the game running. */
    public void runGame()
    {
        // Create starting screen
        draw();
        canvas.setPenColor(Color.BLACK);
        canvas.drawString(10, 10, "Click canvas to start!");
        canvas.update();
        canvas.waitForClick();

        // Loop while the game is not over:
        while (!isGameOver()) {
            // Update the state of the game.
            // This might involve any number of things, but probably
            // involves moving any GameObjects however they should be moved,
            // updating any GameObjects that should be updated,
            // and checking for overlaps.

            handleKeyboard();

            // handle mouse
            handleMouse();
            //ground check
            if (selectedAvatar != null) {
                boolean onPlatform = false;
                checkCoinAndTrophyCollects();
                checkFireballHits();
                //find the closest platform
                int closestY = ground.getY();

                if(platform1.getY() > selectedAvatar.getBottomY() && platform1.getY() < closestY){
                    closestY = platform1.getY();
                }
                if(platform2.getY() > selectedAvatar.getBottomY() && platform2.getY() < closestY){
                    closestY = platform2.getY();
                }
                if(platform3.getY() > selectedAvatar.getBottomY() && platform3.getY() < closestY){
                    closestY = platform3.getY();
                }
                if(platform4.getY() > selectedAvatar.getBottomY() && platform4.getY() < closestY){
                    closestY = platform4.getY();
                }
                if(platform5.getY() > selectedAvatar.getBottomY() && platform5.getY() < closestY){
                    closestY = platform5.getY();
                }
                if(platform6.getY() > selectedAvatar.getBottomY() && platform6.getY() < closestY){
                    closestY = platform6.getY();
                }
                if(platform7.getY() > selectedAvatar.getBottomY() && platform7  .getY() < closestY){
                    closestY = platform7.getY();
                }
                if(platform8.getY() > selectedAvatar.getBottomY() && platform8.getY() < closestY){
                    closestY = platform8.getY();
                }
                if(platform9.getY() > selectedAvatar.getBottomY() && platform9.getY() < closestY){
                    closestY = platform9.getY();
                }
                if(platform10.getY() > selectedAvatar.getBottomY() && platform10.getY() < closestY){
                    closestY = platform10.getY();
                }

                selectedAvatar.setGroundY(
                        closestY - selectedAvatar.getHeight()/2
                );
                //platform checks
                if (platform1.isTop(selectedAvatar)){
                    onPlatform = true; //used chatgpt for onPlatform variable
                    selectedAvatar.setGroundY(platform1.getY() -
                            selectedAvatar.getHeight()/ 2);

                    selectedAvatar.setLocationY(platform1.getY() -
                            selectedAvatar.getHeight()/2);

                    if (selectedAvatar.isFalling()) {

                        selectedAvatar.stopJump();
                    }
                }
                else if (platform2.isTop(selectedAvatar)){
                    onPlatform = true;
                    selectedAvatar.setGroundY(platform2.getY() -
                            selectedAvatar.getHeight()/ 2);

                    selectedAvatar.setLocationY(platform2.getY() -
                            selectedAvatar.getHeight()/2);

                    if (selectedAvatar.isFalling()) {

                        selectedAvatar.stopJump();
                    }
                }
                else if (platform3.isTop(selectedAvatar)){
                    onPlatform = true;
                    selectedAvatar.setGroundY(platform3.getY() -
                            selectedAvatar.getHeight()/ 2);

                    selectedAvatar.setLocationY(platform3.getY() -
                            selectedAvatar.getHeight()/2);

                    if (selectedAvatar.isFalling()) {

                        selectedAvatar.stopJump();
                    }
                }
                else if (platform4.isTop(selectedAvatar)){
                    onPlatform = true;
                    selectedAvatar.setGroundY(platform4.getY() -
                            selectedAvatar.getHeight()/ 2);

                    selectedAvatar.setLocationY(platform4.getY() -
                            selectedAvatar.getHeight()/2);

                    if (selectedAvatar.isFalling()) {

                        selectedAvatar.stopJump();
                    }
                }
                else if (platform5.isTop(selectedAvatar)){
                    onPlatform = true;
                    selectedAvatar.setGroundY(platform5.getY() -
                            selectedAvatar.getHeight()/ 2);

                    selectedAvatar.setLocationY(platform5.getY() -
                            selectedAvatar.getHeight()/2);

                    if (selectedAvatar.isFalling()) {

                        selectedAvatar.stopJump();
                    }
                }
                else if (platform6.isTop(selectedAvatar)){
                    onPlatform = true;
                    selectedAvatar.setGroundY(platform6.getY() -
                            selectedAvatar.getHeight()/ 2);

                    selectedAvatar.setLocationY(platform6.getY() -
                            selectedAvatar.getHeight()/2);

                    if (selectedAvatar.isFalling()) {

                        selectedAvatar.stopJump();
                    }
                }
                else if (platform7.isTop(selectedAvatar)){
                    onPlatform = true;
                    selectedAvatar.setGroundY(platform7.getY() -
                            selectedAvatar.getHeight()/ 2);

                    selectedAvatar.setLocationY(platform7.getY() -
                            selectedAvatar.getHeight()/2);

                    if (selectedAvatar.isFalling()) {

                        selectedAvatar.stopJump();
                    }
                }
                else if (platform8.isTop(selectedAvatar)){
                    onPlatform = true;
                    selectedAvatar.setGroundY(platform8.getY() -
                            selectedAvatar.getHeight()/ 2);

                    selectedAvatar.setLocationY(platform8.getY() -
                            selectedAvatar.getHeight()/2);

                    if (selectedAvatar.isFalling()) {

                        selectedAvatar.stopJump();
                    }
                }
                else if (platform9.isTop(selectedAvatar)){
                    onPlatform = true;
                    selectedAvatar.setGroundY(platform9.getY() -
                            selectedAvatar.getHeight()/ 2);

                    selectedAvatar.setLocationY(platform9.getY() -
                            selectedAvatar.getHeight()/2);

                    if (selectedAvatar.isFalling()) {

                        selectedAvatar.stopJump();
                    }
                }
                else if (platform10.isTop(selectedAvatar)){
                    onPlatform = true;
                    selectedAvatar.setGroundY(platform10.getY() -
                            selectedAvatar.getHeight()/ 2);

                    selectedAvatar.setLocationY(platform10.getY() -
                            selectedAvatar.getHeight()/2);

                    if (selectedAvatar.isFalling()) {

                        selectedAvatar.stopJump();
                    }
                }

                selectedAvatar.updateJump();
                selectedAvatar.fall();
                if (selectedAvatar.getCenterX()>1280||selectedAvatar.getCenterX()<0){
                    selectedAvatar.setLocation(
                            new Location(102,platform1.getY()-selectedAvatar.getHeight()/2-1));
                    selectedAvatar.setGroundY(platform1.getY()-selectedAvatar.getHeight()/2);
                    selectedAvatar.stopJump();
                }
                if(swords != null){
                    for(Sword sword: swords){
                        sword.move();
                    }
                }
                checkSwordHits();
            }
            if (inCave) {
                dragon1.update(platforms);
                dragon2.update(platforms);
                dragon3.update(platforms);
                dragon4.update(platforms);
                dragon5.update(platforms);
                bigDragon.update(platforms);
            }
            draw();
            canvas.pause(40);


            // Draw the state of the game.
            // can adjust this higher or lower as needed
        }
        // Game is now over,
        // draw the state of the game one last time in case there were any last updates
        // that we need to show.
        canvas.clear();
        canvas.drawImage(0,0,"Cave.png",1280,640);
        if (gameWon) {
            canvas.setPenColor(Color.YELLOW);
            canvas.drawString(350,280,"You Win :)",80);
            canvas.drawString(320,380,"Score: "+score,50);
        } else {
            canvas.setPenColor(Color.RED);
            canvas.drawString(380, 280,"Game Over!",80);
            canvas.drawString(320,380,"Score: "+score,50);
        }
        canvas.update();
        // Do any other game wrapup things here.
    }

    /** Do any keyboard-related things here.  You can test for any key being
     * pressed.  Use this list of "VK_" constants to find the one you want:
     * https://docs.oracle.com/en/java/javase/13/docs/api/java.desktop/java/awt/event/KeyEvent.html
     * You can take any other actions you want on your GameObjects here, based
     * on keypresses, like moving objects, interacting with other objects (attacking, defending, throwing
     * or giving objects, etc), or changing the state of an object (such as switching
     * between spells to cast, for instance).  You can also check for overlaps here, of course.
     */
    int count = 0;
    private void handleKeyboard() {

        if(selectedAvatar != null){
            if(canvas.isKeyPressed(KeyEvent.VK_SPACE)){

                if(!spacePressed){
                    if(selectedAvatar instanceof Warrior){
                        swords.add(
                                ((Warrior)selectedAvatar).throwSword());
                    }
                }
                spacePressed = true;

            }
            else{
                spacePressed = false;
            }

            if (canvas.isKeyPressed(KeyEvent.VK_UP)) {

                selectedAvatar.jump();

            }

            if (canvas.isKeyPressed(KeyEvent.VK_RIGHT)) {

                boolean blocked = false;
                if (platform1.isWall(selectedAvatar)) {
                    blocked = true;
                }

                else if (platform2.isWall(selectedAvatar)) {
                    blocked = true;
                }

                else if (platform3.isWall(selectedAvatar)) {
                    blocked = true;
                }
                else if (platform4.isWall(selectedAvatar)) {
                    blocked = true;
                }

                else if (platform5.isWall(selectedAvatar)) {
                    blocked = true;
                }
                else if (platform6.isWall(selectedAvatar)) {
                    blocked = true;
                }

                else if (platform8.isWall(selectedAvatar)) {
                    blocked = true;
                }
                else if (platform9.isWall(selectedAvatar)) {
                    blocked = true;
                }

                else if (platform10.isWall(selectedAvatar)) {
                    blocked = true;
                }
                if(!blocked){
                    selectedAvatar.move();
                }


            }
        }

        // add any other tests you want!
    }

    /** Do any mouse-related things here.  This would probably involve using the
     * location of a mouse click to do things like interact with a clicked GameObject
     * in some way.  You can use the isInside() method in GameObject to test if a
     * mouse click (x, y) is inside the object.
     */

    private void handleMouse() {
        if (canvas.isMousePressed()) {
            int clickX = canvas.getMouseClickX();
            int clickY = canvas.getMouseClickY();

            if(avatar1.isInside(clickX, clickY)){
                if (count == 0){
                    count = count + 1;
                    selectedAvatar = avatar1;
                    selectedAvatar.setLocation(new Location(102, (platform1.getY())-selectedAvatar.getHeight()/2-1));
                    selectedAvatar.setGroundY(platform1.getY() - selectedAvatar.getHeight()/ 2);
                    inCave = true;
                }
            } else if (avatar2.isInside(clickX, clickY)) {
                if (count == 0){
                    count = count + 1;
                    selectedAvatar = avatar2;
                    selectedAvatar.setLocation(new Location(102, (platform1.getY())- selectedAvatar.getHeight()/2-1));
                    selectedAvatar.setGroundY(platform1.getY() - selectedAvatar.getHeight()/ 2);
                    inCave = true;
                }
            } else if (avatar3.isInside(clickX, clickY)) {
                if (count == 0){
                    count = count + 1;
                    selectedAvatar = avatar3;
                    selectedAvatar.setLocation(new Location(102, (platform1.getY())-selectedAvatar.getHeight()/2-1));
                    selectedAvatar.setGroundY(platform1.getY() - selectedAvatar.getHeight()/ 2);
                    inCave = true;
                }
            }
            System.out.println("Mouse click detected at " + clickX + " " + clickY);

        }
    }
    private void checkFireballHits() {
        for (int i=dragon1.getFireballs().size()-1;i>= 0;i--) {
            Fireball fb=dragon1.getFireballs().get(i);
            if (selectedAvatar.overlaps(fb)) {
                selectedAvatar.loseLife();
                dragon1.getFireballs().remove(i);
                selectedAvatar.setLocation(new Location(102,platform1.getY()- selectedAvatar.getHeight()/2-1));
                selectedAvatar.setGroundY(platform1.getY()-selectedAvatar.getHeight()/2);
                selectedAvatar.stopJump();
            }
        }
        for (int i=dragon2.getFireballs().size()-1;i>= 0;i--) {
            Fireball fb=dragon2.getFireballs().get(i);
            if (selectedAvatar.overlaps(fb)) {
                selectedAvatar.loseLife();
                dragon2.getFireballs().remove(i);
                selectedAvatar.setLocation(new Location(102,platform1.getY()-selectedAvatar.getHeight()/2- 1));
                selectedAvatar.setGroundY(platform1.getY()- selectedAvatar.getHeight()/2);
                selectedAvatar.stopJump();
            }
        }
        for (int i=dragon3.getFireballs().size()-1;i>= 0;i--) {
            Fireball fb=dragon3.getFireballs().get(i);
            if (selectedAvatar.overlaps(fb)) {
                selectedAvatar.loseLife();
                dragon3.getFireballs().remove(i);
                selectedAvatar.setLocation(new Location(102,platform1.getY()-selectedAvatar.getHeight()/2-1));
                selectedAvatar.setGroundY(platform1.getY()-selectedAvatar.getHeight()/2);
                selectedAvatar.stopJump();
            }
        }
        for (int i=dragon4.getFireballs().size()-1;i>= 0;i--) {
            Fireball fb=dragon4.getFireballs().get(i);
            if (selectedAvatar.overlaps(fb)) {
                selectedAvatar.loseLife();
                dragon4.getFireballs().remove(i);
                selectedAvatar.setLocation(new Location(102,platform1.getY()-selectedAvatar.getHeight()/2- 1));
                selectedAvatar.setGroundY(platform1.getY()- selectedAvatar.getHeight()/2);
                selectedAvatar.stopJump();
            }
        }
        for (int i=dragon5.getFireballs().size()-1;i>= 0;i--) {
            Fireball fb=dragon5.getFireballs().get(i);
            if (selectedAvatar.overlaps(fb)) {
                selectedAvatar.loseLife();
                dragon5.getFireballs().remove(i);
                selectedAvatar.setLocation(new Location(102, platform1.getY()-selectedAvatar.getHeight()/2- 1));
                selectedAvatar.setGroundY(platform1.getY() - selectedAvatar.getHeight()/2);
                selectedAvatar.stopJump();
            }
        }
        for (int i = bigDragon.getFireballs().size() - 1; i >= 0; i--) {
            Fireball fb = bigDragon.getFireballs().get(i);
            if (selectedAvatar.overlaps(fb)) {
                selectedAvatar.loseLife();
                selectedAvatar.loseLife();
                bigDragon.getFireballs().remove(i);
                selectedAvatar.setLocation(new Location(102, platform1.getY()- selectedAvatar.getHeight()/2- 1));
                selectedAvatar.setGroundY(platform1.getY()-selectedAvatar.getHeight()/2);
                selectedAvatar.stopJump();
            }
        }
        updateHearts();
        if (selectedAvatar.getLives()<=0) {
            gameOver = true;
        }
    }
    private void updateHearts() {
        int lives=selectedAvatar.getLives();
        for (int i=0; i<hearts.length;i++) {
            if (i<lives) {
            }
            else {
                hearts[i].lose();
            }
        }
    }
    private void checkCoinAndTrophyCollects() {
        for (int i=0; i<coins.length;i++) {
            if (!coins[i].isCollected() && selectedAvatar.overlaps(coins[i])) {
                coins[i].collect();
                score=score+ 10;
            }
        }
        boolean allCoins=true;
        for (int i=0; i<coins.length;i++) {
            if (!coins[i].isCollected()) {
                allCoins=false;
            }
        }
        if (allCoins && !trophy.isCollected() && selectedAvatar.overlaps(trophy)) {
            trophy.collect();
            gameWon=true;
        }
    }
    private void checkSwordHits() {
        for (int i=swords.size()-1;i>=0;i--) {
            Sword sword=swords.get(i);
            if (dragon1Alive && sword.overlaps(dragon1)) {
                dragon1Alive=false;
                swords.remove(i);
            }
            else if (dragon2Alive && sword.overlaps(dragon2)) {
                dragon2Alive= false;
                swords.remove(i);
            }
            else if (dragon3Alive && sword.overlaps(dragon3)) {
                dragon3Alive= false;
                swords.remove(i);
            }
            else if (dragon4Alive && sword.overlaps(dragon4)) {
                dragon4Alive= false;
                swords.remove(i);
            }
            else if (dragon5Alive && sword.overlaps(dragon5)) {
                dragon5Alive= false;
                swords.remove(i);
            }
            else if (bigDragonAlive && sword.overlaps(bigDragon)) {
                bigDragonAlive= false;
                swords.remove(i);
            }
        }
    }
}
