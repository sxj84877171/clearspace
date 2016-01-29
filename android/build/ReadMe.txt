说明:
中文名称                 简写
CDLSW2                   开发渠道
乐商店                   LeStore
百度手机助手             BaiduAssistant 
91助手                   91Assistant
安卓市场                 AndroidMarket
豌豆荚                   Wandoujia
安智市场                 AnZhi
360手机助手              360Assistant
应用宝                   YingYonBao
小米应用商店             XiaoMi
锤子                     CuiZi
ViVo                     ViVo
Oppo                     Oppo
机锋市场                 JiFengMarket
酷市场                   KuMarket
中国移动应用商城         ChinaMobileMarket
华为                     HuaWei
GooglePlay				 GooglePlay

因为当前脚本并没有完全自动化,所以需要按照以下步骤,进行打包.
0.更新svn代码,如果无法打包,请删除project下的bin/gen目录,以清除eclipse自动生成的旧文件.

1.发布新渠道时,需要修改以下地方:
	1.1.在eclipse的project目录下修改ant.properties的market_channels字段:
		LeStore,BaiduAssistant,91Assistant,AndroidMarket,Wandoujia,AnZhi,360Assistant,YingYongBao,XiaoMi,CuiZi,ViVo,Oppo,JiFengMarket,KuMarket,ChinaMobileMarket,HuaWei,CDLSW2
	1.2.修改config.py的channels变量.

2.修改版本时,需要修改以下地方:
	2.1.AndroidManifest.xml修改版本.
	2.2.在eclipse的project目录下修改ant.properties的app_version.
	2.3.修改build.bat的版本信息,代码如下python config.py 1.4.65.
	2.4.修改config.py的更新说明.
	
3.上传到服务器,目前必须按以下三步骤做,否则无法更新.
	3.1.提交代码到114.215.236.240的svn地址,目录如下F:\svn\linkit_lync\Server\xphone\photoclean\update\android.
	3.2.到115.29.178.5服务器更新apk,所有apk文件从此处下载.
	3.3.到亚马逊服务器更新xml文件.