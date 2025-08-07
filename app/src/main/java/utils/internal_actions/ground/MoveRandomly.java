package utils.internal_actions.ground;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.*;
import jason.environment.grid.Location;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import static utils.TermUtils.extractLocationListTerm;

//Moves randomly to an unvisited nearby (distance of 1) cell, in 1 of 4 directions (L,U,R,D).
//If no valid cells returns false.
public class MoveRandomly extends DefaultInternalAction {
    private final Random generator = new Random();
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) {
        ListTerm streetsListTerm = (ListTerm) args[0]; //All the known street cells.
        ListTerm visitedListTerm = (ListTerm) args[1]; //All the already visited cells.
        Structure currentPosTerm = (Structure) args[2]; //Current position.
        Term resultTerm = args[3]; //Result term.

        //Valid directions for Ground Agents.
        int[][] directions = {
                {-1,  0}, // L
                { 0, -1}, // U
                { 1,  0}, // R
                { 0,  1}, // D
        };

        //Extracting inputs.
        Location currentPos = new Location(Integer.parseInt(currentPosTerm.getTerm(0).toString()),
                Integer.parseInt(currentPosTerm.getTerm(1).toString()));
        List<Location> visitedList = extractLocationListTerm(visitedListTerm);
        List<Location> streetsList = extractLocationListTerm(streetsListTerm);
        //Computing all the valid moves.
        List<Location> possibleMoves = new ArrayList<>();
        for (int[] direction : directions) {
            int newX = currentPos.x + direction[0];
            int newY = currentPos.y + direction[1];
            Location possMove = new Location(newX, newY);
            if(streetsList.contains(possMove)&& !visitedList.contains(possMove)) {
                possibleMoves.add(possMove);
            }
        }
        //Selecting a random valid move and returning it.
        if (!possibleMoves.isEmpty()) {
            Location finalMove = possibleMoves.get(generator.nextInt(possibleMoves.size()));
            Structure s = new Structure("p",2);
            s.addTerm(new NumberTermImpl(finalMove.x));
            s.addTerm(new NumberTermImpl(finalMove.y));
            return un.unifies(resultTerm, s);
        }else{
            return false;
        }
    }
}
