using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ClearSpace
{

    public class GlobalDef
{
        public const  UInt16 BROADCAST_PORT = 7082;
        public const  UInt16 UDPCMDPIPE_PORT = 7083;
        public const UInt16 REMOTEHTTPSERVER_PORT = 7084;
        public const UInt16 HTTPTIMEOUT = 20000;

        public const int Download_Cache_Buff_Size = 1024*1024*5;
        public static class Status
        {
            public  const int S_DownloadedSuccessful = 0;
            public const int S_FileExists = 1;
            public  const int S_DownloadedUnsuccessful = -1;

            public const int S_HTTPDownladerInitSuccess = 0;
            public const int S_HTTPDownladerInitFail = -1;
        }

        public static string prefixOfServer = "http://dworkstudio.com";
        public static string massDataUrl = "http://114.215.236.240:8080/metrics/c1column";
        public static string deviceId = Guid.NewGuid().ToString();

        public static class MassDataItems
        {
            public const string Mass_Start_times = "start_times";
            public const string Mass_Close_times = "close_times";
            public const string Mass_Transmit_times = "transmit_times";
            public const string Mass_Use_Softap_times = "use_softap_times";
           // public const string Mass_Use_Lan_times = "use_lan_times";
            public const string Mass_Softap_init_fail_times = "softap_init_fail_times";
            public const string Mass_Softap_Stop_fail_times = "softap_stop_fail_times";
            public const string Mass_Mobile_Softap_conn_fail_times = "mobile_softap_conn_fail_times";
            public const string Mass_User_Cancel_times = "user_cancel_times";
            public const string Mass_Transmit_fail_times = "transmit_fail_times";
            public const string Mass_Crush_times = "crush_times";
        }
}
 


}
