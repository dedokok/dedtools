package com.dedokok.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegExUtils {
    public static boolean isFind(String string, String pattern){
        try {
            if (string == null) return false;
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(string);
            boolean isFound = m.find();
            return isFound;
        }
        catch(Exception e){
            return false;
        }
    }
    public static String getGroup(String string, String pattern, int group){
        try {
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(string);
            if (!m.find() || m.groupCount() > group) return null;
            return m.group(group);
        }
        catch(Exception e){
            return null;
        }
    }
}
