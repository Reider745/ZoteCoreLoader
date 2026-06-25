package org.innercore.icstd.test;

import com.zhekasmirnov.innercore.api.gen.CodeGenBindGenBase;
import org.innercore.icstd.extension.TimingExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

@ExtendWith(TimingExtension.class)
public class CodeGenTest {
    @Test
    public void test() {
        Scriptable api = CodeGenBindGenBase.createEmptyModuleObject();
        CodeGenBindGenBase.injectJsStatic(api, true);

        Function staticSum = (Function) api.get("staticSum", api);

        assert ((Number) staticSum.call(null, null, null, new Object[]{5, 5, 5, 5})).intValue() == 15;
        assert ((Number) staticSum.call(null, null, null, new Object[]{5, 5, 5})).intValue() == 15;
        assert ((Number) staticSum.call(null, null, null, new Object[]{5, 5})).intValue() == 10;
        assert ((Number) staticSum.call(null, null, null, new Object[]{5, null, 2f})).intValue() == 7;
        assert ((Number) staticSum.call(null, null, null, new Object[]{"5", "5"})).intValue() == 10;

        Scriptable self = ((Function) api).construct(null, null, new Object[]{});

        Function sum = (Function) self.get("sum", self);

        assert ((Number) sum.call(null, null, self, new Object[]{5, 5, 5, 5})).intValue() == 15;
        assert ((Number) sum.call(null, null, self, new Object[]{5, 5, 5})).intValue() == 15;
        assert ((Number) sum.call(null, null, self, new Object[]{5, 5})).intValue() == 10;
        assert ((Number) sum.call(null, null, self, new Object[]{5, null, 2f})).intValue() == 7;
        assert ((Number) sum.call(null, null, self, new Object[]{"5", "5"})).intValue() == 10;

        Function other = (Function) self.get("other", self);

        assert (other.call(null, null, self, new Object[]{"test", "5", self})).toString().equals("test55");

        assert ((Number) self.get("test", self)).intValue() == 5;
        self.put("test", self, 1);

        assert (other.call(null, null, self, new Object[]{"test", "5", self})).toString().equals("test51");
    }
}
