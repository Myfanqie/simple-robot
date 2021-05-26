package love.simbot.example.tasks;

import love.forte.common.ioc.annotation.Beans;
import love.forte.common.ioc.annotation.Depend;
import love.forte.simbot.api.message.MessageContent;
import love.forte.simbot.api.message.MessageContentBuilder;
import love.forte.simbot.api.message.MessageContentBuilderFactory;
import love.forte.simbot.api.sender.BotSender;
import love.forte.simbot.bot.BotManager;
import love.forte.simbot.timer.Cron;
import love.forte.simbot.timer.EnableTimeTask;
import love.forte.simbot.timer.Fixed;

import java.util.concurrent.TimeUnit;

/**
 * @author liliya
 * @date 2021/5/26 14:09
 * 三点几了，饮茶先
 */
@Beans
@EnableTimeTask
public class ThreeOclock {

    @Depend
    private BotManager botManager;

    @Depend
    private MessageContentBuilderFactory messageContentBuilderFactory;

    //每十秒执行一次
    @Cron(value = "0 55 15 ? * *")
    public void task() {
        MessageContentBuilder messageContentBuilder = messageContentBuilderFactory.getMessageContentBuilder();
        BotSender sender = botManager.getDefaultBot().getSender();
        MessageContent msg = messageContentBuilder.imageLocal("/img/ThreeOclock.jpg").build();
        sender.SENDER.sendGroupMsg("264207436",msg);
        sender.SENDER.sendPrivateMsg("1752181917", msg);
    }

}
