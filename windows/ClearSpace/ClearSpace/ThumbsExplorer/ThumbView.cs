using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Windows.Controls;
using System.Windows;

namespace ThumbsExplorer
{
    /// <summary>
    /// Thumb View For ListView.
    /// </summary>
    public class ThumbView : ViewBase
    {
        /// <summary>
        /// Gets the Default Style Key.
        /// </summary>
        protected override object DefaultStyleKey
        {
            get { return new ComponentResourceKey(GetType(), "ThumbView"); }
        }
        /// <summary>
        /// Gets Item Container Default Style Key.
        /// </summary>
        protected override object ItemContainerDefaultStyleKey
        {
            get { return new ComponentResourceKey(GetType(), "ThumbViewItem"); }
        }
    }
}
