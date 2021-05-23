package love.simbot.example;

import love.forte.common.configuration.Configuration;
import love.forte.simbot.annotation.SimbotApplication;
import love.forte.simbot.annotation.SimbotResource;
import love.forte.simbot.api.sender.BotSender;
import love.forte.simbot.bot.Bot;
import love.forte.simbot.core.SimbotApp;
import love.forte.simbot.core.SimbotContext;
import love.forte.simbot.core.SimbotProcess;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

/**
 * simbot 启动类。
**/
@SimbotApplication
// @SimbotApplication
public class SimbotExampleApplication implements SimbotProcess {
    public static void main(String[] args) {
        SimbotApp.run(SimbotExampleApplication.class, args);
    }

    //生命周期函数
    @Override
    public void post(@NotNull SimbotContext context) {
        Bot bot = context.getBotManager().getDefaultBot();
        BotSender sender = bot.getSender();

        sender.SENDER.sendPrivateMsg(1752181917,"我上线了"+ LocalDateTime.now());
    }

    @Override
    public void pre(@NotNull Configuration config) {

    }
}
