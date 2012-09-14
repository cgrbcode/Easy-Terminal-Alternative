function Plugin() {
	if (typeof XMLHttpRequest == "undefined")
		XMLHttpRequest = function() {
			try {
				return new ActiveXObject("Msxml2.XMLHTTP.6.0");
			} catch (e) {
			}
			try {
				return new ActiveXObject("Msxml2.XMLHTTP.3.0");
			} catch (e) {
			}
			try {
				return new ActiveXObject("Microsoft.XMLHTTP");
			} catch (e) {
			}
			throw new Error("This browser does not support XMLHttpRequest.");
		};
    this.gup=function( name ){
        name = name.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");  
        var regexS = "[\\?&]"+name+"=([^&#]*)";  
        var regex = new RegExp( regexS );  
        var results = regex.exec( window.location.href ); 
         if( results == null )    return "";  
         else    return results[1];
    }
    this.token=this.gup("session");

	this.query = function(queryString, ifunction) {
		var plugin = this;
		this.makeCall("query=" + queryString, function(obj) {
			if (obj.response == "false") {
				plugin.login();
			} else {
				ifunction(obj);
			}
		});
		return new Object();
	};

	this.getFileLink = function(filename) {
		return "/plugin?view " + filename;
	}

	this.makeCall = function(url, callbackFunction) {
		var xmlHttp = new XMLHttpRequest();
		xmlHttp.onreadystatechange = function() {
			if (xmlHttp.readyState == 4 && xmlHttp.status == 200) {
				if (xmlHttp.responseText == "Not found") {
					//do something
				} else {
					//return the restult
					var info = eval("(" + xmlHttp.responseText + ")");
					callbackFunction(info);
				}
			}
		};
		xmlHttp.open("GET", '/plugin?id='+this.token+'&'+url, true);
		xmlHttp.send(null);
	};
	
	
}
