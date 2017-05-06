package ast.expression.operations;

import ast.State;
import ast.expression.interfaces.BadExpression;
import ast.expression.interfaces.Expression;
import ast.expression.interfaces.Value;
import ast.expression.operations.bad_operations.BadUnOp;
import ast.expression.values.BoolValue;
import ast.expression.values.IntValue;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import utils.Tree;
import viewmodel.ASTNode;
import viewmodel.interfaces.IASTVisitor;

import java.util.function.Function;

@RequiredArgsConstructor
@EqualsAndHashCode
public class UnOp<T, R> implements Expression {

    public static <T, R> UnOp<T, R> of(String operator,
                                        Expression operand,
                                        Class<? extends Value<T>> operandClass,
                                        Function<R, Value<R>> resultCtor,
                                        Function<T, R> evalFun) {

        return operand == null ? new BadUnOp<>(operator, operand) : new UnOp<>(operator,
                operand,
                operandClass,
                resultCtor,
                evalFun);
    }

    public static UnOp<Integer, Integer> intOp(String operator, Expression operand, Function<Integer, Integer> evalFun) {
        return UnOp.of(operator, operand, IntValue.class, IntValue::new, evalFun);
    }

    public static UnOp<Integer, Integer> neg(Expression operand) {
        return UnOp.intOp("-", operand, i -> 0 - i);
    }

    public static UnOp<Boolean, Boolean> boolOp(String operator, Expression operand, Function<Boolean, Boolean> evalFun) {
        return UnOp.of(operator, operand, BoolValue.class, BoolValue::new, evalFun);
    }

    public static UnOp<Boolean, Boolean> not(Expression operand) {
        return UnOp.boolOp("not", operand, b -> !b);
    }

    @Getter
    private final String operator;
    @Getter
    private final Expression operand;

    private final Class<? extends Value<T>> operandClass;
    private final Function<R, Value<R>> resultCtor;
    private final Function<T, R> evalFun;

    @Override
    public Expression step(State state) {

        if (operand instanceof BadExpression) {
            return new BadUnOp<>(operator, operand);
        }

        if (!(operand instanceof Value)) {
            return new UnOp<>(operator, operand.step(state), operandClass, resultCtor, evalFun);
        }

        Value val = (Value) operand;

        if ((operandClass.isAssignableFrom(val.getClass()))) {
            return resultCtor.apply(evalFun.apply(operandClass.cast(val).getValue()));
        }

        return new BadUnOp(operator, operand);
    }


    @Override
    public Tree.Node<ASTNode> accept(IASTVisitor<Tree.Node<ASTNode>> visitor) {
        return visitor.visit(this);
    }

}
