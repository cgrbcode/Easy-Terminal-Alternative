function User(etaLocation) {
	this.callbackId = 0;
	this.etaServer = etaLocation;
	this.token = window.localStorage.getItem("token");
	this.setup = function() {
		var iFrame = document.createElement("iframe");
		iFrame.setAttribute("src", this.etaServer + "/external.html?external="
				+ this.token + "&site=" + window.location.host);
		iFrame.setAttribute("style", "border:none;display:none;");
		document.body.appendChild(iFrame);
	};
	this.loggedIn = function(ifunction) {
		this.makeCall("auth?token=" + this.token, function(obj) {
			ifunction(obj.loggedIn);
		});
	};
	this.randomString = function() {
		var chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz";
		var string_length = 30;
		var randomstring = '';
		for ( var i = 0; i < string_length; i++) {
			var rnum = Math.floor(Math.random() * chars.length);
			randomstring += chars.substring(rnum, rnum + 1);
		}
		return randomstring;
	};
	this.makeCall = function(url, callbackFunction) {
		var callback = "callback" + this.callbackId++;

		// [1] Create a script element.
		var script = document.createElement("script");
		script.setAttribute("src", this.etaServer + "/" + url + "&callback="
				+ callback);
		script.setAttribute("type", "text/javascript");

		// [2] Define the callback function on the window object.
		window[callback] = function(jsonObj) {
			callbackFunction(jsonObj);
			window[callback + "done"] = true;
		}
		// [4] JSON download has 1-second timeout.
		setTimeout(function() {
			if (!window[callback + "done"]) {
				callbackFunction(null);
			}

			// [5] Cleanup. Remove script and callback elements.
			document.body.removeChild(script);
			delete window[callback];
			delete window[callback + "done"];
		}, 10000);

		// [6] Attach the script element to the document body.
		document.body.appendChild(script);

	};
	if (this.token == null) {
		this.token = this.randomString();
		window.localStorage.setItem("token", this.token);
	}
}