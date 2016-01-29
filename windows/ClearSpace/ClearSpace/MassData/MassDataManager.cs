using Microsoft.Win32;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading;

namespace ClearSpace.MassData
{
    class MassDataManager
    {
        string regKey = "Software\\Lenovo\\CleanMaster";
        bool appMode = false;
        Object m_produce_locker = new Object();
        Object m_consume_locker = new Object();
        List<string> m_data_list = null;

        bool m_started = false;

        string m_deviceId = string.Empty;
        public string deviceId 
        {
            get
            { 
               return m_deviceId;
            }
        }
        string m_massfile = string.Empty;

        string massfile
        {
            get
            {
                string startpath = AppDomain.CurrentDomain.BaseDirectory;
                string storepath = startpath + "massdata";
                string massdatafile = storepath + "\\mass.txt";
                if (!Directory.Exists(storepath))
                {
                    Directory.CreateDirectory(storepath);
                }

                return massdatafile;
            }
        }

        public MassDataManager(bool appmode = false)
        {
            appMode = appmode;
            m_data_list = new List<string>();
            m_deviceId = getDeviceId();
            m_massfile = massfile;
            Thread t = new Thread(() => {
                if(!appmode)
                    loadDataFromFile();
            });
            t.Start();
        }

        private string getDeviceId()
        {
            string deviceid = getDeviceIdReg();
            if (string.IsNullOrEmpty(deviceid))
            {
                deviceid = FingerPrint.Value();
                setDeviceIdReg(deviceid);
            }
            return deviceid;
        }

        public RegistryKey OpenRegKey(string path)
        {
            RegistryKey key = Registry.CurrentUser;
            RegistryKey reg = null;
            try
            {
                reg = key.CreateSubKey(path);
                reg = key.OpenSubKey(path, true);
            }
            catch (Exception e)
            {
                App.WriteLog("MassDataManager::OpenRegKey: " + e.Message, Log.MsgType.Error);
                key.Close();
                return null;
            }

            key.Close();
            return reg;
        }

        private bool setDeviceIdReg(string deviceid)
        {
            bool ret = true;
            try
            {
                RegistryKey key = OpenRegKey(regKey);
                key.SetValue("deviceid", deviceid);
            }
            catch
            {
                ret = false;
            }
            return ret;
        }

        private string getDeviceIdReg()
        {
            string ret = string.Empty;
            try
            {
                RegistryKey key = OpenRegKey(regKey);
                if (IsRegKeyExist(key, "deviceid"))
                {
                    ret = key.GetValue("deviceid").ToString();
                }
            }
            catch
            {

            }
            return ret;
        }

        public  bool IsRegKeyExist(RegistryKey key, string valueName)
        {
            string[] subkeyNames;
            try
            {
                subkeyNames = key.GetValueNames();
                foreach (string keyName in subkeyNames)
                {
                    if (keyName == valueName)
                    {
                        return true;
                    }
                }
            }
            catch
            {
                return false;
            }
            return false;
        }

        private void loadDataFromFile()
        {
            try
            {
                if (File.Exists(m_massfile))
                {
                    FileStream fs = new FileStream(m_massfile, FileMode.Open, FileAccess.Read, FileShare.Read);
                    using (StreamReader reader = new StreamReader(fs))
                    {
                        string temp = reader.ReadLine();
                        while (!string.IsNullOrEmpty(temp))
                        {
                            addData(temp);
                            temp = reader.ReadLine();
                        }
                    }
                    fs.Close();
                    File.Delete(m_massfile);
                }
            }
            catch (Exception e)
            {
                App.WriteLog("Mass data loadDataFromFile: " + e.Message, Log.MsgType.Error);
            }
        }

        public string peekLast()
        {
            lock (m_consume_locker)
            {
                string temp = string.Empty;
                if(m_data_list != null && m_data_list.Count > 0)
                {
                    temp = m_data_list[m_data_list.Count - 1];
                  
                }
                return temp;
            }
        }

        private void addData(string jsonstr)
        {
            lock (m_produce_locker)
            {
                if (m_data_list != null)
                {
                    m_data_list.Add(jsonstr);
                }
            }
        }

        public void addData(Dictionary<string,string> data)
        {
            string jsonstr = string.Empty;
            StringWriter str = new StringWriter();
            JsonWriter jw = new JsonTextWriter(str);
            jw.Formatting = Formatting.None;
            jw.WriteStartObject();
            jw.WritePropertyName("app_id");
            jw.WriteValue("photomaster");
            jw.WritePropertyName("device_id");
            jw.WriteValue(m_deviceId);
            foreach (KeyValuePair<string, string> kp in data)
            {
                jw.WritePropertyName(kp.Key.ToString());
                jw.WriteValue(kp.Value.ToString());
            }
            jw.WriteEndObject();

            jsonstr = str.ToString();
            addData(jsonstr);
        }

        private void persist()
        {
            try
            {
                if (m_data_list.Count > 0 && !appMode)
                {
                    FileStream fs = new FileStream(m_massfile, FileMode.Append, FileAccess.Write, FileShare.Read);
                    using (StreamWriter writer = new StreamWriter(fs))
                    {
                        foreach (string s in m_data_list)
                        {
                            writer.WriteLine(s);
                        }
                    }
                }
            }
            catch (Exception e)
            {
                App.WriteLog("MassData persist: " + e.Message, Log.MsgType.Error);
            }
            
        }

        public void start()
        {
            if (m_started) return;
            m_started = true;
            Thread thread = new Thread(() =>
            {
                while (m_started)
                {
                    if (m_data_list.Count > 0)
                    {
                        string s = peekLast();
                        try
                        {
                            //upload
                            string retMsg = ClearSpace.Utils.PostJson(GlobalDef.massDataUrl, s);
                            if (!string.IsNullOrEmpty(retMsg))
                            {
                                JObject jobject = null;
                                try
                                {
                                    jobject = JObject.Parse(retMsg);
                                }
                                catch
                                {
                                    App.WriteLog("Mass data get bad ret message from server:　" + retMsg, Log.MsgType.Error);
                                }
                                if (jobject == null) continue;

                                int status = Int32.Parse(jobject["status"].ToString());
                                if (status == 0)
                                {
                                    lock (m_produce_locker)
                                    {
                                        m_data_list.Remove(s);
                                    }
                                }
                                else
                                {
                                    App.WriteLog("Mass data server retured error:　" + retMsg, Log.MsgType.Information);
                                }
                            }
                        }
                        catch (Exception e)
                        {
                            App.WriteLog("MassData start thread: " + e.Message, Log.MsgType.Error);
                        }
                    }
                    else
                    {
                        Thread.Sleep(1);
                    }
                }

                persist();
            });
            thread.Start();
            
        }

        public void stop()
        {
            m_started = false;
        }

    }
}
