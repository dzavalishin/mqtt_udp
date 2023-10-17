using System;
using System.Buffers.Binary;

namespace MqttUdp
{
    partial class PacketEncoder
    {
        class Reader
        {
            public byte[] Buffer { get; set; }
            public int Position { get; set; }

            public Reader(byte[] buffer)
            {
                Buffer = buffer;
            }

            public int ReadInt32Vlq(out int bytesRead)
            {
                bytesRead = 0;
                bool more = true;
                int value = 0;
                int shift = 0;
                while (more)
                {
                    bytesRead++;
                    byte lower7bits = ReadByte();
                    more = (lower7bits & 128) != 0;
                    value |= (lower7bits & 0x7f) << shift;
                    shift += 7;
                }
                return value;
            }

            public byte ReadByte()
            {
                return Buffer[Position++];
            }

            public Span<byte> ReadBytes(int length)
            {
                var result = Buffer.AsSpan(Position, length);
                Position += length;
                return result;
            }

            public long ReadInt64BigEndian()
            {
                var result = BinaryPrimitives.ReadInt64BigEndian(Buffer.AsSpan(Position, sizeof(long)));
                Position += sizeof(long);
                return result;
            }

            public int ReadInt32BigEndian()
            {
                var result = BinaryPrimitives.ReadInt32BigEndian(Buffer.AsSpan(Position, sizeof(int)));
                Position += sizeof(int);
                return result;
            }

            public short ReadInt16BigEndian()
            {
                var result = BinaryPrimitives.ReadInt16BigEndian(Buffer.AsSpan(Position, sizeof(int)));
                Position += sizeof(short);
                return result;
            }

            internal bool Peek()
            {
                return Position < Buffer.Length;
            }
        }
    }
}

