-- implementing some functions from BitOp (http://bitop.luajit.org/) on Lua 5.3
-- copied (thanx!) from https://github.com/xHasKx/luamqtt/blob/master/mqtt/bit.lua

return {
	lshift = function(x, n)
		return x << n
	end,
	rshift = function(x, n)
		return x >> n
	end,
	bor = function(x1, x2)
		return x1 | x2
	end,
	band = function(x1, x2)
		return x1 & x2
	end,
}
