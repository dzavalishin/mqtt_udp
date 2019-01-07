#BEGIN { if(ARGC < 2) }

#! /@version@/ { print $0 }
  /@version@/ { sub( "@version@", ver, $0 ); print $0; next }
  /@release@/ { sub( "@release@", rel, $0 ); print $0; next }

  { print $0 }
