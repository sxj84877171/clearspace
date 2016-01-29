using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using Newtonsoft.Json;
using System.Collections;
using System.Threading;
using Newtonsoft.Json.Linq;
using System.Windows.Controls;
using System.Windows.Media.Imaging;
namespace ClearSpace.NetworkService
{

    public class RemotePhotoInfo
    {
        public string fileName = null;
        public string fileUrl = null;
        public string fileCreationTime = null;
        public long fileSize = 0;

        public RemotePhotoInfo(string fileurl, string filetime, long filesize)
        {
            fileUrl = fileurl;
            fileCreationTime = filetime;
            fileSize = filesize;

            fileName = fileUrl.Substring(fileUrl.LastIndexOf('/') + 1);

            if (fileUrl == null || fileCreationTime == null || fileName == null)
                throw new Exception("RemotePhotoInfo init exception");
        }
    }

    public class RemotePhotoSizeCompare : IComparer<RemotePhotoInfo>
    {
        public int Compare(RemotePhotoInfo x, RemotePhotoInfo y)
        {
            return (int)(x.fileSize - y.fileSize);
        }
    }

    public class HTTPClientService
    {
        string m_downloadurl = null;
        string m_baseurl = null;
        string m_folder = null;
        UIElement m_UIStatus = null;
        List<RemotePhotoInfo> m_filelist = null;
        bool m_started = false;
        EndPoint m_server = null;
        HTTPClientCallback m_callback = null;
        bool m_bInitialized = false;

        public bool isStarted{
            get { return m_started; }
        }

       private int m_downloaded_img_count = 0;
       private int m_downloaded_img_fail_count = 0;
       private int m_totalImgCount = 0;

       public HTTPClientService(string url, string folder, EndPoint server, UIElement ui, HTTPClientCallback callback)
        {
            m_downloadurl = url;
            m_UIStatus = ui;
            m_folder = folder;

            m_server = server;
            m_filelist = new List<RemotePhotoInfo>();
            m_callback = callback;

            string ip = server.ToString().Substring(0,server.ToString().LastIndexOf(':'));
            m_baseurl = "http://" + ip + ":" + GlobalDef.REMOTEHTTPSERVER_PORT +"/download";

            App.WriteLog("new Httpdownload service" + "m_downloadurl:" + m_downloadurl, Log.MsgType.Information); 
        }

        private bool init()
        {
            bool ret = true;

            if (m_bInitialized) return ret;

            try
            {
                if (!Directory.Exists(m_folder))
                {
                    try
                    {
                        Directory.CreateDirectory(m_folder);
                    }
                    catch (Exception e)
                    {
                        Console.WriteLine("create folder " + m_folder + " exception" + e.Message);
                        ret = false;
                    }
                }

                HttpWebRequest request = WebRequest.Create(m_downloadurl) as HttpWebRequest;
                HttpWebResponse response = null;
                response = request.GetResponse() as HttpWebResponse;
                Stream responseStream = response.GetResponseStream();
                StreamReader sr = new StreamReader(responseStream);
                string jsonstr = sr.ReadToEnd();
                //save file list to mem
                JObject jobject = Newtonsoft.Json.Linq.JObject.Parse(jsonstr);
                if (jobject != null)
                {
                    IEnumerator enumrator =  jobject.GetEnumerator();
                    while (enumrator.MoveNext())
                    {
                        KeyValuePair<string, JToken> j = (KeyValuePair<string, JToken>)enumrator.Current;
                        string key = j.Key;
                        JToken t = j.Value;

                        JArray a = t as JArray;
                        if (a != null)
                        {
                            foreach( object o in a)
                            {
                                JObject jo = o as JObject;
                                if (jo != null)
                                {
                                    string filename = jo["filename"].ToString();
                                    string filedate = jo["date"].ToString();
                                    long filesize = Int64.Parse(jo["size"].ToString());
                                    RemotePhotoInfo  rpi = new RemotePhotoInfo(m_baseurl + key + "/" + filename, filedate, filesize);
                                    if(filename != null && filename != "")
                                        m_filelist.Add(rpi);
                                }
                            }
                        }
                    }
                }

                responseStream.Close();
                m_filelist.Sort(new RemotePhotoSizeCompare());
            }
            catch (Exception e)
            {
                Console.WriteLine(e.Message);
                App.WriteLog(e.ToString(), Log.MsgType.Error);
                ret = false;
            }

            return ret;
        }

        public void start()
        {
            if (m_started) return;
            m_started = true;
            Thread threadHand1 = new Thread(() => {

                //init first. well,  init cost time to communicate via internet . put in thread to save the udp recv process
                if (m_bInitialized = init())
                {
                    m_totalImgCount = m_filelist.Count;
                    if (m_callback != null)
                    {
                        m_callback.onStartingDownload(this,m_server, m_filelist.Count, m_UIStatus);
                    }

                    while (m_started && m_filelist.Count > 0)
                    {
                        RemotePhotoInfo remoteimg = m_filelist[0];
                        string filename = remoteimg.fileName;
                        string filepath = m_folder + "\\" + filename;

                        if (tryDownload(remoteimg, filepath))
                        {
                            m_downloaded_img_count++;
                            if (m_callback != null)
                            {
                                m_callback.onHttpFinishloadingOne(this, m_server, remoteimg, filepath, GlobalDef.Status.S_DownloadedSuccessful, this.m_UIStatus);
                            }
                            
                            m_filelist.RemoveAt(0);
                        }
                        else
                        {
                            m_downloaded_img_fail_count++;
                            if (m_callback != null && m_started)
                            {
                                m_callback.onHttpFinishloadingOne(this, m_server, remoteimg, filepath, GlobalDef.Status.S_DownloadedUnsuccessful, this.m_UIStatus);
                            }
                            if (File.Exists(filepath))
                            {
                                try
                                {
                                    File.Delete(filepath);
                                }
                                catch { }
                            }
                        }
                        
                        
                    }

                }
                
                if (m_callback != null)
                {
                    m_started = false;
                    //inform ui to finish this session, 0 indicates all well
                    m_callback.onHttpClientSelfDestroy(this, m_server, m_bInitialized? GlobalDef.Status.S_HTTPDownladerInitSuccess:GlobalDef.Status.S_HTTPDownladerInitFail, m_UIStatus);
                }
            });
            threadHand1.Start();
        }

        public void stop()
        {
            m_started = false;
        }

        private bool tryDownload( RemotePhotoInfo rpi, string filepath)
        {
            bool ret = true;
            bool serverOut = false;
            bool needdownlaod = true;
            string path = filepath;
            long downloadSizeTotal = 0, downloadSizeIndvl= 0;
            
            App.WriteLog("attempting to download " + rpi.fileUrl, Log.MsgType.Information);
            if (File.Exists(path))
            {
                if (Utils.getFileSize(path) == rpi.fileSize /*&& Utils.getFileCreationTime(path) == rpi.fileCreationTime */)
                {
                    needdownlaod = false ;
                }
                else
                {
                    path = path.Substring(0, path.LastIndexOf('.')) + "_" + DateTime.Now.Ticks + path.Substring(path.LastIndexOf('.'));
                }
            }

            if (needdownlaod)
            {
                if (!HttpDownloadFile(rpi, path, downloadSizeTotal, ref serverOut, ref downloadSizeIndvl))
                {
                    downloadSizeTotal += downloadSizeIndvl;
                    if (serverOut)
                    {
                        App.WriteLog("cannot connect server, attempt to try 3 mins", Log.MsgType.Information);
                        DateTime now = DateTime.UtcNow;
                        for (int i = 0; i < 72100 && serverOut && m_started; ++i)
                        {
                            serverOut = false;
                            if (!HttpDownloadFile(rpi, path, downloadSizeTotal, ref serverOut, ref downloadSizeIndvl))
                            {
                                App.WriteLog("Http download Time out with data recved , reset timer", Log.MsgType.Information);
                                if (downloadSizeIndvl > 0) now = DateTime.UtcNow;
                                downloadSizeTotal += downloadSizeIndvl;
                                ret = false;
                                if (!serverOut)
                                    break;
                            }
                            else
                            {
                                ret = true;
                                break;
                            }

                            TimeSpan ts = DateTime.UtcNow - now;
                            if (downloadSizeIndvl <= 0 && ts.TotalSeconds >= 175)
                                break;
                        }

                        if (serverOut)
                        {
                            m_started = false;
                            App.WriteLog("after 3 mins' check, server still out , attempting to cancel", Log.MsgType.Information);
                        }
                        else
                        {
                            App.WriteLog("server reconnected or 406 error accepted, continue to downlaod the rest", Log.MsgType.Information);
                        }
                    }
                    else
                    {
                        if (!HttpDownloadFile(rpi, path, downloadSizeTotal, ref serverOut,  ref downloadSizeIndvl))
                        {
                            ret = false;
                        }
                    }
                }
            }
            return ret;
        }

        private bool HttpDownloadFile(RemotePhotoInfo rpi, string path, long offset, ref bool serverOut, ref long sizeOut)
        {
            bool ret = true;
            serverOut = false;
            HttpWebRequest request = WebRequest.Create(rpi.fileUrl) as HttpWebRequest;
            request.AddRange(offset);
           // HttpWebRequest request = WebRequest.Create("http://192.168.9.15:7084/download/storage/emulated/0/DCIM/P51029-1937391111.jpg") as HttpWebRequest;
            if (request != null)
            {
                request.Timeout = ClearSpace.GlobalDef.HTTPTIMEOUT;
                request.ReadWriteTimeout = ClearSpace.GlobalDef.HTTPTIMEOUT;
            }
            HttpWebResponse response = null;
            Stream stream = null;
            Stream responseStream =  null;
            long fileLength = 0;
            long downloadedsize = 0;
            try
            {
                response = request.GetResponse() as HttpWebResponse;

                if (response.StatusCode == HttpStatusCode.OK || response.StatusCode == HttpStatusCode.PartialContent)
                {
                    if (response.ContentType.ToString().Contains("video") || response.ContentType.ToString().Contains("gif"))
                    {
                        if (m_callback != null && m_started)
                        {
                            m_callback.onHttpClientPreTransVideo(this, m_server, m_UIStatus);
                        }
                    }

                    if (response.ContentLength == 0)  
                    {
                        //for ios reject transmission
                        m_started = false;
                    }
                    else
                    {
                        fileLength = response.ContentLength;
                        responseStream = response.GetResponseStream();
                        responseStream.ReadTimeout = ClearSpace.GlobalDef.HTTPTIMEOUT;
                        stream = new FileStream(path, FileMode.OpenOrCreate, FileAccess.Write, FileShare.Read);
                        if (stream.CanSeek)
                        {
                            stream.Seek(offset, SeekOrigin.Begin);
                        }
                        byte[] bArr = new byte[GlobalDef.Download_Cache_Buff_Size];
                        int size = responseStream.Read(bArr, 0, (int)bArr.Length);
                        while (m_started && size > 0)
                        {
                            stream.Write(bArr, 0, size);
                            downloadedsize += size;
                            size = responseStream.Read(bArr, 0, (int)bArr.Length);
                        }
                    }
                }
                else if (response.StatusCode == HttpStatusCode.NotFound)
                {
                    ret = false;
                }

                if (downloadedsize==0 || (downloadedsize + offset) < fileLength)
                {
                    ret = false;
                }
            }
            catch (WebException e)
            {
                App.WriteLog("Httpdownlad exception: " + e.Message, Log.MsgType.Error);
                serverOut = true;
                ret = false;
                
                if (e.Response != null )
                {
                    HttpWebResponse httpex = e.Response as HttpWebResponse;
                    if (httpex != null && httpex.StatusCode == HttpStatusCode.NotAcceptable)
                    {
                        serverOut = false;
                        App.WriteLog("406 error, skip " + rpi.fileUrl, Log.MsgType.Error);
                    }
                }
            }
            finally
            {
                if(stream != null)
                    stream.Close();
                if(responseStream != null)
                    responseStream.Close();
                sizeOut = downloadedsize;
            }

            if (!m_started)
                ret = false;

            if (ret)
            {
                try
                {
                    Utils.setFileCreationTime(path, Int64.Parse(rpi.fileCreationTime));
                }
                catch { }
            }

            return ret;
        }

        public int getDownloadedImgCount()
        {
            return this.m_downloaded_img_count;
        }

        public int getDownloadFailedImgCount()
        {
            return this.m_downloaded_img_fail_count;
        }

        public int getTotalImgCount()
        {
            return this.m_totalImgCount;
        }
    }
}
