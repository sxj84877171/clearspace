using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.NetworkInformation;
using System.Text;
using System.Threading.Tasks;
using NativeWifi;
using System.Collections.ObjectModel;
using System.IO;
using System.Drawing;
using System.Drawing.Imaging;
using System.Windows.Media.Imaging;
using System.Windows;
using System.Diagnostics;

namespace ClearSpace
{
    class Utils
    {
        public static List<string> getLocalIPAddresses()
        {
            List<string> ips = new List<string>();
            string hostName = Dns.GetHostName();//本机名
            try
            {
                //System.Net.IPAddress[] addressList = Dns.GetHostByName(hostName).AddressList;
                System.Net.IPAddress[] addressList = Dns.GetHostAddresses(hostName);
                foreach (IPAddress ip in addressList)
                {
                    if (ip.AddressFamily == System.Net.Sockets.AddressFamily.InterNetwork)
                        ips.Add(ip.ToString());
                }
            }
            catch { }

            return ips;
      
        }

        public static string getLocalComputerName()
        {
            return System.Net.Dns.GetHostName();
        }

        public static bool ISEnEdition()
        {
            string UIlanguage = System.Globalization.CultureInfo.InstalledUICulture.Name;
            if (UIlanguage.Equals("en-US", StringComparison.CurrentCultureIgnoreCase))
            {
                return true;
            }

            return false;
        }


        public static string getUpEthernet()
        {
            string ret = null;

            try
            {
                NetworkInterface[] adapters = NetworkInterface.GetAllNetworkInterfaces();
                foreach (NetworkInterface adapter in adapters)
                {
                    if (adapter.OperationalStatus == OperationalStatus.Up && adapter.NetworkInterfaceType == NetworkInterfaceType.Ethernet)
                    {
                        ret = adapter.Name;
                        break;
                    }
                }
            }
            catch { }

            return ret;
        }

        public static  string getWifiSsid()
        {
            string ret = null;

            try
            {
                using (WlanClient wlan = new WlanClient())
                {
                    Collection<String> connectedSsids = new Collection<string>();

                    foreach (WlanClient.WlanInterface wlanInterface in wlan.Interfaces)
                    {
                        if (wlanInterface.InterfaceState == Wlan.WlanInterfaceState.Connected)
                        {
                            Wlan.Dot11Ssid ssid = wlanInterface.CurrentConnection.wlanAssociationAttributes.dot11Ssid;
                            connectedSsids.Add(new String(Encoding.ASCII.GetChars(ssid.SSID, 0, (int)ssid.SSIDLength)));
                        }
                    }
                    if (connectedSsids.Count > 0)
                        ret = connectedSsids[0];
                    else
                        ret = null;
                }
            }
            catch {
                ret = null;
            }

            return ret;
        }


        public static string GetSoftAPContractName()
        {

            string str = string.Empty;
            lock (App.locker)
            {
                if (!String.IsNullOrEmpty(App.SoftAp_id))
                    str = App.SoftAp_id;
                else
                {
                    str = "zpdny_" + RandString() + "_" + getLocalComputerName();
                    App.SoftAp_id = str;
                }
            }
             return str;
        }

        public static bool IsPicture(string filePath) 
        {
            try
            {
                FileStream fs = new FileStream(filePath, FileMode.Open, FileAccess.Read);
                BinaryReader reader = new BinaryReader(fs);
                string fileClass;
                byte buffer;
                byte[] b=new byte[2];
                buffer = reader.ReadByte();
                b[0] = buffer;
                fileClass = buffer.ToString();
                buffer = reader.ReadByte();
                b[1]=buffer;
                fileClass += buffer.ToString();

                
                reader.Close();
                fs.Close();
                if (fileClass == "255216" || fileClass == "6677" || fileClass == "13780")//255216是jpg;7173是gif;6677是BMP,13780是PNG;7790是exe,8297是rar 
                {
                    return true;
                }
                else
                {
                    return false;
                }
            }
            catch
            {
                return false;
            }
        }


        public static bool IsVideo(string filePath)
        {
            if (!File.Exists(filePath))
                return false;
            string fileClass = ".mp4|.3gp|.avi|.mpeg|.mov|.wmv|.mkv|.rm|.rmvb";
            FileInfo fi = new FileInfo(filePath);
            if (fileClass.Contains(fi.Extension.ToLower()))
            {
                return true;
            }
            else
            {
                return false;
            }
        }


        public static bool SetFileHidden(string path)
        {
            bool ret = false;
            try
            {
                FileInfo info = new FileInfo(path);
                if (info.Exists)
                {
                    info.Attributes = FileAttributes.Hidden;
                    ret = true;
                }
            }
            catch { }

            return ret;
        }

        public static bool setFileCreationTime(string file, long millisec)
        {
            bool ret = true;
            try
            {
                if(File.Exists(file))
                    File.SetCreationTime(file, DateTime.Parse("1970-1-1").AddMilliseconds(millisec).ToLocalTime());
            }
            catch {
                ret = false;
            }

            return ret;
        }


        public static string getFileCreationTime(string file)
        {
            string ret = null;
            try
            {
                if (File.Exists(file))
                {
                    FileInfo fi = new FileInfo(file);
                    TimeSpan ts = fi.CreationTime.Subtract(DateTime.Parse("1970-1-1"));
                    ret = ts.TotalMilliseconds.ToString();
                }
            }
            catch { ret = null; }

            return ret;
        }


        public static Int64 getFileSize(string file)
        {
            Int64 ret = 0;
            try
            {
                if(File.Exists(file))
                {
                    FileInfo fi = new FileInfo(file);
                    ret = fi.Length;
                }
            }
            catch{
                ret = 0;
            }

            return ret;
        }


        

        public static string ConvertUTF8(string str)
        {
            string ret = "";
            try
            {
                byte[] temp = Encoding.Default.GetBytes(str);
                byte[] temp1 = Encoding.Convert(Encoding.UTF8, Encoding.Default, temp);
                ret = Encoding.Default.GetString(temp1, 0, temp1.Length);
            }
            catch{}

            return ret;
        }

        public static BitmapSource GetThumbnail(string originalImagePath, int width, int height)
        {
            Image originalImage = Image.FromFile(originalImagePath);
            Image bitmap = originalImage.GetThumbnailImage(width, height, null, IntPtr.Zero);

            IntPtr ip = ((Bitmap)bitmap).GetHbitmap();
            BitmapSource bitmapSource = System.Windows.Interop.Imaging.CreateBitmapSourceFromHBitmap(
                ip,
                IntPtr.Zero,
                Int32Rect.Empty,
                System.Windows.Media.Imaging.BitmapSizeOptions.FromEmptyOptions()
                );

            NativeMethodsCall.DeleteObject(ip);
            bitmap.Dispose();
            originalImage.Dispose();
            return bitmapSource;
        }


        public static BitmapSource GetThumbnail(System.Drawing.Image image, int width, int height)
        {
            Image originalImage = image;
            Image bitmap = originalImage.GetThumbnailImage(width, height, null, IntPtr.Zero);

            IntPtr ip = ((Bitmap)bitmap).GetHbitmap();
            BitmapSource bitmapSource = System.Windows.Interop.Imaging.CreateBitmapSourceFromHBitmap(
                ip,
                IntPtr.Zero,
                Int32Rect.Empty,
                System.Windows.Media.Imaging.BitmapSizeOptions.FromEmptyOptions()
                );

            NativeMethodsCall.DeleteObject(ip);
            bitmap.Dispose();
            originalImage.Dispose();
            return bitmapSource;
        }



        /// <summary>
        /// 
        /// </summary>
        /// <param name="dir"></param>
        /// <param name="type">all, img</param>
        /// <returns></returns>
        public static List<string> GetAllFils(string dir, string type)
        {
            List<string> FileList = new List<string>();
            if(!Directory.Exists(dir))
            {
                return FileList;
            }
            DirectoryInfo dirInfo = new DirectoryInfo(dir);
            FileInfo[] allFile = dirInfo.GetFiles();
            foreach (FileInfo fi in allFile)
            {
                if (type.Contains("img"))
                {
                    if (IsPicture(fi.FullName))
                    {
                        FileList.Add(fi.FullName);
                        continue;
                    }
                }
                if (type.Contains("video"))
                {
                    if (IsVideo(fi.FullName))
                    {
                        FileList.Add(fi.FullName);
                    }
                }
            }

            return FileList;
        }


        public static string RandString()
        {
            string str = "1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
            Random r = new Random();
            string result = string.Empty;

            for (int i = 0; i < 5; i++)
            {
                int m = r.Next(0, str.Length);
                string s = str.Substring(m, 1);
                result += s;
            }

            return result;
        }

        public static Process ProcessStart(string fileName, string command)
        {
            try
            {
                Process process = new Process();
                process.StartInfo.FileName = fileName;
                process.StartInfo.Arguments = command;
                process.StartInfo.UseShellExecute = true;
                //process.StartInfo.RedirectStandardInput = true;
                //process.StartInfo.RedirectStandardOutput = true;
                //process.StartInfo.RedirectStandardError = true;
                process.StartInfo.CreateNoWindow = true;
                process.Start();
                return process;
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex.Message);
                return null;
            }
        }

        public static string PostJson(string url, string jsondata)
        {
            string ret = string.Empty;

            //using (var client = new WebClient())
            //{
            //    client.Headers[HttpRequestHeader.ContentType] = "application/json";
            //    ret = client.UploadString(url, "POST", jsondata);
            //}

            try
            {
                var httpWebRequest = WebRequest.Create(url) as HttpWebRequest;
                httpWebRequest.Timeout = ClearSpace.GlobalDef.HTTPTIMEOUT;
                httpWebRequest.ContentType = "application/json";
                httpWebRequest.Method = "POST";

                using (var streamWriter = new StreamWriter(httpWebRequest.GetRequestStream()))
                {
                    streamWriter.Write(jsondata);
                    streamWriter.Flush();
                }

                var response = httpWebRequest.GetResponse() as HttpWebResponse;
                if (httpWebRequest != null && response != null && response.StatusCode == HttpStatusCode.OK)
                {
                    using (var streamReader = new StreamReader(response.GetResponseStream()))
                    {
                        ret = streamReader.ReadToEnd();
                    }
                }

                //if (response != null && response.StatusCode == HttpStatusCode.OK)
                //    App.WriteLog(url + " upload download successful state done", Log.MsgType.Information);
            }
            catch (Exception e)
            {
                App.WriteLog("PostJson: " + e.Message, Log.MsgType.Error);
            }
            return ret;
        }
    }
}
