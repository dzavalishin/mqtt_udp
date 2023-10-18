using System.Net;

static class IPAddressExt
{
#pragma warning disable CS0618 // Type or member is obsolete
    static readonly long start = IPAddress.Parse("224.0.1.0").Address;
    static readonly long end = IPAddress.Parse("239.255.255.255").Address;

    public static bool IsIpv4MulticastAddress(this IPAddress address)
    {
        return address.AddressFamily == System.Net.Sockets.AddressFamily.InterNetwork && address.Address >= start && address.Address <= end;
    }
#pragma warning restore CS0618 // Type or member is obsolete
}
