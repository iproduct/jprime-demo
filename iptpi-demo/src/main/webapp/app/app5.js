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
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
var angular2_1 = require('angular2/angular2');
// import {Observable, Subscriber, Subscription, Scheduler, Subject} from "../node_modules/angular2/node_modules/@reactivex/rxjs/dist/cjs/Rx";
var Subscriber_1 = require('rxjs/Subscriber');
var ipt_rx_websocket_1 = require('./ipt_rx_websocket');
var AppComponent = (function () {
    function AppComponent() {
        var _this = this;
        this.commnad = DEFAULT_COMMAND;
        this.title = 'Command your IPTPI Robot';
        this.position = { "x": 0, "y": 0, "heading": 0, timestamp: 0 };
        this.positions = [];
        // public selectedWish: Wish;
        this.command = DEFAULT_COMMAND;
        this.canvas = document.getElementById("robot-canvas");
        this.ctx = this.canvas.getContext("2d");
        this.robotImage = new Image(); //<HTMLImageElement> document.getElementById("iptpi-image");
        this.compassImage = document.getElementById("compass-image");
        this.needle = document.getElementById("needle-image");
        this.degrees = 0;
        var openSubscriber = Subscriber_1.Subscriber.create(function (e) {
            console.info('socket open');
        });
        // an observer for when the socket is about to close
        var closingSubscriber = Subscriber_1.Subscriber.create(function () {
            console.log('socket is about to close');
        });
        //Create WebSocket Subject
        this.wsSubject = new ipt_rx_websocket_1.IPTRxWebSocketSubject('ws://' + window.location.host + '/ws', null, openSubscriber, closingSubscriber);
        this.wsSubject.subscribe(function (event) {
            var data = event;
            console.log("data: " + data);
            _this.position = JSON.parse(data);
            console.log(_this.position);
            _this.updateCanvasDrawing();
        }, function (err) {
            console.log("Error:", event);
        }, function () {
            console.log("Complete:", event);
        });
        this.robotImage.src = "../img/iptpi_m.png";
        this.robotImage.onload = function () { return _this.updateCanvasDrawing(); };
    }
    // onSelect(wish: Wish) {
    // 	this.selectedWish = wish;
    // 	this.submitToServer(wish);
    // }
    AppComponent.prototype.submitToServer = function (move) {
        console.log(move);
        this.wsSubject.next(JSON.stringify(move));
    };
    // getSelectedClass(wish: Position) {
    // 	return { 'selected': wish === this.selectedWish };
    // }
    // onAddWish() {
    // 	this.wishes[this.wishes.length] = this.newPosition;
    // 	this.submitToServer(this.newPosition);
    // }
    AppComponent.prototype.updateCanvasDrawing = function () {
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
        this.ctx.rotate(-this.position.heading);
        this.ctx.drawImage(this.robotImage, -imageWidth / 2, -imageHeight / 2, imageWidth, imageHeight);
        this.ctx.restore();
        // Draw robot and compass
        this.ctx.drawImage(this.compassImage, 0, 0);
        // Draw robot and compass
        this.ctx.save();
        this.ctx.translate(100, 100);
        this.ctx.rotate(-this.position.heading);
        this.ctx.drawImage(this.needle, -100, -100);
        this.ctx.restore();
    };
    AppComponent.prototype.onGoUp = function () {
        this.command = DEFAULT_COMMAND;
        this.submitToServer(this.command);
    };
    AppComponent.prototype.onGoLeft = function () {
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
    };
    AppComponent.prototype.onGoRight = function () {
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
    };
    AppComponent.prototype.onGoDown = function () {
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
    };
    AppComponent = __decorate([
        angular2_1.Component({
            selector: 'display',
            directives: [angular2_1.CORE_DIRECTIVES, angular2_1.FORM_DIRECTIVES],
            styles: ["\n  .robot-panel {}\n  .position-display label {display: inline-block; margin-left: 30px;}\n  .pos-display {width: 80px; text-align:right;}\n  .robot-controls {margin-top:10px;}\n  .robot-controls button span {font-size: 2em;}\n  .wishes {list-style-type: none; margin-left: 1em; padding: 0; wtitleth: 10em;}\n  .wishes li { cursor: pointer; position: relative; left: 0; transition: all 0.2s ease; }\n  .wishes li:hover {color: #369; background-color: #EEE; left: .2em;}\n  .wishes .badge {\n  \tdisplay: inline-block;\n  \twidth: 15px;\n  \ttext-align: right;\n    font-size: small;\n    color: white;\n    padding: 0.1em 0.7em;\n    background-color: #369;\n    line-height: 1em;\n    position: relative;\n    left: -1px;\n    top: -1px;\n  }\n  .selected { background-color: #EEE; color: #369; }\n  "],
            template: "\n  <div class=\"panel panel-default\">\n    <div class=\"panel-heading\">\n        <h3 class=\"panel-title\">{{title}}</h3>   \n    </div>\n    <div class=\"robot-panel panel-body\">\n            <div class=\"position-display\">\n            \t<span class=\"display-position\">\n            \t\t<label>X: </label>\n      \t\t\t\t<input  class=\"pos-display\" [ng-model]=\"position.x | number:'.2-2'\" readonly></input>\n      \t\t\t<span>\n            \t<span class=\"display-position\">\n            \t\t<label>Y: </label>\n      \t\t\t\t<input class=\"pos-display\" [ng-model]=\"position.y | number:'.2-2'\" readonly></input>\n      \t\t\t<span>\n            \t<span class=\"display-position\">\n            \t\t<label>Heading: </label>\n      \t\t\t\t<input  class=\"pos-display\" [ng-model]=\"position.heading | number:'.2-2'\" readonly></input>\n      \t\t\t<span>\n            </div> \n            <canvas id=\"robot-canvas\" class=\"robot-canvas center-block\" width=\"400\" height=\"500\" >\n            </canvas>\n\n            <div class=\"robot-controls\" class=\"container-fluid\">\n            \t<div class=\"row\">\n            \t\t<button (click)=\"onGoUp()\" class=\"btn btn-md btn-warning col-xs-2 col-xs-offset-5\" aria-label=\"Left Align\">\n  \t\t\t\t\t\t<span class=\"glyphicon glyphicon glyphicon-circle-arrow-up\" aria-hidden=\"true\"></span>\n\t\t\t\t\t</button>\n\t\t\t\t</div>\n            \t<div class=\"row\">\n            \t\t<button (click)=\"onGoLeft()\" class=\"btn btn-md btn-warning col-xs-2 col-xs-offset-3\" aria-label=\"Left Align\">\n  \t\t\t\t\t\t<span class=\"glyphicon glyphicon glyphicon-circle-arrow-left\" aria-hidden=\"true\"></span>\n\t\t\t\t\t</button>\n            \t\t<button (click)=\"onGoRight()\" class=\"btn btn-md btn-warning col-xs-2 col-xs-offset-2\" aria-label=\"Left Align\">\n  \t\t\t\t\t\t<span class=\"glyphicon glyphicon glyphicon-circle-arrow-right\" aria-hidden=\"true\"></span>\n\t\t\t\t\t</button>\n\t\t\t\t</div>\t\n            \t<div class=\"row\">\n            \t\t<button (click)=\"onGoDown()\" class=\"btn btn-md btn-warning col-xs-2 col-xs-offset-5\" aria-label=\"Left Align\">\n  \t\t\t\t\t\t<span class=\"glyphicon glyphicon glyphicon-circle-arrow-down\" aria-hidden=\"true\"></span>\n\t\t\t\t\t</button>\n\t\t\t\t</div>\t\n            </div> \n    </div>\n\n\t<div class=\"position-display\">\n            \t<span class=\"display-position\">\n            \t\t<label>Distance: </label>\n      \t\t\t\t<input  class=\"pos-display\" [(ng-model)]=\"command.deltaX\" placeholder=\"mm\"></input>\n      \t\t\t<span>\n            \t<span class=\"display-position\">\n            \t\t<label>Velocity: </label>\n      \t\t\t\t<input class=\"pos-display\" [(ng-model)]=\"command.velocity\" placeholder=\"mm/s\"></input>\n      \t\t\t<span>\n            \t<span class=\"display-position\">\n            \t\t<label>Angle: </label>\n      \t\t\t\t<input  class=\"pos-display\" [(ng-model)]=\"command.deltaHeading\" placeholder=\"rad\"></input>\n      \t\t\t<span>\n    </div>\n  </div>\n  "
        }), 
        __metadata('design:paramtypes', [])
    ], AppComponent);
    return AppComponent;
}());
angular2_1.bootstrap(AppComponent);
var DEFAULT_COMMAND = {
    deltaX: 400,
    deltaY: 0,
    deltaHeading: 0,
    velocity: 50,
    angularVelocity: 0,
    acceleration: 0,
    angularAcceleration: 0
};
//# sourceMappingURL=app5.js.map