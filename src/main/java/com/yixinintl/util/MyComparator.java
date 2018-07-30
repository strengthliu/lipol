package com.yixinintl.util;

import java.util.Comparator;
import java.util.Map;

/**
 * Created by eilir on 16/7/1.
 */
public class MyComparator implements Comparator{

    Map.Entry<String, ?> base;

    public MyComparator(Map.Entry<String, ?> base) {
        this.base = base;
    }

//    按key升序
    public int compare(Object a, Object b) {
        if(a instanceof Map.Entry){
            return ((String)((Map.Entry) a).getKey()).compareTo((String)((Map.Entry) b).getKey());
        }else{
            return 0;
        }
    }

}
