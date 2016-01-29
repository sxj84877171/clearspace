using System;
using System.Collections.Generic;
using System.Net;
using System.Net.Sockets;
using System.ComponentModel;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading;

using Newtonsoft.Json;

using Timer = System.Timers.Timer;
using NativeWifi;
using System.Net.NetworkInformation;


namespace Shareit.Foundation.Discovery
{
    internal class CloudDiscovery : IDiscovery
    {

        private static CloudDiscovery instance;

        private const int UnicastPort = 55526;

        private readonly Timer heartbeatTimer;
        private const double HeartbeatInterval = 15000D;
        private readonly List<IPEndPoint> peers = new List<IPEndPoint>();
        private readonly object l = new object();
        private readonly int[] broadcastIntervals = { 1000, 3000, 7000, 15000, 20000, 30000 };
        private int broadcastIntervalIndex = 0;

        private const int RegisterTimeout = 5000;
        private const int UnregsiterTimeout = 1500;

        private BackgroundWorker searchUnicast;

        private bool isAdvertising;
        private bool isSearching;

        //private string register_url = "http://anyshare.lenovomm.com/relayserver/register?{0}";
        //private string uregister_url = "http://anyshare.lenovomm.com/relayserver/unregister?{0}";


        internal CloudDiscovery()
        {
            this.heartbeatTimer = new Timer(HeartbeatInterval);
            this.heartbeatTimer.Elapsed += (sender, args) => { this.Register(); this.RegisterSoftAp(); };
        }

        internal static CloudDiscovery Instance { get { return instance ?? (instance = new CloudDiscovery()); } }


        private string softapUrl
        {
            get
            {
                return string.Format("http://114.215.236.240:8080/relayserver/addsoftap?{0}", getSoftApArgs());
            }
        }

        private string RegisterUrl
        {
            get
            {
                return string.Format("http://114.215.236.240:8080/relayserver/register?{0}", GetQueries());
                // return string.Format("http://anyshare.lenovomm.com/relayserver/register?{0}", GetQueries());
            }
        }

        private string UnregisterUrl
        {
            get
            {
                return string.Format("http://114.215.236.240:8080/relayserver/unregister?{0}", GetQueries());
                // return string.Format("http://anyshare.lenovomm.com/relayserver/unregister?{0}", GetQueries());
            }
        }

        private string GetDeviceId()
        {
            string ret = null;
            try
            {
                NetworkInterface[] adapters = NetworkInterface.GetAllNetworkInterfaces();
                foreach (NetworkInterface adapter in adapters)
                {
                    if (adapter.NetworkInterfaceType == NetworkInterfaceType.Wireless80211)
                    {
                        ret = adapter.Id;
                        break;
                    }
                }

                if (ret == null)
                {
                    foreach (NetworkInterface adapter in adapters)
                    {
                        if (adapter.NetworkInterfaceType == NetworkInterfaceType.Ethernet)
                        {
                            ret = adapter.Id;
                            break;
                        }
                    }
                }

                if (ret == null)
                    ret = ClearSpace.GlobalDef.deviceId;
            }
            catch 
            {

            }
            return ret;
        }

        private string getSoftApArgs()
        {
            //            device_id:    设备id(必须)
            //ip:                  在局域网内的设备ip(必须)
            //os_type:       设备的os类型(必须 "android", "windows", "ios", "wp"(win phone))
            //net_id:          网络id,无线网络的ssid; (必须) 
            //softap_id:     起的softap的id(必须)

            //var local = CoreLocalDevice.Instance;
            const string Format = "device_id={0}&net_id={1}&ip={2}&softap_id={3}&os_type=windows";

            string str =  ClearSpace.Utils.getWifiSsid();
            var ssid = str == null ? String.Empty : str;

            var iplist = ClearSpace.Utils.getLocalIPAddresses();
            var ips = string.Empty;
            foreach (string s in iplist)
            {
                ips = ips + "," + s;
            }
            ips = ips.Substring(1);

            var deviceid = GetDeviceId();
            var softap_id = ClearSpace.Utils.GetSoftAPContractName();
            return string.Format(Format, deviceid, ssid, ips, softap_id);
            
        }

        private string GetQueries()
        {
            //var local = CoreLocalDevice.Instance;
            const string Format = "net_id={0}&ip={1}&netmask={2}&bssid={3}&gateway={4}&device_id={5}&os_type=pc";

            var ssid = string.Empty;
            var ip = string.Empty;

            var mask2 = string.Empty;
            var bssid = string.Empty;
            var gateway2 = string.Empty;
            var deviceid = GetDeviceId();
            //if (string.IsNullOrEmpty(local.WirelessSSID))
            //{
            //    var ethernets = local.EndPoints.Where(e => e.Type == CoreNetworkType.Ethernet).ToList();
            //    Func<CoreEndPoint, bool> lenovo =
            //        e => e.DnsSuffix.Equals("lenovo.com", StringComparison.OrdinalIgnoreCase);
            //    if (ethernets.Any(lenovo))
            //    {
            //        var e = ethernets.Where(lenovo).ToList()[0];

            //        ssid = "lenovo";
            //        ip = e.EndPoint.Address.ToString();
            //        mask = e.Mask.ToString();
            //        bssid = "lenovo";
            //        gateway = e.Gateway.ToString();
            //    }
            //}
            //else
            {

                using (var client = new WlanClient())
                {
                    foreach (var wlanInterface in client.Interfaces)
                    {
                        if (wlanInterface.InterfaceState == Wlan.WlanInterfaceState.Connected)
                        {
                            var ssidwlan = wlanInterface.CurrentConnection.wlanAssociationAttributes.dot11Ssid;
                            ssid = new string(Encoding.UTF8.GetChars(ssidwlan.SSID, 0, (int)ssidwlan.SSIDLength));
                        }
                    }


                    NetworkInterface[] interfaces;

                    try
                    {
                        interfaces = NetworkInterface.GetAllNetworkInterfaces();
                    }
                    catch (NetworkInformationException )
                    {
                        //Logger.Error(e, "GetEndPoints: Caught unexpected exception.");
                        return string.Empty;
                    }

                    Func<NetworkInterface, bool> isUp = i => i.OperationalStatus == OperationalStatus.Up;
                    Func<NetworkInterface, bool> isEthernet =
                        i => i.NetworkInterfaceType == NetworkInterfaceType.Ethernet
                             || i.NetworkInterfaceType == NetworkInterfaceType.Wireless80211;

                    foreach (var adapter in interfaces.Where(isUp).Where(isEthernet))
                    {
                        var description = adapter.Description;
                        if (description.ToLowerInvariant().Contains("bluetooth")) continue;

                        var properties = adapter.GetIPProperties();

                        IPAddress localip = null, gateway = null, mask = null;

                        Func<UnicastIPAddressInformation, bool> isIPNetwork = a => a.Address.AddressFamily == AddressFamily.InterNetwork;
                        foreach (var info in properties.UnicastAddresses.Where(isIPNetwork))
                        {
                            localip = info.Address;
                            mask = info.IPv4Mask;
                            break;
                        }

                        if (localip == null || localip.ToString().StartsWith("169") || localip.ToString().Equals("192.168.173.1")) continue;

                        var gateways = properties.GatewayAddresses;
                        foreach (var gw in gateways)
                        {
                            gateway = gw.Address;
                            break;
                        }

                        ip = ip + "," + localip.ToString();
                    }


                }

                return string.Format(Format, ssid, ip.Substring(1), mask2, bssid, gateway2, deviceid);
            }

        }
       /* private void RegisterOnLine(object sender, ElapsedEventArgs args)
        {
            if (string.IsNullOrEmpty(reg_url)) return;
            if (!string.IsNullOrEmpty(PeerList) && PeerList.IndexOf("peers") != -1)
            {
                if (urlAccessTimer.Enabled == true) urlAccessTimer.Stop();
            }
            else
            {
                Messenger.Default.Send(
                    new PropertyChangedMessage<object>(this, null, reg_url, "RegisterOnLine"),
                    CoreMessageTokens.RegisterOnLineToken);
            }
        }*/

      //  private void RegisterOffLine()

        private void RegisterSoftAp()
        {
            try
            {
                var request = (HttpWebRequest)WebRequest.Create(softapUrl);
                request.Proxy = null;
                request.Timeout = RegisterTimeout;
                request.ReadWriteTimeout = RegisterTimeout;

                using (var resp = request.GetResponse())
                using (var reader = new StreamReader(resp.GetResponseStream()))
                {
                    var text = reader.ReadToEnd();
                }
            }
            catch
            {
                // ignored
            }
        }

        private void Register()

        {

            try
            {
                var request = (HttpWebRequest)WebRequest.Create(RegisterUrl);
                request.Proxy = null;
                request.Timeout = RegisterTimeout;
                request.ReadWriteTimeout = RegisterTimeout;

                using (var resp = request.GetResponse())
                using (var reader = new StreamReader(resp.GetResponseStream()))
                {
                    var text = reader.ReadToEnd();

                    if (string.IsNullOrEmpty(text)||text.IndexOf("peers")==-1) return;

                    var response = JsonConvert.DeserializeObject<CloudDiscoveryResponse>(text);

                    //var newPeers = response.Peers.Select(p => new IPEndPoint(IPAddress.Parse(p.IP), UnicastPort)).ToList();
                    //lock (l)
                    //{
                    //    var modified = IsPeersChanged(newPeers);
                    //    if (!modified) return;

                    //    peers.Clear();
                    //    peers.AddRange(newPeers);
                    //    broadcastIntervalIndex = 0;
                    //}
                }
            }
            catch
            {
                // ignored
            }
        }

        private bool IsPeersChanged(IEnumerable<IPEndPoint> source)
        {
            var newIPs = source.Select(s => s.Address.ToString()).ToList();
            return newIPs.Count != peers.Count || peers.Select(p => p.Address.ToString()).Any(ip => !newIPs.Contains(ip));
        }

        private void Unregister()
        {
            try
            {
                var request = (HttpWebRequest)WebRequest.Create(UnregisterUrl);
                request.Proxy = null;
                request.Timeout = UnregsiterTimeout;

                request.BeginGetResponse(null, null);
            }
            catch
            {
                // ignored
            }
        }

        public void Advertise()
        {
            if (isAdvertising) return;
            isAdvertising = true;

            /*Messenger.Default.Send(
                   new PropertyChangedMessage<object>(this, null, reg_url, "RegisterOnLine"),
                   CoreMessageTokens.RegisterOnLineToken);

           urlAccessTimer.Start();*/

            //throw new NotImplementedException();*/

            Register();
            RegisterSoftAp();
            heartbeatTimer.Start();

        }

        public void StopAdvertise()
        {
            if (!isAdvertising) return;
            isAdvertising = false;

            heartbeatTimer.Stop();
            Unregister();

            //if (peers.Count == 0) return;

            //var offline = new PresenceMessage(CoreDevice.Empty, false, PresenceStatus.RECEIVE);
            //var bytes = Encoding.UTF8.GetBytes(offline.ToJson());

            //var client = new UdpClient();
            //lock (l)
            //{
            //    foreach (var peer in peers)
            //    {
            //        try
            //        {
            //            client.Send(bytes, bytes.Length, peer);
            //        }
            //        catch
            //        {
            //            // ignored
            //        }
            //    }
            //}
        }

        public void Search()
        {
            //if (isSearching) return;
            //isSearching = true;
            ////urlAccessTimer.Interval = 15000D;
            ///*Messenger.Default.Send(
            //      new PropertyChangedMessage<object>(this, null, reg_url, "RegisterOnLine"),
            //      CoreMessageTokens.RegisterOnLineToken);
            //if (urlAccessTimer.Enabled == false) urlAccessTimer.Start();*/
            //Register();
            //searchUnicast = new BackgroundWorker { WorkerSupportsCancellation = true };
            //searchUnicast.DoWork += (sender, args) =>
            //{
            //    var worker = sender as BackgroundWorker;
            //    if (worker == null) return;

            //    var client = new UdpClient();
            //    var message = new PresenceMessage(CoreDevice.Empty, true, PresenceStatus.RECEIVE) { IsBack = true };

            //    var bytes = Encoding.UTF8.GetBytes(message.ToJson());
            //    while (!worker.CancellationPending)
            //    {
            //        lock (l)
            //        {
            //            foreach (var peer in peers)
            //            {
            //                ThreadPool.QueueUserWorkItem(state =>
            //                {
            //                    try
            //                    {
            //                        client.Send(bytes, bytes.Length, peer);
            //                        client.Send(bytes, bytes.Length, peer);
            //                        client.Send(bytes, bytes.Length, peer);
            //                    }
            //                    catch (Exception)
            //                    {
            //                        // ignored
            //                    }
            //                }, peer);
            //            }
            //        }

            //        Thread.Sleep(broadcastIntervals[broadcastIntervalIndex]);
            //        broadcastIntervalIndex = (++broadcastIntervalIndex >= broadcastIntervals.Length) ? broadcastIntervals.Length - 1 : broadcastIntervalIndex;
            //    }
            //};
            //searchUnicast.RunWorkerAsync();
        }

        public void StopSearch()
        {
            if (!isSearching) return;
            isSearching = false;

            searchUnicast.CancelAsync();

            heartbeatTimer.Stop();
           // RegisterOffLine();
            //throw new NotImplementedException();

        }

        public void Reset()
        {
            if (isAdvertising)
            {
                this.StopAdvertise();
                this.Advertise();
            }

            if (isSearching)
            {
                this.StopSearch();
                this.Search();
            }
        }
    }


    [JsonObject(MemberSerialization.OptIn)]
    internal class CloudDiscoveryResponse
    {
        [JsonProperty(PropertyName = "peers")]
        internal List<Peer> Peers { get; set; }
    }

    [JsonObject(MemberSerialization.OptIn)]
    internal class Peer
    {
        [JsonProperty(PropertyName = "device_id")]
        internal string DeviceId { get; set; }

        [JsonProperty(PropertyName = "ip")]
        internal string IP { get; set; }
    }
}
