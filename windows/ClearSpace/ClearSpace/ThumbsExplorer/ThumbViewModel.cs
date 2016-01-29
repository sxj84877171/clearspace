using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.ComponentModel;
using System.IO;

namespace ThumbsExplorer
{
    /// <summary>
    /// ThumbViewModel.
    /// </summary>
    public class ThumbViewModel : INotifyPropertyChanged
    {
        #region ... Variables ...
        /// <summary>
        /// Image FileName.
        /// </summary>
        private string mImageFileName = string.Empty;
        /// <summary>
        /// ThumbHeight.
        /// </summary>
        private int mThumbHeight;
        #endregion ... Variables ...

        #region ... Properties ...
        /// <summary>
        /// Gets or sets ImageFileName.
        /// </summary>
        public string ImageFileName
        {
            get { return mImageFileName; }
            set
            {
                if (!mImageFileName.Equals(value))
                {
                    mImageFileName = value;
                    OnPropertyChanged("ImageFileName");
                }
            }
        }
        /// <summary>
        /// Gets DisplayName.
        /// </summary>
        public string DisplayName
        {
            get
            {
                if (!string.IsNullOrEmpty(mImageFileName))
                {
                    return Path.GetFileName(mImageFileName);
                }
                return string.Empty;
            }
        }
        /// <summary>
        /// Gets or sets ThumbHeight.
        /// </summary>
        public int ThumbHeight
        {
            get { return mThumbHeight; }
            set
            {
                if (mThumbHeight != value)
                {
                    mThumbHeight = value;
                    OnPropertyChanged("ThumbHeight");
                }
            }
        }
        #endregion ... Properties ...

        #region ... Constructor ...
        /// <summary>
        /// Constructor.
        /// </summary>
        public ThumbViewModel()
        { }
        /// <summary>
        /// Constructor.
        /// </summary>
        /// <param name="imageFileName"></param>
        /// <param name="thrumbHeight"></param>
        public ThumbViewModel(string imageFileName, int thrumbHeight)
            : this()
        {
            mImageFileName = imageFileName;
            mThumbHeight = thrumbHeight;
        }
        #endregion ... Constructor ...

        #region ... Methods ...
        /// <summary>
        /// OnPropertyChanged.
        /// </summary>
        /// <param name="propertyName"></param>
        protected void OnPropertyChanged(string propertyName)
        {
            if (PropertyChanged != null)
            {
                PropertyChanged(this, new PropertyChangedEventArgs(propertyName));
            }
        }

        #endregion ... Methods ...

        #region ... INotifyPropertyChanged 成员 ...
        /// <summary>
        /// Property Changed.
        /// </summary>
        public event PropertyChangedEventHandler PropertyChanged;

        #endregion ... INotifyPropertyChanged 成员 ...
    }
}
