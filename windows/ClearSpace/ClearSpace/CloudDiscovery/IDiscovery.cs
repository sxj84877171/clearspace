using System;

namespace Shareit.Foundation.Discovery
{
    public interface IDiscovery
    {

        void Advertise();

        void StopAdvertise();

        void Search();

        void StopSearch();

        void Reset();
    }
}
