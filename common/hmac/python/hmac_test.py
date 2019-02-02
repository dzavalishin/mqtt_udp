import hashlib
import hmac

test_key="key"
data="text"

sig_sha256 =  hmac.new(
    key=test_key.encode('utf-8'),
    msg=data.encode('utf-8'),
    digestmod=hashlib.sha256
).hexdigest()

print( sig_sha256 )

if sig_sha256 != "6afa9046a9579cad143a384c1b564b9a250d27d6f6a63f9f20bf3a7594c9e2c6":
    print("ERROR!")
else:
    print("OK")

sig_md5 =  hmac.new(
    key=test_key.encode('utf-8'),
    msg=data.encode('utf-8'),
    digestmod=hashlib.md5
).hexdigest()

print( sig_md5 )

if sig_md5 != "d0ca6177c61c975fd2f8c07d8c6528c6":
    print("ERROR!")
else:
    print("OK")
