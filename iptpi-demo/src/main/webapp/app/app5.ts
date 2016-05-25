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

import {Pipe, AsyncPipe, bootstrap, Component, CORE_DIRECTIVES, FORM_DIRECTIVES} from 'angular2/angular2';
// import {Observable, Subscriber, Subscription, Scheduler, Subject} from "../node_modules/angular2/node_modules/@reactivex/rxjs/dist/cjs/Rx";
import {Subscriber} from 'rxjs/Subscriber';
import {Observable} from 'rxjs/Observable';
import {Subject} from 'rxjs/Subject';
import {Subscription} from 'rxjs/Subscription';
import {IPTRxWebSocketSubject} from './ipt_rx_websocket';

declare function createWebSocketSubject(url: string, protocols: string,
	openSubscriber: Subscriber<any>, closingSubscriber: Subscriber<any>): Subject<any>;
declare function updateCloud(tags: string[]): void;

interface Position {
	x: number;
	y: number;
	heading: number;
	timestamp: number;
}

interface MovementCommand {
	deltaX: number;
	deltaY: number;
	deltaHeading: number;
	velocity:number; 
	angularVelocity: number;
	acceleration: number;
	angularAcceleration: number;
}

@Component({
  selector: 'display',
  directives: [CORE_DIRECTIVES, FORM_DIRECTIVES],
  styles: [`
  .robot-panel {}
  .position-display label {display: inline-block; margin-left: 30px;}
  .pos-display {width: 80px; text-align:right;}
  .robot-controls {margin-top:10px;}
  .robot-controls button span {font-size: 2em;}
  .wishes {list-style-type: none; margin-left: 1em; padding: 0; wtitleth: 10em;}
  .wishes li { cursor: pointer; position: relative; left: 0; transition: all 0.2s ease; }
  .wishes li:hover {color: #369; background-color: #EEE; left: .2em;}
  .wishes .badge {
  	display: inline-block;
  	width: 15px;
  	text-align: right;
    font-size: small;
    color: white;
    padding: 0.1em 0.7em;
    background-color: #369;
    line-height: 1em;
    position: relative;
    left: -1px;
    top: -1px;
  }
  .selected { background-color: #EEE; color: #369; }
  `],
  template: `
  <div class="panel panel-default">
    <div class="panel-heading">
        <h3 class="panel-title">{{title}}</h3>   
    </div>
    <div class="robot-panel panel-body">
            <div class="position-display">
            	<span class="display-position">
            		<label>X: </label>
      				<input  class="pos-display" [ng-model]="position.x | number:'.2-2'" readonly></input>
      			<span>
            	<span class="display-position">
            		<label>Y: </label>
      				<input class="pos-display" [ng-model]="position.y | number:'.2-2'" readonly></input>
      			<span>
            	<span class="display-position">
            		<label>Heading: </label>
      				<input  class="pos-display" [ng-model]="position.heading | number:'.2-2'" readonly></input>
      			<span>
            </div> 
            	<canvas id="robot-canvas" class="robot-canvas center-block" width="400" height="500" >
            </canvas>

            <div class="robot-controls" class="container-fluid">
            	<div class="row">
            		<button (click)="onGoUp()" class="btn btn-md btn-warning col-xs-2 col-xs-offset-5" aria-label="Left Align">
  						<span class="glyphicon glyphicon glyphicon-circle-arrow-up" aria-hidden="true"></span>
					</button>
				</div>
            	<div class="row">
            		<button (click)="onGoLeft()" class="btn btn-md btn-warning col-xs-2 col-xs-offset-3" aria-label="Left Align">
  						<span class="glyphicon glyphicon glyphicon-circle-arrow-left" aria-hidden="true"></span>
					</button>
            		<button (click)="onGoRight()" class="btn btn-md btn-warning col-xs-2 col-xs-offset-2" aria-label="Left Align">
  						<span class="glyphicon glyphicon glyphicon-circle-arrow-right" aria-hidden="true"></span>
					</button>
				</div>	
            	<div class="row">
            		<button (click)="onGoDown()" class="btn btn-md btn-warning col-xs-2 col-xs-offset-5" aria-label="Left Align">
  						<span class="glyphicon glyphicon glyphicon-circle-arrow-down" aria-hidden="true"></span>
					</button>
				</div>	
            </div> 
    </div>
  </div>
  `
})
class AppComponent {
	public title = 'Command your IPTPI Robot';
	public position: Position = { "x": 0, "y": 0, "heading": 0, timestamp: 0};
	public positions: Position[] = [];
	// public selectedWish: Wish;
	public command: MovementCommand = DEFAULT_COMMAND;
	private wsSubject: IPTRxWebSocketSubject;

	private canvas = <HTMLCanvasElement> document.getElementById("robot-canvas");
	private ctx = this.canvas.getContext("2d");
	private robotImage = new Image();  //<HTMLImageElement> document.getElementById("iptpi-image");
	private compassImage = <HTMLImageElement>document.getElementById("compass-image");
	private needle = <HTMLImageElement>document.getElementById("needle-image");
	private degrees: number = 0;

	
	constructor() {
		var openSubscriber = Subscriber.create(function(e) {
			console.info('socket open');
		});

		// an observer for when the socket is about to close
		var closingSubscriber = Subscriber.create(function() {
			console.log('socket is about to close');
		});

		//Create WebSocket Subject
		this.wsSubject = new IPTRxWebSocketSubject('ws://' + window.location.host + '/ws', null,
			openSubscriber, closingSubscriber);

		this.wsSubject.subscribe(
			event => {
				var data: string = event;
				console.log(`data: ${data}`);
				this.position = JSON.parse(data);
				console.log(this.position);
				this.updateCanvasDrawing();
			},
			err => {
				console.log("Error:", event); 
			},
			() => {
				console.log("Complete:", event);
			});
        this.robotImage.src = "../img/iptpi_m.png";
		this.robotImage.onload =  () => this.updateCanvasDrawing();
	}

	// onSelect(wish: Wish) {
	// 	this.selectedWish = wish;
	// 	this.submitToServer(wish);
	// }

	submitToServer(move: MovementCommand) {
		console.log(move);
		this.wsSubject.next(JSON.stringify(move));
	}

	// getSelectedClass(wish: Position) {
	// 	return { 'selected': wish === this.selectedWish };
	// }

	// onAddWish() {
	// 	this.wishes[this.wishes.length] = this.newPosition;
	// 	this.submitToServer(this.newPosition);
	// }

	updateCanvasDrawing(){
		var width = document.body.clientWidth;
		var height = document.body.clientHeight;
		var canvasWidth = Math.min(Math.round(0.8 * width), 500);
		var canvasHeight = canvasWidth;
		this.canvas.width = canvasWidth;
		this.canvas.height = canvasHeight;
		var imageWidth = Math.round(0.6 * canvasWidth);
		var imageHeight = Math.round(1.2 * imageWidth);
		var centerX = canvasWidth * 0.6;
		var centerY = canvasHeight * 0.5;

		this.ctx.fillStyle = "#55FF55";
		this.ctx.fillRect(0, 0, canvasWidth, canvasHeight);

		// Draw robot
		this.ctx.save();
		this.ctx.translate(centerX, centerY);
		this.ctx.rotate(- this.position.heading);
		this.ctx.drawImage(this.robotImage,  -imageWidth / 2, -imageHeight / 2, imageWidth, imageHeight);
		this.ctx.restore();
		// Draw robot and compass
		this.ctx.drawImage(this.compassImage, 0, 0);
		// Draw robot and compass
		this.ctx.save();
		this.ctx.translate(100, 100);
		this.ctx.rotate(-this.position.heading);
		this.ctx.drawImage(this.needle, -100, -100);
		this.ctx.restore();
	}

	onGoUp() {
		this.command = DEFAULT_COMMAND;
		this.submitToServer(this.command);
	}

	onGoLeft() {
		this.command = {
			deltaX: 400,
			deltaY: 0,
			deltaHeading: 1.5,
			velocity: 40,
			angularVelocity: 0,
			acceleration: 0,
			angularAcceleration: 0
		};
		this.submitToServer(this.command);
	}

	onGoRight() {
		this.command = {
			deltaX: 400,
			deltaY: 0,
			deltaHeading: -1.5,
			velocity: 40,
			angularVelocity: 0,
			acceleration: 0,
			angularAcceleration: 0
		};
		this.submitToServer(this.command);
	}

	onGoDown() {
		this.command = {
			deltaX: -200,
			deltaY: 0,
			deltaHeading: 0,
			velocity: -50,
			angularVelocity: 0,
			acceleration: 0,
			angularAcceleration: 0
		};
		this.submitToServer(this.command);
	}


}
bootstrap(AppComponent);

var DEFAULT_COMMAND: MovementCommand = {
	deltaX: 400,
	deltaY: 0,
	deltaHeading: 0,
	velocity: 50,
	angularVelocity: 0,
	acceleration: 0,
	angularAcceleration: 0
};