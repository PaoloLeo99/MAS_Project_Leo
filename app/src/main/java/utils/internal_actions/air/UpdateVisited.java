package utils.internal_actions.air;

import ag.MyAgent;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.*;
import jason.bb.BeliefBase;
import jason.environment.grid.Location;
import java.util.ArrayList;
import java.util.List;

//Mark the current position of the drone and the 8 cells around as visited.
public class UpdateVisited extends DefaultInternalAction {

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) {
        Structure currentPosTerm = (Structure) args[0]; //Drone current pos

        Location currentPos = new Location(Integer.parseInt(currentPosTerm.getTerm(0).toString()),
                Integer.parseInt(currentPosTerm.getTerm(1).toString()));

        //Accessing the Belief Base
        MyAgent ag = (MyAgent)ts.getAg();
        BeliefBase bb = ag.getBB();
        int width = ag.getSensingWidth();

        //Adding visited(X,Y) for each position in the 3x3 box centered in currentPos
        for (int i=-width+1; i<width; i++){
            for (int j=-width+1; j<width; j++){
                int newX = i+currentPos.x;
                int newY = j+currentPos.y;

                Literal lit = Literal.parseLiteral("visited(" + newX + "," +
                        newY + ")");
                bb.add(lit);
            }
        }
        return true;
    }
}
