package ag;

import env.envController;
import utils.d_star_lite.DStarLiteEight;

public class AirAgent extends MyAgent {
    String name;

    public AirAgent() {
        super(0.6, 8, 2,"air");
    }

    @Override
    public void initAg(){
        super.initAg();
        name = getTS().getAgArch().getAgName();
        envController.registerAirAgent(name, this);
        unstuckPathFinder = new DStarLiteEight();
        unstuckPathFinder.init(0,0,0,0);
    }

    public String getName() {
        return name;
    }
}
