using System;
using System.Buffers.Binary;

namespace MqttUdp
{
    partial class PacketEncoder
    {
        class Writer
        {
            public byte[] Buffer { get; set; }
            public int Position { get; set; }

            public Writer(int bufferSize = 512) // Max UDP :-)
            {
                Buffer = new byte[bufferSize];
            }

            public int WriteVlq(int value)
            {
                int count = 0;
                do
                {
                    byte lower7bits = (byte)(value & 0x7f);
                    value >>= 7;
                    if (value > 0)
                        lower7bits |= 128;
                    Write(lower7bits);
                    count++;
                } while (value > 0);
                return count;
            }

            public void Write(byte value)
            {
                Buffer[Position++] = value;
            }

            public void WriteBigEndian(short value)
            {
                BinaryPrimitives.WriteInt16BigEndian(Buffer.AsSpan(Position, sizeof(short)), value);
                Position += sizeof(short);
            }

            public void Write(ReadOnlySpan<byte> data)
            {
                data.CopyTo(Buffer.AsSpan(Position, data.Length));
                Position += data.Length;
            }

            public void WriteBigEndian(long value)
            {
                BinaryPrimitives.WriteInt64BigEndian(Buffer.AsSpan(Position, sizeof(long)), value);
                Position += sizeof(long);
            }
            public void WriteBigEndian(int value)
            {
                BinaryPrimitives.WriteInt32BigEndian(Buffer.AsSpan(Position, sizeof(int)), value);
                Position += sizeof(int);
            }
        }
    }
}

