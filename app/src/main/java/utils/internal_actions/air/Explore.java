package utils.internal_actions.air;
import java.util.*;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.*;
import jason.environment.grid.Location;
import static utils.TermUtils.extractLocationListTerm;

//Returns the next cell to move chosen using a greedy strategy.
public class Explore extends DefaultInternalAction {
    private final Random generator = new Random();

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) {

        ListTerm airListTerm = (ListTerm) args[0]; //List of walkable cells.
        ListTerm visitedListTerm = (ListTerm) args[1]; //List of visited cells.
        Structure currentPosTerm = (Structure) args[2]; //Current position.
        Term resultTerm = args[3]; //Result term.

        //Extracting all the inputs
        Location currentPos = new Location(Integer.parseInt(currentPosTerm.getTerm(0).toString()),
                Integer.parseInt(currentPosTerm.getTerm(1).toString()));
        List<Location> airList = extractLocationListTerm(airListTerm);
        List<Location> visitedList = extractLocationListTerm(visitedListTerm);

        //For each direction (8-way), compute the resulting position (starting from the currentPos).
        //If it is a walkable cell, compute how many adjacent non-visited cells it has (n).
        //If more than 0, put the Location in a map (the key is n).
        List<Location> directions = getDirections();
        Map<Integer, List<Location>> numNonVisitedCells = new HashMap<>();
        for (Location loc : directions) {
            Location canMove = new Location(loc.x + currentPos.x, loc.y + currentPos.y);
            if(airList.contains(canMove)){
                int i = 0;
                for (Location loc2 : directions){
                    Location newPos = new Location(loc2.x + canMove.x, loc2.y + canMove.y);
                    if(!visitedList.contains(newPos)&&airList.contains(newPos)){
                        i+=1;
                    }
                }
                if(i!=0)
                    numNonVisitedCells.computeIfAbsent(i, k -> new ArrayList<>()).add(canMove);
            }
        }

        if (numNonVisitedCells.isEmpty()) {
            return false;
        }else{
            //Extract the list which has the maximum value as key.
            int max = Collections.max(numNonVisitedCells.keySet());
            List<Location> locations = numNonVisitedCells.get(max);
            //Select and return a random location in the list.
            Location l = locations.get(generator.nextInt(locations.size()));
            Structure s = new Structure("p",2);
            s.addTerm(new NumberTermImpl(l.x));
            s.addTerm(new NumberTermImpl(l.y));
            return un.unifies(resultTerm, s);
        }
    }

    public List<Location> getDirections(){
        return Arrays.asList(new Location[]{new Location(1,1),
                new Location(1,-1),new Location(-1,1),new Location(-1,-1),
                new Location(0,1),new Location(0,-1),new Location(1,0),
                new Location(-1,0)});
    }

}

