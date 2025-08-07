package ag;

import jason.asSemantics.Agent;
import utils.d_star_lite.MyPathFinder;


public class MyAgent extends Agent {
    double speed;
    MyPathFinder unstuckPathFinder;
    int sensingDir; //4-way or 8-way
    int sensingWidth; //sensing radius
    String type;

    public MyAgent(double speed, int sensingDir, int sensingWidth, String type) {
        super();
        this.speed = speed;
        this.sensingDir = sensingDir;
        this.sensingWidth = sensingWidth;
        this.type = type;
    }

    //creating the sensing area from the width and radius. Returning it as a matrix
    public int[][] getSensingArea(){
        int[][] sensingArea;
        if (sensingDir == 4){
            sensingArea = new int[4*sensingWidth][2];
        }else if (sensingDir == 8){
            int n_elements = (int) Math.pow(sensingWidth*2+1,2);
            sensingArea = new int[n_elements-1][2];
        }else return null;
        int k=0;
        for (int i=-sensingWidth; i<=sensingWidth; i++) {
            for (int j=-sensingWidth; j<=sensingWidth; j++) {
                if (i==0 && j==0){
                    continue;
                }
                if (sensingDir == 4){
                    if (i==0 || j==0){
                        sensingArea[k][0] = i;
                        sensingArea[k][1] = j;
                        k+=1;
                    }
                }else {
                    sensingArea[k][0]=i;
                    sensingArea[k][1]=j;
                    k+=1;
                }
            }
        }
        return sensingArea;
    }
    public double getSpeed() {
        return speed;
    }
    public MyPathFinder getUnstuckPathFinder() {
        return unstuckPathFinder;
    }

    public int getSensingWidth() {
        return sensingWidth;
    }
    public String getType() {
        return type;
    }
}