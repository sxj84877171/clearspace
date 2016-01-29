using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ClearSpace.NetworkService
{
   public interface AsyncUDPCallback
    {
      void onUDPRecvMessage(System.Net.EndPoint client, int byteread, string message);
    }
}
