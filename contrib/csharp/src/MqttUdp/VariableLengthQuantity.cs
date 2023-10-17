namespace Vlq
{
    using System;
    using System.Collections.Generic;
    using System.IO;
    using System.Linq;

    static class VariableLengthQuantity
    {
        /// <summary>
        /// Reads a 7-bit encoded variable-length quantity from binary and return it as integer.
        /// </summary>
        /// <returns></returns>
        public static uint ReadVariableLengthQuantity(this BinaryReader reader)
        {
            var index = 0;
            uint buffer = 0;
            byte current;
            do
            {
                if (index++ == 8)
                    throw new FormatException("Could not read variable-length quantity from provided stream.");

                buffer <<= 7;

                current = reader.ReadByte();
                buffer |= (current & 0x7FU);
            } while ((current & 0x80) != 0);

            return buffer;
        }

        /// <summary>
        /// Writes the specified integer as a 7-bit encoded variable-length quantity.
        /// </summary>
        /// <param name="integer"></param>
        public static void WriteVariableLengthQuantity(this BinaryWriter writer, ulong integer)
        {
            if (integer > Math.Pow(2, 56))
                throw new OverflowException("Integer exceeds max value.");

            var index = 3;
            var significantBitReached = false;
            var mask = 0x7fUL << (index * 7);
            while (index >= 0)
            {
                var buffer = (mask & integer);
                if (buffer > 0 || significantBitReached)
                {
                    significantBitReached = true;
                    buffer >>= index * 7;
                    if (index > 0)
                        buffer |= 0x80;
                    writer.Write((byte)buffer);
                }
                mask >>= 7;
                index--;
            }

            if (!significantBitReached && index < 0)
                writer.Write(new byte());
        }

        //https://stackoverflow.com/questions/3563271/variable-length-encoding-of-an-integer
        public static int WriteVlq(this BinaryWriter writer, int value)
        {
            if (writer == null)
                throw new ArgumentNullException("writer");
            if (value < 0)
                throw new ArgumentOutOfRangeException("value", value, "value must be 0 or greater");
            int count = 0;

            do
            {
                byte lower7bits = (byte)(value & 0x7f);
                value >>= 7;
                if (value > 0)
                    lower7bits |= 128;
                writer.Write(lower7bits);
                count++;
            } while (value > 0);
            return count;
        }

        public static int ReadInt32Vlq(this BinaryReader reader, out int count)
        {
            if (reader == null)
                throw new ArgumentNullException("reader");

            count = 0;
            bool more = true;
            int value = 0;
            int shift = 0;
            while (more)
            {
                count++;
                byte lower7bits = reader.ReadByte();
                more = (lower7bits & 128) != 0;
                value |= (lower7bits & 0x7f) << shift;
                shift += 7;
            }
            return value;
        }
    }

    public static class VarLenQuantity
    {
        public static ulong ToVlq(ulong integer)
        {
            var array = new byte[8];
            var buffer = ToVlqCollection(integer)
              .SkipWhile(b => b == 0)
              .Reverse()
              .ToArray();
            Array.Copy(buffer, array, buffer.Length);
            return BitConverter.ToUInt64(array, 0);
        }

        public static ulong FromVlq(ulong integer)
        {
            var collection = BitConverter.GetBytes(integer).Reverse();
            return FromVlqCollection(collection);
        }

        public static IEnumerable<byte> ToVlqCollection(ulong integer)
        {
            if (integer > Math.Pow(2, 56))
                throw new OverflowException("Integer exceeds max value.");

            var index = 7;
            var significantBitReached = false;
            var mask = 0x7fUL << (index * 7);
            while (index >= 0)
            {
                var buffer = (mask & integer);
                if (buffer > 0 || significantBitReached)
                {
                    significantBitReached = true;
                    buffer >>= index * 7;
                    if (index > 0)
                        buffer |= 0x80;
                    yield return (byte)buffer;
                }
                mask >>= 7;
                index--;
            }
        }


        public static ulong FromVlqCollection(IEnumerable<byte> vlq)
        {
            ulong integer = 0;
            var significantBitReached = false;

            using (var enumerator = vlq.GetEnumerator())
            {
                int index = 0;
                while (enumerator.MoveNext())
                {
                    var buffer = enumerator.Current;
                    if (buffer > 0 || significantBitReached)
                    {
                        significantBitReached = true;
                        integer <<= 7;
                        integer |= (buffer & 0x7fUL);
                    }

                    if (++index == 8 || (significantBitReached && (buffer & 0x80) != 0x80))
                        break;
                }
            }
            return integer;
        }
    }
}



//static class BinaryReaderExtensions
//{
//    public static void Write7BitEncodedInt(this BinaryWriter bw, int i)
//    {
//        var str = bw.BaseStream;
//        switch (RtlFindMostSignificantBit((uint)i) / 7)
//        {
//            case 0:
//                str.WriteByte((byte)i);
//                break;
//            case 1:
//                str.WriteByte((byte)(i | 0x80));
//                str.WriteByte((byte)(i >> 7));
//                break;
//            case 2:
//                str.WriteByte((byte)(i /***/ | 0x80));
//                str.WriteByte((byte)(i >> 07 | 0x80));
//                str.WriteByte((byte)(i >> 14));
//                break;
//            case 3:
//                str.WriteByte((byte)(i /***/ | 0x80));
//                str.WriteByte((byte)(i >> 07 | 0x80));
//                str.WriteByte((byte)(i >> 14 | 0x80));
//                str.WriteByte((byte)(i >> 21));
//                break;
//            case 4:
//                str.WriteByte((byte)(i /***/ | 0x80));
//                str.WriteByte((byte)(i >> 07 | 0x80));
//                str.WriteByte((byte)(i >> 14 | 0x80));
//                str.WriteByte((byte)(i >> 21 | 0x80));
//                str.WriteByte((byte)((uint)i >> 28));
//                break;
//        }
//    }

//    public static int Read7BitEncodedInt(this BinaryReader br)
//    {
//        sbyte b;
//        int r = -7, v = 0;
//        do
//            v |= ((b = br.ReadSByte()) & 0x7F) << (r += 7);
//        while (b < 0);
//        return v;
//    }
//}
