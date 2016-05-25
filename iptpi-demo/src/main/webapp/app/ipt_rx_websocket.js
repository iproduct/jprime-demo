/* Copyright 2015 IPT – Intellectual Products & Technologies Ltd.
   Author: Trayan Iliev, IPT (http://iproduct.org)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

   */
"use strict";
var __extends = (this && this.__extends) || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
};
var Subscriber_1 = require('rxjs/Subscriber');
var Subject_1 = require('rxjs/Subject');
/**
   * Creates a reactive WebSocket Subject with a given URL, protocol and an optional observer for open and close events.
   *
   * @example
   *  var socketSubject = = new IPTRxWebSocketSubject('ws://127.0.0.1/ws', 'wish-protocol', openObserver, closingObserver);
   *
   * @param {String} url The URL of the WebSocket endpoint.
   * @param {String} protocol The WebSocket protocol.
   * @param {Observer} [openObserver] Optional Observer call after WebSocket open event.
   * @param {Observer} [closingObserver] Optional Observer to call before underlying WebSocket is closed.
   * @returns {Subject} An observable sequence wrapping WebSocket.
   */
var IPTRxWebSocketSubject = (function (_super) {
    __extends(IPTRxWebSocketSubject, _super);
    function IPTRxWebSocketSubject(url, protocol, openObserver, closingObserver) {
        var _this = this;
        _super.call(this);
        this.url = url;
        this.openObserver = openObserver;
        this.closingObserver = closingObserver;
        if (!WebSocket) {
            throw new TypeError('WebSocket not implemented in your runtime.');
        }
        this.websocket = new WebSocket(url);
        this.websocket.onopen = function (event) {
            _this.openObserver.next(event);
            // this.websocket.send("Hi from Websocket client");
        };
        this.websocket.onclose = function (event) {
            _this.closingObserver.next(event);
        };
        this.websocket.onmessage = function (event) {
            try {
                _super.prototype.next.call(_this, event.data);
            }
            catch (e) {
                var errorEvent = new ErrorEvent(e);
                errorEvent.message = "Invalid event structure.";
                errorEvent.error = e;
                _super.prototype.error.call(_this, errorEvent);
            }
        };
        this.websocket.onerror = function (event) {
            _super.prototype.error.call(_this, event);
        };
        this.destination = Subscriber_1.Subscriber.create(function (message) {
            _this.websocket.send(message);
        }, function (error) {
            console.log("WebSocket error: " + error);
            _this.websocket.close(1011, "Error processing client data stream.");
            var errorEvent = new ErrorEvent(error);
            errorEvent.message = "Error processing client data stream.";
            errorEvent.error = error;
            _super.prototype.error.call(_this, errorEvent);
        }, //CloseEvent.code = Internal Error
        function () {
            console.log("WebSocket closing");
            _this.websocket.close(1011, "Error processing client data stream.");
            var closeEvent = new CloseEvent();
            closeEvent.code = 1000; //CLOSE_NORMAL
            closeEvent.reason = "WS connection closed by client.";
            closeEvent.wasClean = true;
            _this.closingObserver.next(closeEvent);
        });
    }
    IPTRxWebSocketSubject.prototype.next = function (value) {
        this.destination.next.call(this.destination, value);
    };
    IPTRxWebSocketSubject.prototype.error = function (err) {
        this.destination.error.call(this.destination, err);
    };
    IPTRxWebSocketSubject.prototype.complete = function () {
        this.destination.complete.call(this.destination);
    };
    return IPTRxWebSocketSubject;
}(Subject_1.Subject));
exports.IPTRxWebSocketSubject = IPTRxWebSocketSubject;
;
//# sourceMappingURL=ipt_rx_websocket.js.map