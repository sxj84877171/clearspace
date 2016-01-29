# -*- coding: utf-8 -*-
import sys
import os
import time
import xml.dom.minidom
from xml.dom.minidom import Document
from xml.etree.ElementTree import ElementTree
from xml.etree.ElementTree import Element
from xml.etree.ElementTree import SubElement
from xml.etree.ElementTree import dump
from xml.etree.ElementTree import Comment
from xml.etree.ElementTree import tostring

def generateConfigFile(app_store, app_ver, app_description):
    xml = ElementTree()
    update = Element('update')
    xml._setroot(update)
    SubElement(update,'version').text = app_ver
    SubElement(update,'name').text = 'cleanspace'
    app_name = 'cleanspace_' + app_ver + '.apk'
    url = 'http://thelinkit.com/photoclean/update/android/%s/%s'% (app_store,app_name)
    url.join('</url>')
    testurl = 'http://thelinkit.com/photoclean/update/android/%s/test/%s'% (app_store,app_name)
    testurl.join('</testurl>')
    
    SubElement(update,'url').text = url 
    SubElement(update,'testurl').text = testurl
    SubElement(update,'description').text = app_description
    
    path = "./bin/" + app_store;
    if not os.path.isdir(path):
        os.mkdir(path)
        
    if not os.path.isdir(path +'/test'):
        os.mkdir(path + '/test')
    testpath = path + '/test/update.xml'
    
    path += '/update.xml'
    xml.write(path,'utf-8');

    xml.write(testpath,'utf-8');
    
if __name__ == '__main__':
    app_ver = sys.argv[1];
    app_description = u"1.传输过程和传输完成增加通知提醒.\n2.优化微信分享,方便好友下载.\n3.添加排名机制";
    print "app_ver is " + app_ver;
    #print "app_store is " + app_store;
    #market_channels = ['LeStore','360','googleplay','yingyongbao','baidu','wandoujia','cdlsw2']
    channels = "LeStore,BaiduAssistant,91Assistant,AndroidMarket,Wandoujia,AnZhi,360Assistant,YingYongBao,XiaoMi,CuiZi,ViVo,Oppo,JiFengMarket,KuMarket,ChinaMobileMarket,HuaWei,CDLSW2"
    market_channels = channels.split(',')
    for channel in market_channels:
        ret = generateConfigFile(channel, app_ver, app_description)
