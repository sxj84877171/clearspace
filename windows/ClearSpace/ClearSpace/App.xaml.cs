using System;
using System.Collections.Generic;
using System.Configuration;
using System.Data;
using System.IO;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using System.Windows;

namespace ClearSpace
{

   
    /// <summary>
    /// Interaction logic for App.xaml
    /// </summary>
    /// 
    public partial class App : Application, ISingleInstanceApp
    {
        public static Log.Log UILog = null;
        public static string SoftAp_id = string.Empty;
        public static object locker = new object();
        MassData.MassDataManager m_mass = null;
        public App()
        {
            WriteLog("ClearSpace starting", Log.MsgType.Information);
            UILog.CleanLogFiles();
            //if (Utils.ISEnEdition())
            {
                ClearSpace.Properties.Resources.Culture = new System.Globalization.CultureInfo(System.Globalization.CultureInfo.InstalledUICulture.Name);
            }
            //else
            //{
            //    ClearSpace.Properties.Resources.Culture = new System.Globalization.CultureInfo("zh-CN");
            //}

            //mass data logistic
            m_mass = new MassData.MassDataManager(true);
            m_mass.start();
        }


        protected override void OnStartup(StartupEventArgs e)
        {
            base.OnStartup(e);
        }

        public static void WriteLog(string logstr, Log.MsgType logtype)
        {
            try
            {
                if (UILog == null)
                {
                    string log_path = Environment.GetEnvironmentVariable("appdata") + "\\lenovo\\ClearSpace\\";
                    UILog = new Log.Log(log_path, Log.LogType.Daily);
                }

                if (UILog != null)
                    UILog.Write(logstr, logtype);
            }
            catch { }

            
        }

        public void uploadMassData(string key, string value)
        {
            Dictionary<string, string> mass = new Dictionary<string, string>();
            mass.Add(key, value);
            if (m_mass != null)
                m_mass.addData(mass);
        }
             

        public void terminate()
        {
            this.Dispatcher.Invoke(new Action(delegate { this.Shutdown(); }));
        }

        protected override void OnExit(ExitEventArgs e)
        {
            if (UILog != null)
            {
                WriteLog("ClearSpace exit", Log.MsgType.Information);
                UILog.Dispose();
                m_mass.stop();
            }
            base.OnExit(e);
        }

        private const string Unique = "Lenovo CDL CleanSpace Application";
        [STAThread]
        public static void Main()
        {
            if (SingleInstance<App>.InitializeAsFirstInstance(Unique))
            {
                var application = new App();
                try
                {
                    AppDomain.CurrentDomain.UnhandledException +=
                    new UnhandledExceptionEventHandler(CurrentDomain_UnhandledException);
                    application.InitializeComponent();
                    application.Run();
                }
                catch (Exception e)
                {
                    WriteLog(e.Message, Log.MsgType.Error);
                    if (application != null)
                    {
                        try
                        {
                            application.uploadMassData(GlobalDef.MassDataItems.Mass_Crush_times, "1");
                        }
                        catch { }
                    }


                }

               //the icon would clean up automatically, but this is cleaner
                if (UILog != null)
                    UILog.Dispose();
                // Allow single instance code to perform cleanup operations
                SingleInstance<App>.Cleanup();
            }

        }

        static void CurrentDomain_UnhandledException(object sender, UnhandledExceptionEventArgs e)
        {
            Exception error = (Exception)e.ExceptionObject;
            Console.WriteLine("MyHandler caught : " + error.Message);
            App.WriteLog("MyHandler caught : " + error.Message, Log.MsgType.Error);
          //  string startpath = AppDomain.CurrentDomain.BaseDirectory;
            //if (!Directory.Exists(startpath + "dump"))
            //{
            //    Directory.CreateDirectory(startpath + "dump");
            //}
           // string dumpfile = startpath + "dump\\UI_dump_" + System.Environment.TickCount + ".dmp";
          //  MiniDump.TryDump(dumpfile, MiniDump.MiniDumpType.Normal);
        }

        #region ISingleInstanceApp Members
        public bool SignalExternalCommandLineArgs(IList<string> args)
        {
            // Handle command line arguments of second instance
            // Bring window to foreground
            if (this.MainWindow != null)
            {
                if (this.MainWindow.WindowState == WindowState.Minimized)
                {
                    this.MainWindow.WindowState = WindowState.Normal;
                }

                this.MainWindow.Activate();
                this.MainWindow.Show();
                this.MainWindow.Topmost = true;
                this.MainWindow.Topmost = false;
            }

            return true;
        }
        #endregion

    }
}
