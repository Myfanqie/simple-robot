package love.simbot.example.listener;

import catcode.CatCodeUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import love.forte.common.ioc.annotation.Beans;
import love.forte.common.ioc.annotation.Depend;
import love.forte.simbot.annotation.Filter;
import love.forte.simbot.annotation.OnGroup;
import love.forte.simbot.annotation.OnPrivate;
import love.forte.simbot.api.message.MessageContent;
import love.forte.simbot.api.message.MessageContentBuilder;
import love.forte.simbot.api.message.MessageContentBuilderFactory;
import love.forte.simbot.api.message.events.PrivateMsg;
import love.forte.simbot.api.sender.MsgSender;
import love.forte.simbot.api.sender.Sender;
import love.simbot.example.enums.API;
import love.simbot.example.fun.YiYan;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * 私聊消息监听的示例类。
 * 所有需要被管理的类都需要标注 {@link Beans} 注解。
 *
 * @author ForteScarlet
 */
@Beans
public class MyPrivateListen {
    private File file = new File("/simple-robot/data.json");
    private JSONObject acgData = JSONUtil.readJSONObject(file, Charset.forName("UTF-8"));
    private Set<String> acgSet = acgData.keySet();

    /**
     * 通过依赖注入获取一个 "消息正文构建器工厂"。
     */
    @Depend
    private MessageContentBuilderFactory messageContentBuilderFactory;

    /**
     * 此监听函数监听一个私聊消息，并会复读这个消息，然后再发送一个表情。
     * 此方法上使用的是一个模板注解{@link OnPrivate}，
     * 其代表监听私聊。
     * 由于你监听的是私聊消息，因此参数中要有个 {@link PrivateMsg} 来接收这个消息实体。
     * <p>
     * 其次，由于你要“复读”这句话，因此你需要发送消息，
     * 因此参数中你需要一个 "消息发送器" {@link Sender}。
     * <p>
     * 当然，你也可以使用 {@link love.forte.simbot.api.sender.MsgSender}，
     * 然后 {@code msgSender.SENDER}.
     */
//    @OnPrivate
//    public void replyPrivateMsg1(PrivateMsg privateMsg, Sender sender) {
//        // 获取消息正文。
//        MessageContent msgContent = privateMsg.getMsgContent();
//
//        // 向 privateMsg 的账号发送消息，消息为当前接收到的消息。
//        sender.sendPrivateMsg(privateMsg, msgContent);
//
//        // 再发送一个表情ID为'9'的表情。
//        // 方法1：使用消息构建器构建消息并发送
//        // 在绝大多数情况下，使用消息构建器所构建的消息正文 'MessageContent'
//        // 是用来发送消息最高效的选择。
//        // 相对的，MessageContentBuilder所提供的构建方法是十分有限的。
//
//        // 获取消息构建器
//        MessageContentBuilder msgBuilder = messageContentBuilderFactory.getMessageContentBuilder();
//        // 通过.text(...) 向builder中追加一句话。
//        // 通过.face(ID) 向builder中追加一个表情。
//        // 通过.build() 构建出最终消息。
//        MessageContent msg = msgBuilder.text("表情：").face(9).build();
//
//        // 直接通过这个msg发送。
//        sender.sendPrivateMsg(privateMsg, msg);
//
//        // 方法2：使用CAT码发送消息。
//        // 使用CAT码构建一个需要解析的消息是最灵活的，
//        // 但是相对的，它的效率并不是十分的可观，毕竟在这其中可能会涉及到很多的'解析'操作。
//
//        // 获取CAT码工具类实例
//        CatCodeUtil catCodeUtil = CatCodeUtil.getInstance();
//
//        // 构建一个类型为 'face', 参数为 'id=9' 的CAT码。
//        // 有很多方法。
//
//        // 1. 通过 codeBuilder 构建CAT码
//        // String cat1 = catCodeUtil.getStringCodeBuilder("face", false).key("id").value(9).build();
//
//        // 2. 通过CatCodeUtil.toCat 构建CAT码
//        // String cat2 = catCodeUtil.toCat("face", "id=9");
//
//        // 3. 通过模板构建CAT码
//        String cat3 = catCodeUtil.getStringTemplate().face(9);
//
//        // 在cat码前增加一句 '表情' 并发送
//        sender.sendPrivateMsg(privateMsg, "表情：" + cat3);
//
//    }

    //舔狗日记api，每天100次调用
    @OnPrivate
    @Filter("舔狗日记")
    public void repeat(PrivateMsg msg, MsgSender sender) {
        String sendUrl = StrUtil.format("{}?key={}", API.API_TIANGOU,API.KEY_TIANGOU);
        String body = HttpUtil.get(sendUrl);
        JSONObject root = JSONUtil.parseObj(body);
        if (root.getInt("code") != 200) {
            sender.SENDER.sendPrivateMsg(msg, "今天的舔狗日记，就到这里吧。。");
        } else {
            String content = root.getJSONArray("newslist").getJSONObject(0).getStr("content");
            String message = StrUtil.format("{}\t {}\n{}", DateUtil.today(), "晴", content);
            sender.SENDER.sendPrivateMsg(msg, message);
        }

    }

    @OnPrivate
    @Filter("一言")
    public void yiyan(PrivateMsg msg, MsgSender sender) {
        JSONObject jsonObject = YiYan.msg();
        String hitokoto = jsonObject.getStr("hitokoto");
        String from = jsonObject.getStr("from");
        sender.SENDER.sendPrivateMsg(msg,hitokoto+"\n--"+from);
    }

    //太二次元了
    @OnPrivate
    public void ACG(PrivateMsg msg, MsgSender sender){
        String m = msg.getMsg();
        if (acgSet.contains(m)){
            //找到这个msg,随机数的方式
            List<String> s = acgData.getJSONArray(m).toList(String.class);
            sender.SENDER.sendPrivateMsg(msg, RandomUtil.randomEle(s));
        }
    }

    @OnPrivate
    @Filter("你会啥")
    public void tips(PrivateMsg msg, MsgSender sender){
        //随机发送一个key
        sender.SENDER.sendPrivateMsg(msg,RandomUtil.randomEle(acgSet.toArray(new String[]{})));
    }


}
