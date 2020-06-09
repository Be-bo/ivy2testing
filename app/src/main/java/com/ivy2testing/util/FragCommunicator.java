package com.ivy2testing.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Interface used to send messages [fragment -> activity]
 * implement in activity and call in fragment
 */
public interface FragCommunicator {
    Object message(Object obj);         // [obj] is message sent by fragment (can return a value as well)
    void mapMessage(Map<Object, Object> map);
}
