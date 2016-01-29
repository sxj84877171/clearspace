using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace ClearSpace.NetworkService
{
   public interface CounterpartScanServiceCallback
    {
       /// <summary>
       /// find mobile softap
       /// </summary>
       /// <param name="name"></param>
       /// <returns> true: try connect; otherwise false</returns>
       bool CounterpartDiscovered(string name);

       /// <summary>
       /// 
       /// </summary>
       void Connected2Counterpart();

       void ConnFailed();
    }
}
