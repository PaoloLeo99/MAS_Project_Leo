package env;

import ag.AirAgent;
import ag.GroundAgent;
import ag.MyAgent;
import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.environment.Environment;
import jason.environment.grid.Location;
import tile.TileManager;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;


public class envController extends Environment {

    public static final Literal m = Literal.parseLiteral("move(X,Y)");
    public static final Literal h = Literal.parseLiteral("hello(AgentName, current_pos(X,Y))");
    public static final Literal s = Literal.parseLiteral("search(X)");
    public static final Literal r = Literal.parseLiteral("remove_obs(X)");
    public static final Literal c = Literal.parseLiteral("close");

    //The maps have an entry for each agent. The mapping is between the agent Name and the object of the respective class.
    public static Map<String, GroundAgent> groundAgents;
    public static Map<String, AirAgent> airAgents;
    private envModel model;
    private long startTime;
    private int nReturnAgents;
    private long expTime; //time when all POIs have been investigated
    private boolean checkExpTime;

    public void init(final String[] args) {
        StartupConfig config = new StartupConfig();
        //Showing the initial menu.
        showStartupDialog(config);
        synchronized (config) {
            while (!config.configReady) {
                try {
                    config.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        System.out.println("Startup complete.");
        startTime = System.currentTimeMillis();
        nReturnAgents = 0;
        checkExpTime = true;

        TileManager tileManager = new TileManager("/tiles", model, config.getMapName());
        this.model = new envModel(tileManager, config.getSpeed());
        if ((args.length == 1) && args[0].equals("gui")) {
            final envView view = new envView(this.model, tileManager);
            this.model.setView(view);
        }
        groundAgents = new HashMap<>();
        airAgents = new HashMap<>();
        model.mapInitialize();

        model.generateObs(config.getNumObstacles());
        if (config.getTarget()) {
            model.addTarget();
        } else {
            System.out.println("No target initialized.");
        }
    }

    //Execute the requested action. Calls updatePercepts if needed.
    @Override
    public boolean executeAction(final String ag, final Structure action) {
        boolean res = false;
        int agentId = model.agents.indexOf(ag);
        int modelId = -1;
        MyAgent agent = null;
        if (groundAgents.containsKey(ag)) {
            modelId = envModel.GROUND;
            agent = groundAgents.get(ag);
        } else if (airAgents.containsKey(ag)) {
            modelId = envModel.AIR;
            agent = airAgents.get(ag);
        }
        assert agent != null;

        if (action.getFunctor().equals(envController.m.getFunctor())) {
            res = model.move(agentId, action.getTerm(0), agent, modelId);
        }
        if (action.getFunctor().equals(envController.h.getFunctor())) {
            res = this.model.createAgent(ag, action.getTerm(0), modelId);
        }
        if (action.getFunctor().equals(envController.s.getFunctor())) {
            res = this.model.search(action.getTerm(0));
            if (model.hidings.size() == model.visitedHidings.size() && checkExpTime) {
                checkExpTime = false;
                long partialTime = System.currentTimeMillis();
                expTime = partialTime - startTime;
            }
        }
        if (action.getFunctor().equals(envController.r.getFunctor())) {
            res = this.model.remove_obs(action.getTerm(0));
        }
        if (action.getFunctor().equals(envController.c.getFunctor())) {
            nReturnAgents++;
            checkReturnedAgents();
        }

        if (res) updatePercepts(ag, action, agent);
        return true;
    }

    //Shows the menu for gathering the initial configuration.
    public void showStartupDialog(StartupConfig config) {
        JFrame frame = new JFrame("Simulation Configuration");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(350, 200);
        frame.setLayout(new GridLayout(7, 2));
        //Define the text field for the number of removable obstacles.
        JLabel obsLabel = new JLabel("Number of Obstacles:");
        JTextField obsField = new JTextField("15");
        //Define the combo box for the simulation speed.
        JLabel speedLabel = new JLabel("Simulation Speed:");
        String[] speeds = {"Normal", "Fast"};
        JComboBox<String> speedBox = new JComboBox<>(speeds);
        //Define the combo box for selecting the operation mode.
        JLabel objectiveLabel = new JLabel("Simulation Goal:");
        String[] objectives = {"Explore All POIs", "Search Target"};
        JComboBox<String> objectiveBox = new JComboBox<>(objectives);
        //Define the combo box for selecting the map.
        JLabel mapLabel = new JLabel("Select Map:");
        String[] maps = {"map2"};
        JComboBox<String> mapBox = new JComboBox<>(maps);
        //Define the button and retrieving the info after pressing it.
        JButton startButton = new JButton("Start");
        startButton.addActionListener(e -> {
            config.setNumObstacles(Integer.parseInt(obsField.getText()));
            config.setMapName((String) mapBox.getSelectedItem());
            config.setTarget((String) Objects.requireNonNull(objectiveBox.getSelectedItem()));
            config.setSpeed((String) speedBox.getSelectedItem());
            config.configReady = true;
            frame.dispose();
            synchronized (config) {
                config.notify();  // Wake up the init thread
            }
        });
        //Add everything to the frame.
        frame.add(obsLabel);
        frame.add(obsField);
        frame.add(speedLabel);
        frame.add(speedBox);
        frame.add(objectiveLabel);
        frame.add(objectiveBox);
        frame.add(mapLabel);
        frame.add(mapBox);
        frame.add(new JLabel());  // filler
        frame.add(startButton);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    //Update agents percepts. The update depends on the action they are performing.
    public void updatePercepts(String agent, final Structure action, MyAgent agentObj) {
        int agentId = model.agents.indexOf(agent);
        Location agentLoc = model.getAgPos(agentId);

        if (action.getFunctor().equals(envController.m.getFunctor()) ||
                action.getFunctor().equals(envController.h.getFunctor())) {
            int[][] directions = agentObj.getSensingArea();
            //If an agent removed an obstacle, other agents should not perceive it. Solved by
            //pre-entive removing them in any directions and re-add them if they are still there.
            assert directions != null;
            for (int[] direction : directions) {
                int newX = agentLoc.x + direction[0];
                int newY = agentLoc.y + direction[1];
                Literal new_percept = Literal.parseLiteral("rem_obstacle(" + newX + "," +
                        newY + ")");
                removePercept(agent, new_percept);
            }
            //Same idea for the agents.
            Literal lit = Literal.parseLiteral("ground_agent(_,_,_)");
            removePerceptsByUnif(agent, lit);
            lit = Literal.parseLiteral("air_agent(_,_,_)");
            removePerceptsByUnif(agent, lit);

            //Retrieving and updating map percepts.
            Map<String, List<Location>> surr = model.getSurroundingPercepts(agentId, agentObj);
            for (Map.Entry<String, List<Location>> l : surr.entrySet()) {
                for (Location loc : l.getValue()) {
                    Literal new_percept = Literal.parseLiteral(l.getKey() + "(" + loc.x + "," + loc.y + ")");
                    addPercept(agent, new_percept);
                }
            }
            //Get surrounding agents.
            Map<String, Location> surrAgs = model.getSurroundingAgents(agentId, agentObj);
            for (Map.Entry<String, Location> e : surrAgs.entrySet()) {
                Location loc = e.getValue();
                String name = e.getKey();
                Literal new_percept = null;
                if (groundAgents.containsKey(name)) {
                    new_percept = Literal.parseLiteral("ground_agent" + "(" + name + "," + loc.x + "," + loc.y + ")");
                } else if (airAgents.containsKey(name)) {
                    new_percept = Literal.parseLiteral("air_agent" + "(" + name + "," + loc.x + "," + loc.y + ")");
                }
                addPercept(agent, new_percept);
            }
        }
        if (action.getFunctor().equals(envController.h.getFunctor())) {
            Literal new_percept = Literal.parseLiteral("my_priority(" + agentId + ")");
            addPercept(agent, new_percept);
        }
        if (action.getFunctor().equals(envController.s.getFunctor())) {
            Literal new_percept = Literal.parseLiteral("target(found)");
            addPercept(agent, new_percept);
        }
    }

    public static void registerGroundAgent(String name, GroundAgent agent) {
        System.out.println("Registering agent " + name);
        groundAgents.put(name, agent);
    }

    public static void registerAirAgent(String name, AirAgent agent) {
        System.out.println("Registering agent " + name);
        airAgents.put(name, agent);
    }

    public void checkReturnedAgents() {
        int n = model.agents.size();
        if (nReturnAgents == n) {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            long seconds = duration / 1000;
            long minutes = seconds / 60;
            long expSeconds = expTime / 1000;
            long expMinutes = expSeconds / 60;
            expSeconds = expSeconds % 60;
            seconds = seconds % 60;
            System.out.println("----------------------------------");
            System.out.println("RESULTS:");
            System.out.printf("Task completed in: %02d:%02d\n", minutes, seconds);
            System.out.println("Number of redundant searches: " + model.nUselessSearches);
            System.out.printf("All POIs explored in: %02d:%02d\n", expMinutes, expSeconds);
            System.out.println("----------------------------------");
        }
    }
}