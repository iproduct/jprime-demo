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
   
import {Observer} from 'rxjs/Observer';
import {Subscriber} from 'rxjs/Subscriber';
import {Observable} from 'rxjs/Observable';
import {Subject} from 'rxjs/Subject';

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
 
export class IPTRxWebSocketSubject extends Subject<string> {
	private websocket: WebSocket;
	private url: string;
	private openObserver: Observer<Event>;
	private closingObserver: Observer<CloseEvent>; 
    
    constructor (url: string, protocol: string, openObserver?: Observer<Event>, closingObserver?: Observer<CloseEvent>) {
	    super();

	    this.url = url;
	    this.openObserver = openObserver;
	    this.closingObserver = closingObserver;
	    if (!WebSocket) { throw new TypeError('WebSocket not implemented in your runtime.'); }
	    this.websocket = new WebSocket(url);

	    this.websocket.onopen = (event) => {
	        this.openObserver.next(event);
	        // this.websocket.send("Hi from Websocket client");
	    };
	    this.websocket.onclose = (event) => {
	        this.closingObserver.next(event);
	    };
	    this.websocket.onmessage = (event) => {
	    	try {
	            super.next.call(this, event.data);
	        } catch (e) {
	        	var errorEvent :ErrorEvent = new ErrorEvent(e);
	        	errorEvent.message = "Invalid event structure.";
	        	errorEvent.error = e;
	        	super.error.call(this, errorEvent);
	        }
	    };
	    this.websocket.onerror = (event) => {
	        super.error.call(this, event);
	    };
	    
	    this.destination = Subscriber.create(
	    	(message: string) => {
	    		this.websocket.send(message);
	    	},
	        (error: any) => {
	            console.log("WebSocket error: " + error);
	            this.websocket.close(1011, "Error processing client data stream.");
	            var errorEvent :ErrorEvent = new ErrorEvent(error);
	        	errorEvent.message = "Error processing client data stream.";
	        	errorEvent.error = error;
	        	super.error.call(this, errorEvent);
	        }, //CloseEvent.code = Internal Error
	        () => {
	        	console.log("WebSocket closing"); 
	        	this.websocket.close(1011, "Error processing client data stream.");
	        	var closeEvent :CloseEvent = new CloseEvent();
	        	closeEvent.code = 1000; //CLOSE_NORMAL
	        	closeEvent.reason = "WS connection closed by client.";
	        	closeEvent.wasClean = true;
	        	this.closingObserver.next(closeEvent);
	        }
	    );
	}

	next(value?: string): void {
    	this.destination.next.call(this.destination, value);
  	}

  	error(err?: any): void {
    	this.destination.error.call(this.destination, err);
  	}

  	complete(): void {
    	this.destination.complete.call(this.destination);
  	}

  };
