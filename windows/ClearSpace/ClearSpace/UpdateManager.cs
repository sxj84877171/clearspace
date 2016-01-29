using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Diagnostics;
using System.Threading;
using System.IO;
using System.Net;
using System.Windows;

namespace ClearSpace
{
    public struct tagUpdateInfo
    {
        public bool hasNewVer;
        public int newVersionNum;
        public int curVerionNum;
        public string newVersionDesc;
        public string newVersionMd5;
        public string newBuildName;
        public string newVerDownloadUrl;
    }

    public delegate void Update_CallBack(tagUpdateInfo updateinfo);


    class UpdateManager
    {
        private static object lockObj = new object();
        public static void CheckUpdate(Update_CallBack callback, bool IsDebug)
        {
            //start new version check proc
            Thread threadHand1 = new Thread(() =>
            {
                lock (lockObj)
                {
                    try
                    {
                        CheckUpdateProc(callback, IsDebug);
                    }
                    catch
                    { }
                }
            });
            threadHand1.Start();

            return;
        }

        public static bool DownloadNewVerion(tagUpdateInfo updateinfo)
        {

            if (updateinfo.hasNewVer)
            {
                if (updateinfo.newVersionNum > updateinfo.curVerionNum)
                {
                    string startpath = AppDomain.CurrentDomain.BaseDirectory;
                    string updateFoler = startpath + "update";
                    string newBinaryFile = updateFoler + "\\" + updateinfo.newBuildName;
                    string localExistBinaryConfig = updateFoler + "\\config.txt";
                    if (!Directory.Exists(updateFoler))
                    {
                        Directory.CreateDirectory(updateFoler);
                    }

                    if (File.Exists(newBinaryFile) && File.Exists(localExistBinaryConfig))
                    {
                        //check the build config file to see if match the latest server version
                        FileStream myFs = new FileStream(localExistBinaryConfig, FileMode.Open);
                        StreamReader fileReader = new StreamReader(myFs);
                        string ver = fileReader.ReadLine();
                        fileReader.Close();
                        myFs.Close();
                        try
                        {
                            int intVer = Int32.Parse(ver);
                            if (intVer == updateinfo.newVersionNum)
                                return true;
                        }
                        catch
                        {
                            
                        }
                        
                    }

                    //delete old files before getting new files
                    if (File.Exists(newBinaryFile))
                        File.Delete(newBinaryFile);
                    try
                    {
                        if (File.Exists(localExistBinaryConfig))
                            File.Delete(localExistBinaryConfig);

                    }
                    catch (Exception e)
                    {
                        Console.WriteLine(e.Message);
                    }



                    if (!File.Exists(newBinaryFile))
                    {
                        //try to download new installer
                        if (!HttpDownloadFile(updateinfo.newVerDownloadUrl, newBinaryFile))
                        {
                            if (!HttpDownloadFile(updateinfo.newVerDownloadUrl, newBinaryFile))
                            {
                                Console.WriteLine("download " + updateinfo.newVerDownloadUrl + "failed!");
                                return false;
                            }
                        }
                        //create corresponding version record file
                        if (File.Exists(localExistBinaryConfig))
                        {
                            File.Delete(localExistBinaryConfig);
                        }
                        FileStream myFs = new FileStream(localExistBinaryConfig, FileMode.Create);
                        StreamWriter mySw = new StreamWriter(myFs);
                        mySw.Write(String.Format("{0}",updateinfo.newVersionNum));
                        mySw.Close();
                        myFs.Close();
                    }
                    return true;
                }
            }

            return false;
        }

        public static void LauchNewInstaller(tagUpdateInfo updateinfo)
        {
            string startpath = AppDomain.CurrentDomain.BaseDirectory;
            string updateFoler = startpath + "update";
            string newBinaryFile = updateFoler + "\\" + updateinfo.newBuildName;
            ProcessStart(newBinaryFile, @"");

            //terminate self
            ((App)Application.Current).terminate();
        }

        private static void CheckUpdateProc(Delegate callback, bool IsDebug)
        {
            string updateUrl = GlobalDef.prefixOfServer + "/update/pc/update.ini";
            if (IsDebug)
                updateUrl = GlobalDef.prefixOfServer + "/update/pc/update_debug.ini";
                
            //get update ini to temp folder
            string tempFolder = System.IO.Path.GetTempPath();
            string xphoneUpdateFile = tempFolder + "cleanspaceUpdate.ini";
            if (!HttpDownloadFile(updateUrl, xphoneUpdateFile))
            {
                if (!HttpDownloadFile(updateUrl, xphoneUpdateFile))
                {
                    return;
                }
            }
            //parse ini file
            IniFile iniFile = new IniFile(xphoneUpdateFile);
            string newVer = iniFile.IniReadValue("VersionInfo", "version");
            string newDesc = iniFile.IniReadValue("VersionInfo", "description");
            string newBuildLink = iniFile.IniReadValue("VersionInfo", "newBuildLink");
            string newBuildName = iniFile.IniReadValue("VersionInfo", "newBuildName");
            string md5 = iniFile.IniReadValue("VersionInfo", "MD5");
            //format new Ver
            //string temp = newVer.Substring(0,1) + "."+newVer.Substring(1,1) + "." + newVer.Substring(2,1) + "." + newVer.Substring(3,1);
            //newVer = temp;
            int intNewVer = Int32.Parse(newVer);
            //Get current version
            // string version = System.Reflection.Assembly.GetExecutingAssembly().GetName().Version.ToString();
            FileVersionInfo fileversion = System.Diagnostics.FileVersionInfo.GetVersionInfo(System.Diagnostics.Process.GetCurrentProcess().MainModule.FileName);
            string curVer = fileversion.ProductVersion;
            int intCurVer = fileversion.FileMajorPart * 1000 + fileversion.FileMinorPart * 100 + fileversion.FileBuildPart * 10 + fileversion.FilePrivatePart;
            tagUpdateInfo updateInfo = new tagUpdateInfo();

            if (intNewVer > intCurVer) //new version available
            {
                
                updateInfo.hasNewVer = true;
                updateInfo.newVersionNum = intNewVer;
                updateInfo.curVerionNum = intCurVer;
                updateInfo.newVersionDesc = newDesc;
                updateInfo.newVersionMd5 = md5;
                updateInfo.newVerDownloadUrl = newBuildLink;
                updateInfo.newBuildName = newBuildName;
            }
            else 
            {
                updateInfo.hasNewVer = false;
            }

            callback.DynamicInvoke(updateInfo);
            return;

        }


        public static Process ProcessStart(string fileName, string command)
        {
            try
            {
                Process process = new Process();
                process.StartInfo.FileName = fileName;
                process.StartInfo.Arguments = command;
                process.StartInfo.UseShellExecute = false;
                process.StartInfo.RedirectStandardInput = true;
                process.StartInfo.RedirectStandardOutput = true;
                process.StartInfo.RedirectStandardError = true;
                process.StartInfo.CreateNoWindow = false;
                process.Start();
                return process;
            }
            catch (Exception ex)
            {
                throw (ex);
            }
        }

        public  static bool HttpDownloadFile(string url, string path)
        {
            bool ret = true;
            HttpWebRequest request = WebRequest.Create(url) as HttpWebRequest;
            HttpWebResponse response = null;
            try
            {
                response = request.GetResponse() as HttpWebResponse;
                Stream responseStream = response.GetResponseStream();
                Stream stream = new FileStream(path, FileMode.Create);
                byte[] bArr = new byte[1024];
                int size = responseStream.Read(bArr, 0, (int)bArr.Length);
                while (size > 0)
                {
                    stream.Write(bArr, 0, size);
                    size = responseStream.Read(bArr, 0, (int)bArr.Length);
                }

                stream.Close();
                responseStream.Close();
            }
            catch (Exception e)
            {
                Console.WriteLine(e.Message);
                ret = false;
            }
            return ret;
        }

        public static string GetCurrentVersion()
        {
            FileVersionInfo fileversion = System.Diagnostics.FileVersionInfo.GetVersionInfo(System.Diagnostics.Process.GetCurrentProcess().MainModule.FileName);
            string version = String.Format("{0}.{1}.{2}", fileversion.FileMajorPart, fileversion.FileMinorPart, fileversion.FileBuildPart /*,fileversion.FilePrivatePart*/);
            return version;
        }

        public static void ShowUpdateDialog(tagUpdateInfo updateInfo)
        {
            //Boolean result = false;
            //SimpleMessageBox uw = new SimpleMessageBox();
            //uw.SetCurVersion(updateInfo.curVerionNum);
            //uw.SetNewVersion(updateInfo.newVersionNum);
            //uw.SetButton(3, "");//set the check box invisible
            //result = (Boolean)uw.ShowDialog();
            //if (result == true)
            //{
            //    UpdateManager.LauchNewInstaller(updateInfo);
            //}
        }


    }
}
