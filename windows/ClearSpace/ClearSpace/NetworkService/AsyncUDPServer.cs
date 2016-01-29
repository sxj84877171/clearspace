using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading.Tasks;

namespace ClearSpace.NetworkService
{
    class AsyncUDPServer
    {
        Socket m_socket =  null;
        string m_ip = null;
        UInt16 m_port = 0;
        bool started = false;
        bool destroyed = true;
        bool initialized = false;
        AsyncUDPCallback m_callback = null;
         public AsyncUDPServer(string localIP, UInt16 port, AsyncUDPCallback callback)
        {
             m_ip = localIP;
             m_port = port;
             m_callback = callback;

        }

        public bool init()
        {
            bool ret = true;
            try{
                m_socket = new Socket(AddressFamily.InterNetwork,
                SocketType.Dgram,
                ProtocolType.Udp);

                uint IOC_IN = 0x80000000;
                uint IOC_VENDOR = 0x18000000;
                uint SIO_UDP_CONNRESET = IOC_IN | IOC_VENDOR | 12;
                m_socket.IOControl((int)SIO_UDP_CONNRESET, new byte[] { Convert.ToByte(false) }, null);

                EndPoint localEP = new IPEndPoint(IPAddress.Parse(m_ip), m_port);
                m_socket.Bind(localEP);
                ret = true;
                initialized = true;
                destroyed = false;
            }
            catch(Exception e)
            {
                Console.WriteLine(e.Message);
                ret = false;
                initialized = false;
            }

            return ret;
        }

        public void finish()
        {
            if (started) //need to call stop first
                return;
            destroyed = true;
            initialized = false;
            if (m_socket != null)
            {
                m_socket.Close();
                m_socket = null;
            }

        }

        public void start()
        {
            if (!initialized) return;
            State state = new State(m_socket);
            started = true;
            m_socket.BeginReceiveFrom(
                state.Buffer, 0, state.Buffer.Length,
                SocketFlags.None,
                ref state.RemoteEP,
                EndReceiveFromCallback,
                state);
        }

        public void stop()
        {
            started = false;
        }

        private void EndReceiveFromCallback(IAsyncResult iar)
        {
            State state = iar.AsyncState as State;
            Socket socket = state.Socket;
            
            try
            {
                //完成接收
                if (state.Buffer[0] != 0)
                {
                    int byteRead = socket.EndReceiveFrom(iar, ref state.RemoteEP);
                    string message = Encoding.Default.GetString(state.Buffer, 0, byteRead);
                    if (m_callback != null)
                    {
                        m_callback.onUDPRecvMessage(state.RemoteEP, byteRead, message);
                    }
                }
            }
            catch (Exception e)
            {
                Console.WriteLine(e.Message);
            }
            finally
            {
                //非常重要继续异步接收
                if (started)
                {
                    socket.BeginReceiveFrom(
                        state.Buffer, 0, state.Buffer.Length,
                        SocketFlags.None,
                        ref state.RemoteEP,
                        EndReceiveFromCallback,
                        state);
                }
            }
        }
 
        /// <summary>
        /// 向客户端发送信息
        /// </summary>
        /// <param name="remoteEndPoint">客户端终结点</param>
        /// <param name="Message">信息</param>
        public void SendMessage(EndPoint remoteEndPoint, string Message)
        {
            byte[] bytes = Encoding.Default.GetBytes(Message);
            m_socket.SendTo(bytes, remoteEndPoint);
        }
    }
 
    /// <summary>
    /// 用于异步接收处理的辅助类
    /// </summary>
    class State
    {
        public State(Socket socket)
        {
            this.Buffer = new byte[1024];
            this.Socket = socket;
            this.RemoteEP = new IPEndPoint(IPAddress.Any, 0);
        }
        /// <summary>
        /// 获取本机（服务器）Socket
        /// </summary>
        public Socket Socket { get; private set; }
        /// <summary>
        /// 获取接收缓冲区
        /// </summary>
        public byte[] Buffer { get; private set; }
        /// <summary>
        /// 获取/设置客户端终结点
        /// </summary>
        public EndPoint RemoteEP;
    }
  }

