using System;
using System.Collections.Concurrent;
using System.Diagnostics;
using System.Threading;
using System.Threading.Tasks;

/// <summary>
/// Used to control the rate of some occurrence per unit of time.
/// </summary>
/// <remarks>
///     <para>
///     To control the rate of an action using a <see cref="RateGate"/>, 
///     code should simply call <see cref="WaitAsync()"/> prior to 
///     performing the action. <see cref="WaitAsync()"/> will block
///     the current thread until the action is allowed based on the rate 
///     limit.
///     </para>
///     <para>
///     This class is thread safe. A single <see cref="RateGate"/> instance 
///     may be used to control the rate of an occurrence across multiple 
///     threads.
///     </para>
/// </remarks>
sealed class RateGate : IDisposable
{
    static readonly long TicksMilliseconds = Stopwatch.Frequency / 1000;
    static readonly TimeSpan DisablePeriodicSignaling = TimeSpan.FromMilliseconds(-1);

    // Semaphore used to count and limit the number of occurrences per
    // unit time.
    readonly SemaphoreSlim _semaphore;

    // Times (in millisecond ticks) at which the semaphore should be exited.
    readonly ConcurrentQueue<long> _exitTimes;

    // Timer used to trigger exiting the semaphore.
    readonly Timer _exitTimer;

    // Whether this instance is disposed.
    bool _isDisposed;

    /// <summary>
    /// Number of occurrences allowed per unit of time.
    /// </summary>
    public int Occurrences { get; }

    /// <summary>
    /// The length of the time unit, in milliseconds.
    /// </summary>
    public long TimeUnitTicks { get; }

    /// <summary>
    /// Initializes a <see cref="RateGate"/> with a rate of <paramref name="occurrences"/> 
    /// per <paramref name="timeUnit"/>.
    /// </summary>
    /// <param name="occurrences">Number of occurrences allowed per unit of time.</param>
    /// <param name="timeUnit">Length of the time unit.</param>
    /// <exception cref="ArgumentOutOfRangeException">
    /// If <paramref name="occurrences"/> or <paramref name="timeUnit"/> is negative.
    /// </exception>
    public RateGate(int occurrences, TimeSpan timeUnit)
    {
        // Check the arguments.
        if (occurrences <= 0)
            throw new ArgumentOutOfRangeException(nameof(occurrences), "Number of occurrences must be a positive integer");
        if (timeUnit != timeUnit.Duration())
            throw new ArgumentOutOfRangeException(nameof(timeUnit), "Time unit must be a positive span of time");
        if (timeUnit >= TimeSpan.FromMilliseconds(UInt32.MaxValue))
            throw new ArgumentOutOfRangeException(nameof(timeUnit), "Time unit must be less than 2^32 milliseconds");

        Occurrences = occurrences;

        TimeUnitTicks = (long)(Stopwatch.Frequency * timeUnit.TotalSeconds);

        // Create the semaphore, with the number of occurrences as the maximum count.
        _semaphore = new SemaphoreSlim(Occurrences, Occurrences);

        // Create a queue to hold the semaphore exit times.
        _exitTimes = new ConcurrentQueue<long>();

        // Create a timer to exit the semaphore. Use the time unit as the original
        // interval length because that's the earliest we will need to exit the semaphore.
        //_exitTimer = new Timer(ExitTimerCallback, null, timeUnit, TimeSpan.FromMilliseconds(-1));
        _exitTimer = new Timer(ExitTimerCallback, null, timeUnit, DisablePeriodicSignaling);
    }

    // Callback for the exit timer that exits the semaphore based on exit times 
    // in the queue and then sets the timer for the next exit time.
    void ExitTimerCallback(object state)
    {
        while(true)
        {
            // While there are exit times that are passed due still in the queue,
            // exit the semaphore and dequeue the exit time.
            long exitTime;
            while (_exitTimes.TryPeek(out exitTime) && (exitTime - Stopwatch.GetTimestamp()) <= 0)
            {
                _semaphore.Release();
                _exitTimes.TryDequeue(out exitTime);
            }

            // Try to get the next exit time from the queue and compute
            // the time until the next check should take place. If the 
            // queue is empty, then no exit times will occur until at least
            // one time unit has passed.
            long ticksUntilNextCheck = _exitTimes.TryPeek(out exitTime)
                ? exitTime - Stopwatch.GetTimestamp()
                : TimeUnitTicks;

            // Set the timer.
            var dueInMilliseconds = ticksUntilNextCheck / TicksMilliseconds;

            if (dueInMilliseconds > 0)
            {
                _exitTimer.Change(dueInMilliseconds, -1);
                break;
            }
        }
    }

    /// <summary>
    /// Blocks the current thread until allowed to proceed or until the
    /// specified timeout elapses.
    /// </summary>
    /// <param name="millisecondsTimeout">Number of milliseconds to wait, or -1 to wait indefinitely.</param>
    /// <param name="cancellationToken">The System.Threading.CancellationToken to observe.</param>
    /// <returns>true if the thread is allowed to proceed, or false if timed out</returns>
    public async Task<bool> WaitAsync(int millisecondsTimeout, CancellationToken cancellationToken)
    {
        // Check the arguments.
        if (millisecondsTimeout < -1)
            throw new ArgumentOutOfRangeException(nameof(millisecondsTimeout));

        CheckDisposed();

        // Block until we can enter the semaphore or until the timeout expires.
        var entered = await _semaphore.WaitAsync(millisecondsTimeout, cancellationToken)
            .ConfigureAwait(false);

        // If we entered the semaphore, compute the corresponding exit time 
        // and add it to the queue.
        if (entered)
        {
            var timeToExit = Stopwatch.GetTimestamp() + TimeUnitTicks;
            _exitTimes.Enqueue(timeToExit);
        }

        return entered;
    }

    /// <summary>
    /// Blocks the current thread until allowed to proceed or until the
    /// specified timeout elapses.
    /// </summary>
    /// <param name="timeout">A System.TimeSpan that represents the number of milliseconds to wait, a System.TimeSpan that represents -1 milliseconds to wait indefinitely.</param>
    /// <param name="cancellationToken">The System.Threading.CancellationToken to observe.</param>
    /// <returns>true if the thread is allowed to proceed, or false if timed out</returns>
    public Task<bool> WaitAsync(TimeSpan timeout, CancellationToken cancellationToken)
    {
        return WaitAsync((int)timeout.TotalMilliseconds, cancellationToken);
    }

    /// <summary>
    /// Blocks the current thread indefinitely until allowed to proceed.
    /// </summary>
    public Task WaitAsync()
    {
        return WaitAsync(Timeout.Infinite, CancellationToken.None);
    }

    // Throws an ObjectDisposedException if this object is disposed.
    void CheckDisposed()
    {
        if (_isDisposed)
            throw new ObjectDisposedException("RateGate is already disposed");
    }

    /// <inheritdoc />
    public void Dispose()
    {
        Dispose(true);
    }

    /// <summary>
    /// Releases unmanaged resources held by an instance of this class.
    /// </summary>
    /// <param name="isDisposing">Whether this object is being disposed.</param>
    void Dispose(bool isDisposing)
    {
        if (_isDisposed) return;
        if (!isDisposing) return;
        // The semaphore and timer both implement IDisposable and 
        // therefore must be disposed.
        _semaphore.Dispose();
        _exitTimer.Dispose();
        _isDisposed = true;
    }
}