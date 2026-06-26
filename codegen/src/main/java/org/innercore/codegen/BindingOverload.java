package org.innercore.codegen;

import java.util.ArrayList;
import java.util.List;

/** A binding overload prepared for code generation. */
public final class BindingOverload {
    public String name = "";
    public String moduleName = "";
    public String mangledName = "";
    public String bindType = "";
    public String moduleClsName = "";
    public String fnType = "";
    public VarDecl ret = new VarDecl(Encoding.BASE_TYPE_VOID);
    public List<VarDecl> args = new ArrayList<VarDecl>();
    public int argsCnt = 0;
    public boolean needJavaWrap = false;
    public String callSrc = "";
    public List<String> attrs = new ArrayList<String>();

    @Override
    public String toString() {
        return "BindingOverload{" +
                "name='" + name + '\'' +
                ", moduleName='" + moduleName + '\'' +
                ", mangledName='" + mangledName + '\'' +
                ", bindType='" + bindType + '\'' +
                ", moduleClsName='" + moduleClsName + '\'' +
                ", fnType='" + fnType + '\'' +
                ", ret=" + ret +
                ", args=" + args +
                ", argsCnt=" + argsCnt +
                ", needJavaWrap=" + needJavaWrap +
                ", callSrc='" + callSrc + '\'' +
                ", attrs=" + attrs +
                '}';
    }
}
