Freesound{
	classvar <uris;
	classvar <>token;
	classvar <>authType  = "token";
	classvar <parseFunc;
	classvar <>server;
	classvar <>clientId;
	classvar <>clientSecret;
	classvar <>tokenFile;
	classvar <defaultFields = "id,name,tags,username,license,previews";

	*parseJSON{|jsonStr|
		var parsed = jsonStr;
		var a,x;
		jsonStr.do({|char,pos|
			var inString = false;
			char.switch(
				$",{(jsonStr[pos-1]==$\ && inString).not.if({inString = inString.not})},
				${,{ if(inString.not){parsed[pos] = $(} },
				$},{ if(inString.not){parsed[pos] = $)} }
			)
		});
		^parsed.interpret;
	}

	*initClass{
		uris = (
		\BASE :  "https://www.freesound.org/apiv2",
		\TEXT_SEARCH : "/search/text/",
		\CONTENT_SEARCH : "/search/content/",
		\COMBINED_SEARCH : "/search/combined/",
		\SOUND : "/sounds/%/",
		\SOUND_ANALYSIS : "/sounds/%/analysis/",
		\SIMILAR_SOUNDS : "/sounds/%/similar/",
		\COMMENTS : "/sounds/%/comments/",
		\DOWNLOAD : "/sounds/%/download/",
		\UPLOAD : "/sounds/upload/",
		\DESCRIBE : "/sounds/%/describe/",
		\PENDING : "/sounds/pending_uploads/",
		\BOOKMARK : "/sounds/%/bookmark/",
		\RATE : "/sounds/%/rate/",
		\COMMENT : "/sounds/%/comment/",
		\AUTHORIZE : "/oauth2/authorize/",
		\LOGOUT : "/api-auth/logout/",
		\LOGOUT_AUTHORIZE : "/oauth2/logout_and_authorize/",
		\ME : "/me/",
		\USER : "/users/%/",
		\USER_SOUNDS : "/users/%/sounds/",
		\USER_PACKS : "/users/%/packs/",
		\PACK : "/packs/%/",
		\PACK_SOUNDS : "/packs/%/sounds/",
		\PACK_DOWNLOAD : "/packs/%/download/"
		);

		try{
				parseFunc = {|str| str.parseYAML}
		}{
				parseFunc = {|str| Freesound.parseJSON(str)};
		};
		tokenFile = PathName(Freesound.class.filenameSymbol.asString).pathOnly;
		tokenFile = tokenFile ++ Platform.pathSeparator++"token.json"
	}

	*uri{|uri_key,args|
		^(uris[\BASE] ++ uris[uri_key].format(args));
	}


	*getAuthorizationPage{
		"Open this url in your browser:".postln;
		"-------------------------------".postln;
		(Freesound.uri(\AUTHORIZE) ++ "?" ++ "client_id=" ++
		Freesound.clientId ++ "&response_type=code").postln;
		"-------------------------------".postln;
	}

	*refreshToken{
		var tokenObj = parseFunc.value(File(tokenFile, "r").readAllString);
		Freesound.getToken(tokenObj["refresh_token"], refresh: true);
	}

	*getToken{|code, refresh = false|
		var tmpFile = PathName.tmp ++ "fs_" ++ UniqueID.next ++ ".txt";
		var cmd =  "curl -X POST -d 'client_id=%&client_secret=%";
		cmd = format(cmd, Freesound.clientId, Freesound.clientSecret);
		if(refresh){
			cmd = cmd ++ "&grant_type=refresh_token&refresh_token=%"
		}{
			cmd = cmd ++ "&grant_type=authorization_code&code=%"
		};
		cmd = format(cmd, code);
		cmd = cmd ++ "' https://www.freesound.org/apiv2/oauth2/access_token/ >'%'";
		cmd = format(cmd, tmpFile);
		cmd.unixCmd({|res, pid|
			var tokenObj = parseFunc.value(File(tmpFile,"r").readAllString);
			tokenObj.postln;
			if(tokenObj.includesKey("access_token")){
				Freesound.token = tokenObj["access_token"];
				File.delete(tokenFile);
				File.copy(tmpFile, tokenFile);
			}
		});
	}
}

FSReq{
	var <url, <filePath, <cmd;
	*new{|anUrl,params|
		if(Freesound.token.isNil){throw("API key is not set! Can't proceed")};
		^super.new.init(anUrl, params);
	}

	*getHeader{
		var header ="Authorization:";
		if (Freesound.authType == "oauth2")
		    {header = header + "Bearer"}
		{header = header + "Token" };
		header = header + Freesound.token;
		^header;
	}

	init{|anUrl, params, method="GET"|
		var paramsString, separator = "?";
		url = anUrl;
		filePath = PathName.tmp ++ "fs_" ++ UniqueID.next ++ ".txt";
		params = params?IdentityDictionary.new;
		paramsString = params.keys(Array).collect({|k|
			k.asString ++ "=" ++ params[k].asString.urlEncode}).join("&");
		if (url.contains(separator)){separator = "&"};
		cmd = "curl -H '%' '%'>% ".format(
			FSReq.getHeader, this.url ++ separator ++ paramsString, filePath
		);
		cmd.postln;
	}

	get{|action, objClass|
		cmd.unixCmd({|res,pid|
			var result = objClass.new(
				File(filePath,"r").readAllString.postln;
				Freesound.parseFunc.value(
					File(filePath,"r").readAllString
				)
			);
			action.value(result);
		});
	}

	*retrieve{|uri,path,action|
		var cmd;
		cmd = "curl -H '%' '%'>'%'".format(FSReq.getHeader, uri, path);
		cmd.postln;
		cmd.unixCmd(action);
	}
}

FSObj : Object{
	var <dict;
	*new{|jsonDict|
		^super.new.init(jsonDict);
	}

	init{|jsonDict|
		dict = jsonDict.as(Dictionary);
		dict.keysDo{|k|
			this.addUniqueMethod(k.replace("-","_").asSymbol,{
				var obj = dict[k];
				if (obj.isKindOf(Dictionary)){obj=FSObj.new(obj)};
				obj;
			});
		};
	}
	at{|x| ^this.dict.at(x)}
}



FSPager : FSObj {
	next{|action|
		FSReq.new(dict["next"]).get(action,FSPager);
	}

	prev{|action|
		FSReq.new(dict["prev"]).get(action,FSPager);
	}

	at{|i|
		^FSSound.new(this.results[i]);
	}

	do{|f|
		this.results.do({|snd,i| f.value( FSSound.new(snd))});
	}

}

FSSound : FSObj{
	*initParams{|params|
		if (params.isNil){params = ()};
		if(params.includesKey("fields").not){
			params["fields"] = Freesound.defaultFields
		};
		^params;
	}

	*getSound{|soundId, action|
		FSReq.new(Freesound.uri(\SOUND,soundId)).get(action,FSSound);
	}

	*textSearch{|query, filter, sort, params, action|
		params = FSSound.initParams(params);
		params.putAll(('query' : query, 'filter' : filter, 'sort' : sort));
		FSReq.new(Freesound.uri(\TEXT_SEARCH),params).get(action,FSPager);
	}

	*contentSearch{|target, filter, params, action|
		params = FSSound.initParams(params);
		params.putAll(('target' : target, 'filter' : filter));
		FSReq.new(Freesound.uri(\CONTENT_SEARCH), params).get(action,FSPager);
	}

	*combinedSearch{|query, filter, target, descriptorsFilter, sort, params, action|
		params = FSSound.initParams(params);
		params.putAll(('query' : query, 'filter' : filter,
			'sort' : sort, 'target' : target, 'descriptors_filter' : descriptorsFilter));
		FSReq.new(Freesound.uri(\COMBINED_SEARCH), params).get(action,FSPager);
	}

	retrieve{|path, action|
		FSReq.retrieve(Freesound.uri(\DOWNLOAD, this.id),
			path++this.name,
			action
		);
	}

	previewFilename{|format = "ogg"|
		^this.name.splitext[0]++"."++format;
	}

	retrievePreview{|path, action, quality = "hq", format = "ogg"|
		var key = "%-%-%".format("preview",quality,format);
		FSReq.retrieve(
			this.previews.dict[key],
			path ++ this.previewFilename(format),
			action
		);
	}

	getAnalysis{|descriptors, action|
		var url = Freesound.uri(\SOUND_ANALYSIS,this.id);
		var params = nil;
		if(descriptors.notNil){params = ('descriptors' : descriptors)};
		FSReq.new(url,params).get(action,FSObj);
	}

	retrieveAnalysisFrames{|path, action|
		var fname = this.original_filename.splitext[0]++".json";
		FSReq.retrieve(this.analysis_frames,path++"/"++fname,action);
	}

	getSimilar{|params, action|
		var url = Freesound.uri(\SIMILAR_SOUNDS,this.id);
		params = FSSound.initParams(params);
		FSReq(url,params).get(action,FSPager);
	}

}


+String{
	urlEncode{
		var str="";
		this.do({|c|
			if(c.isAlphaNum)
			{str = str++c}
			{str=str++"%"++c.ascii.asHexString(2)}
		})
		^str;
	}
}
