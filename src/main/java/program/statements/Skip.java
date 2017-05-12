package program.statements;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import program.Configuration.ConfigType;
import program.State;
import utils.Tree;
import viewmodel.ASTNode;
import viewmodel.interfaces.INodeVisitor;

@EqualsAndHashCode
@ToString
public class Skip implements IStatement {

    @Override
    public StatementConfiguration step(State state) {
        return new StatementConfiguration(this, state, ConfigType.TERMINATED);
    }

    @Override
    public StatementConfiguration next(State state) {
        return step(state);
    }

    @Override
    public Tree.Node<ASTNode> accept(INodeVisitor<Tree.Node<ASTNode>> visitor) {
        return visitor.visit(this);
    }

}
