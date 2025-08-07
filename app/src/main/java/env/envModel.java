package env;


import ag.MyAgent;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.environment.grid.GridWorldModel;
import tile.TileManager;
import jason.environment.grid.Location;
import java.util.*;


public class envModel extends GridWorldModel {

    public static final int gridSize = 20;
    public static final int n_agents=10;

    public static final int EDGE = 8; //Map contours and mountains.
    public static final int GRASS = 16; //Walkable cell with higher cost.
    public static final int OBST_FIX = 32; //Not-removable obstacle.
    public static final int HIDING = 64; //Ground agent can interact with it. May contain a target.
    public static final int STREET = 256; //Walkable cell with 0 cost.
    public static final int OBST_REM = 512; //Removable obstacle. Removing it costs time.
    public static final int CLEARED = 1024; //Hiding after being interacted with.
    public static final int GROUND = 2048;  //Ground agent.
    public static final int AIR = 4096; //Air agent.
    public static final int TARGET = 8192; //Target to find.

    public List<String> agents = new ArrayList<>(); //A list containing the agent names.
    private final int[][] bitmap; //The bit map associated to the current map. Each cell has a specific number which represent the content of that cell
    private final Map<Integer,Integer> indexToMask; //mapping between the values in the bitmap and the correspondent values defined above.
    private final double speedModifier;
    public List<Location> hidings; //A list of the POIs locations.
    public List<Location> visitedHidings;
    public int nUselessSearches;

    public envModel(TileManager tileManager, double speedModifier) {
        super(envModel.gridSize,envModel.gridSize,n_agents);
        this.hidings = new ArrayList<>();
        this.speedModifier = speedModifier;
        bitmap = tileManager.getBitMap();
        indexToMask = tileManager.getIntToMask();
        nUselessSearches = 0; visitedHidings = new ArrayList<>();
    }

    //Initialize the view of the map. For each cell calls the add method defined in the view.
    public void mapInitialize() {
        for (int x=0; x<gridSize; x++) {
            for (int y=0; y<gridSize; y++) {
                int mask = indexToMask.get(bitmap[x][y]);
                if (mask==HIDING) {
                    hidings.add(new Location(y,x));
                }
                super.add(mask,y,x);
            }
        }
    }

    //If called adds a target in one of the hidings.
    public void addTarget(){
        Random rand = new Random();
        int x = rand.nextInt(hidings.size());
        System.out.println("Target added in: "+hidings.get(x));
        super.add(TARGET,hidings.get(x));
    }

    //The first interaction with the agent. Adds the agent in the list and initialize its position.
    public boolean createAgent(String agName, Term t2, int type) {
        agents.add(agName);
        int i=agents.indexOf(agName);
        Structure s = (Structure) t2;
        Location l = new Location(Integer.parseInt(s.getTerm(0).toString()),
                Integer.parseInt(s.getTerm(1).toString()));

        setAgPosCustom(i,l,type);
        return true;
    }

    //Changes the agent position. The time needed to be able to move again depends on the agent speed and the cell it must traverse.
    public boolean move(int agId, Term x, MyAgent a, int type){
        Structure s = (Structure) x;
        Location l2 = new Location(Integer.parseInt(s.getTerm(0).toString()),Integer.parseInt(s.getTerm(1).toString()));
        setAgPosCustom(agId,l2,type);
        double speedCellModifier = 1.0;
        if(hasObject(GRASS,l2) && a.getType().equals("ground")){
            speedCellModifier = 1.5;
        }
        double speed = a.getSpeed()*1000*speedCellModifier/speedModifier;
        try {
            Thread.sleep((long) speed);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    //Search a given cell. After 4s the cell is marked as CLEARED. If a target is found it returns True.
    public boolean search(Term x){
        Structure s = (Structure) x;
        Location lHiding = new Location(Integer.parseInt(s.getTerm(0).toString()),
                Integer.parseInt(s.getTerm(1).toString()));
        try {
            Thread.sleep((long) (4000/speedModifier));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if(visitedHidings.contains(lHiding))
            nUselessSearches++;
        else
            visitedHidings.add(lHiding);

        add(CLEARED,lHiding);
        return hasObject(TARGET,lHiding);
    }

    //Removes the obstacle. It takes 4s.
    public boolean remove_obs(Term x){
        Structure s = (Structure) x;
        Location lObst = new Location(Integer.parseInt(s.getTerm(0).toString()),
                Integer.parseInt(s.getTerm(1).toString()));
        try {
            Thread.sleep((long) (4000/speedModifier));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        remove(OBST_REM,lObst);
        return true;
    }

    //Computes the agent percepts considering its sensing range.
    public Map<String,List<Location>> getSurroundingPercepts(int agId, MyAgent ag) {
        Map<String, List<Location>> surroundings = new HashMap<>();
        Location l = getAgPos(agId);
        int[][] directions = ag.getSensingArea();
        for (int[] direction : directions) {
            int newX = l.x + direction[0];
            int newY = l.y + direction[1];
            if (newX >= 0 && newX < gridSize && newY >= 0 && newY < gridSize) {
                if (hasObject(EDGE, new Location(newX, newY))) {
                    surroundings.computeIfAbsent("edge", k -> new ArrayList<>()).add(new Location(newX, newY));
                }
                if (hasObject(GRASS, new Location(newX, newY))) {
                    surroundings.computeIfAbsent("grass", k -> new ArrayList<>()).add(new Location(newX, newY));
                }
                if (hasObject(OBST_FIX, new Location(newX, newY))) {
                    surroundings.computeIfAbsent("fixed_obstacle", k -> new ArrayList<>()).add(new Location(newX, newY));
                }
                if (hasObject(HIDING, new Location(newX, newY))) {
                    surroundings.computeIfAbsent("hiding", k -> new ArrayList<>()).add(new Location(newX, newY));
                }
                if (hasObject(STREET, new Location(newX, newY))) {
                    surroundings.computeIfAbsent("street", k -> new ArrayList<>()).add(new Location(newX, newY));
                }
                if (hasObject(OBST_REM, new Location(newX, newY))) {
                    surroundings.computeIfAbsent("rem_obstacle", k -> new ArrayList<>()).add(new Location(newX, newY));
                }
            }
        }
        return surroundings;
    }

    //Returns the locations of the agents in the sensing range of agent ag.
    public Map<String,Location> getSurroundingAgents(int agId, MyAgent ag) {
        int[][] directions = ag.getSensingArea();
        Map<String, Location> surroundingAgs = new HashMap<>();
        Location l = getAgPos(agId);

        for (int[] direction : directions) {
            int newX = l.x + direction[0];
            int newY = l.y + direction[1];
            if (newX >= 0 && newX < gridSize && newY >= 0 && newY < gridSize) {
                Location newLoc = new Location(newX, newY);
                if (hasObject(GROUND, newLoc) || hasObject(AIR, newLoc)) {
                    for (int j = 0; j < agents.size(); j++) {
                        if (getAgPos(j).equals(newLoc)){
                            surroundingAgs.computeIfAbsent(agents.get(j), k -> newLoc);}
                    }
                }
            }
        }
        if (hasObject(GROUND,l)) {
            for(int j=0;j<agents.size();j++){
                if(getAgPos(j).equals(l) && j!=agId)
                    surroundingAgs.computeIfAbsent(agents.get(j), k ->l);
            }
        }
        return surroundingAgs;
    }

    //Generate the removable obstacles on the map.
    public void generateObs(int n){
        List<Location> streets = new ArrayList<>();
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                if (hasObject(STREET,new Location(j,i))){
                    streets.add(new Location(j,i));
                }
            }
        }
        Random rand = new Random();
        for (int i = 0; i < n; i++) {
            int index = rand.nextInt(streets.size());
            add(OBST_REM,streets.get(index));
            streets.remove(index);
        }
    }

    //Custom setAgPos, the logic adapts to the project.
    public void setAgPosCustom(int ag, Location l, int type) {
        Location oldLoc = this.getAgPos(ag);
        if (oldLoc != null) {
            this.remove(type, oldLoc.x, oldLoc.y);
        }
        this.agPos[ag] = l;
        this.add(type, l.x, l.y);
    }
}