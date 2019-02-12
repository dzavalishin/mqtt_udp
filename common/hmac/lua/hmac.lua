--HMAC implementation
--http://tools.ietf.org/html/rfc2104
--http://en.wikipedia.org/wiki/HMAC

local ffi = require'ffi'
local bit = require'bit'

local function string_xor(s1, s2)
	assert(#s1 == #s2, 'strings must be of equal length')
	local buf = ffi.new('uint8_t[?]', #s1)
	for i=1,#s1 do
		buf[i-1] = bit.bxor(s1:byte(i,i), s2:byte(i,i))
	end
	return ffi.string(buf, #s1)
end

--any hash function works, md5, sha256, etc.
--blocksize is that of the underlying hash function (64 for MD5 and SHA-256, 128 for SHA-384 and SHA-512)
local function compute(key, message, hash, blocksize, opad, ipad)
   if #key > blocksize then
		key = hash(key) --keys longer than blocksize are shortened
   end
   key = key .. string.rep('\0', blocksize - #key) --keys shorter than blocksize are zero-padded
   opad = opad or string_xor(key, string.rep(string.char(0x5c), blocksize))
   ipad = ipad or string_xor(key, string.rep(string.char(0x36), blocksize))
	return hash(opad .. hash(ipad .. message)), opad, ipad --opad and ipad can be cached for the same key
end

local function new(hash, blocksize)
	return function(message, key)
		return (compute(key, message, hash, blocksize))
	end
end

local glue = require'glue'

return glue.autoload({
	new = new,
	compute = compute,
}, {
	md5 = 'hmac_md5',
	sha256 = 'hmac_sha2',
	sha384 = 'hmac_sha2',
	sha512 = 'hmac_sha2',
})
