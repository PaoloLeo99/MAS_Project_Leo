package utils.internal_actions.shared;

import ag.MyAgent;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.*;
import jason.environment.grid.Location;
import utils.d_star_lite.MyPathFinder;
import utils.d_star_lite.State;
import java.util.*;
import static utils.TermUtils.extractLocationListTerm;

//Finds the best path from the agent current position to a goal, given the agent's map knowledge.

/// /If there are multiple goals, the algorithm chooses the closest.
/// /Returns the GOAL chosen and the PATH to reach it.
public class PathFinder extends DefaultInternalAction {
    private final int grassCost = 2;
    private final int obsCost = -1;
    private final int remObsCost = 4;

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception{
        Structure currentPosTerm = (Structure) args[0]; //Current position.
        //A list of lists. It contains the lists of the objects with cost != 1. In this case,
        //non-removable obstacles, removable obstacles and grass.
        ListTerm objectsListTerm = (ListTerm) args[1];
        ListTerm goalTerm = (ListTerm) args[2]; //List of goals.
        Structure resultTermGoal = (Structure) args[3];
        ListTerm resultTermPath = (ListTerm) args[4];

        //Extract terms object lists from objectsListTerm.
        ListTerm obstaclesListTerm = (ListTerm) objectsListTerm.getFirst();
        ListTerm remObstaclesListTerm = (ListTerm) objectsListTerm.get(1);
        ListTerm grassListTerm = (ListTerm) objectsListTerm.get(2);
        //Extracting all the terms.
        Location currentPos = new Location(Integer.parseInt(currentPosTerm.getTerm(0).toString()),
                Integer.parseInt(currentPosTerm.getTerm(1).toString()));
        List<Location> goals = extractLocationListTerm(goalTerm);
        List<Location> obstacles = extractLocationListTerm(obstaclesListTerm);
        List<Location> remObstacles = extractLocationListTerm(remObstaclesListTerm);
        List<Location> grass = extractLocationListTerm(grassListTerm);

        if(goals.isEmpty())
            return false;

        //Extract the pathfinder.
        MyAgent ag = (MyAgent) ts.getAg();
        MyPathFinder pathfinder =  ag.getUnstuckPathFinder();

        //Update the pathfinder costs. Each object has a different cost.
        pathfinder.updateStart(currentPos.x, currentPos.y);
        for(Location loc: obstacles){
            pathfinder.updateCell(loc.x, loc.y, obsCost);
        }
        for(Location loc: remObstacles){
            pathfinder.updateCell(loc.x, loc.y, remObsCost);
        }
        for(Location loc: grass){
            pathfinder.updateCell(loc.x, loc.y, grassCost);
        }

        int minLength = Integer.MAX_VALUE;
        Location currentGoal = null;
        List<State> minPath = null;
        //Compute for each goal the best path and choose the closest goal.
        for (Location l:goals){
            //Set the goal.
            pathfinder.updateGoal(l.x, l.y);
            if(pathfinder.replan()){
                //Running D*Lite for the path.
                List<State> path = pathfinder.getPath();
                path.removeFirst();
                //Compute the path len.
                int len = path.size();
                //If a shorter path is found, save it and its goal.
                if(len < minLength){
                    minLength = len;
                    currentGoal = l;
                    List<State> copiedPath = new ArrayList<>();
                    for (State s : path) {
                        copiedPath.add(new State(s));
                    }
                    minPath=copiedPath;
                }
            }else {
                if(obstacles.contains(l)){
                    pathfinder.updateCell(l.x, l.y, obsCost);
                }
            }
        }

        if(currentGoal == null){
            return false;
        }else{
            //Returning the closest goal and path to reach it.
            ListTerm resultList = new ListTermImpl();
            for(State i : minPath) {
                Structure s = new Structure("p", 2);
                s.addTerm(new NumberTermImpl(i.x));
                s.addTerm(new NumberTermImpl(i.y));
                resultList.add(s);
            }
            un.unifies(resultTermPath, resultList);

            Structure s = new Structure("p", 2);
            s.addTerm(new NumberTermImpl(currentGoal.x));
            s.addTerm(new NumberTermImpl(currentGoal.y));
            un.unifies(resultTermGoal, s);
            return true;
        }
    }
}

