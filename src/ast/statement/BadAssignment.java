package ast.statement;

import ast.State;
import ast.expression.interfaces.Expression;
import utils.Pair;

public class BadAssignment extends Assignment implements BadStatement {

    public BadAssignment(Expression identifier, Expression value) {
        super(identifier, value);
    }

    @Override
    public Pair<Statement, State> step(State state) {
        return stuckStep(state);
    }

}
