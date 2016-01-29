using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace ClearSpace.NetworkService
{
    class UDPBroadcastService
    {
        private IPEndPoint m_TargetIPPort = null;
        private UdpClient m_BroadcastService = null;
        private bool m_bEnableBroadcast = false;
        private string m_sContent = null;
        private UInt16 m_port = 0;
        string m_content = null;
        private List<UdpClient> m_BroadcastClients = null;
        public UDPBroadcastService(UInt16 port, string content)
        {
            m_BroadcastClients = new List<UdpClient>();
            m_port = port;
            m_content = content;
        }

        private bool isIPExists(string ip)
        {
            bool ret = false;
            foreach (UdpClient udp in m_BroadcastClients)
            {
                IPEndPoint ipport = (udp.Client.LocalEndPoint as IPEndPoint);
                if (ipport != null)
                {
                    if (ipport.Address.ToString().Equals(ip))
                    {
                        ret = true;
                        break;
                    }
                }
            }

            return ret;
        }

        private void UpdateAdapter()
        {
            List<string> ips = Utils.getLocalIPAddresses();
            foreach (string s in ips)
            {
                if (!isIPExists(s))
                {
                    m_BroadcastService = new UdpClient(new IPEndPoint(IPAddress.Parse(s), 0));
                    m_BroadcastClients.Add(m_BroadcastService);
                }
            }
        }

        public bool init()
        {
            bool ret = true;
            try
            {
                UpdateAdapter();
                m_TargetIPPort = new IPEndPoint(IPAddress.Broadcast, m_port);
                m_sContent = (m_content != null ? m_content : "");
            }
            catch (Exception e)
            {
                Console.WriteLine(e.Message);
                ret = false;
            }

            return ret;
        }


        private void broadcastThread()
        {
            UdpClient issuedClient = null;
            while (m_bEnableBroadcast)
            {
                byte[] buf = Encoding.Default.GetBytes(m_sContent);
                foreach (UdpClient udp in m_BroadcastClients)
                {
                    try
                    {
                        udp.Send(buf, buf.Length, m_TargetIPPort);
                    }
                    catch
                    {
                        issuedClient = udp;
                        break;
                    }
                }

                if (issuedClient != null)
                {
                    m_BroadcastClients.Remove(issuedClient);
                    issuedClient = null;
                }
                UpdateAdapter();
                Thread.Sleep(1000);
            }
        }

        public void start()
        {
            m_bEnableBroadcast = true;
            Thread t = new Thread(new ThreadStart(broadcastThread));
            t.IsBackground = true;
            t.Start();

        }

        public void stop()
        {
            m_bEnableBroadcast = false;
        }
    }
}
