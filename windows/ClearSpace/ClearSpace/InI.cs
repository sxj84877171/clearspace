using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Runtime.InteropServices;
using System.Windows.Interop;
namespace ClearSpace
{
    public class IniFile
    {
        public string path;     //INI文件名

        //声明读写INI文件的API函数     
        public IniFile(string INIPath)
        {
            path = INIPath;
        }

        //类的构造函数，传递INI文件名
        public void IniWriteValue(string Section, string Key, string Value)
        {
            NativeMethodsCall.WritePrivateProfileString(Section, Key, Value, this.path);
        }

        //读INI文件         
        public string IniReadValue(string Section, string Key)
        {
            StringBuilder temp = new StringBuilder(256);
            int i = NativeMethodsCall.GetPrivateProfileString(Section, Key, "", temp, 256, this.path);
            return temp.ToString();
        }
    }
}
