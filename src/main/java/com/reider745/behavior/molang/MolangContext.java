package com.reider745.behavior.molang;

import org.json.JSONObject;
import team.unnamed.mocha.MochaEngine;
import team.unnamed.mocha.parser.ast.*;
import team.unnamed.mocha.runtime.value.*;

import java.util.*;
import java.util.function.BiFunction;

/** Per-actor Molang state. Parsing and evaluation are provided by MIT-licensed Mocha. */
public final class MolangContext {
    private static final Map<String, List<Expression>> CACHE = Collections.synchronizedMap(
            new LinkedHashMap<>(128, .75f, true) {
                protected boolean removeEldestEntry(Map.Entry<String, List<Expression>> entry) {
                    return size() > 1024;
                }
            });
    private static final ThreadLocal<MochaEngine<?>> PARSER = ThreadLocal.withInitial(MochaEngine::create);
    private final MutableObjectBinding variables = new MutableObjectBinding();
    private final Map<String, Value> savedVariables = new LinkedHashMap<>();
    private final MochaEngine<?> engine;
    private int iterations;

    public MolangContext(BiFunction<String, List<Value>, Object> queries) {
        engine = MochaEngine.createStandard();
        ObjectValue query = name -> ObjectProperty.property(new Function<Object>() {
            private Value value(List<Value> args) { return Value.of(queries.apply(name.toLowerCase(Locale.ROOT), args)); }
            public Value evaluate(team.unnamed.mocha.runtime.ExecutionContext<Object> context, Arguments args) {
                List<Value> values = new ArrayList<>();
                for (int i = 0, size = args.length(); i < size; i++) values.add(args.next().eval());
                return value(values);
            }
            public double getAsNumber() { return value(List.of()).getAsNumber(); }
            public boolean getAsBoolean() { return getAsNumber() != 0; }
        }, false);
        MutableObjectBinding persistent = new MutableObjectBinding() {
            @Override public boolean set(String name, Value value) {
                if (value == null) savedVariables.remove(name.toLowerCase(Locale.ROOT));
                else savedVariables.put(name.toLowerCase(Locale.ROOT), value);
                return variables.set(name, value);
            }
            @Override public ObjectProperty getProperty(String name) { return variables.getProperty(name); }
        };
        engine.scope().set("variable", persistent);
        engine.scope().set("v", persistent);
        engine.scope().set("query", query);
        engine.scope().set("q", query);
        engine.scope().set("__zote_loop", (Function<Object>) (context, args) -> {
            int count = (int) Math.max(0, Math.min(4096, args.next().eval().getAsNumber()));
            Value body = args.next().eval();
            if (body instanceof Function<?> function) {
                @SuppressWarnings("unchecked") Function<Object> block = (Function<Object>) function;
                for (int i = 0; i < count; i++) {
                    if (++iterations > 4096) throw new IllegalArgumentException("Molang loop budget exceeded");
                    block.evaluate(context);
                    Object flag = context.flag();
                    context.flag(null);
                    if (flag == StatementExpression.Op.BREAK) break;
                }
            }
            return Value.nil();
        });
        engine.scope().set("__zote_for_each", (Function<Object>) (context, args) -> {
            Expression expression = args.next().expression();
            Value array = args.next().eval();
            Value body = args.next().eval();
            if (expression instanceof AccessExpression access && array instanceof ArrayValue values
                    && body instanceof Function<?> function && context.eval(access.object()) instanceof ObjectValue object) {
                @SuppressWarnings("unchecked") Function<Object> block = (Function<Object>) function;
                for (Value item : values.values()) {
                    if (++iterations > 4096) throw new IllegalArgumentException("Molang loop budget exceeded");
                    object.set(access.property(), item);
                    block.evaluate(context);
                    Object flag = context.flag();
                    context.flag(null);
                    if (flag == StatementExpression.Op.BREAK) break;
                }
            }
            return Value.nil();
        });
    }

    public double evaluate(Object expression) {
        if (expression == null || expression == JSONObject.NULL) return 0;
        if (expression instanceof Number number) return number.doubleValue();
        if (expression instanceof Boolean bool) return bool ? 1 : 0;
        String source = expression.toString().trim();
        if (source.isEmpty()) return 0;
        List<Expression> program = compile(source);
        iterations = 0;
        double result = engine.eval(program);
        return Double.isFinite(result) ? result : 0;
    }

    public static List<Expression> compile(String source) {
        if (source.length() > 16384) throw new IllegalArgumentException("Molang expression is too long");
        int nesting = 0;
        boolean quoted = false;
        for (int i = 0; i < source.length(); i++) {
            char ch = source.charAt(i);
            if (quoted && ch == '\\') { i++; continue; }
            if (ch == '\'') quoted = !quoted;
            if (!quoted && (ch == '(' || ch == '[' || ch == '{') && ++nesting > 64) {
                throw new IllegalArgumentException("Molang expression nesting exceeds 64");
            }
            if (!quoted && (ch == ')' || ch == ']' || ch == '}')) nesting--;
        }
        List<Expression> program = CACHE.get(source);
        if (program != null) return program;
        try {
            program = PARSER.get().parse(source);
        } catch (team.unnamed.mocha.parser.ParseException e) {
            throw new IllegalArgumentException("Invalid Molang expression: " + source, e);
        }
        for (Expression expression : program) instrument(expression, 0);
        program = List.copyOf(program);
        CACHE.put(source, program);
        return program;
    }

    private static void instrument(Expression expression, int depth) {
        if (depth > 64) throw new IllegalArgumentException("Molang expression nesting exceeds 64");
        List<Expression> children = new ArrayList<>();
        if (expression instanceof CallExpression call) {
            if (call.function() instanceof IdentifierExpression id) {
                if (id.name().equalsIgnoreCase("loop")) call.function(new IdentifierExpression("__zote_loop"));
                if (id.name().equalsIgnoreCase("for_each")) call.function(new IdentifierExpression("__zote_for_each"));
            }
            children.add(call.function());
            children.addAll(call.arguments());
        } else if (expression instanceof BinaryExpression binary) {
            children.add(binary.left()); children.add(binary.right());
        } else if (expression instanceof UnaryExpression unary) {
            children.add(unary.expression());
        } else if (expression instanceof ExecutionScopeExpression scope) {
            children.addAll(scope.expressions());
        } else if (expression instanceof AccessExpression access) {
            children.add(access.object());
        } else if (expression instanceof ArrayAccessExpression array) {
            children.add(array.array()); children.add(array.index());
        } else if (expression instanceof TernaryConditionalExpression ternary) {
            children.add(ternary.condition()); children.add(ternary.trueExpression()); children.add(ternary.falseExpression());
        }
        for (Expression child : children) instrument(child, depth + 1);
    }

    public void variable(String name, Object value) {
        engine.scope().get("variable");
        Value converted = Value.of(value);
        variables.set(name, converted);
        savedVariables.put(name.toLowerCase(Locale.ROOT), converted);
    }

    public JSONObject save() {
        JSONObject result = new JSONObject();
        savedVariables.forEach((name, value) -> {
            if (value instanceof NumberValue) result.put(name, value.getAsNumber());
            else if (value instanceof StringValue) result.put(name, value.getAsString());
        });
        return result;
    }

    public void restore(JSONObject json) {
        if (json != null) for (String name : json.keySet()) variable(name, json.get(name));
    }
}
