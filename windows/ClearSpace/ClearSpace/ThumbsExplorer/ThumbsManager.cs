using System;
using System.Collections.Generic;
using System.Configuration;
using System.Data;
using System.Linq;
using System.Windows;
using System.Windows.Input;
using Microsoft.Win32;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.IO;
using System.Windows.Media.Imaging;
namespace ThumbsExplorer
{

    public class MyList : ObservableCollection<MyListItem>
    {

    }

    public class MyListItem : INotifyPropertyChanged
    {
        private string mImgGroupDate = String.Empty;

        private ThumbViewModelCollection mThumbsList = new ThumbViewModelCollection();
        public event PropertyChangedEventHandler PropertyChanged;

        public MyListItem(string date)
        {
            mImgGroupDate = date;
        }

        public string ImgGroupDate
        {
            get {return mImgGroupDate;}
        }

        public int ImgCount
        {
            get { 
                return mThumbsList.Count; 
            }
            set {
                OnPropertyChanged("ImgCount"); 
            }
        }

        public ThumbViewModelCollection ThumbsList
        {
            get
            {
                return mThumbsList;
            }
        }

        protected void OnPropertyChanged(string propertyName)
        {
            if (PropertyChanged != null)
            {
                PropertyChanged(this, new PropertyChangedEventArgs(propertyName));
            }
        }
    }


    public class ThumbsListComparer : IComparer<string>
    {
        public int Compare(string x, string y)
        {
            FileInfo fix = new FileInfo(x);
            FileInfo fiy = new FileInfo(y);

            TimeSpan ts = fiy.CreationTimeUtc - fix.CreationTimeUtc;
            return (int)ts.TotalMilliseconds;
        }
    }

    public class ThumbsManager
    {
        private string mImgFolder = String.Empty;
        private static Dictionary<string, BitmapSource> mThumbsDic =  new Dictionary<string, BitmapSource>();
        private MyList mGroupedThumbsList = new MyList();
        public MyList DateGroupedThumbsList
        {
            get
            {
                return mGroupedThumbsList;
            }
        }
        public ThumbsManager()
        {
            mImgFolder = ".";
        }

        public ThumbsManager(string imgFolder)
        {
            mImgFolder = imgFolder;
        }

        
        /// <summary>
        /// generate thumbs list of the target img folder
        /// return bool
        /// </summary>
        public bool init()
        {
            bool ret = true;

            if (Directory.Exists(mImgFolder))
            {
                //go through img folder here, grouped by creation date
                ret = generateThumbsList();
            }
            else
            {
                ret = false;
            }

            return ret;
        }

        private bool generateThumbsList()
        {
            bool ret = true;
            List<string> imgFiles = ClearSpace.Utils.GetAllFils(this.mImgFolder,"img|video");
            imgFiles.Sort(new ThumbsListComparer());
            foreach (string s in imgFiles)
            {
                if (File.Exists(s))
                {
                   FileInfo fi = new FileInfo(s);
                   string date = String.Format("{0}.{1}.{2}", fi.CreationTime.Year, fi.CreationTime.Month, fi.CreationTime.Day);
                   bool b = false; //find or not find
                   foreach(MyListItem mi in mGroupedThumbsList)
                   {
                       if (mi.ImgGroupDate.Equals(date))
                       {
                           mi.ThumbsList.Add(new ThumbViewModel(s, 0));
                           b = true;
                       }
                   }
                   if (!b)
                   {
                       MyListItem mli = new MyListItem(date);
                       mli.ThumbsList.Add(new ThumbViewModel(s, 0));
                       mGroupedThumbsList.Add(mli);
                   }
                }
            }
            return ret;
        }

        public bool reset(string newFolder)
        {
            bool ret = true;
            if (!newFolder.Equals(mImgFolder))
            {
                mGroupedThumbsList.Clear();
                mThumbsDic.Clear();
                mImgFolder = newFolder;
                ret = init();
            }
            return ret;
        }

        public void addNewImg(string s)
        {
            if (File.Exists(s))
            {
                FileInfo fi = new FileInfo(s);
                string date = String.Format("{0}.{1}.{2}", fi.CreationTime.Year, fi.CreationTime.Month, fi.CreationTime.Day);
                bool b = false; //find or not find
                foreach (MyListItem mi in mGroupedThumbsList)
                {
                    if (mi.ImgGroupDate.Equals(date))
                    {
                        foreach (ThumbViewModel tvm in mi.ThumbsList)
                        {
                            if (tvm.ImageFileName.Equals(s))
                                return;
                        }
                        mi.ThumbsList.Insert(0,new ThumbViewModel(s, 0));
                        mi.ImgCount = 0; //set to 0, just trigger the ui update
                        b = true;
                    }
                }
                if (!b)
                {
                    MyListItem mli = new MyListItem(date);
                    mli.ThumbsList.Add(new ThumbViewModel(s, 0));
                    mli.ImgCount = 0;
                    foreach (MyListItem m in mGroupedThumbsList)
                    {
                        DateTime d1 = DateTime.Parse(m.ImgGroupDate);
                        DateTime d2 = DateTime.Parse(mli.ImgGroupDate);
                        TimeSpan ts = d1 - d2;
                        if (ts.TotalMilliseconds < 0)
                        {
                            mGroupedThumbsList.Insert(mGroupedThumbsList.IndexOf(m), mli);
                            return;
                        }
                    }
                    mGroupedThumbsList.Add(mli);
                }
            }
        }

        public static void saveThumbs(string filename, BitmapSource bitmap)
        {
            if (!String.IsNullOrEmpty(filename))
            {
                mThumbsDic.Add(filename, bitmap);
            }
        }

        public static BitmapSource getThumbs(string filename)
        {
            BitmapSource bs = null;
            if (!String.IsNullOrEmpty(filename))
            {
                if (mThumbsDic.ContainsKey(filename))
                    bs = mThumbsDic[filename];
            }
            return bs;
        }
    }
}