package program.statements;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import program.Configuration;
import program.IProgramElement;
import program.PException;
import program.State;
import viewmodel.interfaces.INodeVisitor;

import java.util.Set;

import static program.Configuration.ConfigType.INTERMEDIATE;
import static program.Configuration.ConfigType.TERMINATED;

@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public class TryCatch implements IProgramElement {

    @Getter
    private final IProgramElement s1;
    @Getter
    private final IProgramElement s2;
    @Getter
    private final PException e;

    @Override
    public <V> V accept(INodeVisitor<V> visitor) {
        return visitor.visit(this);
    }

    @Override
    public Configuration step(State state) {

        Configuration s1Conf = s1.step(state);
        if (s1Conf.getConfigType() == TERMINATED) {
            return s1Conf;
        }

        if (s1Conf.getElement() instanceof PException) {
            if (s1Conf.getElement().equals(e)) {
                return new Configuration(s2, s1Conf.getState(), INTERMEDIATE);
            }
            return s1Conf;
        }

        return new Configuration(new TryCatch(s1Conf.getElement(), s2, e), s1Conf.getState(), INTERMEDIATE);
    }

    @Override
    public Set<Configuration> peek(State state) {
        return s1.peek(state);
    }

    @Override
    public IProgramElement copy() {
        return new TryCatch(s1.copy(), s2.copy(), e.copy());
    }
}
