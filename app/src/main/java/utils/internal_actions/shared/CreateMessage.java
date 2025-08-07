package utils.internal_actions.shared;

import ag.MyAgent;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.*;
import jason.bb.BeliefBase;
import java.util.Iterator;
import java.util.List;
import static utils.TermUtils.extractLiteralListTerm;
import static utils.TermUtils.extractStringListTerm;

//Returns a ListTerm with all the predicates that the agents wants to send to another agent.
public class CreateMessage extends DefaultInternalAction {

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) {
        ListTerm functorsListTerm = (ListTerm)args[0]; //Functors of the beliefs to send.
        ListTerm alreadySentListTerm = (ListTerm)args[1]; //Already sent beliefs.
        ListTerm returnTerm = (ListTerm)args[2]; //Return term.

        //Extracting inputs.
        List<String> functorList = extractStringListTerm(functorsListTerm);
        List<Literal> alreadySentList = extractLiteralListTerm(alreadySentListTerm);

        //Accessing Belief Base.
        MyAgent ag = (MyAgent)ts.getAg();
        BeliefBase bb = ag.getBB();
        Iterator<Literal> it =bb.iterator();
        ListTerm resultList = new ListTermImpl();
        //Adding to the return list all the beliefs with functor in functorList and not already sent.
        while(it.hasNext()){
            Literal lit = it.next();
            String functor = lit.getFunctor();
            if(functorList.contains(functor) && !alreadySentList.contains(lit.clearAnnots())){
                //add to list
                resultList.add(lit.clearAnnots());
            }
        }
        un.unifies(returnTerm, resultList);

        return true;
    }
}
