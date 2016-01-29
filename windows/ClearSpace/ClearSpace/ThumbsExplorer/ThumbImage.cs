using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Windows.Controls;
using System.Windows;
using System.IO;
using System.Drawing;
using System.Windows.Interop;
using System.Windows.Media.Imaging;
using System.Windows.Threading;
using System.Windows.Input;

namespace ThumbsExplorer
{
    /// <summary>
    /// Thumb Image.
    /// </summary>
    public class ThumbImage : System.Windows.Controls.Image
    {
        #region ... Variables ...
        /// <summary>
        /// Set ImageSource Handler.
        /// </summary>
        private static SetImageSourceHandler setImageSourceHandler;
        #endregion ... Variables ...
        private string mFilename = String.Empty;
        #region ... Properties ...
        /// <summary>
        /// Gets or sets ThumbImageSource.
        /// </summary>
        public string ThumbImageSource {
            get
            {
                return mFilename;
            } 
            set
            {
                mFilename = value;
            } 
        }
        /// <summary>
        /// ThumbImageSourceProperty.
        /// </summary>
        public static DependencyProperty ThumbImageSourceProperty
            = DependencyProperty.Register("ThumbImageSource", typeof(string), typeof(System.Windows.Controls.Image), 
            new FrameworkPropertyMetadata(new PropertyChangedCallback(OnThumbImageSourcePropertyChanged)));
               
        #endregion ... Properties ...

         #region ... Methods ...
        /// <summary>
        /// OnThumbImageSourcePropertyChanged.
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="args"></param>
        private static void OnThumbImageSourcePropertyChanged(DependencyObject sender, DependencyPropertyChangedEventArgs args)
        {
            if (sender is System.Windows.Controls.Image && args.NewValue is string)
            {
                System.Windows.Controls.Image image = sender as System.Windows.Controls.Image;
                ThumbImage thumImg = image as ThumbImage;
                string fileName = args.NewValue as string;
                if (thumImg != null)
                    thumImg.ThumbImageSource = fileName;
                setImageSourceHandler = new SetImageSourceHandler(SetImageSource);
                image.Dispatcher.BeginInvoke(setImageSourceHandler, DispatcherPriority.ApplicationIdle, new object[] { image, fileName });
            }
        }

        /// <summary>
        /// Set Image.Source.
        /// </summary>
        /// <param name="image"></param>
        /// <param name="fileName"></param>
        private static void SetImageSource(System.Windows.Controls.Image image, string fileName)
        {
            try
            {
                if (!string.IsNullOrEmpty(fileName) && File.Exists(fileName))
                {
                    BitmapSource bs = ThumbsManager.getThumbs(fileName);
                    if (bs == null)
                    {
                        //if video
                        if (ClearSpace.Utils.IsVideo(fileName))
                        {
                            bs = new BitmapImage(new Uri(@"pack://application:,,,../images/video.png", UriKind.RelativeOrAbsolute));
                            using(MemoryStream outStream = new MemoryStream())
                            {
                                BitmapEncoder enc = new BmpBitmapEncoder();
                                enc.Frames.Add(BitmapFrame.Create((BitmapImage)bs));
                                enc.Save(outStream);
                               // System.Drawing.Bitmap bitmap = new System.Drawing.Bitmap(outStream);
                                using (System.Drawing.Image sourceImage = System.Drawing.Image.FromStream(outStream))
                                {
                                    using (Graphics g = Graphics.FromImage(sourceImage))
                                    {
                                        FileInfo fi = new FileInfo(fileName);
                                        String strSize = (fi.Length >> 20) + "MB";
                                        Font font = new Font("Microsoft yahei", 8);
                                        SizeF sf = g.MeasureString(strSize, font);
                                        g.DrawString(strSize, font,
                                            Brushes.White, new PointF((62-sf.Width)/2, 42));
                                        g.Flush();
                                        bs = ClearSpace.Utils.GetThumbnail(sourceImage, 62, 62); ;
                                    }
                                }
                            }
                        }
                        else
                        {
                            System.Drawing.Image sourceImage = System.Drawing.Image.FromFile(fileName);
                            int imageWidth = 0, imageHeight = 0;
                            InitializeImageSize(sourceImage, image, out imageWidth, out imageHeight);

                            bs = ClearSpace.Utils.GetThumbnail(sourceImage, imageWidth, imageHeight);

                            if (imageHeight > 62 || imageWidth > 62)
                            {
                                int divX = 0, divY = 0;
                                if (imageWidth > 62)
                                {
                                    divX = Convert.ToInt32((imageWidth - 62) >> 1);
                                }
                                if (imageHeight > 62)
                                {
                                    divY = Convert.ToInt32((imageHeight - 62) >> 1);
                                }
                                CroppedBitmap cb = new CroppedBitmap(bs, new Int32Rect(divX, divY, 62, 62));
                                bs = cb;
                            }

                            bs.Freeze();
                            // WriteableBitmap writeableBmp = new WriteableBitmap(bitmapSource);
                            sourceImage.Dispose();
                        }
                        ThumbsManager.saveThumbs(fileName, bs);
                    }
                    image.Source = bs;
                }
            }
            catch (Exception ex) {
                Console.WriteLine(ex.Message);
                ClearSpace.App.WriteLog(ex.Message, ClearSpace.Log.MsgType.Error);
            }
        }

        /// <summary>
        /// Initialize ImageSize.
        /// </summary>
        /// <param name="sourceImage"></param>
        /// <param name="image"></param>
        /// <param name="imageWidth"></param>
        /// <param name="imageHeight"></param>
        private static void InitializeImageSize(System.Drawing.Image sourceImage, System.Windows.Controls.Image image, 
            out int imageWidth, out int imageHeight)
        {
            int width = sourceImage.Width;
            int height = sourceImage.Height;
            float aspect = (float)width / (float)height;
            if (width > height)
            {
                imageHeight = 62;
                imageWidth = Convert.ToInt32(aspect * imageHeight);
            }
            else if (width < height)
            {
                imageWidth = 62;
                imageHeight = Convert.ToInt32(imageWidth / aspect);
            }
            else
            {
                imageHeight = 62;
                imageWidth = 62;
            }
        }
        #endregion ... Methods ...
    }

    /// <summary>
    /// Set ImageSource Handler.
    /// </summary>
    /// <param name="image"></param>
    /// <param name="fileName"></param>
    public delegate void SetImageSourceHandler(System.Windows.Controls.Image image, string fileName);
}
