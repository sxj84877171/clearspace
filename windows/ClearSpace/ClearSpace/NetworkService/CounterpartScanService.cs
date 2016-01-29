using NativeWifi;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Runtime.InteropServices;
using System.Text;
using System.Threading;
using Timer = System.Timers.Timer;
namespace ClearSpace.NetworkService
{
    class CounterpartScanService
    {
        CounterpartScanServiceCallback callback = null;
        bool m_started = false;
        bool m_connected = false;
        string m_cur_profile = string.Empty;
        string m_cur_wifi_ssid = string.Empty;
        private readonly Timer connTimer;
        bool stop_scan = false;
        bool m_set_originate_wifi = false;
        WlanClient m_wlan = null;
        string m_last_mobile_softap_profile = string.Empty;
        WlanClient.WlanInterface m_wlanInterface = null;
        object locker = new object();
        ConnStatus m_connstatus =  ConnStatus.disconn; 
        enum ConnStatus
        {

            connecting = 0,
            connected = 1,
            disconn = 2
        }
        public bool isStarted
        {
            get { return m_started; }
        }

        public CounterpartScanService(CounterpartScanServiceCallback cb)
        {
            this.callback = cb;
            this.connTimer = new Timer(20000D);
            this.connTimer.Elapsed += (sender, args) => {
                if (!m_connected)
                {
                    try
                    {
                        m_wlanInterface.Scan();
                        m_wlanInterface.DeleteProfile(m_cur_profile);
                        App.WriteLog("Connect error:  (" + m_cur_profile + ") release token and delelte profile anyway.", Log.MsgType.Information);
                    }
                    catch 
                    {
                    }
                    if (callback != null)
                        callback.ConnFailed();
                }
                stop_scan = false;
                connTimer.Stop();
            
            };
        }

        public void start()
        {
            if (m_started) return;
            m_started = true;

            m_wlan = new WlanClient();
            foreach (WlanClient.WlanInterface wlanInterface in m_wlan.Interfaces)
            {
                if (wlanInterface.NetworkInterface != null && wlanInterface.NetworkInterface.NetworkInterfaceType == System.Net.NetworkInformation.NetworkInterfaceType.Wireless80211)
                {
                    m_wlanInterface = wlanInterface;
                }
            }

            if (m_wlanInterface == null) return;
            Thread threadHand1 = new Thread(() =>
            {
            
                while (m_started)
                {
                    if (stop_scan) continue;
                    string str = Utils.GetSoftAPContractName();
                    str = str.Substring(str.IndexOf('_'));

                    string tempwifi = GetWifiSSID();
                    if (string.IsNullOrEmpty(tempwifi) || !tempwifi.Contains(str))
                    {
                        m_connected = false;
                    }

                    //scan all ssid try to find countpart ssid
                    List<Wlan.WlanAvailableNetwork> ssids = scanSsid();
                    for(int i = 0; i < ssids.Count; i ++)
                    {
                        Wlan.WlanAvailableNetwork avNetwork = ssids[i];
                        string tempssid = new String(Encoding.ASCII.GetChars(avNetwork.dot11Ssid.SSID, 0, (int)avNetwork.dot11Ssid.SSIDLength));

                        //if (string.IsNullOrEmpty(m_cur_profile))
                        //{
                        if (tempssid.Contains(str) && !tempssid.Equals(m_last_mobile_softap_profile,StringComparison.CurrentCultureIgnoreCase))//"SW2_NETGEAR-5G"
                            {
                                if (callback != null)
                                {
                                    if (!m_connected && callback.CounterpartDiscovered(tempssid))
                                    {
                                        m_cur_profile = tempssid;
                                        //connect
                                        ConnectCouterpart(avNetwork);
                                        stop_scan = true;
                                        connTimer.Start();
                                    }
                                }

                                break;
                            }
                        //}
                        //else
                        //{
                        //    if (tempssid.Contains(m_cur_profile))//"SW2_NETGEAR-5G"
                        //    {
                        //        if (callback != null)
                        //        {
                        //            if (!m_connected && callback.CounterpartDiscovered(m_cur_profile))
                        //            {
                        //                //connect
                        //                ConnectCouterpart(avNetwork);
                        //                stop_scan = true;
                        //                connTimer.Start();
                        //            }
                        //        }

                        //        break;
                        //    }
                        //}


                        if (i >= (ssids.Count-1))
                        {
                            try
                            {
                                m_wlanInterface.Scan();
                            }
                            catch
                            {
                            }

                            if(m_started)
                                Thread.Sleep(2000);

                            if (!string.IsNullOrEmpty(m_cur_profile))
                            {
                                m_cur_profile = "";
                                lock (locker)
                                    m_connected = false;

                                //callback clean mobile softap context
                                if (callback != null)
                                    callback.ConnFailed();

                                if (!string.IsNullOrEmpty(m_cur_wifi_ssid))
                                {
                                    m_wlanInterface.Connect(Wlan.WlanConnectionMode.Profile, Wlan.Dot11BssType.Infrastructure, m_cur_wifi_ssid);
                                }
                            }
                        }
                    }
                    
                }
            });
            threadHand1.Start();
        }

        public void stop()
        {
            m_started = false;
            m_wlan.Dispose();
            connTimer.Stop();
        }

        private List<Wlan.WlanAvailableNetwork> scanSsid()
        {

            List<Wlan.WlanAvailableNetwork> ssidLst = new List<Wlan.WlanAvailableNetwork>();
            try
            {

                Wlan.WlanAvailableNetwork[] networks = m_wlanInterface.GetAvailableNetworkList(Wlan.WlanGetAvailableNetworkFlags.IncludeAllAdhocProfiles);
                ssidLst = networks.ToList<Wlan.WlanAvailableNetwork>();
            }
            catch(Exception e)
            {
                Console.WriteLine(e.Message);
                App.WriteLog("CountpartScanSerivce Scanssid error: " + e.Message, Log.MsgType.Error);
            }

            return ssidLst;
        }


        private bool ConnectCouterpart(Wlan.WlanAvailableNetwork mobileSoftap)
        {
            bool ret = true;
            string targetProfilename = string.Empty;
            string temp_m_cur_wifi_ssid = GetWifiSSID();
            if (!string.IsNullOrEmpty(temp_m_cur_wifi_ssid))
            {
                string str = Utils.GetSoftAPContractName();
                str = str.Substring(str.IndexOf('_'));
                if (!temp_m_cur_wifi_ssid.Contains(str) && !m_set_originate_wifi)
                {
                    m_cur_wifi_ssid = temp_m_cur_wifi_ssid;
                    m_set_originate_wifi = true;
                }
            }
            try
            {
                if ((mobileSoftap.flags & Wlan.WlanAvailableNetworkFlags.Connected) != Wlan.WlanAvailableNetworkFlags.Connected)
                {
                    if ((mobileSoftap.flags & Wlan.WlanAvailableNetworkFlags.HasProfile) == Wlan.WlanAvailableNetworkFlags.HasProfile)
                    {
                        targetProfilename = mobileSoftap.profileName;
                    }
                    else
                    {
                        //set profile
                        string temprofile = @"<?xml version=""1.0"" ?>" +
                                        @"<WLANProfile xmlns=""http://www.microsoft.com/networking/WLAN/profile/v1"">" +
                                        "<name>{0}</name>" +
                                        "<SSIDConfig>" +
                                        "<SSID>" +
                                        "<hex>{1}</hex>" +
                                        "<name>{0}</name>" +
                                        "</SSID>" +
                                        "</SSIDConfig>" +
                                        "<connectionType>ESS</connectionType> " +
                                        "<connectionMode>auto</connectionMode> " +
                                        "<MSM>" +
                                        "<security>" +
                                        "<authEncryption>" +
                                        "<authentication>WPAPSK</authentication> " +
                                        "<encryption>AES</encryption> " +
                                        "<useOneX>false</useOneX> " +
                                        "</authEncryption>" +
                                        "<sharedKey>" +
                                        "<keyType>passPhrase</keyType> " +
                                        "<protected>false</protected> " +
                                        "<keyMaterial>Aa123456</keyMaterial> " +
                                        "</sharedKey>" +
                                        "</security>" +
                                        "</MSM>" +
                                        "</WLANProfile>"
                                    ;
                        string tempssid = new String(Encoding.ASCII.GetChars(mobileSoftap.dot11Ssid.SSID, 0, (int)mobileSoftap.dot11Ssid.SSIDLength));
                        string mac = StringToHex(tempssid);
                        string myProfileXML = string.Format(temprofile, tempssid, mac);
                        m_wlanInterface.SetProfile(Wlan.WlanProfileFlags.AllUser, myProfileXML, true);
                        targetProfilename = tempssid;
                    }
                    m_wlanInterface.WlanConnectionNotification += ConnCallback;
                    //connTimer.Start();
                    m_wlanInterface.Connect(Wlan.WlanConnectionMode.Profile, Wlan.Dot11BssType.Infrastructure, targetProfilename);
                }
                
            }
            catch (Exception e)
            {
                ret = false;
                App.WriteLog("connect mobile softap error: " + e.Message, Log.MsgType.Error);
            }
                
            return ret;
        }

        private void ConnCallback(Wlan.WlanNotificationData notifyData, Wlan.WlanConnectionNotificationData connNotifyData)
        {
            lock (locker)
            {
                if (notifyData.notificationCode == 4)
                {
                    string str = Utils.GetSoftAPContractName();
                    str = str.Substring(str.IndexOf('_'));
                    if (connNotifyData.profileName.Contains(str))
                    {
                        App.WriteLog("Connect to mobile successfully release token, ready to donwloand", Log.MsgType.Information);
                        m_cur_profile = connNotifyData.profileName;
                        if (callback != null)
                            callback.Connected2Counterpart();
                        m_connected = true;
                        connTimer.Stop();
                        stop_scan = false;
                        m_wlanInterface.WlanConnectionNotification -= ConnCallback;
                    }
                    else
                    {
                        //m_connected = false;
                        //if (callback != null)
                        //    callback.ConnFailed();
                        //m_wlanInterface.WlanConnectionNotification -= ConnCallback;
                    }

                    //connTimer.Stop();
                }
            }
        }

        public void Disconnect()
        {
            try
            {
                if (m_connected)
                {
                    App.WriteLog("Mobile softap download complete, delete profile and release token", Log.MsgType.Information);
                    m_connected = false;
                    m_wlanInterface.DeleteProfile(m_cur_profile);
                    m_last_mobile_softap_profile = m_cur_profile;
                    m_cur_profile = "";
                    if (!string.IsNullOrEmpty(m_cur_wifi_ssid))
                    {
                        m_wlanInterface.Connect(Wlan.WlanConnectionMode.Profile, Wlan.Dot11BssType.Infrastructure, m_cur_wifi_ssid);
                    }
                }
            }
            catch(Exception e)
            {
                App.WriteLog("Disconnect mobile softap error: " + e.Message, Log.MsgType.Error);
            }
        }

        public string StringToHex(string str)
        {
            StringBuilder sb = new StringBuilder();
            byte[] byStr = System.Text.Encoding.Default.GetBytes(str); //默认是System.Text.Encoding.Default.GetBytes(str)
            for (int i = 0; i < byStr.Length; i++)
            {
                sb.Append(Convert.ToString(byStr[i], 16));
            }

            return (sb.ToString().ToUpper());
        }

        private string GetWifiSSID()
        {
            string ret = null;

            try
            {

                Collection<String> connectedSsids = new Collection<string>();
                if (m_wlanInterface.InterfaceState == Wlan.WlanInterfaceState.Connected)
                {
                    Wlan.Dot11Ssid ssid = m_wlanInterface.CurrentConnection.wlanAssociationAttributes.dot11Ssid;
                    connectedSsids.Add(new String(Encoding.ASCII.GetChars(ssid.SSID, 0, (int)ssid.SSIDLength)));
                }
                   
                if (connectedSsids.Count > 0)
                    ret = connectedSsids[0];
                else
                    ret = null;
                
            }
            catch
            {
                ret = null;
            }

            return ret;
        }
    }
}
