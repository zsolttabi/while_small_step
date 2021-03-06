package program;

import org.junit.Assert;
import org.junit.Test;
import program.expressions.Identifier;
import program.expressions.Value;
import program.statements.*;

import java.math.BigInteger;

import static org.hamcrest.CoreMatchers.*;
import static program.Configuration.ConfigType.*;
import static program.expressions.BinOp.Arithmetic.ADD;
import static program.expressions.BinOp.Logical.AND;

public class StatementTests {

    @Test
    public void When_SkipStmSteps_Then_ConfigIsTerminated() {

        IProgramElement underTest = new Skip();
        State state = new State();

        Configuration result = underTest.step(state);

        Assert.assertEquals(new Configuration(new Skip(), new State(), TERMINATED), result);
    }

    @Test
    public void Given_IdWithoutValue_When_AssignStmSteps_Then_ConfigIsTerminated_And_IdHasValue() {

        Identifier identifier = new Identifier("x");
        Value<?> value = new Value<>(5);

        State state = new State();
        State expectedState = new State();
        expectedState.set(identifier, value);
        IProgramElement underTest = new Assignment(identifier, value);

        Configuration result = underTest.step(state);

        Assert.assertEquals(new Configuration(underTest, expectedState, TERMINATED), result);
    }


    @Test
    public void Given_IdWithOldValue_When_AssignStmStepsWithNewValue_Then_ConfigIsTerminated_And_IdHasNewValue() {

        Identifier identifier = new Identifier("x");
        Value<?> value = new Value<>(new BigInteger("5"));

        State state = new State();
        state.set(identifier, new Value<>(new BigInteger("1")));
        State expectedState = new State();
        expectedState.set(identifier, value);

        IProgramElement underTest = new Assignment(identifier, value);

        Configuration result = underTest.step(state);

        Assert.assertEquals(new Configuration(underTest, expectedState, TERMINATED), result);
    }

    @Test
    public void Given_UninitializedVar_When_VarIsReadByAssignStm_Then_ConfigIsStuck() {

        IProgramElement x = new Identifier("x");
        IProgramElement y = new Identifier("y");
        IProgramElement underTest = new Assignment(x, y);

        Configuration result = underTest.step(new State());

        Assert.assertEquals(new Configuration(new Assignment(x, y.step(new State()).getElement()),
                new State(),
                STUCK), result);
    }

    @Test
    public void Given_StuckExpr_When_ExprIsReadByAssignStm_Then_ConfigIsStuck() {

        IProgramElement x = new Identifier("x");
        IProgramElement stuckVal = ADD.of(new Value<>(true), new Value<>(new BigInteger("1"))).step(new State()).getElement();
        IProgramElement underTest = new Assignment(x, stuckVal);

        Configuration result = underTest.step(new State());

        Assert.assertEquals(new Configuration(new Assignment(x, stuckVal), new State(), STUCK), result);
    }


    @Test
    public void Given_SeqStmWithSkipAsS1_When_SeqStmSteps_Then_ConfigIsIntermediateWithS2() {

        IProgramElement s1 = new Skip();
        IProgramElement s2 = new If(null, null, null);
        State state = new State();
        IProgramElement underTest = new Sequence(s1, s2);

        Configuration result = underTest.step(state);

        Assert.assertEquals(new Configuration(s2, new State(), INTERMEDIATE), result);
    }

    @Test
    public void Given_S1IsStuck_When_SeqStmSteps_Then_ConfigIsStuckWithSeqStm() {

        IProgramElement stuckS1 = new Assignment(new Identifier("x"), new Identifier("y")).step(new State()).getElement();
        IProgramElement s2 = new Skip();
        State state = new State();
        IProgramElement underTest = new Sequence(stuckS1, s2);

        Configuration result = underTest.step(state);

        Assert.assertEquals(new Configuration(new Sequence(stuckS1, s2), new State(), STUCK), result);
    }


    @Test
    public void Given_FalseCond_When_IfSamSteps_Then_ConfigIsIntermediateWithS1() {

        IProgramElement s1 = new Assignment(new Identifier("x"), new Value<>(new BigInteger("1")));
        IProgramElement s2 = new Assignment(new Identifier("y"), new Value<>(new BigInteger("2")));
        State state = new State();
        IProgramElement underTest = new If(new Value<>(true), s1, s2);

        Configuration result = underTest.step(state);

        Assert.assertEquals(new Configuration(s1, new State(), INTERMEDIATE), result);
    }

    @Test
    public void Given_FalseCond_When_IfSamSteps_Then_ConfigIsIntermediateWithS2() {

        IProgramElement s1 = new Assignment(new Identifier("x"), new Value<>(new BigInteger("1")));
        IProgramElement s2 = new Assignment(new Identifier("y"), new Value<>(new BigInteger("2")));
        State state = new State();
        IProgramElement underTest = new If(new Value<>(false), s1, s2);

        Configuration result = underTest.step(state);

        Assert.assertEquals(new Configuration(s2, new State(), INTERMEDIATE), result);
    }

    @Test
    public void Given_StuckIfStmWithExpr_When_StmSteps_Then_ConfigIsStuck() {

        IProgramElement stuckExpr = AND.of(new Value<>(new BigInteger("1")), new Value<>(true)).step(new State()).getElement();
        State state = new State();
        IProgramElement s1 = new Assignment(new Identifier("x"), new Value<>(new BigInteger("1")));
        IProgramElement s2 = new Assignment(new Identifier("y"), new Value<>(new BigInteger("2")));
        IProgramElement underTest = new If(stuckExpr, s1, s2);

        Configuration result = underTest.step(state);

        Assert.assertEquals(new Configuration(new If(stuckExpr, s1, s2), new State(), STUCK), result);
    }

    @Test
    public void Given_IfStmWithWrongTypeExpr_When_StmSteps_Then_ConfigIsStuck() {

        IProgramElement intExpr = new Value<>(4);
        State state = new State();
        IProgramElement s1 = new Assignment(new Identifier("x"), new Value<>(new BigInteger("1")));
        IProgramElement s2 = new Assignment(new Identifier("y"), new Value<>(new BigInteger("2")));
        IProgramElement underTest = new If(intExpr, s1, s2);

        Configuration result = underTest.step(state);

        Assert.assertEquals(new Configuration(new If(intExpr, s1, s2), new State(), STUCK), result);
    }


    @Test
    public void Given_WhileStm_When_StmSteps_Then_ConfigWithIfStmIsCreated() {

        IProgramElement cond = new Value<>(true);
        IProgramElement s = new Assignment(new Identifier("x"), new Value<>(new BigInteger("1")));
        IProgramElement underTest = new While(cond, s);

        Configuration result = underTest.step(new State());

        Assert.assertEquals(result, new Configuration(new If(cond, new Sequence(s, underTest), new Skip()), new State(), INTERMEDIATE));
    }

    @Test
    public void Given_AbortStm_When_StmSteps_Then_ConfigIsStuck() {

        IProgramElement underTest = new Abort();

        Configuration result = underTest.step(new State());

        Assert.assertEquals(result, new Configuration(new Abort(), new State(), STUCK));
    }


    @Test
    public void Given_SomeS1S2_When_OrStmSteps_Then_EitherS1OrS2Steps() {

        IProgramElement s1 = new If(new Value<>(true), new Skip(), new Skip());
        IProgramElement s2 = new While(new Value<>(false), new Skip());
        IProgramElement underTest = new Or(s1, s2);

        State state = new State();
        Configuration result = underTest.step(state);

        Assert.assertThat(result,
                either(equalTo(s1.step(state)))
                        .or(equalTo(s2.step(state))));
    }

    @Test
    public void Given_IntermediateS1_And_IntermediateS2_When_ParStmSteps_Then_ConfigIsIntermediateWithParStm() {

        IProgramElement s1 = new If(new Value<>(true), new Skip(), new Skip());
        IProgramElement s2 = new While(new Value<>(false), new Skip());
        IProgramElement underTest = new Par(s1, s2);

        Configuration result = underTest.step(new State());
        Assert.assertThat(result.getElement(), instanceOf(Par.class));
        Assert.assertThat(result.getConfigType(), equalTo(INTERMEDIATE));
        Assert.assertThat((Par) result.getElement(),
                either(equalTo(new Par(s1.step(new State()).getElement(), s2)))
                        .or(equalTo(new Par(s1, s2.step(new State()).getElement()))));
    }

    @Test
    public void Given_TerminatedS1_And_TerminatedS2_When_ParSteps_Then_ConfigIsIntermediateWithEitherS1OrS2() {

        IProgramElement s1 = new Skip();
        IProgramElement s2 = new Skip();
        IProgramElement underTest = new Par(s1, s2);

        Configuration result = underTest.step(new State());

        Assert.assertThat(result.getElement(), instanceOf(Skip.class));
        Assert.assertThat(result.getConfigType(), equalTo(INTERMEDIATE));
        Assert.assertThat(result.getElement(), equalTo(new Skip()));

    }

    @Test
    public void Given_StuckS1_And_StuckS2_When_ParSteps_Then_ConfigIsIntermediateWithPar() {

        IProgramElement s1 = new Abort();
        IProgramElement s2 = new Abort();
        IProgramElement underTest = new Par(s1, s2);

        Configuration result = underTest.step(new State());

        Assert.assertThat(result.getElement(), instanceOf(Par.class));
        Assert.assertThat(result.getConfigType(), equalTo(INTERMEDIATE));
        Assert.assertThat(result.getElement(), equalTo(underTest));
    }

    @Test
    public void Given_StuckS1_And_StuckS2_When_ParStepsTwice_Then_ConfigIsStuckWithPar() {

        IProgramElement s1 = new Abort();
        IProgramElement s2 = new Abort();
        IProgramElement underTest = new Par(s1, s2);

        Configuration result = underTest.step(new State()).getElement().step(new State());

        Assert.assertThat(result.getElement(), instanceOf(Par.class));
        Assert.assertThat(result.getConfigType(), equalTo(STUCK));
        Assert.assertThat(result.getElement(), equalTo(underTest));
    }

}