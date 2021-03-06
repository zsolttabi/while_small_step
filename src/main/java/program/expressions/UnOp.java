package program.expressions;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import program.Configuration;
import program.IProgramElement;
import program.State;
import viewmodel.interfaces.INodeVisitor;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;

import static program.Configuration.ConfigType.*;

@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public class UnOp<T, R> implements IProgramElement {

    @Getter
    private final String operator;
    @Getter
    private final IProgramElement operand;
    private final Class<T> operandClass;
    private final Function<T, R> operatorFunction;

    public static <T, R> UnOp<T, R> of(String operator, IProgramElement operand, Class<T> operandClass, Function<T, R> operatorFunction) {
        return new UnOp<>(operator, operand, operandClass, operatorFunction);
    }

    public static UnOp<BigInteger, BigInteger> arithmetic(String operator, IProgramElement operand, Function<BigInteger, BigInteger> operatorFunction) {
        return UnOp.of(operator, operand, BigInteger.class, operatorFunction);
    }

    public static UnOp<Boolean, Boolean> logical(String operator, IProgramElement operand, Function<Boolean, Boolean> operatorFunction) {
        return UnOp.of(operator, operand, Boolean.class, operatorFunction);
    }

    @Override
    public Configuration step(State state) {

        if (!(operand instanceof Value)) {
            Configuration operandConf = operand.step(state);
            return new Configuration(new UnOp<>(operator,
                    operandConf.getElement(),
                    operandClass,
                    operatorFunction), operandConf.getState(), operandConf.getConfigType() == STUCK ? STUCK : INTERMEDIATE);
        }

        Object operandValue = ((Value) operand).getValue();
        if (!operandValue.getClass().equals(operandClass)) {
            return new Configuration(this, state, STUCK);
        }

        return new Configuration(new Value<>(operatorFunction.apply(operandClass.cast(operandValue))),
                state,
                TERMINATED);
    }

    @Override
    public Set<Configuration> peek(State state) {
        if (!(operand instanceof Value)) {
            return operand.peek(state);
        }
        return Collections.singleton(new Configuration(this, state, INTERMEDIATE));
    }

    @Override
    public IProgramElement copy() {
        return new UnOp<>(operator, operand.copy(), operandClass, operatorFunction);
    }

    @Override
    public <V> V accept(INodeVisitor<V> visitor) {
        return visitor.visit(this);
    }

    public enum Arithmetic {

        NEG(o -> UnOp.arithmetic("-", o, BigInteger::negate));

        private final Function<IProgramElement, UnOp<BigInteger, BigInteger>> operationProvider;

        Arithmetic(Function<IProgramElement, UnOp<BigInteger, BigInteger>> operationProvider) {
            this.operationProvider = operationProvider;
        }

        public UnOp<BigInteger, BigInteger> of(IProgramElement operand) {
            return operationProvider.apply(operand);
        }
    }

    public enum Logical {

        NOT(o -> UnOp.logical("!", o, a -> !a));

        private final Function<IProgramElement, UnOp<Boolean, Boolean>> operationProvider;

        Logical(Function<IProgramElement, UnOp<Boolean, Boolean>> operationProvider) {
            this.operationProvider = operationProvider;
        }

        public UnOp<Boolean, Boolean> of(IProgramElement operand) {
            return operationProvider.apply(operand);
        }
    }

}
