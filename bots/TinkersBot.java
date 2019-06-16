package bots;

import arena.BattleBotArena;
import arena.BotInfo;
import arena.Bullet;

import java.awt.*;

/**
 * My strategy is extremely simple yet effective. The bot plays a purely reactionary
 * game to save on CPU time, while also dodging effectively and shooting when another bot
 * comes near. While it may not be as effective against players, the nature of random
 * bots means they will eventually come near enough to shoot and I can dodge the bullets easily.
 */
public class TinkersBot extends Bot {

    //Instance variables
    private BotHelper helpMe = new BotHelper(); //Create a bot helper object
    private Boolean closeBullet = false; //Boolean value to check if a bullet is close
    private static int tooClose = Bot.RADIUS * 5; //Distance at which a bullet is too close to ignore
    private BotInfo closeBot;  //Variable for the closest bot
    private double closeBotDis; //Distance to closest bot
    private static int shootDis = Bot.RADIUS * 10; //Distance that is close enough to shoot from


    /**
     * This method is called at the beginning of each round. Use it to perform
     * any initialization that you require when starting a new round.
     */
    @Override
    public void newRound() {

    }

    /**
     * This method is called at every time step to find out what you want your
     * Bot to do. The legal moves are defined in constants in the BattleBotArena
     * class (UP, DOWN, LEFT, RIGHT, FIREUP, FIREDOWN, FIRELEFT, FIRERIGHT, STAY,
     * SEND_MESSAGE). <br><br>
     * <p>
     * The <b>FIRE</b> moves cause a bullet to be created (if there are
     * not too many of your bullets on the screen at the moment). Each bullet
     * moves at speed set by the BULLET_SPEED constant in BattleBotArena. <br><br>
     * <p>
     * The <b>UP</b>, <b>DOWN</b>, <b>LEFT</b>, and <b>RIGHT</b> moves cause the
     * bot to move BOT_SPEED
     * pixels in the requested direction (BOT_SPEED is a constant in
     * BattleBotArena). However, if this would cause a
     * collision with any live or dead bot, or would move the Bot outside the
     * playing area defined by TOP_EDGE, BOTTOM_EDGE, LEFT_EDGE, and RIGHT_EDGE,
     * the move will not be allowed by the Arena.<br><Br>
     * <p>
     * The <b>SEND_MESSAGE</b> move (if allowed by the Arena) will cause a call-back
     * to this Bot's <i>outgoingMessage()</i> method, which should return the message
     * you want the Bot to broadcast. This will be followed with a call to
     * <i>incomingMessage(String)</i> which will be the echo of the broadcast message
     * coming back to the Bot.
     *
     * @param me       A BotInfo object with all publicly available info about this Bot
     * @param shotOK   True iff a FIRE move is currently allowed
     * @param liveBots An array of BotInfo objects for the other Bots currently in play
     * @param deadBots An array of BotInfo objects for the dead Bots littering the arena
     * @param bullets  An array of all Bullet objects currently in play
     * @return A legal move (use the constants defined in BattleBotArena)
     */
    @Override
    public int getMove(BotInfo me, boolean shotOK, BotInfo[] liveBots, BotInfo[] deadBots, Bullet[] bullets) {

        closeBullet = false;
        closeBot = helpMe.findClosest(me, liveBots);

        //Bullet dodging
        //Check every bullet
        for (Bullet bullet : bullets) {

            //Check if the bullet is too close
            if ((helpMe.calcDistance(bullet.getX(), bullet.getY(), me.getX(), me.getY())) <= tooClose) {
                closeBullet = true;

                //Dodge accordingly

                //Dodging horizontal bullets
                if (((bullet.getX() >= me.getX() + Bot.RADIUS) || (bullet.getX() + Bot.RADIUS <= me.getX()))
                        && ((bullet.getXSpeed() != 0) && (bullet.getYSpeed() == 0))
                ) {
                    //Dodge up
                    if (me.getY() + Bot.RADIUS > bullet.getY()) {

                        return BattleBotArena.DOWN;
                    }

                    //Dodge down
                    else {

                        return BattleBotArena.UP;
                    }
                }

                //Dodging vertical bullets
                if (((bullet.getY() >= me.getY() + Bot.RADIUS) || (bullet.getY() + Bot.RADIUS <= me.getY()))
                        && ((bullet.getYSpeed() != 0) && (bullet.getXSpeed() == 0))
                ) {
                    //Dodge right
                    if (me.getX() + Bot.RADIUS > bullet.getX()) {

                        return BattleBotArena.RIGHT;
                    }

                    //Dodge left
                    else {
                        return BattleBotArena.LEFT;
                    }
                }

            }
        } //end of bullet dodging


        //Shooting

        closeBotDis = helpMe.calcDistance(me.getX() + Bot.RADIUS, me.getY() + Bot.RADIUS, closeBot.getX(), closeBot.getY());
        //Check if there is a bot close enough to shoot
        if (closeBotDis <= shootDis) {
            //Check if the bot is lined up in the x-axis within 20 units
            if (Math.abs(me.getX() - closeBot.getX()) <= 20) {
                //Check if the bot is above my bot
                if (shotOK && me.getY() + Bot.RADIUS > closeBot.getY() + Bot.RADIUS) {
                    return BattleBotArena.FIREUP; //shoot up

                    //Check if the bot is below my bot
                } else if (shotOK && me.getY() + Bot.RADIUS < closeBot.getY() + Bot.RADIUS) {
                    return BattleBotArena.FIREDOWN; //shoot down
                }

                //Check if the bot is lined up with my bot in the y-axis within 20 units
            } else if (Math.abs(me.getY() - closeBot.getY()) <= 20) {
                //Check if the bot is to the left
                if (shotOK && me.getX() + Bot.RADIUS > closeBot.getX() + Bot.RADIUS) { // farther apart from the bot
                    return BattleBotArena.FIRELEFT; //shoot left
                    //check if the bot is to the right of my bot
                } else if (me.getX() + Bot.RADIUS < closeBot.getX() + Bot.RADIUS) {
                    return BattleBotArena.FIRERIGHT; //shoot right
                }
            }

        }

        return 0;
    }

    /**
     * Called when it is time to draw the Bot. Your Bot should be (mostly)
     * within a circle inscribed inside a square with top left coordinates
     * <i>(x,y)</i> and a size of <i>RADIUS * 2</i>. If you are using an image,
     * just put <i>null</i> for the ImageObserver - the arena has some special features
     * to make sure your images are loaded before you will use them.
     *
     * @param g The Graphics object to draw yourself on.
     * @param x The x location of the top left corner of the drawing area
     * @param y The y location of the top left corner of the drawing area
     */
    @Override
    public void draw(Graphics g, int x, int y) {
        g.setColor(Color.GREEN);
        g.fillOval(x, y, RADIUS * 2, RADIUS * 2);
    }

    /**
     * This method will only be called once, just after your Bot is created,
     * to set your name permanently for the entire match.
     *
     * @return The Bot's name
     */
    @Override
    public String getName() {
        return "Btink";
    }

    /**
     * This method is called at every time step to find out what team you are
     * currently on. Of course, there can only be one winner, but you can
     * declare and change team allegiances throughout the match if you think
     * anybody will care. Perhaps you can send coded broadcast message or
     * invitation to other Bots to set up a temporary team...
     *
     * @return The Bot's current team name
     */
    @Override
    public String getTeamName() {
        return null;
    }

    /**
     * This is only called after you have requested a SEND_MESSAGE move (see
     * the documentation for <i>getMove()</i>). However if you are already over
     * your messaging cap, this method will not be called. Messages longer than
     * 200 characters will be truncated by the arena before being broadcast, and
     * messages will be further truncated to fit on the message area of the screen.
     *
     * @return The message you want to broadcast
     */
    @Override
    public String outgoingMessage() {
        return null;
    }

    /**
     * This is called whenever the referee or a Bot sends a broadcast message.
     *
     * @param botNum The ID of the Bot who sent the message, or <i>BattleBotArena.SYSTEM_MSG</i> if the message is from the referee.
     * @param msg    The text of the message that was broadcast.
     */
    @Override
    public void incomingMessage(int botNum, String msg) {

    }

    /**
     * This is called by the arena at startup to find out what image names you
     * want it to load for you. All images must be stored in the <i>images</i>
     * folder of the project, but you only have to return their names (not
     * their paths).<br><br>
     * <p>
     * PLEASE resize your images in an image manipulation
     * program. They should be squares of size RADIUS * 2 so that they don't
     * take up much memory.
     *
     * @return An array of image names you want the arena to load.
     */
    @Override
    public String[] imageNames() {
        return new String[0];
    }

    /**
     * Once the arena has loaded your images (see <i>imageNames()</i>), it
     * calls this method to pass you the images it has loaded for you. Store
     * them and use them in your draw method.<br><br>
     * <p>
     * PLEASE resize your images in an
     * image manipulation program. They should be squares of size RADIUS * 2 so
     * that they don't take up much memory.<br><br>
     * <p>
     * CAREFUL: If you got the file names wrong, the image array might be null
     * or contain null elements.
     *
     * @param images The array of images (or null if there was a problem)
     */
    @Override
    public void loadedImages(Image[] images) {
    }
}
