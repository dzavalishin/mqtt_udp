local mqtt_udp_lib = {}

function mqtt_udp_lib.unpack_remaining_length(pkt)
    remaining_length = 0
    while( 1 )
    do
        pkt = strsub( pkt, 1 );
        b = pkt[0];
        remaining_length = bit.lshft( remaining_length, 7);
        remaining_length = bit.bor( remaining_length, bit.band(b, 0x7F) );
        if( bit.band(b, 0x80) == 0)
		then
            break
		end
        return remaining_length, pkt
    end
end

return mqtt_udp_lib