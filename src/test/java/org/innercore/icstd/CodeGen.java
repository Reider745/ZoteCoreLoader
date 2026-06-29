package org.innercore.icstd;

import com.zhekasmirnov.innercore.api.gen.CodeGenBindGenBase;
import org.innercore.icstd.annotations.ApiBinding;
import org.innercore.icstd.annotations.ArgOverload;

@ApiBinding
public class CodeGen extends CodeGenBindGenBase {
    @ApiBinding public int test;

    @ApiBinding
    public CodeGen() {
        this.test = 5;
    }

    @ApiBinding
    public int sum(int a, int b) {
        return a + b;
    }

    @ApiBinding
    public int sum(int a, int b, int c) {
        return a + b + c;
    }

    @ApiBinding
    public String other(String a, int b, CodeGen c) {
        return a + b + c.test;
    }

    @ApiBinding
    public static int staticSum(int a, int b, @ArgOverload("0") int c) {
        return a + b + c;
    }

}
