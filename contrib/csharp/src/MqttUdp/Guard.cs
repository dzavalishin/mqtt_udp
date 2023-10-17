using System;
using System.Runtime.CompilerServices;

class Guard
{
    [MethodImpl(MethodImplOptions.AggressiveInlining)]
    public static void Assert<T>(T expected, T actual, string? message = null)
    {
        if (!Equals(expected, actual)) throw new InvalidOperationException($"Expected {actual} to be {expected}.");
    }

    [MethodImpl(MethodImplOptions.AggressiveInlining)]
    public static void Assert(bool succes, string message)
    {
        if (!succes) throw new InvalidOperationException(message);
    }

    [MethodImpl(MethodImplOptions.AggressiveInlining)]
    public static void IsNotNull(object obj, string paramName)
    {
        if (obj == null) throw new ArgumentNullException(paramName);
    }
}
