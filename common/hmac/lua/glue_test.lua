local glue = require'glue'
require'unit'

test(glue.round(1.2), 1)
test(glue.round(-1.2), -1)
test(glue.round(1.5), 2) --half-up
test(glue.round(-1.5), -1) --half-up
test(glue.round(2^52+.49), 2^52) --largest number that works

test(glue.snap(7, 5), 5)
test(glue.snap(7.5, 5), 10) --half-up
test(glue.snap(-7.5, 5), -5) --half-up

test(glue.clamp(3, 2, 5), 3)
test(glue.clamp(1, 2, 5), 2)
test(glue.clamp(6, 2, 5), 5)

test(glue.count({[0] = 1, 2, 3, a = 4}), 4)
test(glue.count{}, 0)

test(glue.indexof('b', {'a', 'b', 'c'}), 2)
test(glue.indexof('b', {'x', 'y', 'z'}), nil)

test(glue.index{a=5,b=7,c=3}, {[5]='a',[7]='b',[3]='c'})

test(glue.keys({a=5,b=7,c=3}, true), {'a','b','c'})
test(glue.keys({'a','b','c'}, true), {1,2,3})

local t1, t2 = {}, {}
for k,v in glue.sortedpairs{c=5,b=7,a=3} do
	table.insert(t1, k)
	table.insert(t2, v)
end
test(t1, {'a','b','c'})
test(t2, {3,7,5})

test(glue.update({a=1,b=2,c=3}, {d='add',b='overwrite'}, {b='over2'}), {a=1,b='over2',c=3,d='add'})

test(glue.merge({a=1,b=2,c=3}, {d='add',b='overwrite'}, {b='over2'}), {a=1,b=2,c=3,d='add'})

test(glue.extend({5,6,8}, {1,2}, {'b','x'}), {5,6,8,1,2,'b','x'})

test(glue.append({1,2,3}, 5,6), {1,2,3,5,6})

local function insert(t,i,...)
	local n = select('#',...)
	glue.shift(t,i,n)
	for j=1,n do t[i+j-1] = select(j,...) end
	return t
end
test(insert({'a','b'}, 1, 'x','y'), {'x','y','a','b'}) --2 shifts
test(insert({'a','b','c','d'}, 3, 'x', 'y'), {'a','b','x','y','c','d'}) --2 shifts
test(insert({'a','b','c','d'}, 4, 'x', 'y'), {'a','b','c','x','y','d'}) --1 shift
test(insert({'a','b','c','d'}, 5, 'x', 'y'), {'a','b','c','d','x','y'}) --0 shifts
test(insert({'a','b','c','d'}, 6, 'x', 'y'), {'a','b','c','d',nil,'x','y'}) --out of bounds
test(insert({'a','b','c','d'}, 1, 'x', 'y'), {'x','y','a','b','c','d'}) --first pos
test(insert({}, 1, 'x', 'y'), {'x','y'}) --empty dest
test(insert({}, 3, 'x', 'y'), {nil,nil,'x','y'}) --out of bounds

local function remove(t,i,n) return glue.shift(t,i,-n) end
test(remove({'a','b','c','d'}, 1, 3), {'d'})
test(remove({'a','b','c','d'}, 2, 2), {'a', 'd'})
test(remove({'a','b','c','d'}, 3, 2), {'a', 'b'})
test(remove({'a','b','c','d'}, 1, 5), {}) --too many
test(remove({'a','b','c','d'}, 4, 2), {'a', 'b', 'c'}) --too many
test(remove({'a','b','c','d'}, 5, 5), {'a', 'b', 'c', 'd'}) --from too far
test(remove({}, 5, 5), {}) --from too far

test(glue.reverse({}), {})
test(glue.reverse({5}), {5})
test(glue.reverse({5, 2}), {2, 5})
test(glue.reverse({5, 2, 1}), {1, 2, 5})
test(glue.reverse({1, 3, 7, 5, 2}), {2, 5, 7, 3, 1})
test(glue.reverse({1, 3, 7, 5, 2}, 3), {1, 3, 2, 5, 7})
test(glue.reverse({1, 3, 7, 5, 2}, 2, 3), {1, 7, 3, 5, 2})

test(glue.binsearch(10, {}), nil)
test(glue.binsearch(10, {11}), 1)
test(glue.binsearch(11, {11}), 1)
test(glue.binsearch(12, {11}), nil)
test(glue.binsearch(12, {11, 13}), 2)
test(glue.binsearch(13, {11, 13}), 2)
test(glue.binsearch(11, {11, 13}), 1)
test(glue.binsearch(14, {11, 13}), nil)
test(glue.binsearch(10, {11, 13}), 1)
test(glue.binsearch(14, {11, 13, 15}), 3)
test(glue.binsearch(12, {11, 13, 15}), 2)
test(glue.binsearch(10, {11, 13, 15}), 1)
test(glue.binsearch(16, {11, 13, 15}), nil)

local function test1(s,sep,expect)
	local t={} for c in glue.gsplit(s,sep) do t[#t+1]=c end
	assert(#t == #expect)
	for i=1,#t do assert(t[i] == expect[i]) end
	test(t, expect)
end
test1('','',{''})
test1('','asdf',{''})
test1('asdf','',{'asdf'})
test1('', ',', {''})
test1(',', ',', {'',''})
test1('a', ',', {'a'})
test1('a,b', ',', {'a','b'})
test1('a,b,', ',', {'a','b',''})
test1(',a,b', ',', {'','a','b'})
test1(',a,b,', ',', {'','a','b',''})
test1(',a,,b,', ',', {'','a','','b',''})
test1('a,,b', ',', {'a','','b'})
test1('asd  ,   fgh  ,;  qwe, rty.   ,jkl', '%s*[,.;]%s*', {'asd','fgh','','qwe','rty','','jkl'})
test1('Spam eggs spam spam and ham', 'spam', {'Spam eggs ',' ',' and ham'})
t = {} for s,n in glue.gsplit('a 12,b 15x,c 20', '%s*(%d*),') do t[#t+1]={s,n} end
test(t, {{'a','12'},{'b 15x',''},{'c 20',nil}})
--TODO: use case with () capture

local i = 0
local function assert_lines(s, t)
	i = i + 1
	local dt = {}
	for s in glue.lines(s, '*L') do
		table.insert(dt, s)
	end
	if #t ~= #dt then goto err end
	for i=1,#t do
		if t[i] ~= dt[i] then goto err end
	end
	do return end
	::err::
	require'pp'('actual  ', #dt, dt)
	require'pp'('expected', #t, t)
	error('test '..i..' failed')
end
assert_lines('', {''})
assert_lines(' ', {' '})
assert_lines('x\ny', {'x\n', 'y'})
assert_lines('x\ny\n', {'x\n', 'y\n', ''})
assert_lines('x\n\ny', {'x\n', '\n', 'y'})
assert_lines('\n', {'\n', ''})
assert_lines('\n\r\n', {'\n','\r\n',''})
assert_lines('\r\n\n', {'\r\n','\n',''})
assert_lines('\n\r', {'\n','\r',''})
assert_lines('\n\r\n\r', {'\n','\r\n','\r',''})
assert_lines('\n\n\r', {'\n','\n','\r',''})

test(glue.trim('  a  d '), 'a  d')

test(glue.esc'^{(.-)}$', '%^{%(%.%-%)}%$')
test(glue.esc'%\0%', '%%%z%%')

if jit and jit.version:find'2%.1' then
	test(glue.tohex(0xdeadbeef01), 'deadbeef01')       --LuaJIT 2.1+
	test(glue.tohex(0xdeadbeef02, true), 'DEADBEEF02') --LuaJIT 2.1+
end
test(glue.tohex'\xde\xad\xbe\xef\x01', 'deadbeef01')
test(glue.tohex('\xde\xad\xbe\xef\x02', true), 'DEADBEEF02')
test(glue.fromhex'deadbeef01', '\xde\xad\xbe\xef\x01')
test(glue.fromhex'DEADBEEF02', '\xde\xad\xbe\xef\x02')
test(glue.fromhex'5', '\5')
test(glue.fromhex'5ff', '\5\xff')

test(glue.starts('abc', 'ab'), true)
test(glue.starts('aabc', 'ab'), false)
test(glue.starts('', ''), true)
test(glue.starts('abc', ''), true)
test(glue.starts('', 'a'), false)

test(glue.collect(('abc'):gmatch('.')), {'a','b','c'})
test(glue.collect(2,ipairs{5,7,2}), {5,7,2})

test(glue.pass(32), 32)

local t0 = {a = 1, b = 2}
local t1 = glue.inherit({}, t0)
local t2 = glue.inherit({}, t1)
assert(t2.a == 1)
assert(t2.b == 2)
t0.b = 3
assert(t2.b == 3)
glue.inherit(t1)
assert(not t2.a)

local t = {k0 = {v0 = 1}}
test(glue.attr(t, 'k0').v0, 1) --existing key
glue.attr(t, 'k').v = 1
test(t.k, {v = 1}) --created key
glue.attr(t, 'k2', 'v2')
test(t.k2, 'v2') --custom value

assert(glue.canopen('glue.lua'))
assert(glue.readfile(glue.bin..'/glue.lua'):match'glue', 'glue')
assert(glue.readfile(glue.bin..'/glue.lua'):match'glue', 'glue')

test(select(2,pcall(glue.assert,false,'bad %s','dog')), 'bad dog')
test(select(2,pcall(glue.assert,false,'bad dog %s')), 'bad dog %s')
test({pcall(glue.assert,1,2,3)}, {true, 1})
test({pcall(glue.assert,false)}, {false, 'assertion failed!'})
test({pcall(glue.assert,false,123)}, {false, '123'})
test({pcall(glue.assert,false,123,456)}, {false, '123'})

--TODO: assert, protect, pcall, fpcall, fcall

local n = 0
local f = glue.memoize(function() n = n + 1; return 6; end)
test(f(), 6)
test(f(), 6)
test(n, 1)
local n = 0
local f = glue.memoize(function(x) n = n + 1; return x and 2*x; end)
for i=1,100 do
	test(f(2), 4)
	test(f(3), 6)
	test(f(0/0), 0/0)
	test(f(), nil) --no distinction between 0 args and 1 nil arg!
	test(f(nil), nil)
end
test(n, 4)
local n = 0
local f = glue.memoize(function(x, y) n = n + 1; return x and y and x + y; end)
for i=1,100 do
	test(f(3,2), 5)
	test(f(2,3), 5)
	test(f(nil,3), nil)
	test(f(3,nil), nil)
	test(f(nil,nil), nil)
	test(f(), nil)     --no distinction between missing args and nil args!
	test(f(nil), nil)  --same here, this doesn't increment the count!
	test(f(0/0), nil)
	test(f(nil, 0/0), nil)
	test(f(0/0, 1), 0/0)
	test(f(1, 0/0), 0/0)
	test(f(0/0, 0/0), 0/0)
end
test(n, 10)
local n = 0
local f = glue.memoize(function(x, ...)
	n = n + 1
	local z = x or -10
	for i=1,select('#',...) do
		z = z + (select(i,...) or -1)
	end
	return z
end)
for i=1,100 do
	test(f(10, 1, 1), 12) --1+2 args
	test(f(), -10) --1+0 args (no distinction between 0 args and 1 arg)
	test(f(nil), -10) --same here, this doesn't increment the count!
	test(f(nil, nil), -11) --but this does: 1+1 args
	test(f(0/0), 0/0) --1+0 args with NaN
end
test(n, 4)
local n = 0
local f = glue.memoize(function(x, y, z) n = n + 1; return x + y + z + n end)
test(f(1, 1, 1), 4)
test(f(1, 1, 1, 1), 4) --arg#4 ignored even though using memoize_vararg()

local M = {}
local x, y, z, p = 0, 0, 0, 0
glue.autoload(M, 'x', function() x = x + 1 end)
glue.autoload(M, 'y', function() y = y + 1 end)
glue.autoload(M, {z = function() z = z + 1 end, p = function() p = p + 1 end})
local _ = M.x, M.x, M.y, M.y, M.z, M.z, M.p, M.p
assert(x == 1)
assert(y == 1)
assert(z == 1)
assert(p == 1)

glue.luapath('foo')
glue.cpath('bar')
glue.luapath('baz', 'after')
glue.cpath('zab', 'after')
local so = package.cpath:match'%.dll' and 'dll' or 'so'
local norm = function(s) return s:gsub('/', package.config:sub(1,1)) end
assert(package.path:match('^'..glue.esc(norm'foo/?.lua;')))
assert(package.cpath:match('^'..glue.esc(norm'bar/?.'..so..';')))
assert(package.path:match(glue.esc(norm'baz/?.lua;baz/?/init.lua')..'$'))
assert(package.cpath:match(glue.esc(norm'zab/?.'..so)..'$'))

assert(glue.writefile('glue_test.tmp', 'abc', nil, 'glue_test.tmp.tmp'))
assert(glue.readfile('glue_test.tmp') == 'abc')
assert(glue.writefile('glue_test.tmp', 'def', nil, 'glue_test.tmp.tmp'))
assert(glue.readfile('glue_test.tmp') == 'def')
os.remove('glue_test.tmp')

if jit then
	local ffi = require'ffi'

	local function malloc(bytes, ctype, size)
		local data = glue.malloc(ctype, size)
		assert(ffi.sizeof(data) == bytes)
		if size then
			assert(ffi.typeof(data) == ffi.typeof('$(&)[$]', ffi.typeof(ctype or 'char'), size))
		else
			assert(ffi.typeof(data) == ffi.typeof('$&', ffi.typeof(ctype or 'char')))
		end
		glue.free(data)
	end
	malloc(400, 'int32_t', 100)
	malloc(200, 'int16_t', 100)
	malloc(100, nil, 100)
	ffi.cdef'typedef struct { char c; } S'
	malloc(1, 'S')
	--malloc(1) --NOTE: doesn't work for primitive types
	malloc(4, 'int32_t', 1)

	assert(glue.addr(ffi.cast('void*', 0x55555555)) == 0x55555555)
	assert(glue.ptr('int*', 0x55555555) == ffi.cast('void*', 0x55555555))

	if ffi.abi'64bit' then
		assert(glue.addr(ffi.cast('void*', 0x5555555555)) == 0x5555555555)
		--going out of our way not to use the LL suffix so that Lua 5.1 can compile this.
		local huge = ffi.new('union { struct { uint32_t lo; uint32_t hi; }; struct{} *p; }',
			{lo = 0x12345678, hi = 0xdeadbeef})
		local huges = '\x78\x56\x34\x12\xef\xbe\xad\xde'
		assert(glue.addr(huge.p) == huges) --string comparison
		assert(glue.ptr('union{}*', huges) == huge.p) --pointer comparison
	end
end

--list the namespace
for k,v in glue.sortedpairs(glue) do
	print(string.format('glue.%-20s %s', k, v))
end

--TODO: freelist
