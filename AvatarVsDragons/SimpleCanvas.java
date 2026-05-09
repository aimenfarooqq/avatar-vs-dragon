import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.*;

// Last updated by Phil Kirlin, April 24, 2026.
// SimpleCanvas version5: integrated sound support (playSound / preloadSound / fixSounds / loopSound).

/**
 * A SimpleCanvas represents a window on the screen that can be drawn on.
 */
public class SimpleCanvas {
    private JFrame frame;
    private Graphics2D offscreenGraphics;
    private BufferedImage offscreenImage;
    private final int height;
    private final int width;
    private Color bgColor = Color.WHITE;
    private int lastMouseClickX = 0, lastMouseClickY = 0;
    private boolean isMousePressed = false;
    private double mouseX = 0;
    private double mouseY = 0;

    // Cache to store images so we don't re-load them from disk every frame
    private static final Map<String, Image> imageCache = new HashMap<>();

    // Cache of pre-opened Clips so we don't re-open them on every playSound() call
    private static final Map<String, Clip> clipCache = new HashMap<>();

    // Silent clip looped in the background by fixSounds() to keep the audio driver awake
    private static Clip keepAliveClip;

    // for synchronization
    private static final Object mouseLock = new Object();
    private static final Object keyLock = new Object();

    // queue of typed key characters
    private LinkedList<Character> keysTyped;

    // set of key codes currently pressed down
    private Set<Integer> keysDown;

    // currently looping sound clip
    private Clip loopingClip;

    // --- Constructors ---

    /**
     * Creates a new SimpleCanvas of the specified width and height.
     */
    public SimpleCanvas(int width, int height) {
        this(width, height, "SimpleCanvas");
    }

    /**
     * Creates a new SimpleCanvas of the specified width and height, with the specified title.
     */
    public SimpleCanvas(int width, int height, String title) {
        this.height = height;
        this.width = width;

        SwingUtilities.invokeLater(() -> {
            frame = new JFrame(title);
            frame.setVisible(false);

            // Create single offscreen buffer for incremental drawing
            offscreenImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            offscreenGraphics = offscreenImage.createGraphics();

            // initialize keystroke buffers
            keysTyped = new LinkedList<>();
            keysDown = new TreeSet<>();

            // add antialiasing
            RenderingHints hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            offscreenGraphics.addRenderingHints(hints);

            // clear screen
            offscreenGraphics.setColor(bgColor);
            offscreenGraphics.fillRect(0, 0, width, height);
            offscreenGraphics.setColor(Color.BLACK);

            // Create a custom panel that paints the offscreen buffer directly
            JPanel canvas = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    // Draw the offscreen buffer directly - Swing handles double buffering
                    g.drawImage(offscreenImage, 0, 0, null);
                }

                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(width, height);
                }
            };

            MouseAdapter mouseAd = new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    synchronized (mouseLock) {
                        lastMouseClickX = e.getX();
                        lastMouseClickY = e.getY();
                        isMousePressed = false;
                    }
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    synchronized (mouseLock) {
                        lastMouseClickX = e.getX();
                        lastMouseClickY = e.getY();
                        isMousePressed = true;
                    }
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    synchronized (mouseLock) {
                        lastMouseClickX = e.getX();
                        lastMouseClickY = e.getY();
                        mouseX = e.getX();
                        mouseY = e.getY();
                    }
                }

                @Override
                public void mouseMoved(MouseEvent e) {
                    synchronized (mouseLock) {
                        mouseX = e.getX();
                        mouseY = e.getY();
                    }
                }
            };
            canvas.addMouseListener(mouseAd);
            canvas.addMouseMotionListener(mouseAd);
            frame.setContentPane(canvas);
            frame.addKeyListener(new KeyAdapter() {
                @Override
                public void keyTyped(KeyEvent e) {
                    synchronized (keyLock) {
                        keysTyped.addFirst(e.getKeyChar());
                    }
                }

                @Override
                public void keyPressed(KeyEvent e) {
                    synchronized (keyLock) {
                        keysDown.add(e.getKeyCode());
                    }
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    synchronized (keyLock) {
                        keysDown.remove(e.getKeyCode());
                    }
                }
            });
            frame.setFocusTraversalKeysEnabled(false);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            frame.pack();
        });
    }

    // --- Drawing shapes ---

    /**
     * Draws a circle on the canvas with the center at (centerX, centerY) and the specified radius.
     */
    public void drawCircle(int centerX, int centerY, int radius) {
        SwingUtilities.invokeLater(() ->
                offscreenGraphics.drawOval(centerX - radius, centerY - radius, radius * 2, radius * 2)
        );
    }

    /**
     * Draws a circle on the canvas with the center at (centerX, centerY) and the specified radius, filled
     * with the current pen color.
     */
    public void drawFilledCircle(int centerX, int centerY, int radius) {
        SwingUtilities.invokeLater(() ->
                offscreenGraphics.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2)
        );
    }

    /**
     * Draws an oval on the canvas with the center at (centerX, centerY) and the specified radii in the
     * x and y directions.
     */
    public void drawOval(int centerX, int centerY, int radiusX, int radiusY) {
        SwingUtilities.invokeLater(() ->
                offscreenGraphics.drawOval(centerX - radiusX, centerY - radiusY, radiusX * 2, radiusY * 2)
        );
    }

    /**
     * Draws an oval on the canvas with the center at (centerX, centerY) and the specified radii in the
     * x and y directions, filled with the current pen color.
     */
    public void drawFilledOval(int centerX, int centerY, int radiusX, int radiusY) {
        SwingUtilities.invokeLater(() ->
                offscreenGraphics.fillOval(centerX - radiusX, centerY - radiusY, radiusX * 2, radiusY * 2)
        );
    }

    /**
     * Draws a rectangle on the canvas with the left corner at (topLeftX, topLeftY) and the specified
     * width and height.
     */
    public void drawRectangle(int topLeftX, int topLeftY, int width, int height) {
        SwingUtilities.invokeLater(() ->
                offscreenGraphics.drawRect(topLeftX, topLeftY, width, height)
        );
    }

    /**
     * Draws a rectangle on the canvas with the left corner at (topLeftX, topLeftY) and the specified
     * width and height, filled with the current pen color.
     */
    public void drawFilledRectangle(int topLeftX, int topLeftY, int width, int height) {
        SwingUtilities.invokeLater(() ->
                offscreenGraphics.fillRect(topLeftX, topLeftY, width, height)
        );
    }

    /**
     * Draws a polygon on the canvas defined by the xPoints and yPoints arrays. Each pair of
     * (x, y) coordinates specifies a point.
     */
    public void drawPolygon(int[] xPoints, int[] yPoints) {
        SwingUtilities.invokeLater(() ->
                offscreenGraphics.drawPolygon(xPoints, yPoints, xPoints.length)
        );
    }

    /**
     * Draws a filled polygon on the canvas defined by the xPoints and yPoints arrays. Each pair of
     * (x, y) coordinates specifies a point.
     */
    public void drawFilledPolygon(int[] xPoints, int[] yPoints) {
        SwingUtilities.invokeLater(() ->
                offscreenGraphics.fillPolygon(xPoints, yPoints, xPoints.length)
        );
    }

    /**
     * Draws a line on the canvas from (x1, y1) to (x2, y2).
     */
    public void drawLine(int x1, int y1, int x2, int y2) {
        SwingUtilities.invokeLater(() ->
                offscreenGraphics.drawLine(x1, y1, x2, y2)
        );
    }

    /**
     * Draws an arc (a portion of an oval's outline) centered at (centerX, centerY) with the
     * specified radius. The arc begins at startAngle and extends for arcAngle degrees.
     * Angles are in degrees, with 0 at the 3 o'clock position, increasing counterclockwise.
     */
    public void drawArc(int centerX, int centerY, int radius, int startAngle, int arcAngle) {
        SwingUtilities.invokeLater(() ->
                offscreenGraphics.drawArc(centerX - radius, centerY - radius, radius * 2, radius * 2, startAngle, arcAngle)
        );
    }

    /**
     * Draws a filled arc (a pie-slice shape) centered at (centerX, centerY) with the
     * specified radius. The arc begins at startAngle and extends for arcAngle degrees.
     * Angles are in degrees, with 0 at the 3 o'clock position, increasing counterclockwise.
     */
    public void drawFilledArc(int centerX, int centerY, int radius, int startAngle, int arcAngle) {
        SwingUtilities.invokeLater(() ->
                offscreenGraphics.fillArc(centerX - radius, centerY - radius, radius * 2, radius * 2, startAngle, arcAngle)
        );
    }

    // --- Drawing text ---

    /**
     * Draws the specified text on the canvas, with the baseline of the text at y
     * and the left edge at x.
     */
    public void drawString(int x, int y, String text) {
        drawString(x, y, text, 12);
    }

    /**
     * Draws the specified text on the canvas, with the baseline of the text at y
     * and the left edge at x, in the specified font size.
     */
    public void drawString(int x, int y, String text, int fontSize) {
        SwingUtilities.invokeLater(() -> {
            Font currentFont = offscreenGraphics.getFont();
            Font newFont = new Font(currentFont.getFontName(), currentFont.getStyle(), fontSize);
            offscreenGraphics.setFont(newFont);
            offscreenGraphics.drawString(text, x, y);
        });
    }

    /**
     * Draws the specified text on the canvas, horizontally centered on x,
     * with the baseline of the text at y.
     */
    public void drawStringCentered(int x, int y, String text) {
        drawStringCentered(x, y, text, 12);
    }

    /**
     * Draws the specified text on the canvas, horizontally centered on x,
     * with the baseline of the text at y, in the specified font size.
     */
    public void drawStringCentered(int x, int y, String text, int fontSize) {
        SwingUtilities.invokeLater(() -> {
            Font currentFont = offscreenGraphics.getFont();
            Font newFont = new Font(currentFont.getFontName(), currentFont.getStyle(), fontSize);
            offscreenGraphics.setFont(newFont);

            FontMetrics metrics = offscreenGraphics.getFontMetrics(newFont);
            int stringWidth = metrics.stringWidth(text);

            offscreenGraphics.drawString(text, x - stringWidth / 2, y);
        });
    }

    // --- Drawing images ---

    /**
     * Draws the specified image on the canvas with the top left corner at (x, y), at its original size.
     * The filename can be a file path, URL, or classpath resource.
     */
    public void drawImage(int x, int y, String filename) {
        Image image = loadImage(filename);
        int ws = image.getWidth(null), hs = image.getHeight(null);
        if (ws < 0 || hs < 0) throw new IllegalArgumentException("image " + filename + " is corrupt");

        SwingUtilities.invokeLater(() ->
                offscreenGraphics.drawImage(image, x, y, null)
        );
    }

    /**
     * Draws the specified image on the canvas with the top left corner at (x, y),
     * scaled to the specified width and height.
     * The filename can be a file path, URL, or classpath resource.
     */
    public void drawImage(int x, int y, String filename, int width, int height) {
        Image image = loadImage(filename);
        int ws = image.getWidth(null), hs = image.getHeight(null);
        if (ws < 0 || hs < 0) throw new IllegalArgumentException("image " + filename + " is corrupt");

        SwingUtilities.invokeLater(() ->
                offscreenGraphics.drawImage(image, x, y, width, height, null)
        );
    }

    /**
     * Draws the specified image on the canvas, flipped horizontally (flipped left-to-right),
     * with the top left corner at (x, y).
     * The filename can be a file path, URL, or classpath resource.
     */
    public void drawImageFlippedHorizontal(int x, int y, String filename) {
        Image image = loadImage(filename);
        int ws = image.getWidth(null), hs = image.getHeight(null);
        if (ws < 0 || hs < 0) throw new IllegalArgumentException("image " + filename + " is corrupt");

        SwingUtilities.invokeLater(() ->
                offscreenGraphics.drawImage(image, x + ws, y, -ws, hs, null)
        );
    }

    /**
     * Draws the specified image on the canvas, flipped vertically (flipped top-to-bottom),
     * with the top left corner at (x, y).
     * The filename can be a file path, URL, or classpath resource.
     */
    public void drawImageFlippedVertical(int x, int y, String filename) {
        Image image = loadImage(filename);
        int ws = image.getWidth(null), hs = image.getHeight(null);
        if (ws < 0 || hs < 0) throw new IllegalArgumentException("image " + filename + " is corrupt");

        SwingUtilities.invokeLater(() ->
                offscreenGraphics.drawImage(image, x, y + hs, ws, -hs, null)
        );
    }

    /**
     * Draws the specified image on the canvas, rotated by the given number of degrees.
     * Note: unlike drawImage, (centerX, centerY) specifies the CENTER of the image,
     * not the top left corner. The image rotates in place around this center point.
     * Positive degrees rotate clockwise.
     * The filename can be a file path, URL, or classpath resource.
     */
    public void drawImageRotated(int centerX, int centerY, String filename, int degrees) {
        Image image = loadImage(filename);
        int ws = image.getWidth(null), hs = image.getHeight(null);
        if (ws < 0 || hs < 0) throw new IllegalArgumentException("image " + filename + " is corrupt");

        SwingUtilities.invokeLater(() -> {
            var savedTransform = offscreenGraphics.getTransform();
            offscreenGraphics.rotate(Math.toRadians(degrees), centerX, centerY);
            offscreenGraphics.drawImage(image, centerX - ws / 2, centerY - hs / 2, null);
            offscreenGraphics.setTransform(savedTransform);
        });
    }

    // --- Pen and appearance settings ---

    /**
     * Sets the current pen color on the canvas. All future drawing will take place
     * using this color until changed.
     */
    public void setPenColor(Color c) {
        SwingUtilities.invokeLater(() ->
                offscreenGraphics.setColor(c)
        );
    }

    /**
     * Sets the canvas background color. Must call clear() to take effect.
     */
    public void setBackgroundColor(Color c) {
        SwingUtilities.invokeLater(() ->
                bgColor = c
        );
    }

    /**
     * Sets the thickness of the lines drawn for the borders of shapes.
     */
    public void setLineThickness(int size) {
        SwingUtilities.invokeLater(() ->
                offscreenGraphics.setStroke(new BasicStroke(size))
        );
    }

    /**
     * Enables or disables antialiasing for shape and text rendering.
     * When enabled, shapes have smooth edges. When disabled, edges are crisp/pixelated.
     * Default is enabled (true).
     */
    public void setAntialiasing(boolean enabled) {
        SwingUtilities.invokeLater(() -> {
            if (enabled) {
                RenderingHints hints = new RenderingHints(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );
                hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                offscreenGraphics.addRenderingHints(hints);
            } else {
                RenderingHints hints = new RenderingHints(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_OFF
                );
                hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
                offscreenGraphics.addRenderingHints(hints);
            }
        });
    }

    // --- Pixel access ---

    /**
     * Returns the color of the pixel at location (x, y) on the canvas.
     */
    public Color getPixelColor(int x, int y) {
        final Color[] answer = new Color[1];
        try {
            SwingUtilities.invokeAndWait(() ->
                    answer[0] = new Color(offscreenImage.getRGB(x, y), true)
            );
        } catch (InterruptedException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return answer[0];
    }

    /**
     * Sets the pixel color at location (x, y) on the canvas.
     */
    public void setPixelColor(int x, int y, Color c) {
        SwingUtilities.invokeLater(() ->
                offscreenImage.setRGB(x, y, c.getRGB())
        );
    }

    // --- Mouse ---

    /**
     * Returns true if a mouse button is currently pressed down.
     */
    public boolean isMousePressed() {
        synchronized (mouseLock) {
            return isMousePressed;
        }
    }

    /**
     * Returns the current x-coordinate of the mouse pointer on the canvas.
     */
    public double getMouseX() {
        synchronized (mouseLock) {
            return mouseX;
        }
    }

    /**
     * Returns the current y-coordinate of the mouse pointer on the canvas.
     */
    public double getMouseY() {
        synchronized (mouseLock) {
            return mouseY;
        }
    }

    /**
     * Retrieves the x-coordinate of the most recent mouse click.
     */
    public int getMouseClickX() {
        return lastMouseClickX;
    }

    /**
     * Retrieves the y-coordinate of the most recent mouse click.
     */
    public int getMouseClickY() {
        return lastMouseClickY;
    }

    /**
     * Pauses the main program until the mouse is clicked on the canvas. The mouse click position
     * can be retrieved later.
     */
    public void waitForClick() {
        MouseWaiter mw = new MouseWaiter();
        try {
            SwingUtilities.invokeAndWait(() -> frame.getContentPane().addMouseListener(mw));
        } catch (InterruptedException | InvocationTargetException e) {
            e.printStackTrace();
        }
        try {
            synchronized (mw) {
                mw.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> frame.getContentPane().removeMouseListener(mw));
    }

    // --- Keyboard ---

    /**
     * Returns true if there is a key character waiting to be processed.
     */
    public boolean hasNextKeyTyped() {
        synchronized (keyLock) {
            return !keysTyped.isEmpty();
        }
    }

    /**
     * Returns and removes the next key character from the keystroke buffer.
     * Call hasNextKeyTyped() first to check if a character is available.
     */
    public char nextKeyTyped() {
        synchronized (keyLock) {
            if (keysTyped.isEmpty()) {
                throw new NoSuchElementException("your program has already processed all keystrokes");
            }
            return keysTyped.removeLast();
        }
    }

    /**
     * Returns true if the key with the specified key code is currently pressed down.
     * Use KeyEvent constants for key codes (e.g., KeyEvent.VK_LEFT, KeyEvent.VK_SPACE).
     */
    public boolean isKeyPressed(int keycode) {
        synchronized (keyLock) {
            return keysDown.contains(keycode);
        }
    }

    // --- Sound ---

    /**
     * Preloads the specified WAV file into an internal cache so that the first call to
     * playSound() on that file plays without delay. Calling this method is optional:
     * playSound() will preload automatically on first use, but doing so during an animation
     * loop can cause a visible hitch.
     *
     * <p>This method is synchronous and may take noticeable time to open and decode the file.
     * Call preloadSound() during program setup, before your animation loop begins -- not
     * from inside the loop itself.
     *
     * <p>Subsequent calls with the same filename are no-ops.
     * The filename can be a file path, URL, or classpath resource.
     */
    public void preloadSound(String filename) {
        if (clipCache.containsKey(filename)) return;
        try {
            AudioInputStream stream = resolveAudioStream(filename);
            Clip clip = AudioSystem.getClip();
            clip.open(stream);
            stream.close();
            clipCache.put(filename, clip);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            throw new IllegalArgumentException("error preloading sound '" + filename + "': " + e.getMessage());
        }
    }

    /**
     * Starts a silent audio clip looping in the background to keep the operating system's
     * audio driver active. On some systems (notably macOS), the OS can put the audio driver
     * to sleep, causing a noticeable delay the next time a sound is played. Calling
     * fixSounds() prevents that delay.
     *
     * <p>This fix is optional and not needed on every system. Call it once near the start
     * of your program if you notice latency when playing sounds. Subsequent calls are no-ops.
     */
    public void fixSounds() {
        if (keepAliveClip != null) return;
        try {
            keepAliveClip = AudioSystem.getClip();
            AudioFormat silentFormat = new AudioFormat(44100, 16, 1, true, false);
            byte[] silence = new byte[1024];
            keepAliveClip.open(silentFormat, silence, 0, silence.length);
            keepAliveClip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (LineUnavailableException e) {
            keepAliveClip = null;
            throw new IllegalArgumentException("error starting keep-alive audio clip: " + e.getMessage());
        }
    }

    /**
     * Plays the specified WAV file once. Playback is non-blocking (the method returns
     * immediately). If the file has not been preloaded, it is loaded on first call -- this
     * can cause a brief delay, so consider calling preloadSound() during setup.
     *
     * <p>Rapidly re-triggering the same sound restarts it from the beginning rather than
     * overlapping playback.
     */
    public void playSound(String filename) {
        if (!clipCache.containsKey(filename)) {
            preloadSound(filename);
        }
        Clip clip = clipCache.get(filename);
        if (clip.isRunning()) return;
        new Thread(() -> {
            clip.setFramePosition(0);
            clip.start();
        }).start();
    }

    /**
     * Loops the specified WAV file continuously until stopSound() is called or another
     * loopSound() call replaces it. Playback is non-blocking.
     */
    public void loopSound(String filename) {
        stopSound();
        try {
            AudioInputStream stream = resolveAudioStream(filename);
            loopingClip = AudioSystem.getClip();
            loopingClip.open(stream);
            stream.close();
            loopingClip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            throw new IllegalArgumentException("error looping sound '" + filename + "': " + e.getMessage());
        }
    }

    /**
     * Stops the currently looping sound, if any.
     */
    public void stopSound() {
        if (loopingClip != null) {
            loopingClip.stop();
            loopingClip.close();
            loopingClip = null;
        }
    }

    // --- Canvas control ---

    /**
     * Shows the canvas window on the screen, if it's currently hidden. No effect if it's already visible.
     */
    public void show() {
        update();
        try {
            SwingUtilities.invokeAndWait(() ->
                    frame.setVisible(true)
            );
        } catch (InterruptedException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * Hides the canvas window on the screen, if it's currently shown. No effect if it's already hidden.
     */
    public void hide() {
        SwingUtilities.invokeLater(() ->
                frame.setVisible(false)
        );
    }

    /**
     * If any drawing operations have taken place, update() actually draws them to the canvas.
     * Shapes are not normally drawn instantaneously; instead, they are not drawn until this
     * method is called. Therefore, this method can be used to have a number of new shapes all
     * appear simultaneously on the screen.
     */
    public void update() {
        SwingUtilities.invokeLater(() -> {
            frame.repaint();
            frame.getContentPane().repaint();
        });
    }

    /**
     * Clears the canvas with the current background color.
     */
    public void clear() {
        SwingUtilities.invokeLater(() -> {
            Color saveColor = offscreenGraphics.getColor();
            offscreenGraphics.setColor(bgColor);
            offscreenGraphics.fillRect(0, 0, width, height);
            offscreenGraphics.setColor(saveColor);
        });
    }

    /**
     * Pauses execution for the specified number of milliseconds.
     */
    public void pause(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the JPanel used for drawing. This can be embedded in another Swing container
     * (e.g., a JFrame with a custom layout) instead of using the built-in window.
     */
    public JPanel getPanel() {
        final JPanel[] result = new JPanel[1];
        try {
            SwingUtilities.invokeAndWait(() ->
                    result[0] = (JPanel) frame.getContentPane()
            );
        } catch (InterruptedException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return result[0];
    }

    /**
     * Returns the width of the canvas in pixels.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the height of the canvas in pixels.
     */
    public int getHeight() {
        return height;
    }

    // --- Private helpers ---

    private static class MouseWaiter extends MouseAdapter {
        @Override
        public void mouseReleased(MouseEvent e) {
            synchronized (this) {
                notifyAll();
            }
        }
    }

    /**
     * Resolves a filename to an AudioInputStream, trying as a file, URL, and classpath resource.
     */
    private static AudioInputStream resolveAudioStream(String filename) throws UnsupportedAudioFileException, IOException {
        if (filename == null) {
            throw new IllegalArgumentException("filename cannot be null");
        }

        AudioInputStream stream = null;

        // Try as a file first (most common case)
        File file = new File(filename);
        if (file.exists()) {
            stream = AudioSystem.getAudioInputStream(file);
        }

        // Try as a URL (handles http://, https://, etc.)
        if (stream == null && (filename.startsWith("http://") || filename.startsWith("https://"))) {
            stream = AudioSystem.getAudioInputStream(URI.create(filename).toURL());
        }

        // Try as a classpath resource (for bundled resources)
        if (stream == null) {
            URL resourceUrl = SimpleCanvas.class.getResource(filename);
            if (resourceUrl != null) {
                stream = AudioSystem.getAudioInputStream(resourceUrl);
            }
        }

        // One last try: classpath with leading slash if not present
        if (stream == null && !filename.startsWith("/")) {
            URL resourceUrl = SimpleCanvas.class.getResource("/" + filename);
            if (resourceUrl != null) {
                stream = AudioSystem.getAudioInputStream(resourceUrl);
            }
        }

        if (stream == null) {
            throw new IllegalArgumentException("sound '" + filename + "' not found");
        }

        return stream;
    }

    /**
     * Loads an image from a file, URL, or classpath resource into the cache.
     */
    private static Image loadImage(String filename) {
        if (filename == null) {
            throw new IllegalArgumentException("filename cannot be null");
        }

        // Check cache first!
        if (imageCache.containsKey(filename)) {
            return imageCache.get(filename);
        }

        try {
            BufferedImage img = null;

            // Try as a file first (most common case)
            File file = new File(filename);
            if (file.exists()) {
                img = ImageIO.read(file);
            }

            // Try as a URL (handles http://, https://, etc.)
            if (img == null && (filename.startsWith("http://") || filename.startsWith("https://"))) {
                img = ImageIO.read(URI.create(filename).toURL());
            }

            // Try as a classpath resource (for bundled resources)
            if (img == null) {
                URL resourceUrl = SimpleCanvas.class.getResource(filename);
                if (resourceUrl != null) {
                    img = ImageIO.read(resourceUrl);
                }
            }

            // One last try: classpath with leading slash if not present
            if (img == null && !filename.startsWith("/")) {
                URL resourceUrl = SimpleCanvas.class.getResource("/" + filename);
                if (resourceUrl != null) {
                    img = ImageIO.read(resourceUrl);
                }
            }

            if (img != null) {
                // Store in cache before returning
                imageCache.put(filename, img);
                return img;
            }

            // None of the approaches worked
            throw new IllegalArgumentException("image '" + filename + "' not found");

        } catch (IOException e) {
            throw new IllegalArgumentException("error loading image '" + filename + "': " + e.getMessage());
        }
    }
}
