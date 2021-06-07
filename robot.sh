if test -z "$(ps aux|grep simbot-mirai-demo |grep -v grep|cut -c 9-16|xargs)"; then
	echo "The result is empty."
else
	ps aux|grep simbot-mirai-demo |grep -v grep|cut -c 9-16|xargs kill -15
fi

nohup java -jar simbot-mirai-demo-2.0.5.jar --simbot.yml  > log.file  2>&1 &