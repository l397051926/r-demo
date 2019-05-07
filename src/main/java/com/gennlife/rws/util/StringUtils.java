/**
 * copyRight
 */
package com.gennlife.rws.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;

/**
 * Created by liuzhen.
 * Date: 2017/10/19
 * Time: 15:22
 */
public class StringUtils extends org.apache.commons.lang3.StringUtils{
    public synchronized static String getUUID(){
        return UUID.randomUUID().toString().toUpperCase().replace("-","");
    }

    public static boolean isEmpty(String... args){
        boolean result = false;
        if(args!=null&&args.length>0){
            for(String arg:args){
                if(org.apache.commons.lang3.StringUtils.isEmpty(arg)){
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    public static String inputStream2String(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuffer buffer = new StringBuffer();
        String line = "";
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
        }
        return buffer.toString();

    }

}
