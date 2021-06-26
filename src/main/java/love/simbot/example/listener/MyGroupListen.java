package love.simbot.example.listener;

import catcode.Neko;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import love.forte.common.ioc.annotation.Beans;
import love.forte.simbot.annotation.*;
import love.forte.simbot.api.message.MessageContent;
import love.forte.simbot.api.message.containers.GroupAccountInfo;
import love.forte.simbot.api.message.containers.GroupInfo;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.message.events.PrivateMsg;
import love.forte.simbot.api.sender.MsgSender;
import love.forte.simbot.filter.MatchType;
import love.simbot.example.enums.API;
import love.simbot.example.fun.YiYan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.io.File;
import java.nio.charset.Charset;
import java.time.temporal.ValueRange;
import java.util.List;
import java.util.Set;

/**
 * 群消息监听的示例类。
 * 所有需要被管理的类都需要标注 {@link Beans} 注解。
 *
 * @author ForteScarlet
 */
@Beans
public class MyGroupListen {
    private static final Jedis jedis = new Jedis("106.14.70.31");
    private static Set<String> keys;
    private static File file = new File("data.json");
    private static JSONObject root = JSONUtil.readJSONObject(file, Charset.defaultCharset());;

    static {
        //首先是选到第一个数据库，如果这个数据库没有数据的话那就加入一些数据进去
        jedis.select(1);
        keys = jedis.keys("*");
        if (keys.isEmpty()) {
            for (String key : root.keySet()) {
                JSONArray jsonArray = root.getJSONArray(key);
                String[] strings = jsonArray.toArray(new String[]{});
                jedis.sadd(key, strings);
            }
            System.out.println("OK!");
        }
    }

    /**
     * log
     */
    private static final Logger LOG = LoggerFactory.getLogger(MyGroupListen.class);

    /**
     * 此监听函数代表，收到消息的时候，将消息的各种信息打印出来。
     * <p>
     * 此处使用的是模板注解 {@link OnGroup}, 其代表监听一个群消息。
     * <p>
     * 由于你监听的是一个群消息，因此你可以通过 {@link GroupMsg} 作为参数来接收群消息内容。
     */
    @OnGroup
    public void onGroupMsg(GroupMsg groupMsg) {
        // 打印此次消息中的 纯文本消息内容。
        // 纯文本消息中，不会包含任何特殊消息（例如图片、表情等）。
        System.out.println(groupMsg.getText());

        // 打印此次消息中的 消息内容。
        // 消息内容会包含所有的消息内容，也包括特殊消息。特殊消息使用CAT码进行表示。
        // 需要注意的是，绝大多数情况下，getMsg() 的效率低于甚至远低于 getText()
        System.out.println(groupMsg.getMsg());

        // 获取此次消息中的 消息主体。
        // messageContent代表消息主体，其中通过可以获得 msg, 以及特殊消息列表。
        // 特殊消息列表为 List<Neko>, 其中，Neko是CAT码的封装类型。

        MessageContent msgContent = groupMsg.getMsgContent();

        // 打印消息主体
        System.out.println(msgContent);
        // 打印消息主体中的所有图片的链接（如果有的话）
        List<Neko> imageCats = msgContent.getCats("image");
        System.out.println("img counts: " + imageCats.size());
        for (Neko image : imageCats) {
            System.out.println("Img url: " + image.get("url"));
        }

        // 获取发消息的人。
        GroupAccountInfo accountInfo = groupMsg.getAccountInfo();
        // 打印发消息者的账号与昵称。
        System.out.println(accountInfo.getAccountCode());
        System.out.println(accountInfo.getAccountNickname());

        // 获取群信息
        GroupInfo groupInfo = groupMsg.getGroupInfo();
        // 打印群号与名称
        System.out.println(groupInfo.getGroupCode());
        System.out.println(groupInfo.getGroupName());
    }

    @OnGroup
    @Filter("舔狗日记")
    public void repeat(GroupMsg msg, MsgSender sender) {
        String sendUrl = StrUtil.format("{}?key={}", API.API_TIANGOU, API.KEY_TIANGOU);
        System.out.println(sendUrl);
        String body = HttpUtil.get(sendUrl);

        JSONObject root = JSONUtil.parseObj(body);
        if (root.getInt("code") != 200) {
            sender.SENDER.sendGroupMsg(msg, "今天的舔狗日记，就到这里吧。。");
        } else {
            String content = root.getJSONArray("newslist").getJSONObject(0).getStr("content");
            String message = StrUtil.format("{}\t {}\n{}", DateUtil.today(), "晴", content);
            sender.SENDER.sendGroupMsg(msg, message);
        }

    }

    @OnGroup
    @Filter("一言")
    public void yiyan(GroupMsg msg, MsgSender sender) {
        JSONObject jsonObject = YiYan.msg();
        String hitokoto = jsonObject.getStr("hitokoto");
        String from = jsonObject.getStr("from");
        sender.SENDER.sendGroupMsg(msg, hitokoto + "\n--" + from);
    }

    @OnGroup
    public void tingws(GroupMsg msg, MsgSender sender) {
        String msgContent = msg.getMsg();
        //如果存在这个值那就随机给一个呗
        if (keys.contains(msgContent)) {

            sender.SENDER.sendGroupMsg(msg, jedis.srandmember(msgContent));
        }
    }

    @OnGroup
    @Filter(value = "提示")
    public void tishi(GroupMsg msg, MsgSender sender) {
        sender.SENDER.sendGroupMsg(msg, jedis.randomKey());
    }



    @Filter(value = "听我说",matchType = MatchType.STARTS_WITH)
    @OnGroup
    public void tingwoshuo(GroupMsg msg, MsgSender sender) {
        String msg1 = msg.getMsg();
        if (msg1.equals("听我说")) return;
        String substring = msg1.substring(3);
        String[] split = substring.split("，");
        String key = split[0];
        String value = split[1];


        key = key.trim();

        JSONArray targetJson = root.getJSONArray(key);
        if (null == targetJson){
            targetJson = new JSONArray();
        }
        targetJson.add(value);
        root.set(key,targetJson);
        String s = JSONUtil.toJsonStr(root);
        //最后是将文件修改了
        FileUtil.writeString(s,file,Charset.defaultCharset());

        Long sadd = jedis.sadd(key, value);
        if (sadd == 1L) {
            keys.add(key);
            sender.SENDER.sendGroupMsg(msg, "\"" + key + "\"" + "添加成功！");
        } else {
            LOG.error(key + "=============>  " + value);
            sender.SENDER.sendGroupMsg(msg, "或许添加失败了QAQ！");
        }
    }

    @Filter(value = "test")
    @OnGroup
    public void test(GroupMsg msg, MsgSender sender) {
        sender.SENDER.sendGroupMsg(msg, "爷来了！");
    }

}
