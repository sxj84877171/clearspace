using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;
using ClearSpace.NetworkService;
using Newtonsoft.Json;
using System.IO;
using Newtonsoft.Json.Linq;
using System.ComponentModel;
using Shareit.Foundation.Discovery;
using ThumbsExplorer;
using EPHotspotLib;
using System.Diagnostics;

namespace ClearSpace
{
    /// <summary>
    /// Interaction logic for MainWindow.xaml
    /// </summary>
    public partial class MainWindow : Window, AsyncUDPCallback, HTTPClientCallback, CounterpartScanServiceCallback
    {
        private UDPBroadcastService broadcast = null;
        private AsyncUDPServer asudp = null;
        private CounterpartScanService scanSoftAp = null;
        private string m_basefolder = null;
        private object locker = new Object();
        private object locker1 = new Object();
        private object mobile_softap_locker = new object();
        private bool mobile_softap_holder = false;
        private bool softap_conn = false;
        private Dictionary<EndPoint, HTTPClientService> m_httpServsDit = null;
        EasyPlusHotspot hotspot = null;
        private bool isClosing = false;
      //  private bool isForceRestart = false;
        private tagUpdateInfo m_updateinfo;
        CloudDiscovery cdiscovery = null;
        Image img = new Image();
        ThumbsManager thumbmgr = null;
        System.Windows.Threading.DispatcherTimer mobile_softap_conn_timeout = null;
        string mDeviceId = string.Empty;
        string mGroupTime = string.Empty;

        switch_net_tip tipwnd = null;
        string m_lastClientIp = string.Empty;

        MassData.MassDataManager m_mass = null;

        public MainWindow()
        {
            InitializeComponent();
            tipwnd = new switch_net_tip(this); 

            this.VersionInfo.Text = "(v" + UpdateManager.GetCurrentVersion() +")" ;
            m_httpServsDit = new Dictionary<EndPoint, HTTPClientService>();
            cdiscovery = new CloudDiscovery();
            if (!CreateBaseFolder())
            {
                return;
            }

            if (Utils.ISEnEdition())
            {
                LoadStyleDynamically(@"Style_en-US.xaml");
            }
            else
            {
                LoadStyleDynamically(@"Style_zh-CN.xaml");
            }

            CheckVersionUpdate(true);
            hotspot = new EPHotspotLib.EasyPlusHotspot();
            //set own computer name
            this.text_ComputerName.Text = Utils.getLocalComputerName();
            this.OfficialWebsite.Text = Utils.getLocalComputerName();
            //set network name
            Thread thread = new Thread(() => {
                while (!isClosing)
                {
                    this.Dispatcher.Invoke(new Action(delegate
                    {
                        setCurNetwork();
                    }));
                    Thread.Sleep(3000);
                }
            });
            thread.Start();

            //EP softAp start
            Thread thread1 = new Thread(() =>
            {
                try
                {
                    int result = hotspot.StartSoftAP(Utils.GetSoftAPContractName(), "Aa123456", Process.GetCurrentProcess().Id, 0);
                    if(result != 0)
                        uploadMassData(GlobalDef.MassDataItems.Mass_Softap_init_fail_times, "1");
                }
                catch (Exception e)
                {
                    Console.WriteLine(e.Message);
                    App.WriteLog("start softap error: " + e.Message, Log.MsgType.Error);
                    uploadMassData(GlobalDef.MassDataItems.Mass_Softap_init_fail_times, "1");
                }
            });
            thread1.Start();

            //shareit foundation
            Thread t1 = new Thread(() =>
            {
              cdiscovery.Advertise();
            });
            t1.Start();
            
            //build broadcast content
            StringWriter str = new StringWriter();
            JsonWriter jw = new JsonTextWriter(str);
            jw.Formatting = Formatting.Indented;
            jw.WriteStartObject();
            jw.WritePropertyName("cmd");
            jw.WriteValue("KSendPCInfo");

            jw.WritePropertyName("name");
            jw.WriteValue(Utils.getLocalComputerName());

            jw.WritePropertyName("version");
            jw.WriteValue("1.1");

            jw.WritePropertyName("softAp");
            jw.WriteValue(Utils.GetSoftAPContractName());
            jw.WriteEndObject();


            //start udp cmd server
            asudp = NetworkServiceFactory.GenerateUDPServer("0.0.0.0", GlobalDef.UDPCMDPIPE_PORT, this);
            if (asudp != null && asudp.init())
                asudp.start();
            else
            {
                App.WriteLog("Udp server init error, try again in 5secs", Log.MsgType.Information);
                Thread.Sleep(5000);
                if (asudp != null && asudp.init())
                    asudp.start();
                else
                {
                    App.WriteLog("Udp server down", Log.MsgType.Information);
                }

            }

            //start broadcast service
            broadcast = NetworkServiceFactory.GenerateUDPBroadcastService(GlobalDef.BROADCAST_PORT, str.ToString());
            if (broadcast != null && broadcast.init())
                broadcast.start();
            else
                App.WriteLog("Broadcast service down", Log.MsgType.Information);

            //start scan softap service
            scanSoftAp = NetworkServiceFactory.GenerateScanSoftApService(this);
            scanSoftAp.start();

            thumbmgr = new ThumbsManager();
            this.DataContext = thumbmgr;

            mobile_softap_conn_timeout = new System.Windows.Threading.DispatcherTimer();
            mobile_softap_conn_timeout.Tick += new EventHandler(tm_Tick);
            mobile_softap_conn_timeout.Interval = TimeSpan.FromSeconds(30);

            //mass data logistic
            m_mass = new MassData.MassDataManager();
            m_mass.start();

            //upload start times mass data
            uploadMassData(GlobalDef.MassDataItems.Mass_Start_times, "1");
        }


        private void ResetUIState()
        {
            this.TransferCompletedIndicator.Visibility = Visibility.Collapsed;
            this.ProgressIndicator.Visibility = Visibility.Visible;
           // this.ThumbnailCanvas.Visibility = Visibility.Visible;
            this.text_TransState1.Text = Properties.Resources.CS_TRANS_PREPARING;
            this.text_TransState.Text = String.Format(Properties.Resources.CLEANSPACE_TRANSFER_STATUS, 0);

        }

        public void LoadStyleDynamically(string styleUrl)
        {
            System.Collections.ObjectModel.Collection<ResourceDictionary> appResources = App.Current.Resources.MergedDictionaries;
            ResourceDictionary skin = new ResourceDictionary();
            Uri skinUri = new Uri(styleUrl, UriKind.RelativeOrAbsolute);
            skin.Source = skinUri;
            appResources.Add(skin);
        }

        private bool CreateBaseFolder()
        {
            bool ret = true;
            string desktop = Environment.GetFolderPath(Environment.SpecialFolder.DesktopDirectory);
            m_basefolder = desktop + "\\" + Properties.Resources.CLEANSPACE_APPNAME;
            if (!Directory.Exists(m_basefolder))
            {
                try
                {
                    Directory.CreateDirectory(m_basefolder);
                    if (Directory.Exists(m_basefolder))
                    {
                        //set folder image
                        BitmapImage image = new BitmapImage();
                        image.BeginInit();
                        image.UriSource = new Uri(@"pack://application:,,,../images/folder.png");
                        image.EndInit();
                        PngBitmapEncoder encoder = new PngBitmapEncoder();
                        encoder.Frames.Add(BitmapFrame.Create(image));

                        using (var filestream = new FileStream(m_basefolder + "\\folder.jpg", FileMode.Create))
                            encoder.Save(filestream);
                        Utils.SetFileHidden(m_basefolder + "\\folder.jpg");
                        //set desktop.ini
                        string s = @"[ExtShellFolderViews]
{BE098140-A513-11D0-A3A4-00C04FD706EC}={BE098140-A513-11D0-A3A4-00C04FD706EC}
[{BE098140-A513-11D0-A3A4-00C04FD706EC}]
Attributes=1
IconArea_Image=folder.png
[.ShellClassInfo]
ConfirmFileOp=0 "; 
                        byte[] bytes = Encoding.Default.GetBytes(s);
                        FileStream fs = new FileStream(m_basefolder + "\\desktop.ini", FileMode.Create);
                        fs.Write(bytes,0,bytes.Length);
                        fs.Close();
                        Utils.SetFileHidden(m_basefolder + "\\desktop.ini");
                    }
                }
                catch (Exception e)
                {
                    Console.WriteLine("create folder " + m_basefolder + " exception" + e.Message);
                    ret = false;
                }
            }
            return ret;
        }

        #region udp service callback 

        public void onUDPRecvMessage(System.Net.EndPoint client, int byteread, string message)
        {
            string clientip = client.ToString().Substring(0, client.ToString().IndexOf(':'));

 
          //  App.WriteLog("recv msg from " + client.ToString() + " : " + message, Log.MsgType.Information);

            //parse json message protocol
            //if start download cmd
            JObject jobject = null;
            try
            {
                jobject = JObject.Parse(message);
            }
            catch { 
                return;
            }
            if (jobject == null) return;


            string cmd = jobject["cmd"].ToString();
            if (cmd != null && cmd != "")
            {
                if (cmd.Equals("kTestAlive", StringComparison.CurrentCultureIgnoreCase))
                {
                    Dictionary<string, string> dic = new Dictionary<string, string>();
                    dic.Add("cmd", "kAlive");
                    dic.Add("name", Utils.getLocalComputerName());
                    String state = "kReady";
                    if (m_httpServsDit != null)
                    {
                        if (m_httpServsDit.Count > 0)
                            state = "kBusy";
                    }
                    dic.Add("state", state);
                    string jsonstr = BuildJsonString(dic);
                    asudp.SendMessage(client, jsonstr);
                }


                if (cmd.Equals("KStartDownload", StringComparison.CurrentCultureIgnoreCase))
                {
                    if (m_httpServsDit != null)
                    {
                        mobile_softap_conn_timeout.Stop();
                        bool forceRestart = false;
                        string deviceid = jobject["deviceid"].ToString();
                        string groupTime = jobject["groupTime"].ToString();
                        if (!String.IsNullOrEmpty(mDeviceId) && !String.IsNullOrEmpty(mGroupTime))
                        {
                            if (mDeviceId.Equals(deviceid, StringComparison.CurrentCultureIgnoreCase) &&
                                mGroupTime.Equals(groupTime, StringComparison.CurrentCultureIgnoreCase))
                            {
                                return;
                            }
                            else if (mDeviceId.Equals(deviceid, StringComparison.CurrentCultureIgnoreCase) &&
                                !mGroupTime.Equals(groupTime, StringComparison.CurrentCultureIgnoreCase))
                            {
                                forceRestart = true;
                                App.WriteLog("deviceid: " + deviceid + " request download again with a diff timestamp", Log.MsgType.Information);
                            }
                        }


                        Dictionary<string, string> dic = new Dictionary<string, string>();
                        dic.Add("cmd", "KStartDownload");
                        dic.Add("name", Utils.getLocalComputerName());
                        String state = "kReady";
                        if (m_httpServsDit != null)
                        {
                            if (m_httpServsDit.Count > 0)
                                state = "kBusy";
                        }
                        dic.Add("state", state);
                        string jsonstr = BuildJsonString(dic);
                        if (!forceRestart)
                            asudp.SendMessage(client, jsonstr);
                        if (state.Equals("kBusy", StringComparison.CurrentCultureIgnoreCase))
                        {
                            if (forceRestart)
                            {
                                //isForceRestart = true;
                                StopAllSessions();
                                while (m_httpServsDit.Count > 0)
                                    Thread.Sleep(1);
                                dic["state"] = "kReady";
                                jsonstr = BuildJsonString(dic);
                                asudp.SendMessage(client, jsonstr);
                                App.WriteLog("deivce: " + deviceid + " force start ...go..", Log.MsgType.Information);
                               // isForceRestart = false;
                            }
                            else
                            {
                                return;
                            }
                        }
                        mDeviceId = deviceid;
                        mGroupTime = groupTime;
                    }

                    if (!SetMobileSoftapHolder("downlaod cmd"))
                    {
                        return;
                    }

                    //mass data trans times
                    uploadMassData(GlobalDef.MassDataItems.Mass_Transmit_times, "1");

                    this.Dispatcher.Invoke(new Action(delegate {
                        this.ResetUIState();
                    }));

                    string filelisturl = jobject["path"].ToString();
                    if (filelisturl == null || filelisturl.Equals(""))
                    {
                        Console.WriteLine("can not get url of image files list");
                        App.WriteLog("can not get url of image files list", Log.MsgType.Information);
                        return;
                    }

                    //reunit filelist url 
                    filelisturl = "http://" + clientip + ":" + GlobalDef.REMOTEHTTPSERVER_PORT +"/download" + filelisturl;
                    m_lastClientIp = clientip;

                    //make sure the base foler exsit
                    if (m_basefolder == null || !Directory.Exists(m_basefolder))
                    {
                        if(!CreateBaseFolder())
                            return;
                    }

                    //generate special device id folder
                    string deviceid1 = jobject["deviceid"].ToString();
                    string devicename = jobject["devicename"].ToString();
                    string foldername = "\\" + Utils.ConvertUTF8(devicename) + "_" + deviceid1;
                    if(deviceid1 == null || deviceid1 == "" || devicename == null || devicename == "")
                    {
                        foldername = "\\UnkonwnDevcie_" + DateTime.Now.Ticks;
                    }
                    string deviceSavePath = m_basefolder + foldername;

                    HTTPClientService httpclient = NetworkServiceFactory.GenerateHTTPService(filelisturl, deviceSavePath, client, null, this);
                    if (httpclient!=null)
                    {
                        this.Dispatcher.Invoke(new Action(delegate { 
                            this.ConnectedIndicator.Visibility = Visibility.Visible;
                            this.NoConnIndicator.Visibility = Visibility.Collapsed;
                            this.PhoneImg.Source = new BitmapImage(new Uri(@"images/img_phone_white.png", UriKind.Relative));
                            this.PhoneImgGif.Visibility = Visibility.Collapsed;
                            this.PhoneImg.Visibility = Visibility.Visible;
                            this.text_PhoneName.Text = Utils.ConvertUTF8(devicename);
                            this.NoConnCover.Visibility = Visibility.Collapsed;
                            this.ImgBrowserView.Visibility = Visibility.Visible;
                            if (!thumbmgr.reset(deviceSavePath))
                            {
                                App.WriteLog("thumbmgr reset&init fail", Log.MsgType.Error);
                            }
                        }));

                        httpclient.start();
                        if (m_httpServsDit != null)
                        {
                            if (isClosing)
                            {
                                httpclient.stop();
                            }
                            else
                                m_httpServsDit.Add(client, httpclient);
                        }
                    }

                }

                if (cmd.Equals("KStopDownload", StringComparison.CurrentCultureIgnoreCase))
                {
                    StopAllSessions();
                }

                if (cmd.Equals("KDelFile", StringComparison.CurrentCultureIgnoreCase))
                {

                }

            }

        }
        #endregion udp service callback 

        #region http service callback
        public void onStartingDownload(HTTPClientService http, EndPoint server, int imgCount, UIElement ui)
        {
            App.WriteLog(server.ToString()+" start downloading. total imgs: "　+ imgCount, Log.MsgType.Information);
            this.Dispatcher.Invoke(new Action(delegate
            {
                string trans_status = String.Format(Properties.Resources.CLEANSPACE_TRANSFER_STATUS2, imgCount);
                this.text_TransState1.Text = trans_status;
                trans_status = String.Format(Properties.Resources.CLEANSPACE_TRANSFER_STATUS, 1);
                this.text_TransState.Text = trans_status;
                this.RecieveStatus.Text = Properties.Resources.CS_PC_RECEIVING;
            }));
        }


        public void onHttpClientPreTransVideo(HTTPClientService http, EndPoint server, UIElement ui)
        {
            this.Dispatcher.Invoke(new Action(delegate
                {
                    Image thumbnail = ui as Image;

                    if (thumbnail != null)
                    {
                        thumbnail.Source = new BitmapImage(new Uri(@"./images/video.png", UriKind.Relative));
                    }
                    
                }));
        }

        public void onHttpFinishloadingOne(HTTPClientService http, EndPoint server, RemotePhotoInfo rpi, string path, int status, UIElement ui)
        {
            lock (locker)
            {
                if (status == GlobalDef.Status.S_DownloadedSuccessful)
                {
                    string tempurl = rpi.fileUrl.Replace(":" + GlobalDef.REMOTEHTTPSERVER_PORT + "/download", ":" + GlobalDef.REMOTEHTTPSERVER_PORT + "/downloadcomplete");

                    App.WriteLog(server.ToString() + " download img:  " + rpi.fileUrl + " success", Log.MsgType.Information);
                    this.Dispatcher.Invoke(new Action(delegate
                    {
                        string trans_status = String.Format(Properties.Resources.CLEANSPACE_TRANSFER_STATUS2, http.getTotalImgCount());
                        this.text_TransState1.Text = trans_status;
                        trans_status = String.Format(Properties.Resources.CLEANSPACE_TRANSFER_STATUS, http.getDownloadedImgCount() >= http.getTotalImgCount() ? http.getDownloadedImgCount() : http.getDownloadedImgCount() + 1);
                        this.text_TransState.Text = trans_status;
                        Image thumbnail = ui as Image;
                        if (File.Exists(path) && (Utils.IsPicture(path) || Utils.IsVideo(path)))
                        {
                            thumbmgr.addNewImg(path);
                            if (thumbnail != null)
                            {
                                try
                                {
                                    //BinaryReader binReader = new BinaryReader(File.Open(path, FileMode.Open));
                                    //FileInfo fileInfo = new FileInfo(path);
                                    //byte[] bytes = binReader.ReadBytes((int)fileInfo.Length);
                                    //binReader.Close();
                                    //BitmapImage bitmap = new BitmapImage();
                                    //bitmap.BeginInit();
                                    //bitmap.StreamSource = new MemoryStream(bytes);
                                    //bitmap.EndInit();
                                    thumbnail.Source = Utils.GetThumbnail(path, 48, 48);
                                }
                                catch { }
                            }
                        }
                    }));


                    //new thread to post delete command for the damn server may timeout
                    int result = DownloadCompleteRequest(tempurl);
                    DateTime now = DateTime.UtcNow;
                    while (result == -1)
                    {
                        TimeSpan ts = DateTime.UtcNow - now;
                        if (ts.TotalSeconds >= 175 || isClosing || !http.isStarted)
                        {
                            StopAllSessions();
                            break;
                        }
                        result = DownloadCompleteRequest(tempurl);
                    }
                    //Thread t = new Thread(() => {   });
                    //t.Start();
                    //t.Join();
                }
                else
                {
                    App.WriteLog(server.ToString() + " download img:  " + rpi.fileUrl + " fail. retry....", Log.MsgType.Information);
                }
            }
        }


        public void onHttpClientSelfDestroy(HTTPClientService http, EndPoint server, int status, UIElement ui)
        {
            lock (locker1)
            {
                if (status != GlobalDef.Status.S_HTTPDownladerInitSuccess)
                    uploadMassData(GlobalDef.MassDataItems.Mass_Transmit_fail_times, "1");

                this.m_httpServsDit.Remove(server);
                softap_conn = false;
                scanSoftAp.Disconnect();

                mDeviceId = string.Empty;
                mGroupTime = string.Empty;

                ReleaseMobileSoftapHolder();
                try
                {
                    this.Dispatcher.Invoke(new Action(delegate
                    {
                        if (tipwnd != null && tipwnd.IsLoaded)
                            tipwnd.Hide();
                        string alldone_status = String.Format(Properties.Resources.CS_TRANS_COMPLETE_RESULT, http.getDownloadedImgCount());
                        this.text_TransState1.Text = alldone_status;
                        this.RecieveStatus.Text = Properties.Resources.CS_PC_READY;
                        this.TransferCompletedIndicator.Visibility = Visibility.Visible;
                        this.ProgressIndicator.Visibility = Visibility.Collapsed;
                       // this.ThumbnailCanvas.Visibility = Visibility.Hidden;

                    }));
                }
                catch
                {
                    //UI destroyed. ignore..
                }
                App.WriteLog(server.ToString() + " done. success: "+ http.getDownloadedImgCount() + " failed: " + http.getDownloadFailedImgCount(), Log.MsgType.Information);
            }
        }

        #endregion http service callback

        #region Mobile softap callback

        string mobile_softap_name = string.Empty;
        public bool CounterpartDiscovered(string name)
        {
            bool ret = true;

            if (!string.IsNullOrEmpty(name) && name.Equals(mobile_softap_name, StringComparison.CurrentCultureIgnoreCase))
                return true;

            
            //if (m_httpServsDit != null)
            //{
            //    if (m_httpServsDit.Count > 0)
            //        ret = false;
            //    else
            //    {
            ret = SetMobileSoftapHolder("mobile soft ap detector");
            //       if (b)
            //       {
            //           ret = true;
            //       }
            //       else
            //       {
            //           ret = false;
            //       }
            //    }
            //}

            if (ret && string.IsNullOrEmpty(mobile_softap_name))
            {
                mobile_softap_name = name;
            }

            return ret;
        }

        void tm_Tick(object sender, EventArgs e)
        {
            mobile_softap_conn_timeout.Stop();
            //scanSoftAp.Disconnect();
            //ConnFailed();
            if (m_httpServsDit.Count < 1)
            {
                this.Dispatcher.Invoke(new Action(delegate
                {
                    if (this.WindowState != System.Windows.WindowState.Minimized)
                    {
                        tipwnd.Hide();
                    }
                }));
            }
        }

        
        public void Connected2Counterpart()
        {
            uploadMassData(GlobalDef.MassDataItems.Mass_Use_Softap_times, "1");
            ReleaseMobileSoftapHolder();
            softap_conn = true;
            mobile_softap_conn_timeout.Start();
            this.Dispatcher.Invoke(new Action(delegate
            { 
                if (this.WindowState != System.Windows.WindowState.Minimized)
                {
                    tipwnd.Top = this.Top + 150;
                    tipwnd.Left = this.Left + 365;
                    tipwnd.Show();
                }  
            }));
            
        }

        public void ConnFailed()
        {
            uploadMassData(GlobalDef.MassDataItems.Mass_Mobile_Softap_conn_fail_times, "1");
            ReleaseMobileSoftapHolder();
            softap_conn = false;
            mobile_softap_name = "";
        }
        #endregion
        //////////////////////////////////////////////////////////
                
        private void Window_Closed(object sender, CancelEventArgs e)
        {
            if (m_httpServsDit.Count > 0)
            {
                if (MessageBox.Show(Properties.Resources.CS_CONFIRM_QUIT, Properties.Resources.CLEANSPACE_APPNAME,MessageBoxButton.YesNo) != MessageBoxResult.Yes)
                {
                    e.Cancel = true;
                    return;
                }
            }

            isClosing = true;
            tipwnd.Close();
            if (broadcast != null)
                broadcast.stop();
            if (asudp != null)
            {
                asudp.stop();
                asudp.finish();
            }
            if(scanSoftAp != null)
                scanSoftAp.stop();

            //upload close times mass data
            uploadMassData(GlobalDef.MassDataItems.Mass_Close_times, "1");
            Thread.Sleep(10);

            if (m_mass != null)
                m_mass.stop();

            StopAllSessions();
            cdiscovery.StopAdvertise();
            try
            {
                int result = hotspot.StopSoftAP(Process.GetCurrentProcess().Id);
                if (result != 0)
                    uploadMassData(GlobalDef.MassDataItems.Mass_Softap_Stop_fail_times, "1");
            }
            catch(Exception ex) {
                App.WriteLog("stop softap error: " + ex.Message, Log.MsgType.Error);
                uploadMassData(GlobalDef.MassDataItems.Mass_Softap_Stop_fail_times, "1");
            }
        }

        private int DownloadCompleteRequest(string url)
        {
            int ret = 0; //ok flag
            DateTime now = DateTime.UtcNow;
            try
            {
                HttpWebRequest request = WebRequest.Create(url) as HttpWebRequest;
                request.Timeout = ClearSpace.GlobalDef.HTTPTIMEOUT;
                HttpWebResponse response = request.GetResponse() as HttpWebResponse;
                while (request == null || response == null || response.StatusCode != HttpStatusCode.OK)
                {
                    request = WebRequest.Create(url) as HttpWebRequest;
                    request.Timeout = ClearSpace.GlobalDef.HTTPTIMEOUT;
                    response = request.GetResponse() as HttpWebResponse;
                }

                //if (response != null && response.StatusCode == HttpStatusCode.OK)
                //    App.WriteLog(url + " upload download successful state done", Log.MsgType.Information);
            }
            catch (Exception e){
                Console.WriteLine(e.Message);
                App.WriteLog(e.Message, Log.MsgType.Error);
                ret = -1; //error flag
            }

            return ret;
        }



        private bool SetMobileSoftapHolder(string id)
        {
            
            lock (mobile_softap_locker)
            {
                if (mobile_softap_holder)
                    return false;
               App.WriteLog(id + " get locker", Log.MsgType.Information);
               mobile_softap_holder = true;
               return true;
            }
        }

        private void ReleaseMobileSoftapHolder()
        {
            App.WriteLog("release locker ", Log.MsgType.Information);
            mobile_softap_holder = false;
        }

        private void CheckVersionUpdate(bool bShowTip, bool positiveCheck = false, bool isDebug = false)
        {
            UpdateManager.CheckUpdate(new Update_CallBack((tagUpdateInfo updateInfo) =>
            {
                if (updateInfo.hasNewVer)
                {
                    if (UpdateManager.DownloadNewVerion(updateInfo))
                    {
                        if (!positiveCheck)
                            Thread.Sleep(2100);
                        this.Dispatcher.Invoke(new Action(delegate
                        {
                            if (bShowTip)
                            {
                                //UpdateManager.ShowUpdateDialog(updateInfo);//show something to let uer select to continue update
                                this.Dispatcher.Invoke(new Action(delegate {
                                    this.m_updateinfo = updateInfo;
                                    this.ShowUpdatePanel(true);
                                }));
                            }
                        }));
                    }
                }
                else
                {
                    if (positiveCheck == true) //if prositive check , inform user there is no new version available
                    {
                        this.Dispatcher.Invoke(
                            new Action(delegate
                            {
                                double left = this.Left;
                                this.Left = left - 10;
                                Thread.Sleep(50);
                                this.Left = left;
                                Thread.Sleep(50);
                                this.Left = left + 10;
                                Thread.Sleep(50);
                                this.Left = left;
                                Thread.Sleep(50);
                                this.Left = left - 10;
                                Thread.Sleep(50);
                                this.Left = left;
                                Thread.Sleep(50);
                                this.Left = left + 10;
                                Thread.Sleep(50);
                                this.Left = left;
                            }));

                    }
                }

            }
            ), isDebug);
        }

        private void setCurNetwork()
        {
            try
            {
                bool wifi = true;
                string nn = Utils.getWifiSsid();
                if (nn == null)
                {
                    wifi = false;
                    nn = Utils.getUpEthernet();
                }
                this.Cur_Network.Text = Properties.Resources.CS_CURRENT_NETWORK + (nn != null ? nn : Properties.Resources.CS_NO_NETWORK);
                if (nn != null)
                {
                    if (wifi)
                    {
                        //set network icon to wifi
                        this.wifiIcon.Visibility = Visibility.Visible;
                        this.lineIcon.Visibility = Visibility.Collapsed;
                    }
                    else
                    {
                        //set network icon to local network
                        this.wifiIcon.Visibility = Visibility.Collapsed;
                        this.lineIcon.Visibility = Visibility.Visible;
                    }
                }
                else
                {
                    //collapse network icon
                    this.wifiIcon.Visibility = Visibility.Collapsed;
                    this.lineIcon.Visibility = Visibility.Collapsed;
                }
            }
            catch { }
            return ;
        }

        private void StopAllSessions()
        {
            lock (locker1)
            {
                if (m_httpServsDit != null)
                {
                    foreach (KeyValuePair<EndPoint, HTTPClientService> o in m_httpServsDit)
                    {
                        ((HTTPClientService)o.Value).stop();
                    }
                }
            }
        }

        public void agressiveStop()
        {
            uploadMassData(GlobalDef.MassDataItems.Mass_User_Cancel_times, "1");


            if (this.m_httpServsDit.Count > 0)
            {
                if (!string.IsNullOrEmpty(m_lastClientIp))
                {
                    string url = "http://" + m_lastClientIp + ":" + GlobalDef.REMOTEHTTPSERVER_PORT + "/downloadstopbypc";
                    try
                    {
                        HttpWebRequest request = WebRequest.Create(url) as HttpWebRequest;
                        //request.Timeout = ClearSpace.GlobalDef.HTTPTIMEOUT;
                        HttpWebResponse response = request.GetResponse() as HttpWebResponse;
                    }
                    catch { }
                }
                StopAllSessions();
            }
            else
            {
                tipwnd.Hide();
                softap_conn = false;
                scanSoftAp.Disconnect();
            }
        }


        public void uploadMassData(string key , string value)
        {
            Dictionary<string, string> mass = new Dictionary<string, string>();
            mass.Add(key, value);
            if(m_mass != null)
                m_mass.addData(mass);
        }
             

        private string BuildJsonString(Dictionary<string, string> arg0)
        {
            StringWriter str = new StringWriter();
            JsonWriter jw = new JsonTextWriter(str);
            jw.Formatting = Formatting.Indented;
            jw.WriteStartObject();
            foreach (KeyValuePair<string, string> j in arg0)
            {
                jw.WritePropertyName(j.Key);
                jw.WriteValue(j.Value);
            }
            jw.WriteEndObject();

            return str.ToString();
        }
/**********************************************************************************************/
        private void Close_Btn_Click(object sender, RoutedEventArgs e)
        {
            this.Close();
        }

        private void Min_Btn_Click(object sender, RoutedEventArgs e)
        {
            this.WindowState = WindowState.Minimized;
        }

        private void Win_MouseLeftButtonDown(object sender, MouseButtonEventArgs e)
        {
            if (e.GetPosition(this).Y < 150)
            {
                DragMove();
                tipwnd.Top = this.Top + 150;
                tipwnd.Left = this.Left + 365;
            }
        }


        private void OpenFile_MouseEnter(object sender, MouseEventArgs e)
        {
            this.Cursor = Cursors.Hand;
        }

        private void OpenFile_MouseLeave(object sender, MouseEventArgs e)
        {
            this.Cursor = Cursors.Arrow;
        }

        private void OpenFile_Click(object sender, MouseButtonEventArgs e)
        {
            if (sender.Equals(this.OpenFileBtn))
            {
                bool readyopen = true;
                if (m_basefolder == null || !Directory.Exists(m_basefolder))
                {
                    if (!CreateBaseFolder())
                        readyopen = false;
                }

                if (readyopen)
                    System.Diagnostics.Process.Start("explorer.exe", m_basefolder);
            }
            else if (sender.Equals(this.OfficialWebsite))
            {
                Utils.ProcessStart("http://" + OfficialWebsite.Text, "");
            }
        }

        private void ShowUpdatePanel(bool show)
        {
            this.UpdatePanel.Visibility = (show?Visibility.Visible:Visibility.Hidden);
        }

        private void UpdateYes_Click(object sender, RoutedEventArgs e)
        {
            ShowUpdatePanel(false);
            UpdateManager.LauchNewInstaller(m_updateinfo);
        }

        private void UpdateNo_Click(object sender, RoutedEventArgs e)
        {
            ShowUpdatePanel(false);
        }

        private void Image_MouseUp_1(object sender, MouseButtonEventArgs e)
        {
            if (NativeMethodsCall.GetAsyncKeyState(NativeMethodsCall.VK_LCONTROL) < 0)
            {
                CheckVersionUpdate(true, true, true);
            }
        }

        private void Window_Activated_1(object sender, EventArgs e)
        {
            if (tipwnd != null && tipwnd.IsLoaded)
            {
                tipwnd.Activate();
                this.Activate();
            }
        }

        private void Window_StateChanged_1(object sender, EventArgs e)
        {
            MainWindow mw = sender as MainWindow;
            if (mw != null && tipwnd != null && tipwnd.IsLoaded)
            {
                if (mw.WindowState == System.Windows.WindowState.Minimized)
                {
                    tipwnd.WindowState = System.Windows.WindowState.Minimized;
                }
                if (mw.WindowState == System.Windows.WindowState.Normal)
                {
                    tipwnd.WindowState = System.Windows.WindowState.Normal;
                }
            }
        }

    }
}
