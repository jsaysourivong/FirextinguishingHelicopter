package org.csc133.a3;

import com.codename1.ui.Dialog;
import com.codename1.ui.Display;
import com.codename1.ui.geom.Dimension;
import com.codename1.ui.geom.Point;
import org.csc133.a3.gameObjects.*;

import java.util.ArrayList;
import java.util.Random;

public class GameWorld {

    private final int numberOfFires = 2;

    private int numberOfBuildings = 3;
    private int totalFireSize = 0;
    private int xPosition;
    private int yPosition;
    private int lastTick;

    private Dimension mapSize;

    private River river;
    private Helipad helipad;
    private Helicopter helicopter;

    private ArrayList<GameObject> gameObjects;
    private ArrayList<Fire> fires;
    private ArrayList<Building> buildings;

    private Random r = new Random();

    public GameWorld() {
    }

    public void init() {

        buildings = new ArrayList<>();
        fires = new ArrayList<>();
        gameObjects = new ArrayList<>();
        gameObjects.add(createRiver());
        createBuilding();
        addBuildingToGO();
        createFire();
        addFireToGO();
        gameObjects.add(createHelipad());
        gameObjects.add(createHelicopter((int)helipad.getX() + 60,
                (int)helipad.getY() + 60));

        lastTick = (int) System.currentTimeMillis();
    }

    private void addFireToGO(){
        for(Fire fire : fires){
            gameObjects.add(fire);
        }
    }

    private void addBuildingToGO(){
        for(Building building : buildings){
            gameObjects.add(building);
        }
    }

    private void createBuilding(){
        for(int i = 0;i<numberOfBuildings;i++){
            switch (i){
                    case 0:
                        xPosition = mapSize.getWidth() - 400;
                        yPosition = mapSize.getHeight() - 900;
                        buildings.add(new Building(
                            xPosition,
                            yPosition,
                            mapSize.getWidth() / 8,
                            mapSize.getHeight() / 3));
                        break;

                    case 1:
                        xPosition = mapSize.getWidth() -
                                (mapSize.getWidth() - 400);
                        yPosition = mapSize.getHeight() - 800;
                        buildings.add(new Building(
                                xPosition,
                                yPosition,
                                mapSize.getWidth() / 8,
                                mapSize.getHeight() / 2));
                        break;

                    case 2:
                        xPosition = mapSize.getWidth() -
                                (mapSize.getWidth() / 2);
                        yPosition = mapSize.getHeight() - 150;
                        buildings.add(new Building(
                                xPosition,
                                yPosition,
                                mapSize.getWidth() -
                                        (mapSize.getWidth() /2 - 200),
                                mapSize.getHeight() / 10));
                        break;
            }
        }
    }
    private River createRiver() {
        river = new River(mapSize);
        return river;
    }

    private void createFire() {
        for(Building building : buildings){
            for(int i = 0 ; i < numberOfFires;i++){
                Fire fire = new Fire();
                building.setFireInBuilding(fire);
                fires.add(fire);
            }
        }
    }

    private Helipad createHelipad() {
        helipad = new Helipad(mapSize);
        return helipad;
    }

    private Helicopter createHelicopter(int xPosition, int yPosition) {
        helicopter = new Helicopter(new Point (xPosition, yPosition), helipad);
        return helicopter;
    }

    private void checkSizeOfFire() {
        fires.removeIf(fire -> fire.size <= 0);
    }

    private int getRandomX(int leftBound, int rightBound) {
        return r.nextInt((rightBound - leftBound) + 1) + leftBound;
    }

    private int getRandomY(int lowerBound, int upperBound) {
        return r.nextInt((upperBound - lowerBound) + 1) + lowerBound;
    }

    public void tick() {

        //Consider using this style
        /*gameObjects.forEach(object -> object.tick(elapsedTimeInMiliSeconds));*/

        int elapsedTimeInMiliSeconds = ((int) System.currentTimeMillis() -
                lastTick);

        for(GameObject go : gameObjects){
            go.tick(elapsedTimeInMiliSeconds);
        }

        fireGrow();
        drinkWater();
        checkSizeOfFire();
        helicopter.receiveFuel(helipad);
        updateBuildingDamage();
        updateBuildingValue();
        helicopter.updateLocalTransforms();
        checkGameConditions();
    }

    public void updateBuildingValue(){
        for(Building build : buildings){
            build.updateBuildingValue(gameObjects);
        }
    }

    public void updateBuildingDamage(){
        for(Building build : buildings){
            build.setBuildingDamage(gameObjects);
        }
    }

    /**
     * Checks to see if losing or winning
     * game conditions have been satisfied.
     */
    private void checkGameConditions() {
        if (helicopter.fuel <= 0) {
            if (showLosingDialog()) {
                // user clicked yes
                //
                init();
            } else {
                // user clicked no
                //
                quit();
            }
        }
        if ((winningGameConditions())) {
            // user clicked yes
            //
            if (showWinningDialog()) {
                init();
            } else {
                quit();
            }
        }
    }

    private boolean helicopterLand() {
        return (helicopter.getX() > helipad.getX()
                && helicopter.getX() < helipad.getX() + helipad.getSize()
                && helicopter.getY() > helipad.getY()
                && helicopter.getY() < helipad.getY() + helipad.getSize());
    }

    /**
     * There is two different dialogs; this is winning dialog
     *
     * @return true if yes, false if no
     */
    private boolean showWinningDialog() {
        return Dialog.show("You Won!", "Score: " +
                getTotalBuildingValue() +
                "\nWould you like to play again?", "yes", "No");
    }

    private boolean showLosingDialog() {
        return Dialog.show("Game Over!", "You ran out of fuel\n" +
                "Would you like to play again?", "yes", "No");
    }

    /**
     * This is all the conditions necessary to win
     *
     * @return true if game conditions have been satisfied, false otherwise
     */
    private boolean winningGameConditions() {
        if ((!fires.stream().iterator().hasNext())
                && (helicopter.getSpeed() == 0)
                && helicopterLand()) {
            return true;
        } else {
            return false;
        }
    }

    public void Start() {
        helicopter.startOrStopEngine();
    }

    public void quit() {
        Display.getInstance().exitApplication();
    }

    public void headHelicopterLeft() {
        helicopter.headLeft(+15);
    }

    public void headHelicopterRight() {
        helicopter.headRight(-15);
    }

    public void increaseSpeed() {
        helicopter.addSpeed(-1);
    }

    public void decreaseSpeed() {
        helicopter.addSpeed(1);
    }

    public void fightFire() {
        helicopter.fightFire(fires);
    }

    public void drinkWater() {
        helicopter.drink(river);
    }

    private void fireGrow() {
        int randomInt;
        for (GameObject go : gameObjects) {
            if(go instanceof Fire){
                Fire fire = (Fire)go;
                randomInt = r.nextInt(((500 + 1) + 1) + 1);
                if (randomInt == 300 || randomInt == 500 || randomInt == 100) {
                    fire.randomFireGrowth();
                }
            }

        }
    }
    public ArrayList<GameObject> getGameObjects(){
        return gameObjects;
    }

    public String getHeading() {
        int heading = helicopter.getHeading();
        if(heading == 360 || heading == -360){
            heading = heading % 360;
            helicopter.setHeading(0);
        }
        return String.valueOf(heading);
    }

    public String getSpeed() {
        return String.valueOf(Math.abs(helicopter.getSpeed()));
    }

    public String getFuel() {
        return String.valueOf(helicopter.fuel);
    }

    public void setDimension(Dimension mapSize) {
        this.mapSize = mapSize;
    }

    public String getFires() {
        int count = 0;
        return String.valueOf(fires.size());
    }

    public String getFireSize() {
        int temp = 0;
        for(GameObject go: gameObjects){
            if(go instanceof Fire){
                Fire fire = (Fire)go;
                temp += fire.getFireArea();
            }
        }
        return String.valueOf(temp);
    }

    public String getDamage() {

        int temp = 0;
        for(GameObject go : gameObjects){
            if(go instanceof Building){
                Building build = (Building)go;
                temp += build.getBuildingDamage();
            }
        }
        return String.valueOf(temp);
    }

    public String getLoss() {
        int totalFinancialLoss;
        int totalBuildingDamage = 0;
        int totalBuildingArea = 0;
        for(GameObject go : gameObjects){
            if(go instanceof Building){
                Building build = (Building)go;
                totalBuildingDamage += build.getBuildingDamage();
                totalBuildingArea += build.getBuildingArea();
            }
        }
        totalFinancialLoss =
                (totalBuildingDamage / totalBuildingArea) * 100;
        return String.valueOf(totalFinancialLoss);

    }

    public String getTotalBuildingValue(){
        int totalValueSaved = 0;
        for(GameObject go : gameObjects){
            if(go instanceof Building){
                Building build = (Building)go;
                totalValueSaved += build.getBuildingValue();
            }
        }
        return String.valueOf(totalValueSaved);
    }

}