using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Text;
using System.Threading.Tasks;
using System.Windows;

namespace ClearSpace.NetworkService
{
    class NetworkServiceFactory
    {
        public static AsyncUDPServer GenerateUDPServer(string ip, UInt16 port, AsyncUDPCallback callback)
        {
            return new AsyncUDPServer(ip, port, callback);
        }

        public static UDPBroadcastService GenerateUDPBroadcastService(UInt16 port, string content)
        {
            return new UDPBroadcastService(port, content);
        }

        public static HTTPClientService GenerateHTTPService(string url, string folder, EndPoint server, UIElement ui, HTTPClientCallback callback)
        {
            return new HTTPClientService(url, folder,server, ui, callback);
        }

        public static CounterpartScanService GenerateScanSoftApService(CounterpartScanServiceCallback cb)
        {
            return new CounterpartScanService(cb);
        }
    }
}
