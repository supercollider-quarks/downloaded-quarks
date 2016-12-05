PaneView {
	var <parent, <orientation, <num_of_panes, <partition,
	    <handle_size, <handle_line_size, <handle_line_color,
	    <panes, <handles, <container, <view;
	*new {|parent orientation=\horizontal num_of_panes=2 partition=\equal
		   handle_size=10, handle_line_size=2, handle_line_color|
		var children = [];
		if(num_of_panes.isArray, {
			children = num_of_panes;
			num_of_panes = children.size });
		^super.newCopyArgs(
			parent, orientation, num_of_panes, partition,
			handle_size, handle_line_size, handle_line_color).init(children) }
	*vfrom {|...children| ^this.new(nil, \vertical, children) }
	*hfrom {|...children| ^this.new(nil, \horizontal, children) }
	init {|children|
		if(handle_line_color.isNil, { handle_line_color = Color.gray(0.5) });
		if(parent.isNil, {
			view = View().minSize_(Size(*((num_of_panes-1)*handle_size).dup))
			.onResize_({|v| v.children[0].bounds_(0@0@v.bounds.extent) });
			parent = view });
		container = View(parent, parent.bounds.extent).resize_(5);
		panes = nil!num_of_panes;
		handles = nil!(num_of_panes-1);
		this.perform(orientation);
		children.do({|i,k| this.panes[k].layout_(i.isKindOf(Layout).if(
			{ i }, { HLayout(i).spacing_(0).margins_(0) }))})}
	asView { ^(view ?? container) }
	horizontal { var pv = container, hw = handle_size, hwl = handle_line_size,
		vw = (pv.bounds.width - ((num_of_panes - 1) * hw)),
		vwp = if(partition == \equal, {
			vw.partition(num_of_panes, floor(vw/num_of_panes))
		},{ var
			parts = floor(partition.normalizeSum * vw),
			diff = vw - parts.sum;
			if(diff>0, { parts[0] = parts[0] + diff });
			parts }),
		vh = pv.bounds.height;
		{ var ac = 0;
			num_of_panes.do({|n|
				panes[n] = View(pv,Rect(ac,0,vwp[n],vh)).resize_(4);
				ac = ac + vwp[n];
				if(n + 1 < num_of_panes, {
					handles[n] = {|i| var is_drag;
						i.mouseDownAction_({ is_drag = true })
						.mouseUpAction_({ is_drag = false })
						.mouseMoveAction_({|v,l|
							l = l - round(hw/2);
							if(is_drag and: not((panes[n].bounds.width + l <= 0) or: (panes[n+1].bounds.width - l <= 0)), {
								panes[n].resizeTo(panes[n].bounds.width + l, vh);
								panes[n+1].resizeTo(panes[n+1].bounds.width - l, vh);
								panes[n+1].moveTo(panes[n+1].bounds.left + l, 0);
								i.moveTo(handles[n].bounds.left + l, 0) })});
						View(i,Rect(hw-hwl/2,0,hwl,vh)).background_(handle_line_color).resize_(4);
						i }.(View(pv,Rect(ac,0,hw,vh)).resize_(4));
					ac = ac + hw })}) }.();
		{ var prev_size = pv.bounds.width;
			pv.onResize_({|v| if(num_of_panes-1 * hw <= v.bounds.width, {
				var diff = (v.bounds.width - prev_size);
				vh = pv.bounds.height;
				if(diff < 0, { var num = panes.select({|p| p.bounds.width > 0}).size;
					if(num > 0, { var ac = 0, leftover = 0,
						dp = diff.abs.partition(num,floor(diff.abs/num));
						dp = dp.neg.iter;
						panes.collect({|p n|
							p.moveTo(p.bounds.left + ac, 0);
							if(p.bounds.width > 0, {
								var l = dp.next + leftover, lr = p.bounds.width + l;
								p.resizeTo(lr.max(0), vh);
								if(lr < 0, {
									leftover = lr;
									ac = ac - p.bounds.width;
								}, {
									leftover = 0;
									ac = ac + l })});
							if(n + 1 < num_of_panes, { handles[n].moveTo(handles[n].bounds.left + ac, 0) })})})
				}, { var ac = 0, dp = diff.partition(num_of_panes,floor(diff/num_of_panes));
					panes.collect({|p n| var l = dp[n];
						p.moveTo(p.bounds.left + ac, 0);
						if(l > 0, {
							p.resizeTo(p.bounds.width + l, vh);
							ac = ac + l });
						if(n + 1 < num_of_panes, { handles[n].moveTo(handles[n].bounds.left + ac, 0) })})});
				prev_size = v.bounds.width
			},{ var ac = 0;
				prev_size = num_of_panes - 1 * hw;
				panes.do({|p n|
					p.resizeTo(0,vh);
					p.moveTo(ac,0);
					if(n + 1 < num_of_panes, { handles[n].moveTo(hw*n,0) });
					ac = ac + hw });
			})}) }.() }
	vertical { var pv = container, hh = handle_size, hhl = handle_line_size, // same as horizontal - too lazy today to encapsultate logic :)
		vh = (pv.bounds.height - ((num_of_panes - 1) * hh)),
		vhp = if(partition == \equal, {
			vh.partition(num_of_panes, floor(vh/num_of_panes))
		},{ var
			parts = floor(partition.normalizeSum * vh),
			diff = vh - parts.sum;
			if(diff>0, { parts[0] = parts[0] + diff });
			parts }),
		vw = pv.bounds.width;
		{ var ac = 0;
			num_of_panes.do({|n|
				panes[n] = View(pv,Rect(0,ac,vw,vhp[n])).resize_(2);
				ac = ac + vhp[n];
				if(n + 1 < num_of_panes, {
					handles[n] = {|i| var is_drag;
						i.mouseDownAction_({ is_drag = true })
						.mouseUpAction_({ is_drag = false })
						.mouseMoveAction_({|v,x,l|
							l = l - round(hh/2);
							if(is_drag and: not((panes[n].bounds.height + l <= 0) or: (panes[n+1].bounds.height - l <= 0)), {
								panes[n].resizeTo(vw, panes[n].bounds.height + l);
								panes[n+1].resizeTo(vw, panes[n+1].bounds.height - l);
								panes[n+1].moveTo(0, panes[n+1].bounds.top + l);
								i.moveTo(0, handles[n].bounds.top + l) })});
						View(i,Rect(0,round(hh-hhl/2),vw,hhl)).background_(handle_line_color).resize_(2);
						i }.(View(pv,Rect(0,ac,vw,hh)).resize_(2));
					ac = ac + hh })}) }.();
		{ var prev_size = pv.bounds.height;
			pv.onResize_({|v| if(num_of_panes-1 * hh <= v.bounds.height, {
				var diff = (v.bounds.height - prev_size);
				vw = pv.bounds.width;
				if(diff < 0, { var num = panes.select({|p| p.bounds.height > 0}).size;
					if(num > 0, { var ac = 0, topover = 0,
						dp = diff.abs.partition(num,floor(diff.abs/num));
						dp = dp.neg.iter;
						panes.collect({|p n|
							p.moveTo(0, p.bounds.top + ac);
							if(p.bounds.height > 0, {
								var l = dp.next + topover, lr = p.bounds.height + l;
								p.resizeTo(vw, lr.max(0));
								if(lr < 0, {
									topover = lr;
									ac = ac - p.bounds.height;
								}, {
									topover = 0;
									ac = ac + l })});
							if(n + 1 < num_of_panes, { handles[n].moveTo(0, handles[n].bounds.top + ac) })})})
				}, { var ac = 0, dp = diff.partition(num_of_panes,floor(diff/num_of_panes));
					panes.collect({|p n| var l = dp[n];
						p.moveTo(0, p.bounds.top + ac);
						if(l > 0, {
							p.resizeTo(vw, p.bounds.height + l);
							ac = ac + l });
						if(n + 1 < num_of_panes, { handles[n].moveTo(0, handles[n].bounds.top + ac) })})});
				prev_size = v.bounds.height
			},{ var ac = 0;
				prev_size = num_of_panes - 1 * hh;
				panes.do({|p n|
					p.resizeTo(vw,0);
					p.moveTo(0,ac);
					if(n + 1 < num_of_panes, { handles[n].moveTo(0,hh*n) });
					ac = ac + hh });
			})}) }.() } }
