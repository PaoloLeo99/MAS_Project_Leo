package env;

public class StartupConfig {
    private int numObstacles;
    private String mapName;
    public boolean configReady = false;
    private String speed;
    private Boolean targetTag; //if true there is the target, if false it's an exploration problem

    public void setSpeed(String speed) {
        this.speed = speed;
    }
    public void setNumObstacles(int numObstacles) {
        this.numObstacles = numObstacles;
    }
    public void setTarget(String objective) {
        switch (objective) {
            case "Search Target" -> targetTag=true;
            case "Explore All POIs" -> targetTag=false;
            default -> targetTag=false;
        };
    }
    public void setMapName(String mapName) {
        this.mapName = mapName;
    }
    public double getSpeed() {
        return switch (speed) {
            case "Normal" -> 1.0;
            case "Fast" -> 2.0;
            default -> 1.0;
        };
    }
    public int getNumObstacles() {
        return numObstacles;
    }
    public String getMapName() { return mapName; }
    public Boolean getTarget(){
        return targetTag;
    }

}

