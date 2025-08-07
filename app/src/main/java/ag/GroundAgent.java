package ag;


import env.envController;
import utils.d_star_lite.DStarLite;

public class GroundAgent extends MyAgent {
    String name;

    public GroundAgent() {
        super(1.5, 4, 1,"ground");
    }
    @Override
    public void initAg(){
        super.initAg();
        name = getTS().getAgArch().getAgName();
        envController.registerGroundAgent(name, this);
        unstuckPathFinder = new DStarLite();
        unstuckPathFinder.init(0,0,0,0);
    }

    public String getName() {
        return name;
    }

}

