﻿#pragma checksum "..\..\MainWindow.xaml" "{406ea660-64cf-4c82-b6f0-42d48172a799}" "0057E962A8056A617AE71DEDA66D41AC"
//------------------------------------------------------------------------------
// <auto-generated>
//     This code was generated by a tool.
//     Runtime Version:4.0.30319.34014
//
//     Changes to this file may cause incorrect behavior and will be lost if
//     the code is regenerated.
// </auto-generated>
//------------------------------------------------------------------------------

using ClearSpace.Properties;
using System;
using System.Diagnostics;
using System.Windows;
using System.Windows.Automation;
using System.Windows.Controls;
using System.Windows.Controls.Primitives;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Ink;
using System.Windows.Input;
using System.Windows.Markup;
using System.Windows.Media;
using System.Windows.Media.Animation;
using System.Windows.Media.Effects;
using System.Windows.Media.Imaging;
using System.Windows.Media.Media3D;
using System.Windows.Media.TextFormatting;
using System.Windows.Navigation;
using System.Windows.Shapes;
using System.Windows.Shell;
using WMControl;


namespace ClearSpace {
    
    
    /// <summary>
    /// MainWindow
    /// </summary>
    public partial class MainWindow : System.Windows.Window, System.Windows.Markup.IComponentConnector {
        
        
        #line 20 "..\..\MainWindow.xaml"
        [System.Diagnostics.CodeAnalysis.SuppressMessageAttribute("Microsoft.Performance", "CA1823:AvoidUnusedPrivateFields")]
        internal WMControl.WMButton Min_Btn;
        
        #line default
        #line hidden
        
        
        #line 31 "..\..\MainWindow.xaml"
        [System.Diagnostics.CodeAnalysis.SuppressMessageAttribute("Microsoft.Performance", "CA1823:AvoidUnusedPrivateFields")]
        internal WMControl.WMButton Close_Btn;
        
        #line default
        #line hidden
        
        
        #line 49 "..\..\MainWindow.xaml"
        [System.Diagnostics.CodeAnalysis.SuppressMessageAttribute("Microsoft.Performance", "CA1823:AvoidUnusedPrivateFields")]
        internal System.Windows.Controls.StackPanel NoConnIndicator;
        
        #line default
        #line hidden
        
        
        #line 53 "..\..\MainWindow.xaml"
        [System.Diagnostics.CodeAnalysis.SuppressMessageAttribute("Microsoft.Performance", "CA1823:AvoidUnusedPrivateFields")]
        internal System.Windows.Controls.StackPanel ConnectedIndicator;
        
        #line default
        #line hidden
        
        
        #line 54 "..\..\MainWindow.xaml"
        [System.Diagnostics.CodeAnalysis.SuppressMessageAttribute("Microsoft.Performance", "CA1823:AvoidUnusedPrivateFields")]
        internal System.Windows.Controls.StackPanel TransferCompletedIndicator;
        
        #line default
        #line hidden
        
        
        #line 59 "..\..\MainWindow.xaml"
        [System.Diagnostics.CodeAnalysis.SuppressMessageAttribute("Microsoft.Performance", "CA1823:AvoidUnusedPrivateFields")]
        internal System.Windows.Controls.TextBlock text_TransState;
        
        #line default
        #line hidden
        
        
        #line 63 "..\..\MainWindow.xaml"
        [System.Diagnostics.CodeAnalysis.SuppressMessageAttribute("Microsoft.Performance", "CA1823:AvoidUnusedPrivateFields")]
        internal System.Windows.Controls.Image PhoneImg;
        
        #line default
        #line hidden
        
        
        #line 67 "..\..\MainWindow.xaml"
        [System.Diagnostics.CodeAnalysis.SuppressMessageAttribute("Microsoft.Performance", "CA1823:AvoidUnusedPrivateFields")]
        internal System.Windows.Controls.TextBlock text_ComputerName;
        
        #line default
        #line hidden
        
        
        #line 68 "..\..\MainWindow.xaml"
        [System.Diagnostics.CodeAnalysis.SuppressMessageAttribute("Microsoft.Performance", "CA1823:AvoidUnusedPrivateFields")]
        internal System.Windows.Controls.TextBlock text_PhoneName;
        
        #line default
        #line hidden
        
        
        #line 73 "..\..\MainWindow.xaml"
        [System.Diagnostics.CodeAnalysis.SuppressMessageAttribute("Microsoft.Performance", "CA1823:AvoidUnusedPrivateFields")]
        internal System.Windows.Controls.StackPanel UpdatePanel;
        
        #line default
        #line hidden
        
        
        #line 78 "..\..\MainWindow.xaml"
        [System.Diagnostics.CodeAnalysis.SuppressMessageAttribute("Microsoft.Performance", "CA1823:AvoidUnusedPrivateFields")]
        internal System.Windows.Controls.TextBlock OpenFileBtn;
        
        #line default
        #line hidden
        
        private bool _contentLoaded;
        
        /// <summary>
        /// InitializeComponent
        /// </summary>
        [System.Diagnostics.DebuggerNonUserCodeAttribute()]
        [System.CodeDom.Compiler.GeneratedCodeAttribute("PresentationBuildTasks", "4.0.0.0")]
        public void InitializeComponent() {
            if (_contentLoaded) {
                return;
            }
            _contentLoaded = true;
            System.Uri resourceLocater = new System.Uri("/CleanSpace;component/mainwindow.xaml", System.UriKind.Relative);
            
            #line 1 "..\..\MainWindow.xaml"
            System.Windows.Application.LoadComponent(this, resourceLocater);
            
            #line default
            #line hidden
        }
        
        [System.Diagnostics.DebuggerNonUserCodeAttribute()]
        [System.CodeDom.Compiler.GeneratedCodeAttribute("PresentationBuildTasks", "4.0.0.0")]
        [System.ComponentModel.EditorBrowsableAttribute(System.ComponentModel.EditorBrowsableState.Never)]
        [System.Diagnostics.CodeAnalysis.SuppressMessageAttribute("Microsoft.Design", "CA1033:InterfaceMethodsShouldBeCallableByChildTypes")]
        [System.Diagnostics.CodeAnalysis.SuppressMessageAttribute("Microsoft.Maintainability", "CA1502:AvoidExcessiveComplexity")]
        [System.Diagnostics.CodeAnalysis.SuppressMessageAttribute("Microsoft.Performance", "CA1800:DoNotCastUnnecessarily")]
        void System.Windows.Markup.IComponentConnector.Connect(int connectionId, object target) {
            switch (connectionId)
            {
            case 1:
            
            #line 6 "..\..\MainWindow.xaml"
            ((ClearSpace.MainWindow)(target)).MouseLeftButtonDown += new System.Windows.Input.MouseButtonEventHandler(this.Win_MouseLeftButtonDown);
            
            #line default
            #line hidden
            
            #line 7 "..\..\MainWindow.xaml"
            ((ClearSpace.MainWindow)(target)).Closing += new System.ComponentModel.CancelEventHandler(this.Window_Closed);
            
            #line default
            #line hidden
            return;
            case 2:
            this.Min_Btn = ((WMControl.WMButton)(target));
            
            #line 20 "..\..\MainWindow.xaml"
            this.Min_Btn.Click += new System.Windows.RoutedEventHandler(this.Min_Btn_Click);
            
            #line default
            #line hidden
            return;
            case 3:
            this.Close_Btn = ((WMControl.WMButton)(target));
            
            #line 31 "..\..\MainWindow.xaml"
            this.Close_Btn.Click += new System.Windows.RoutedEventHandler(this.Close_Btn_Click);
            
            #line default
            #line hidden
            return;
            case 4:
            this.NoConnIndicator = ((System.Windows.Controls.StackPanel)(target));
            return;
            case 5:
            this.ConnectedIndicator = ((System.Windows.Controls.StackPanel)(target));
            return;
            case 6:
            this.TransferCompletedIndicator = ((System.Windows.Controls.StackPanel)(target));
            return;
            case 7:
            this.text_TransState = ((System.Windows.Controls.TextBlock)(target));
            return;
            case 8:
            this.PhoneImg = ((System.Windows.Controls.Image)(target));
            return;
            case 9:
            this.text_ComputerName = ((System.Windows.Controls.TextBlock)(target));
            return;
            case 10:
            this.text_PhoneName = ((System.Windows.Controls.TextBlock)(target));
            return;
            case 11:
            this.UpdatePanel = ((System.Windows.Controls.StackPanel)(target));
            return;
            case 12:
            
            #line 75 "..\..\MainWindow.xaml"
            ((System.Windows.Controls.Button)(target)).Click += new System.Windows.RoutedEventHandler(this.UpdateYes_Click);
            
            #line default
            #line hidden
            return;
            case 13:
            
            #line 76 "..\..\MainWindow.xaml"
            ((System.Windows.Controls.Button)(target)).Click += new System.Windows.RoutedEventHandler(this.UpdateNo_Click);
            
            #line default
            #line hidden
            return;
            case 14:
            this.OpenFileBtn = ((System.Windows.Controls.TextBlock)(target));
            
            #line 78 "..\..\MainWindow.xaml"
            this.OpenFileBtn.MouseEnter += new System.Windows.Input.MouseEventHandler(this.OpenFile_MouseEnter);
            
            #line default
            #line hidden
            
            #line 78 "..\..\MainWindow.xaml"
            this.OpenFileBtn.MouseLeave += new System.Windows.Input.MouseEventHandler(this.OpenFile_MouseLeave);
            
            #line default
            #line hidden
            
            #line 78 "..\..\MainWindow.xaml"
            this.OpenFileBtn.MouseLeftButtonUp += new System.Windows.Input.MouseButtonEventHandler(this.OpenFile_Click);
            
            #line default
            #line hidden
            return;
            }
            this._contentLoaded = true;
        }
    }
}

