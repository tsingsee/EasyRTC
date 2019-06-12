'use strict';

var rpcClient = null;
var pc = null;
var sessid;
var remoteVideo;
var vertoObj = null;
var localStream = null;

var hostName = window.location.hostname;
if (hostName.indexOf("127.0.0.1") >= 0 || hostName.indexOf("192.168") >= 0)
    hostName = "rtc.hfmtx.com";
console.log("The host name: " + hostName);

$(document).ready(function() {
    pop("#login", "verto_demo_login", "1008");
    pop("#passwd", "verto_demo_passwd", "1111");
    pop("#ext", "verto_demo_ext", "3580");

    sessid = $.genUUID();
    remoteVideo = $('#webcam')[0];

    $("#loginbtn").click(function() {
        login();
    });

    $("#logoutbtn").click(function() {
        logout();
    });

    $("#callbtn").click(function() {
        docall();
    });

    $("#hupbtn").click(function() {
        hangup();
    });

    online(false);
    login();
});

function login() {
    goto_page("progress");

    rpcClient = new $.JsonRpcClient({
        login: $("#login").val() + "@" + hostName,
        passwd: $("#passwd").val(),
        socketUrl: "wss://" + hostName + ":8082",
        // socketUrl: "wss://easyrtc.easydss.com:8082",
        loginParams: null,
        userVariables: null,
        sessid: sessid,
        onmessage: function(e) {
            console.log("onMessage: " + JSON.stringify(e));

            var data = JSON.parse(e.data);
            if (data.params.sdp) {
                var answer = {
                    type : "answer",
                    sdp : data.params.sdp
                };
                pc.setRemoteDescription(answer, function (desc) {
                    console.log("Set remote description success");
                }, function (e) {
                    console.error("Cannot set remote desc: " + e);
                });

                goto_page("incall");
            }
        },
        onWSConnect: function(o) {
            console.log("onWSConnect: " + o);
            o.call('login', {});
        },
        onWSLogin: function(success) {
            console.log("onWSLogin: " + success);
            setTimeout(function() {
                goto_page("main");
                online(success);
            }, 1000);
        },
        onWSClose: function(success) {
            console.log("onWSClose: " + success);
            setTimeout(function() {
                goto_page("main");
                online(false);
            }, 1000);
        }
    });
    rpcClient.call('login', {});
}

function logout() {
    rpcClient.closeSocket();
    online(false);
}

function hangup() {
    rpcClient.call('verto.bye', vertoObj);
    vertoObj = null;
    if (pc) {
        pc.close();
        pc = null;
    }
    if (localStream) {
        if (localStream.stop) {
              localStream.stop();
        } else {
              localStream.getTracks().forEach(function(track) {
                  track.stop();
              });
        }
        localStream = null;
    }
    goto_page("main");
}

function docall() {
    var options = {
        audio: true,
        video: { width: { min: 160, max: 640 } }
        // video: { width: { min: 90, max: 320 } }
    };
	
	navigator.mediaDevices.getUserMedia(options).then(function(stream) {
	    console.log('Use local stream: ' + stream);
        localStream = stream;
	  
	    var videoTracks = stream.getVideoTracks();
	    var audioTracks = stream.getAudioTracks();
	    if (videoTracks.length > 0)
	  	    console.log('Using video device: ' + videoTracks[0].label);
	    if (audioTracks.length > 0)
		    console.log('Using audio device: ' + audioTracks[0].label);

		var configuration = {
			iceServers: [{
				"urls": "stun:46.101.0.44"
			}]
		};
		pc = new RTCPeerConnection(configuration);
		pc.onicecandidate = function(candidate) {
			if (!candidate.candidate)
				return;
			
			console.log("Local candidate" + JSON.stringify(candidate.candidate));
            console.log("local desc: " + pc.localDescription);
		};
		pc.oniceconnectionstatechange = function(event) {
			console.log("oniceconnectionstatechange: " +
                    event.target.iceConnectionState);
		};
        pc.onaddstream = function(e) {
            console.log("onaddstream: " + e.stream);
            remoteVideo.srcObject = e.stream;
        };

		stream.getTracks().forEach(function(track) {
			pc.addTrack(track, stream);
	    });

		var offerOptions = {
		    offerToReceiveAudio: 1,
		    offerToReceiveVideo: 1
		};
        pc.createOffer(offerOptions).then(
                function (offerSdp) {
                    pc.setLocalDescription(offerSdp, function(e) {
                        console.log("Set local desc success: " + offerSdp);
                        goto_page("progress");
                        setTimeout(sdpTimeout, 2000);
                    }, function(e) {
                        console.error("Set local desc fail: " + e)
                    });
                }, function(e) {
                    console.error('Cannot create offer ' + e);
                });
    }).catch(function(e) {
        console.error('Cannot get user media: ' + e);
    });
}

function sdpTimeout() {
    console.log("Timeout local sdp: " + pc.localDescription.sdp);

    var dialogParams = {
        callID: $.genUUID(),
        destination_number: $("#ext").val(),
        localTag: null,
        login: $("#login").val() + "@" + hostName,
        remote_caller_id_name: "Outbound Call",
        remote_caller_id_number: $("#ext").val(),
        screenShare: false,
        tag: "webcam",
        useMic: "any",
        useSpeak: "any",
        useStereo: false,
        useVideo: true,
        /*
        videoParams: {
            "minWidth": "320",
            "minHeight": "180",
            "maxWidth": "640",
            "maxHeight": "360",
            "minFrameRate": 15
        },
        */
    }

    vertoObj = {
        dialogParams: dialogParams,
        sdp: pc.localDescription.sdp,
        sessid: sessid
    }
    rpcClient.call('verto.invite', vertoObj);
}


function pop(id, cname, dft) {
    var tmp = $.cookie(cname) || dft;
    $.cookie(cname, tmp, {
        expires: 365
    });
    $(id).val(tmp).change(function() {
        $.cookie(cname, $(id).val(), {
            expires: 365
        });
    });
}

function goto_page(where, force) {
    $( ":mobile-pagecontainer" ).pagecontainer( "change", "#page-" + where);
}

function online(on) {
    if (on) {
        $("#online").show();
        $("#offline").hide();
    } else {

        $("#online").hide();
        $("#offline").show();
    }
}

(function($) {
    var generateGUID = (typeof (window.crypto) !== 'undefined' && typeof (window.crypto.getRandomValues) !== 'undefined') ? function() {
        var buf = new Uint16Array(8);
        window.crypto.getRandomValues(buf);
        var S4 = function(num) {
            var ret = num.toString(16);
            while (ret.length < 4) {
                ret = "0" + ret;
            }
            return ret;
        };
        return (S4(buf[0]) + S4(buf[1]) + "-" + S4(buf[2]) + "-" + S4(buf[3]) + "-" + S4(buf[4]) + "-" + S4(buf[5]) + S4(buf[6]) + S4(buf[7]));
    } : function() {
        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
            var r = Math.random() * 16 | 0
              , v = c == 'x' ? r : (r & 0x3 | 0x8);
            return v.toString(16);
        });
    };

    $.genUUID = function() {
        return generateGUID();
    }

    $.JsonRpcClient = function(options) {
        var self = this;
        this.options = $.extend({
            ajaxUrl: null,
            socketUrl: null,
            onmessage: null,
            login: null,
            passwd: null,
            sessid: null,
            loginParams: null,
            userVariables: null,
            getSocket: function(onmessage_cb) {
                return self._getSocket(onmessage_cb);
            }
        }, options);
        self.ws_cnt = 0;
        this.wsOnMessage = function(event) {
            self._wsOnMessage(event);
        }
        ;
    }
    ;
    $.JsonRpcClient.prototype._ws_socket = null;
    $.JsonRpcClient.prototype._ws_callbacks = {};
    $.JsonRpcClient.prototype._current_id = 1;
    $.JsonRpcClient.prototype.speedTest = function(bytes, cb) {
        var socket = this.options.getSocket(this.wsOnMessage);
        if (socket !== null) {
            this.speedCB = cb;
            this.speedBytes = bytes;
            socket.send("#SPU " + bytes);
            var loops = bytes / 1024;
            var rem = bytes % 1024;
            var i;
            var data = new Array(1024).join(".");
            for (i = 0; i < loops; i++) {
                socket.send("#SPB " + data);
            }
            if (rem) {
                socket.send("#SPB " + data);
            }
            socket.send("#SPE");
        }
    }
    ;
    $.JsonRpcClient.prototype.call = function(method, params, success_cb, error_cb) {
        if (!params) {
            params = {};
        }
        if (this.options.sessid) {
            params.sessid = this.options.sessid;
        }
        var request = {
            jsonrpc: '2.0',
            method: method,
            params: params,
            id: this._current_id++
        };
        if (!success_cb) {
            success_cb = function(e) {
                console.log("Success: ", e);
            }
            ;
        }
        if (!error_cb) {
            error_cb = function(e) {
                console.log("Error: ", e);
            }
            ;
        }
        var socket = this.options.getSocket(this.wsOnMessage);
        if (socket !== null) {
            this._wsCall(socket, request, success_cb, error_cb);
            return;
        }
        if (this.options.ajaxUrl === null) {
            throw "$.JsonRpcClient.call used with no websocket and no http endpoint.";
        }
        $.ajax({
            type: 'POST',
            url: this.options.ajaxUrl,
            data: $.toJSON(request),
            dataType: 'json',
            cache: false,
            success: function(data) {
                if ('error'in data)
                    error_cb(data.error, this);
                success_cb(data.result, this);
            },
            error: function(jqXHR, textStatus, errorThrown) {
                try {
                    var response = $.parseJSON(jqXHR.responseText);
                    if ('console'in window)
                        console.log(response);
                    error_cb(response.error, this);
                } catch (err) {
                    error_cb({
                        error: jqXHR.responseText
                    }, this);
                }
            }
        });
    }
    ;
    $.JsonRpcClient.prototype.notify = function(method, params) {
        if (this.options.sessid) {
            params.sessid = this.options.sessid;
        }
        var request = {
            jsonrpc: '2.0',
            method: method,
            params: params
        };
        var socket = this.options.getSocket(this.wsOnMessage);
        if (socket !== null) {
            this._wsCall(socket, request);
            return;
        }
        if (this.options.ajaxUrl === null) {
            throw "$.JsonRpcClient.notify used with no websocket and no http endpoint.";
        }
        $.ajax({
            type: 'POST',
            url: this.options.ajaxUrl,
            data: $.toJSON(request),
            dataType: 'json',
            cache: false
        });
    }
    ;
    $.JsonRpcClient.prototype.batch = function(callback, all_done_cb, error_cb) {
        var batch = new $.JsonRpcClient._batchObject(this,all_done_cb,error_cb);
        callback(batch);
        batch._execute();
    }
    ;
    $.JsonRpcClient.prototype.socketReady = function() {
        if (this._ws_socket === null || this._ws_socket.readyState > 1) {
            return false;
        }
        return true;
    }
    ;
    $.JsonRpcClient.prototype.closeSocket = function() {
        var self = this;
        if (self.socketReady()) {
            self._ws_socket.onclose = function(w) {
                console.log("Closing Socket");
            }
            ;
            self._ws_socket.close();
        }
    }
    ;
    $.JsonRpcClient.prototype.loginData = function(params) {
        var self = this;
        self.options.login = params.login;
        self.options.passwd = params.passwd;
        self.options.loginParams = params.loginParams;
        self.options.userVariables = params.userVariables;
    }
    ;
    $.JsonRpcClient.prototype.connectSocket = function(onmessage_cb) {
        var self = this;
        if (self.to) {
            clearTimeout(self.to);
        }
        if (!self.socketReady()) {
            self.authing = false;
            if (self._ws_socket) {
                delete self._ws_socket;
            }
            self._ws_socket = new WebSocket(self.options.socketUrl);
            if (self._ws_socket) {
                self._ws_socket.onmessage = onmessage_cb;
                self._ws_socket.onclose = function(w) {
                    if (!self.ws_sleep) {
                        self.ws_sleep = 1000;
                    }
                    if (self.options.onWSClose) {
                        self.options.onWSClose(self);
                    }
                    console.error("Websocket Lost " + self.ws_cnt + " sleep: " + self.ws_sleep + "msec");
                    self.to = setTimeout(function() {
                        console.log("Attempting Reconnection....");
                        self.connectSocket(onmessage_cb);
                    }, self.ws_sleep);
                    self.ws_cnt++;
                    if (self.ws_sleep < 3000 && (self.ws_cnt % 10) === 0) {
                        self.ws_sleep += 1000;
                    }
                }
                ;
                self._ws_socket.onopen = function() {
                    if (self.to) {
                        clearTimeout(self.to);
                    }
                    self.ws_sleep = 1000;
                    self.ws_cnt = 0;
                    if (self.options.onWSConnect) {
                        self.options.onWSConnect(self);
                    }
                    var req;
                    while ((req = $.JsonRpcClient.q.pop())) {
                        self._ws_socket.send(req);
                    }
                }
                ;
            }
        }
        return self._ws_socket ? true : false;
    }
    ;
    $.JsonRpcClient.prototype.stopRetrying = function() {
        if (self.to)
            clearTimeout(self.to);
    }
    $.JsonRpcClient.prototype._getSocket = function(onmessage_cb) {
        if (this.options.socketUrl === null || !("WebSocket"in window))
            return null;
        this.connectSocket(onmessage_cb);
        return this._ws_socket;
    }
    ;
    $.JsonRpcClient.q = [];
    $.JsonRpcClient.prototype._wsCall = function(socket, request, success_cb, error_cb) {
        var request_json = $.toJSON(request);
        if (socket.readyState < 1) {
            self = this;
            $.JsonRpcClient.q.push(request_json);
        } else {
            socket.send(request_json);
        }
        if ('id'in request && typeof success_cb !== 'undefined') {
            this._ws_callbacks[request.id] = {
                request: request_json,
                request_obj: request,
                success_cb: success_cb,
                error_cb: error_cb
            };
        }
    }
    ;
    $.JsonRpcClient.prototype._wsOnMessage = function(event) {
        var response;
        if (event.data[0] == "#" && event.data[1] == "S" && event.data[2] == "P") {
            if (event.data[3] == "U") {
                this.up_dur = parseInt(event.data.substring(4));
            } else if (this.speedCB && event.data[3] == "D") {
                this.down_dur = parseInt(event.data.substring(4));
                var up_kps = (((this.speedBytes * 8) / (this.up_dur / 1000)) / 1024).toFixed(0);
                var down_kps = (((this.speedBytes * 8) / (this.down_dur / 1000)) / 1024).toFixed(0);
                console.info("Speed Test: Up: " + up_kps + " Down: " + down_kps);
                this.speedCB(event, {
                    upDur: this.up_dur,
                    downDur: this.down_dur,
                    upKPS: up_kps,
                    downKPS: down_kps
                });
                this.speedCB = null;
            }
            return;
        }
        try {
            response = $.parseJSON(event.data);
            if (typeof response === 'object' && 'jsonrpc'in response && response.jsonrpc === '2.0') {
                if ('result'in response && this._ws_callbacks[response.id]) {
                    var success_cb = this._ws_callbacks[response.id].success_cb;
                    delete this._ws_callbacks[response.id];
                    success_cb(response.result, this);
                    return;
                } else if ('error'in response && this._ws_callbacks[response.id]) {
                    var error_cb = this._ws_callbacks[response.id].error_cb;
                    var orig_req = this._ws_callbacks[response.id].request;
                    if (!self.authing && response.error.code == -32000 && self.options.login && self.options.passwd) {
                        self.authing = true;
                        this.call("login", {
                            login: self.options.login,
                            passwd: self.options.passwd,
                            loginParams: self.options.loginParams,
                            userVariables: self.options.userVariables
                        }, this._ws_callbacks[response.id].request_obj.method == "login" ? function(e) {
                            self.authing = false;
                            console.log("logged in");
                            delete self._ws_callbacks[response.id];
                            if (self.options.onWSLogin) {
                                self.options.onWSLogin(true, self);
                            }
                        }
                        : function(e) {
                            self.authing = false;
                            console.log("logged in, resending request id: " + response.id);
                            var socket = self.options.getSocket(self.wsOnMessage);
                            if (socket !== null) {
                                socket.send(orig_req);
                            }
                            if (self.options.onWSLogin) {
                                self.options.onWSLogin(true, self);
                            }
                        }
                        , function(e) {
                            console.log("error logging in, request id:", response.id);
                            delete self._ws_callbacks[response.id];
                            error_cb(response.error, this);
                            if (self.options.onWSLogin) {
                                self.options.onWSLogin(false, self);
                            }
                        });
                        return;
                    }
                    delete this._ws_callbacks[response.id];
                    error_cb(response.error, this);
                    return;
                }
            }
        } catch (err) {
            console.log("ERROR: " + err);
            return;
        }
        if (typeof this.options.onmessage === 'function') {
            event.eventData = response;
            if (!event.eventData) {
                event.eventData = {};
            }
            var reply = this.options.onmessage(event);
            if (reply && typeof reply === "object" && event.eventData.id) {
                var msg = {
                    jsonrpc: "2.0",
                    id: event.eventData.id,
                    result: reply
                };
                var socket = self.options.getSocket(self.wsOnMessage);
                if (socket !== null) {
                    socket.send($.toJSON(msg));
                }
            }
        }
    }
    ;
    $.JsonRpcClient._batchObject = function(jsonrpcclient, all_done_cb, error_cb) {
        this._requests = [];
        this.jsonrpcclient = jsonrpcclient;
        this.all_done_cb = all_done_cb;
        this.error_cb = typeof error_cb === 'function' ? error_cb : function() {}
        ;
    }
    ;
    $.JsonRpcClient._batchObject.prototype.call = function(method, params, success_cb, error_cb) {
        if (!params) {
            params = {};
        }
        if (this.options.sessid) {
            params.sessid = this.options.sessid;
        }
        if (!success_cb) {
            success_cb = function(e) {
                console.log("Success: ", e);
            }
            ;
        }
        if (!error_cb) {
            error_cb = function(e) {
                console.log("Error: ", e);
            }
            ;
        }
        this._requests.push({
            request: {
                jsonrpc: '2.0',
                method: method,
                params: params,
                id: this.jsonrpcclient._current_id++
            },
            success_cb: success_cb,
            error_cb: error_cb
        });
    }
    ;
    $.JsonRpcClient._batchObject.prototype.notify = function(method, params) {
        if (this.options.sessid) {
            params.sessid = this.options.sessid;
        }
        this._requests.push({
            request: {
                jsonrpc: '2.0',
                method: method,
                params: params
            }
        });
    }
    ;
    $.JsonRpcClient._batchObject.prototype._execute = function() {
        var self = this;
        if (this._requests.length === 0)
            return;
        var batch_request = [];
        var handlers = {};
        var i = 0;
        var call;
        var success_cb;
        var error_cb;
        var socket = self.jsonrpcclient.options.getSocket(self.jsonrpcclient.wsOnMessage);
        if (socket !== null) {
            for (i = 0; i < this._requests.length; i++) {
                call = this._requests[i];
                success_cb = ('success_cb'in call) ? call.success_cb : undefined;
                error_cb = ('error_cb'in call) ? call.error_cb : undefined;
                self.jsonrpcclient._wsCall(socket, call.request, success_cb, error_cb);
            }
            if (typeof all_done_cb === 'function')
                all_done_cb(result);
            return;
        }
        for (i = 0; i < this._requests.length; i++) {
            call = this._requests[i];
            batch_request.push(call.request);
            if ('id'in call.request) {
                handlers[call.request.id] = {
                    success_cb: call.success_cb,
                    error_cb: call.error_cb
                };
            }
        }
        success_cb = function(data) {
            self._batchCb(data, handlers, self.all_done_cb);
        }
        ;
        if (self.jsonrpcclient.options.ajaxUrl === null) {
            throw "$.JsonRpcClient.batch used with no websocket and no http endpoint.";
        }
        $.ajax({
            url: self.jsonrpcclient.options.ajaxUrl,
            data: $.toJSON(batch_request),
            dataType: 'json',
            cache: false,
            type: 'POST',
            error: function(jqXHR, textStatus, errorThrown) {
                self.error_cb(jqXHR, textStatus, errorThrown);
            },
            success: success_cb
        });
    }
    ;
    $.JsonRpcClient._batchObject.prototype._batchCb = function(result, handlers, all_done_cb) {
        for (var i = 0; i < result.length; i++) {
            var response = result[i];
            if ('error'in response) {
                if (response.id === null || !(response.id in handlers)) {
                    if ('console'in window)
                        console.log(response);
                } else {
                    handlers[response.id].error_cb(response.error, this);
                }
            } else {
                if (!(response.id in handlers) && 'console'in window) {
                    console.log(response);
                } else {
                    handlers[response.id].success_cb(response.result, this);
                }
            }
        }
        if (typeof all_done_cb === 'function')
            all_done_cb(result);
    }
    ;
}
)(jQuery);
