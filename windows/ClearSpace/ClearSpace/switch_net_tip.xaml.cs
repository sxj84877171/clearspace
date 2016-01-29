using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Shapes;
using Timer = System.Timers.Timer;
namespace ClearSpace
{
    /// <summary>
    /// Interaction logic for switch_net_tip.xaml
    /// </summary>
    public partial class switch_net_tip : Window
    {
      
        Timer timer;
        Window parent = null;
        public switch_net_tip(Window w)
        {
            InitializeComponent();

            parent = w;
            Application.Current.MainWindow = w;
            this.timer = new Timer(5000);
            this.timer.Elapsed += (s, args) =>
            {
                this.Dispatcher.Invoke(new Action(delegate
                 {
                     this.Close();
                 }));
            };

           // timer.Start();
        }


        private void Window_Closed(object sender, System.ComponentModel.CancelEventArgs e)
        {
            //timer.Stop();
        }

        private void Close_Btn_Click(object sender, RoutedEventArgs e)
        {
            this.Hide();
        }

        private void StopTransRecoverNet(object sender, MouseButtonEventArgs e)
        {
            Hide();
            if (parent != null && parent.IsLoaded)
            {
               ((MainWindow) parent).agressiveStop();
            }
        }
    }
}
