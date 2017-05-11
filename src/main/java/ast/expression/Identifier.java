package ast.expression;


import ast.ExprConfig;
import ast.State;
import ast.expression.interfaces.Expression;
import ast.expression.interfaces.Value;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import utils.Tree;
import viewmodel.ASTNode;
import viewmodel.interfaces.IASTElement;
import viewmodel.interfaces.IASTVisitor;

@RequiredArgsConstructor
@EqualsAndHashCode
public class Identifier implements Expression, IASTElement<Tree.Node<ASTNode>> {

    @Getter
    private final String identifier;

    @Override
    public ExprConfig step(State state) {
        Value<?> value = state.get(this);
        return  ExprConfig.of(value == null ? new StuckIdentifier(identifier) : value, state);
    }

    @Override
    public Tree.Node<ASTNode> accept(IASTVisitor<Tree.Node<ASTNode>> visitor) {
        return visitor.visit(this);
    }

}
