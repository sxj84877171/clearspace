using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Text;
using System.Threading.Tasks;
using System.Windows;

namespace ClearSpace.NetworkService
{
    public interface HTTPClientCallback
    {
        void onStartingDownload(HTTPClientService http, EndPoint server, int imgCount, UIElement ui);


        /// <summary>
        ///
        /// </summary>
        /// <param name="url"></param>
        /// <param name="server"></param>
        /// <param name="status">S_DownloadedSuccessful or S_DownloadedUnsuccessful</param>
        /// <param name="ui"></param>
        void onHttpFinishloadingOne(HTTPClientService http, EndPoint server, RemotePhotoInfo rpi, string path, int status, UIElement ui);


        /// <summary>
        /// 
        /// </summary>
        /// <param name="server"></param>
        /// <param name="status">S_HTTPDownladerInitSuccess or S_HTTPDownladerInitFail</param>
        /// <param name="ui"></param>
        void onHttpClientSelfDestroy(HTTPClientService http, EndPoint server, int status, UIElement ui);

        void onHttpClientPreTransVideo(HTTPClientService http, EndPoint server, UIElement ui);
    }
}
