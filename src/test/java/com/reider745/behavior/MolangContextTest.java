package com.reider745.behavior;

import com.reider745.behavior.molang.MolangContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MolangContextTest {
    @Test void evaluatesRealMolangWithMathAliasesAndQueries() {
        MolangContext context = new MolangContext((name, args) -> switch (name) {
            case "health" -> 7;
            case "is_alive" -> true;
            case "is_name" -> args.get(0).getAsString().equals("test:mob");
            default -> 0;
        });
        assertEquals(1, context.evaluate("math.sin(90)"));
        assertEquals(7, context.evaluate("query.health"));
        assertEquals(7, context.evaluate("q.health()"));
        assertEquals(1, context.evaluate("q.is_alive && query.is_name('test:mob')"));
        assertEquals(0, context.evaluate("q.is_alive ? 0 : 1"));
        assertEquals(9, context.evaluate("t.a = 3; return t.a * t.a;"));
        assertEquals(0, context.evaluate("t.a"));
    }

    @Test void variablesArePersistentPerActorAndSurviveSaving() {
        MolangContext first = new MolangContext((name, args) -> 0);
        MolangContext second = new MolangContext((name, args) -> 0);
        first.evaluate("v.count = 4;");
        assertEquals(4, first.evaluate("variable.count"));
        assertEquals(0, second.evaluate("v.count"));
        second.restore(first.save());
        assertEquals(4, second.evaluate("v.count"));
        second.evaluate("v.count = v.count + 1;");
        assertEquals(4, first.evaluate("v.count"));
        assertEquals(5, second.evaluate("v.count"));
    }

    @Test void usesOpenSourceLoopAndStatementSemanticsWithExecutionBudget() {
        MolangContext context = new MolangContext((name, args) -> 0);
        assertEquals(6, context.evaluate("v.n = 0; loop(3, { v.n = v.n + 2; }); return v.n;"));
        assertEquals(3, context.evaluate("v.n = 0; loop(10, {v.n = v.n + 1; v.n >= 3 ? break;}); return v.n;"));
        assertThrows(IllegalArgumentException.class, () -> context.evaluate("loop(4096, {loop(4096, {v.n = 1;});});"));
        assertEquals(2, context.evaluate("1 + 1"));
        assertThrows(IllegalArgumentException.class, () -> context.evaluate("v.foo = ("));
    }
}
