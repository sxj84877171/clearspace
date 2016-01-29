using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Input;
using ThumbsExplorer;
namespace ClearSpace
{
    partial class Generic : ResourceDictionary
    {


        private void Window_MouseLeftButtonUp(object sender, MouseButtonEventArgs e)
        {
            ThumbImage img = sender as ThumbImage;
            if (img != null)
            {
                ClearSpace.Utils.ProcessStart(img.ThumbImageSource, @"rundl132.exe C:\WINDOWS\system32\shimgvw.dll,ImageView_Fullscreen");
            }
        }
    }
}
