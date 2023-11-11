package com.reider745.api.hooks;

import java.util.ArrayList;
import java.util.HashMap;

public class Arguments extends HashMap<String, Object> {
    public <T>T arg(String key) {
        return (T) this.get(key);
    }

    private static final String IntegerName = Integer.class.getName(),
            DoubleName = Double.class.getName(),
            FloatName = Float.class.getName(),
            BooleanName = Boolean.class.getName(),
            ByteName = Byte.class.getName(),
            ShortName = Short.class.getName(),
            LongName = Short.class.getName(),
            CharacterName = Character.class.getName(),
            RetNullName = JarFileLoader.RetNull.class.getName(),
            ObjectName = Object.class.getName();


    public static String getConvertCode(String retType, boolean toObject, String getValueCode){
        boolean isVoid = retType.equals("void");
        String type = (isVoid ? RetNullName : retType);

        if(toObject){
            String convert_code = "("+ObjectName+") "+getValueCode;
            switch (retType) {
                case "int" -> convert_code = "(("+ObjectName+") "+IntegerName+".valueOf("+getValueCode+"))";
                case "double" -> convert_code = "(("+ObjectName+") "+DoubleName+".valueOf("+getValueCode+"))";
                case "float" -> convert_code = "(("+ObjectName+") "+FloatName+".valueOf("+getValueCode+"))";
                case "boolean" -> convert_code = "(("+ObjectName+") "+BooleanName+".valueOf("+getValueCode+"))";
                case "byte" -> convert_code = "(("+ObjectName+") "+ByteName+".valueOf("+getValueCode+"))";
                case "short" -> convert_code = "(("+ObjectName+") "+ShortName+".valueOf("+getValueCode+"))";
                case "long" -> convert_code = "(("+ObjectName+") "+LongName+".valueOf("+getValueCode+"))";
                case "char" -> convert_code = "(("+ObjectName+") "+CharacterName+".valueOf("+getValueCode+"))";
            }
            return convert_code;
        }else{
            String convert_code = "("+type+") _ctr_hook.getResult();";
            switch (retType){
                case "int" -> convert_code = "(("+IntegerName+") "+getValueCode+").intValue();";
                case "double" -> convert_code = "(("+DoubleName+") "+getValueCode+").doubleValue();";
                case "float" -> convert_code = "(("+FloatName+") "+getValueCode+").floatValue();";
                case "boolean" -> convert_code = "(("+BooleanName+") "+getValueCode+").booleanValue();";
                case "byte" -> convert_code = "(("+ByteName+") "+getValueCode+").byteValue();";
                case "short" -> convert_code = "(("+ShortName+") "+getValueCode+").shortValue();";
                case "long" -> convert_code = "(("+LongName+") "+getValueCode+").longValue();";
                case "char" -> convert_code = "(("+CharacterName+") "+getValueCode+").charValue();";
            }

            return convert_code;
        }
    }

    /*public static String[] parseSignature(String signature){
        if(signature.equals("-1")) return new String[]{};

        ArrayList<String> types = new ArrayList<>();
        boolean is1 = false, is2 = true;

        for(int i = 0;i < signature.length();i++){
            char symbol = signature.charAt(i);
            if(symbol == ')') break;

            String type = "";
            switch (symbol){
                case 'I' -> type = "int";
                case 'F' -> type = "float";
                case 'C' -> type = "char";
                case '(' -> {}
                case 'Z' -> type = "boolean";
                case 'B' -> type = "byte";
                case 'S' -> type = "short";
                case 'J' -> type = "long";
                case 'D' -> type = "double";
                case '[' -> {
                    is1 = true;
                    is2 = false;
                }
                case 'L' -> {
                    i++;
                    char s;
                    while ((s = signature.charAt(i)) != ';') {
                        type += s;
                        i++;
                    }

                    type = type.replace('/', '.');
                }
                default -> throw new RuntimeException("Not support symbol "+symbol+"  "+signature);
            }

            if(is1 && is2){
                type += "[]";
                is1 = false;
                is2 = true;
            }else if(!is2)
                is2 = true;

            if(type != "")
                types.add(type);
        }
        System.out.println(types.toString());
        return types.toArray(new String[0]);
    }*/
}
