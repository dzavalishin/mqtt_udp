public class TopicFilter
{
    internal readonly string filter;

    public TopicFilter(string filter)
    {
        this.filter = filter;
    }

    public bool IsMatch(string topicName)
    {
        int tc = 0;
        int fc = 0;

        while (true)
        {
            // begin of path part
            if (filter[fc] == '+')
            {
                fc++; // eat +
                      // matches one path part, skip all up to / or end in topic
                while ((tc < topicName.Length) && (topicName[tc] != '/'))
                    tc++; // eat all non slash
                // now either both have /, or both at end
                // both finished
                if ((tc == topicName.Length) && (fc == filter.Length))
                    return true;
                // one finished, other not
                if ((tc == topicName.Length) != (fc == filter.Length))
                    return false;
                // both continue
                if ((topicName[tc] == '/') && (filter[fc] == '/'))
                {
                    tc++; fc++;
                    continue; // path part eaten
                }
                // one of them is not '/' ?
                return false;
            }
            // TODO check it to be at end?
            // we came to # in filter, done
            if (filter[fc] == '#')
                return true;
            // check parts to be equal
            while (true)
            {
                // both finished
                if ((tc == topicName.Length) && (fc == filter.Length))
                    return true;
                // one finished
                if ((tc == topicName.Length) || (fc == filter.Length))
                    return false;
                // both continue
                if ((topicName[tc] == '/') && (filter[fc] == '/'))
                {
                    tc++; fc++;
                    break; // path part eaten
                }
                // both continue
                if (topicName[tc] != filter[fc])
                {
                    return false;
                }
                // continue
                tc++; fc++;
            }
        }
    }
}
