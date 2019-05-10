package bots;

import arena.BattleBotArena;
import arena.BotInfo;
import arena.Bullet;

import java.awt.*;
import java.lang.Math;
import java.util.*;
import java.time.Instant;

/**
 * BloisBot is a bot that uses a strategy of avoiding other bots and shooting when necessary.
 * The bot analyzes a grid of locations around itself (5x5) and determines which location is the furthest
 * from other live bots, dead bots, and the walls of the arena. It tries to find a path to give itself the most amount
 * of space to move around in, and shoots other bots when they are lined up with the BloisBot.
 *
 * @author Eric Blois
 * @version 1.0
 */

public class BloisBot extends Bot {
    //Instantiate a bot helper for detecting other objects
    BotHelper helper = new BotHelper();

    Image image;
    Image targetImage;
    //An instant at which the last shot was fired
    Instant lastShotInstant = Instant.now().minusMillis(1000);
    //An instant at which the last location was picked
    Instant lastLocationUpdate = Instant.now().minusMillis(1000);
    //The target location for this bot to move to
    double targetX = 0;
    double targetY = 0;

    long fireSpeedInMillis = 100;
    long targetUpdateSpeedInMillis = 500;
    //Sets the target to the nearest enemy or open space
    private void setTarget(BotInfo me, BotInfo[] liveBots, BotInfo[] deadBots, Bullet[] bullets) {
        /*BotInfo nearestEnemy = helper.findClosest(me, liveBots);

        if (nearestEnemy.getTeamName() != me.getTeamName()) {
            targetX = nearestEnemy.getX();
            targetY = nearestEnemy.getY();
        } else {
            setTargetToOpenSpace(me, liveBots, deadBots, bullets);
        }*/
        setTargetToOpenSpace(me, liveBots, deadBots, bullets);
    }

    //Checks 25 locations around the bot and determines which one is the furthest from obstacles, then sets that location as a target location
    private void setTargetToOpenSpace(BotInfo me, BotInfo[] liveBots, BotInfo[] deadBots, Bullet[] bullets) {
        if (lastLocationUpdate.plusMillis(targetUpdateSpeedInMillis).isBefore(Instant.now())) {
            lastLocationUpdate = Instant.now();
            //Locations around me that will be checked
            double[] xLocations = {me.getX() - this.RADIUS, me.getX() - this.RADIUS / 2, me.getX(), me.getX() + this.RADIUS / 2, me.getX() + this.RADIUS};
            double[] yLocations = {me.getY() - this.RADIUS, me.getY() - this.RADIUS / 2, me.getY(), me.getY() + this.RADIUS / 2, me.getY() + this.RADIUS};
            //The index of the safest x and y location
            int safeX = 2;
            int safeY = 2;

            double furthestDistance = 0;
            if (deadBots.length > 0) {
                BotInfo closestLive = helper.findClosest(me, liveBots);
                BotInfo closestDead = helper.findClosest(me, deadBots);

                for (int x = 0; x < 5; x++) {
                    for (int y = 0; y < 5; y++) {
                        double distFromObstacles = getMinDistFromObstacles(xLocations[x], yLocations[y], closestLive, closestDead);
                        //Check if this location is further from obstacles than the minimum, and won't be hit by bullets
                        if (distFromObstacles > furthestDistance && getIncomingBullets(xLocations[x], yLocations[y], bullets).length == 0) {
                            furthestDistance = distFromObstacles;
                            safeX = x;
                            safeY = y;
                        }
                    }
                }
                targetX = xLocations[safeX];
                targetY = yLocations[safeY];
            }
        }
    }
    //Gets the minimum distance from obstacles at any point
    private double getMinDistFromObstacles(double meX, double meY, BotInfo closestLive, BotInfo closestDead) {
        //Get the maximum possible distance from an object
        double minDistance = BattleBotArena.RIGHT_EDGE - BattleBotArena.LEFT_EDGE;
        //Get the distance from the closest live and dead bots
        double distFromClosestLive = helper.calcDistance(meX, meY, closestLive.getX(), closestLive.getY());
        double distFromClosestDead = helper.calcDistance(meX, meY, closestDead.getX(), closestDead.getY());
        //Check each closest object to see if the distance is less than the current minimum
        if (distFromClosestLive < minDistance) {
            minDistance = distFromClosestLive;
        }
        if (distFromClosestDead < minDistance) {
            minDistance = distFromClosestDead;
        }
        //Check distance from edges of screen
        if (meX - BattleBotArena.LEFT_EDGE < minDistance) {
            minDistance = meX - BattleBotArena.LEFT_EDGE;
        }
        if (BattleBotArena.RIGHT_EDGE - meX < minDistance) {
            minDistance = BattleBotArena.RIGHT_EDGE - meX;
        }
        if (meY - BattleBotArena.TOP_EDGE < minDistance) {
            minDistance = meY - BattleBotArena.TOP_EDGE;
        }
        if (BattleBotArena.BOTTOM_EDGE - meY < minDistance) {
            minDistance = BattleBotArena.BOTTOM_EDGE - meY;
        }
        return minDistance;
    }

    //Check if a move will result in a collision or being in line with a bullet
    private boolean checkMove(BotInfo me, BotInfo[] liveBots, BotInfo[] deadBots, Bullet[] bullets, int move) {
        //Get min and max x values of me
        double maxX = me.getX() + this.RADIUS;
        double minX = me.getX() - this.RADIUS;
        //Get min and max y values of me
        double maxY = me.getY() + this.RADIUS;
        double minY = me.getY() - this.RADIUS;
        //Check if there are dead and live bots
        if (deadBots.length > 0 && liveBots.length > 0) {
            //Get the closest live and dead bots
            BotInfo closestLive = helper.findClosest(me, liveBots);
            BotInfo closestDead = helper.findClosest(me, deadBots);

            //Get min and max x values of live bot
            double liveMaxX = closestLive.getX() + this.RADIUS;
            double liveMinX = closestLive.getX() - this.RADIUS;
            //Get min and max y values of live bot
            double liveMaxY = closestLive.getY() + this.RADIUS;
            double liveMinY = closestLive.getY() - this.RADIUS;

            //Get min and max x values of dead bot
            double deadMaxX = closestDead.getX() + this.RADIUS;
            double deadMinX = closestDead.getX() - this.RADIUS;
            //Get min and max y values of dead bot
            double deadMaxY = closestDead.getY() + this.RADIUS;
            double deadMinY = closestDead.getY() - this.RADIUS;

            //Check what the move is
            switch (move) {
                case BattleBotArena.UP:
                    //Check if me will collide with top edge
                    if (minY - BattleBotArena.TOP_EDGE < 2) {
                        return false;
                    } //Check if me will collide with bottom edge of nearest live bot
                    else if (liveMaxY < minY && minY - liveMaxY < 2) {
                        return false;
                    } //Check if me will collide with bottom edge of nearest dead bot
                    else if (deadMaxY < minY && minY - deadMaxY < 2) {
                        return false;
                    } //Check if me will be hit by a bullet
                    else if (getIncomingBullets(me.getX(), me.getY() - 1, bullets).length > 0) {
                        return false;
                    } else {
                        return true;
                    }
                case BattleBotArena.DOWN:
                    //Check if me will collide with bottom edge
                    if (BattleBotArena.BOTTOM_EDGE - maxY < 2) {
                        return false;
                    } //Check if me will collide with top edge of nearest live bot
                    else if (maxY < liveMinY && liveMinY - maxY < 2) {
                        return false;
                    } //Check if me will collide with top edge of nearest dead bot
                    else if (maxY < deadMinY && deadMinY - maxY < 2) {
                        return false;
                    } //Check if me will be hit by a bullet
                    else if (getIncomingBullets(me.getX(), me.getY() + 1, bullets).length > 0) {
                        return false;
                    } else {
                        return true;
                    }
                case BattleBotArena.LEFT:
                    //Check if me will collide with left edge
                    if (minX - BattleBotArena.LEFT_EDGE < 2) {
                        return false;
                    } //Check if me will collide with right edge of nearest live bot
                    else if (liveMaxX < minX && minX - liveMaxX < 2) {
                        return false;
                    } //Check if me will collide with right edge of nearest dead bot
                    else if (deadMaxX < minX && minX - deadMaxX < 2) {
                        return false;
                    } //Check if me will be hit by a bullet
                    else if (getIncomingBullets(me.getX() - 1, me.getY(), bullets).length > 0) {
                        return false;
                    } else {
                        return true;
                    }
                case BattleBotArena.RIGHT:
                    //Check if me will collide with right edge
                    if (BattleBotArena.RIGHT_EDGE - maxX < 2) {
                        return false;
                    } //Check if me will collide with left edge of nearest live bot
                    else if (maxX < liveMinX && liveMinX - maxX < 2) {
                        return false;
                    } //Check if me will collide with left edge of nearest dead bot
                    else if (maxX < deadMinX && deadMinX - maxX < 2) {
                        return false;
                    } //Check if me will be hit by a bullet
                    else if (getIncomingBullets(me.getX() + 1, me.getY(), bullets).length > 0) {
                        return false;
                    } else {
                        return true;
                    }
            }
        }
        return true;
    }
    // Moves the bot toward the current target location
    private int moveTowardTarget(BotInfo me, BotInfo[] liveBots, BotInfo[] deadBots, Bullet[] bullets) {
        //Get the x and y distances to the target location
        double xDist = me.getX() - targetX;
        double yDist = me.getY() - targetY;
        //Check if each move is good
        boolean upCheck = checkMove(me, liveBots, deadBots, bullets, BattleBotArena.UP);
        boolean downCheck = checkMove(me, liveBots, deadBots, bullets, BattleBotArena.DOWN);
        boolean leftCheck = checkMove(me, liveBots, deadBots, bullets, BattleBotArena.LEFT);
        boolean rightCheck = checkMove(me, liveBots, deadBots, bullets, BattleBotArena.RIGHT);
        //Check if the location is further from me horizontally than vertically
        if (Math.abs(xDist) >= Math.abs(yDist) && (leftCheck || rightCheck)) {
            //Move to location horizontally
            if (xDist > 0 && leftCheck) {
                return BattleBotArena.LEFT;
            } else {
                return BattleBotArena.RIGHT;
            }
        } else if (upCheck || downCheck) {
            //Move to location vertically
            if (yDist > 0 && upCheck) {
                return BattleBotArena.UP;
            } else {
                return BattleBotArena.DOWN;
            }
        } else {
            System.out.println("bad move!");
            return (int) (Math.random() * ((3) + 1)) + 1;
        }
    }

    private Bullet[] getIncomingBullets(double meX, double meY, Bullet[] bullets) {
        if (bullets.length > 0) {
            //Get min and max x values of me
            double maxX = meX + this.RADIUS * 2;
            double minX = meX - this.RADIUS * 2;
            //Get min and max y values of me
            double maxY = meY + this.RADIUS * 2;
            double minY = meY - this.RADIUS * 2;
            //Instantiate an array of incoming bullets
            java.util.List<Bullet> incomingBullets = new ArrayList<>();
            //Iterate through each bullet
            for (int i = 0; i < bullets.length; i++) {
                Bullet bullet = bullets[i];
                //Get the bullet's x and y distance from me
                double xDist = meX - bullet.getX();
                double yDist = meY - bullet.getY();
                //Get the bullet's total distance from me
                double dist = helper.calcDistance(meX, meY, bullet.getX(), bullet.getY());
                /*//Check if bullet is near me
                if (dist > this.RADIUS*5) {
                    continue;
                }*/
                //Check if bullet is moving toward me
                if (bullet.getXSpeed() > 0 && xDist > 0) {
                    //Check if bullet's y-value is between the min and max y-values of me
                    if (bullet.getY() >= minY && bullet.getY() <= maxY) {
                        //Add this bullet to the array of incoming bullets
                        incomingBullets.add(bullet);
                    }
                } else if (bullet.getXSpeed() < 0 && xDist < 0) {
                    //Check if bullet's y-value is between the min and max y-values of me
                    if (bullet.getY() >= minY && bullet.getY() <= maxY) {
                        //Add this bullet to the array of incoming bullets
                        incomingBullets.add(bullet);
                    }
                } else if (bullet.getYSpeed() < 0 && yDist < 0) {
                    //Check if bullet's x-value is between the min and max y-values of me
                    if (bullet.getX() >= minX && bullet.getX() <= maxX) {
                        //Add this bullet to the array of incoming bullets
                        incomingBullets.add(bullet);
                    }
                } else if (bullet.getYSpeed() > 0 && yDist > 0) {
                    //Check if bullet's x-value is between the min and max y-values of me
                    if (bullet.getX() >= minX && bullet.getX() <= maxX) {
                        //Add this bullet to the array of incoming bullets
                        incomingBullets.add(bullet);
                    }
                }
            }
            return incomingBullets.toArray(new Bullet[incomingBullets.size()]);
        }
        return bullets;
    }

    private int avoidBullets(BotInfo me, Bullet[] bullets) {
        Bullet[] incomingBullets = getIncomingBullets(me.getX(), me.getY(), bullets);
        //Check if there is an incoming bullet
        if (incomingBullets.length > 0) {
            //Get closest incoming bullet
            Bullet closestIncoming = helper.findClosest(me, incomingBullets);

            //Get the bullet's x and y distance from me
            double xDist = me.getX() - closestIncoming.getX();
            double yDist = me.getY() - closestIncoming.getY();

            //Check if bullet is further from me horizontally than vertically
            if (Math.abs(xDist) >= Math.abs(yDist)) {
                //Move away from the bullet vertically
                if (yDist > 0) {
                    return BattleBotArena.DOWN;
                } else {
                    return BattleBotArena.UP;
                }
            } else {
                //Move away from the bullet horizontally
                if (xDist > 0) {
                    return BattleBotArena.RIGHT;
                } else {
                    return BattleBotArena.LEFT;
                }
            }
        }
        return BattleBotArena.STAY;
    }
    //Shoots a bot if it is lined up and the timing is right
    private int shootBot(BotInfo me, BotInfo[] liveBots) {
            //Check if it has been at least 200 milliseconds since the last shot
            if (lastShotInstant.plusMillis(fireSpeedInMillis).isBefore(Instant.now())) {
                //Instantiate an array of bots that are inline with me
                java.util.List<BotInfo> inlineBots = new ArrayList<BotInfo>();
                //Iterate through each live bot
                for (int i = 0; i < liveBots.length; i++) {
                    BotInfo bot = liveBots[i];
                    //Get min and max x values of bot
                    double maxX = bot.getX() + this.RADIUS;
                    double minX = bot.getX() - this.RADIUS;
                    //Get min and max y values of bot
                    double maxY = bot.getY() + this.RADIUS;
                    double minY = bot.getY() - this.RADIUS;

                    //Check if bot is inline with me, and is not on my team
                    if (me.getTeamName() != bot.getTeamName() && (me.getX() > minX && me.getX() < maxX) || (me.getY() > minY && me.getY() < maxY)) {
                        //Add bot to list of inline bots
                        inlineBots.add(bot);
                    }
                }


                if (!inlineBots.isEmpty()) {
                    //Get list of inline bots
                    BotInfo[] botsArray = inlineBots.toArray(new BotInfo[inlineBots.size()]);
                    //Get the closest inline bot by converting the list to an array
                    BotInfo closestInline = helper.findClosest(me, botsArray);
                    //Get the x distance and y distance of the bot
                    double xDist = me.getX() - closestInline.getX();
                    double yDist = me.getY() - closestInline.getY();
                    //Check if bot is inline horizontally or vertically
                    //Get min and max x values of bot
                    double maxX = closestInline.getX() + this.RADIUS;
                    double minX = closestInline.getX() - this.RADIUS;
                    //Get min and max y values of bot
                    double maxY = closestInline.getY() + this.RADIUS;
                    double minY = closestInline.getY() - this.RADIUS;

                    //Check if bot is inline with me vertically
                    if (me.getX() > minX && me.getX() < maxX) {
                        //Shoot at the bot vertically
                        if (yDist > 0) {
                            //Shoot up
                            lastShotInstant = Instant.now();
                            return BattleBotArena.FIREUP;
                        } else {
                            //Shoot down
                            lastShotInstant = Instant.now();
                            return BattleBotArena.FIREDOWN;
                        }
                    } else {
                        //Shoot at the bot horizontally
                        if (xDist > 0) {
                            //Shoot left
                            lastShotInstant = Instant.now();
                            return BattleBotArena.FIRELEFT;
                        } else {
                            //Shoot right
                            lastShotInstant = Instant.now();
                            return BattleBotArena.FIRERIGHT;
                        }
                    }
                }
            }
        return BattleBotArena.STAY;
    }
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
        //Update the target location
        setTarget(me, liveBots, deadBots, bullets);
        //Check if there is at least one live bot and one bullet
        if (liveBots.length > 0 && bullets.length > 0) {
            //Attempt to avoid bullets if necessary
            int avoidBullets = avoidBullets(me, bullets);
            if (avoidBullets != BattleBotArena.STAY) {
                System.out.print("Dodging!");
                return avoidBullets;
            }
            //Attempt to shoot other bots
            int shootBot = shootBot(me, liveBots);
            if (shootBot != BattleBotArena.STAY) {
                return shootBot;
            }
            int moveTowardTarget = moveTowardTarget(me, liveBots, deadBots, bullets);
            if (moveTowardTarget != BattleBotArena.STAY) {
                return moveTowardTarget;
            }
        }
        return BattleBotArena.DOWN;
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
        g.drawImage(image, x, y, Bot.RADIUS*2, Bot.RADIUS*2, null);
        g.drawImage(targetImage, (int) targetX, (int) targetY, Bot.RADIUS, Bot.RADIUS, null);
    }

    /**
     * This method will only be called once, just after your Bot is created,
     * to set your name permanently for the entire match.
     *
     * @return The Bot's name
     */
    @Override
    public String getName() {
        return "bloise";
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
        String[] names = {"bloise.png", "greenX.png"};
        return names;
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
        if (images != null) {
            image = images[0];
            targetImage = images[1];
        }
    }
}
