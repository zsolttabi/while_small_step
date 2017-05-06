package ast.statement;

import app.SimpleASTNode;
import ast.State;
import ast.statement.bad_statements.BadSequence;
import ast.statement.interfaces.BadStatement;
import ast.statement.interfaces.Statement;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import utils.Element;
import utils.Pair;
import utils.Tree;
import utils.Visitor;

@RequiredArgsConstructor
public class Sequence implements Statement, Element<Tree.Node<SimpleASTNode>> {

    @Getter
    private final Statement s1;
    @Getter
    private final Statement s2;

    public static Sequence of(Statement s1, Statement s2) {
        return s1 == null || s2 == null ? new BadSequence(s1, s2) : new Sequence(s1, s2);
    }

    @Override
    public Pair<Statement, State> step(State state) {

        Pair<Statement, State> s1NewConfig = s1.step(state);

        if (s1NewConfig.getFirst() instanceof BadStatement) {
            return Pair.of(new BadSequence(s1NewConfig.getFirst(), s2), state);
        }

        return s1NewConfig.getFirst() == null ?
                Pair.of(s2, state) : Pair.of(new Sequence(s1NewConfig.getFirst(), s2),
                s1NewConfig.getSecond());
    }

    @Override
    public Tree.Node<SimpleASTNode> accept(Visitor<Tree.Node<SimpleASTNode>> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Sequence sequence = (Sequence) o;

        if (getS1() != null ? !getS1().equals(sequence.getS1()) : sequence.getS1() != null) return false;
        return getS2() != null ? getS2().equals(sequence.getS2()) : sequence.getS2() == null;
    }

    @Override
    public int hashCode() {
        int result = getS1() != null ? getS1().hashCode() : 0;
        result = 31 * result + (getS2() != null ? getS2().hashCode() : 0);
        return result;
    }
}