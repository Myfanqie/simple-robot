package love.simbot.example.fun;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import love.simbot.example.enums.API;

/**
 * @author liliya
 * @date 2021/5/23 23:03
 * 一言api
 */
public class YiYan {
    public static JSONObject msg(){
        String s = HttpUtil.get(API.API_YIYAN);
        return JSONUtil.parseObj(s);
    }
}
