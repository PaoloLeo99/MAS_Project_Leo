package utils;

import jason.asSyntax.ListTerm;
import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.environment.grid.Location;
import java.util.ArrayList;
import java.util.List;

public class TermUtils {

    //Extract a List of Location from a List of Term
    public static List<Location> extractLocationListTerm(ListTerm listTerm) {
        List<Location> list = new ArrayList<>();
        for (Term t : listTerm) {
            Structure s = (Structure) t;
            list.add(new Location(Integer.parseInt(s.getTerm(0).toString()),
                    Integer.parseInt(s.getTerm(1).toString())));
        }
        return list;
    }

    //Extract a List of Strings from a List of Term
    public static List<String> extractStringListTerm(ListTerm listTerm) {
        List<String> list = new ArrayList<>();
        for (Term t : listTerm) {
            list.add(t.toString());
        }
        return list;
    }

    //Extract a List of Literal from a List of Term
    public static List<Literal> extractLiteralListTerm(ListTerm listTerm) {
        List<Literal> list = new ArrayList<>();
        for (Term t : listTerm) {
            Literal l = Literal.parseLiteral(t.toString());
            list.add(l.clearAnnots());
        }
        return list;
    }
}
