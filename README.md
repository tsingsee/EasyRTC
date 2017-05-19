##EasyRTC介绍##
EasyPlayer是播放RTSP视频的屠龙刀，而EasyPusher又是推送RTSP的倚天剑，那将这两个神兵利器合起来，岂不是很厉害！基于这个思路，我们实现了一款视频对讲APP，称之为EasyRTC。
简单来说，针对两个用户A和B，EasyRTC首先将A、B两端的音视频数据推送，然后在B、A端分别预览对方的视频，加上WebRTC进行回音抵消，即可实现音视频对讲。
![](http://www.easydarwin.org/github/images/easyrtc/1.png)

APP界面很简单，首先在设置里面填写下地址、端口、我的ID，对方的ID，其中app会自动生成一个4位数的ID作为我的ID。

![设置参数](http://img.blog.csdn.net/20170419202633197?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvanl0MDU1MQ==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

点击OK后，会进入主界面，主界面为左右的两分屏，分别显示自己和对方的画面。下图是作者测试的两个手机的现场图片：
![这里写图片描述](http://img.blog.csdn.net/20170419215707899?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvanl0MDU1MQ==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

实现语音对讲不能不提的就是回音消除的问题。作者使用了WebRTC的AECM模块进行回音消除，经测试效果十分明显，回音基本上都消掉了。

##Demo下载##
APP下载地址：http://fir.im/EasyRTC

![EasyRTC](http://www.easydarwin.org/github/images/easyrtc/EasyRTC_Android.png)

## 获取更多信息 ##

邮件：[support@easydarwin.org](mailto:support@easydarwin.org) 

WEB：[www.EasyDarwin.org](http://www.easydarwin.org)

QQ交流群：[465901074](http://jq.qq.com/?_wv=1027&k=2G045mo "EasyPusher & EasyRTSPClient")

Copyright &copy; EasyDarwin.org 2012-2017

![EasyDarwin](http://www.easydarwin.org/skin/easydarwin/images/wx_qrcode.jpg)