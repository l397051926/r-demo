package com.gennlife.rws.content;

import javax.sound.midi.Soundbank;

/**
 * @author
 * @create 2019 15 21:00
 * @desc
 **/
public class RedisContent {
    public static final String RWS_SERVICE = "rws_service_";



    public static String getRwsService(String val){
        return RWS_SERVICE.concat(val);
    }
    
}
