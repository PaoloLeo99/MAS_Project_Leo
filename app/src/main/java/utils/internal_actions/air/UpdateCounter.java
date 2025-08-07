package utils.internal_actions.air;


import ag.MyAgent;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.*;
import jason.bb.BeliefBase;
import jason.environment.grid.Location;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import static utils.TermUtils.extractLocationListTerm;

//Returns the value to add to the exploration counter. The value depends on the new information
//acquired since the last update.
public class UpdateCounter extends DefaultInternalAction {

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) {
        ListTerm processedListTerm = (ListTerm) args[0]; //List of already processed Term.
        Term resultTerm =  args[1]; //Result term.

        //Extracting the input.
        List<Location> processedList = extractLocationListTerm(processedListTerm);
        //Extracting the Belief Base.
        MyAgent ag = (MyAgent)ts.getAg();
        BeliefBase bb = ag.getBB();
        //Iterate through the BB and update the counter for each important belief not already processed
        Iterator<Literal> it =bb.iterator();
        List<Location> toAdd = new ArrayList<>();
        int counter = 0;
        while(it.hasNext()){
            Literal lit = it.next();
            Location loc;
            String functor = lit.getFunctor();

            if(functor.equals("grass")||functor.equals("fixed_obstacle")||functor.equals("edge")
                    ||functor.equals("hiding")||functor.equals("energy_station")||functor.equals("success")){
                loc = new Location(Integer.parseInt(lit.getTerm(0).toString()),
                        Integer.parseInt(lit.getTerm(1).toString()));
                if(processedList.contains(loc)){
                    continue;
                }
                //Each belief type affects the counter differently.
                switch (functor) {
                    case "grass", "fixed_obstacle", "edge" -> counter++;
                    case "hiding" -> counter += 10;
                    case "success" -> counter += 15;
                    case "energy_station" -> counter += 20;
                    default -> {
                        continue;
                    }
                }
                toAdd.add(loc);
            }
        }

        for (Location loc : toAdd) {
            Literal newLit = Literal.parseLiteral("processed(" + loc.x + "," +
                    loc.y + ")");
            bb.add(newLit);
        }
        return un.unifies(new NumberTermImpl(counter), resultTerm);
    }
}

